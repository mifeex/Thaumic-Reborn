package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeSpatialIndex;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.visnet.VisMachineAccess;
import com.thaumcraftmodern.world.block.InfernalFurnaceBlock;
import com.thaumcraftmodern.world.block.ArcaneBellowsBlock;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Exact tick and transport contract of TC4 {@code TileArcaneFurnace}. */
public final class InfernalFurnaceBlockEntity extends BlockEntity
        implements EssentiaTransport {
    public static final int INVENTORY_SIZE = 32;
    public static final int NORMAL_COOK_TIME = 140;
    public static final int SPEEDY_COOK_TIME = 80;
    public static final int BELLOWS_REDUCTION = 20;
    public static final int ESSENTIA_SPEED_TICKS = 600;
    public static final int ESSENTIA_SUCTION = 128;
    static final int AURA_SEARCH_RADIUS = 10;
    static final int INITIAL_AURA_SEARCH_INTERVAL = 40;
    static final int AURA_SEARCH_BACKOFF_STEP = 40;
    static final int MAX_AURA_SEARCH_INTERVAL = 200;

    private final NonNullList<ItemStack> items = NonNullList.withSize(
            INVENTORY_SIZE, ItemStack.EMPTY);
    private int furnaceCookTime;
    private int furnaceMaxCookTime;
    private int speedyTime;
    private int drawDelay;
    private BlockPos cachedAuraNode;
    private int auraSearchCooldown;
    private int auraSearchInterval = INITIAL_AURA_SEARCH_INTERVAL;

    public InfernalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFERNAL_FURNACE.get(), pos, state);
    }

    public static void serverTick(Level rawLevel, BlockPos pos,
            BlockState state, InfernalFurnaceBlockEntity furnace) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        if (state.getValue(InfernalFurnaceBlock.PART) == 10) {
            furnace.nozzleTick(level);
        } else if (state.getValue(InfernalFurnaceBlock.PART) == 0) {
            furnace.coreTick(level);
        }
    }

    private void coreTick(ServerLevel level) {
        boolean cookedFlag = false;
        if (furnaceCookTime > 0) {
            furnaceCookTime--;
            cookedFlag = true;
        }
        if (cookedFlag && speedyTime > 0) speedyTime--;
        if (cookedFlag && speedyTime <= 0) {
            speedyTime = drainIgnisVis(level, 5);
        }
        if (furnaceMaxCookTime == 0) furnaceMaxCookTime = calcCookTime();
        if (furnaceCookTime > furnaceMaxCookTime) {
            furnaceCookTime = furnaceMaxCookTime;
        }
        if (furnaceCookTime == 0 && cookedFlag) smeltFirst(level);
        if (furnaceCookTime == 0 && !cookedFlag && firstSmeltable(level) >= 0) {
            furnaceCookTime = furnaceMaxCookTime = calcCookTime();
            setChanged();
        }
    }

    private int drainIgnisVis(ServerLevel level, int maximum) {
        int networkDrain = VisMachineAccess.consumeNearest(
                level,
                worldPosition,
                PrimalAspect.IGNIS,
                maximum
        );
        if (networkDrain > 0) {
            auraSearchCooldown = 0;
            auraSearchInterval = INITIAL_AURA_SEARCH_INTERVAL;
            return networkDrain;
        }

        AuraNodeBlockEntity node = cachedAuraNode == null ? null
                : level.getBlockEntity(cachedAuraNode) instanceof AuraNodeBlockEntity found
                ? found : null;
        if (node == null && auraSearchCooldown-- <= 0) {
            node = AuraNodeSpatialIndex.nearest(
                    level,
                    worldPosition,
                    AURA_SEARCH_RADIUS,
                    ignored -> true
            );
            cachedAuraNode = node == null
                    ? null
                    : node.getBlockPos().immutable();
            if (node == null) {
                auraSearchCooldown = auraSearchInterval;
                auraSearchInterval = nextAuraSearchInterval(
                        auraSearchInterval
                );
            } else {
                auraSearchCooldown = INITIAL_AURA_SEARCH_INTERVAL;
                auraSearchInterval = INITIAL_AURA_SEARCH_INTERVAL;
            }
        }
        if (node == null) return 0;
        return node.drainForMachine(PrimalAspect.IGNIS, maximum);
    }

    static int nextAuraSearchInterval(int currentInterval) {
        return Math.min(
                MAX_AURA_SEARCH_INTERVAL,
                currentInterval + AURA_SEARCH_BACKOFF_STEP
        );
    }

    private void nozzleTick(ServerLevel level) {
        InfernalFurnaceBlockEntity core = core();
        Direction outward = outwardDirection();
        if (core == null || outward == null || core.speedyTime >= 60
                || ++drawDelay % 5 != 0) return;
        EssentiaTransport remote = EssentiaConnections.neighbour(
                level, worldPosition, outward).orElse(null);
        Direction remoteSide = outward.getOpposite();
        if (remote != null && remote.canOutputTo(remoteSide)
                && remote.suctionAmount(remoteSide) < suctionAmount(outward)
                && remote.takeEssentia("ignis", 1, remoteSide) == 1) {
            core.speedyTime += ESSENTIA_SPEED_TICKS;
            core.setChanged();
        }
    }

    public boolean addItemsToInventory(ItemStack incoming) {
        if (incoming.isEmpty() || level == null || level.isClientSide) return false;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stored = items.get(slot);
            if (!stored.isEmpty() && ItemStack.isSameItemSameTags(stored, incoming)
                    && stored.getCount() + incoming.getCount()
                    <= stored.getMaxStackSize()) {
                stored.grow(incoming.getCount());
                if (!canSmelt(incoming)) destroyItem(slot);
                setChanged();
                return true;
            }
            if (stored.isEmpty()) {
                items.set(slot, incoming.copy());
                if (!canSmelt(incoming)) destroyItem(slot);
                setChanged();
                return true;
            }
        }
        return false;
    }

    private void destroyItem(int slot) {
        items.set(slot, ItemStack.EMPTY);
        if (!(level instanceof ServerLevel server)) return;
        server.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 0.3F,
                2.6F + (server.random.nextFloat() - server.random.nextFloat()) * 0.8F);
        server.sendParticles(ParticleTypes.SMOKE,
                worldPosition.getX() + server.random.nextFloat(),
                worldPosition.getY() + 1.0D,
                worldPosition.getZ() + server.random.nextFloat(),
                12, 0.1D, 0.08D, 0.1D, 0.01D);
    }

    private void smeltFirst(ServerLevel level) {
        int slot = firstSmeltable(level);
        if (slot < 0) return;
        ItemStack source = items.get(slot);
        SmeltingRecipe recipe = recipe(level, source);
        if (recipe == null) return;
        ItemStack result = recipe.assemble(new SimpleContainer(source),
                level.registryAccess());
        if (result.isEmpty()) return;
        ejectItem(level, result.copy(), source.copy(), recipe.getExperience());
        source.shrink(1);
        if (source.isEmpty()) items.set(slot, ItemStack.EMPTY);
        setChanged();
    }

    private void ejectItem(ServerLevel level, ItemStack result,
            ItemStack source, float experience) {
        Direction facing = outputDirection();
        double x = worldPosition.getX() + 0.5D + facing.getStepX() * 1.2D;
        double z = worldPosition.getZ() + 0.5D + facing.getStepZ() * 1.2D;
        spawnOutput(level, result, x, z, facing, 0.13D);

        ItemStack bonus = smeltingBonus(source);
        if (!bonus.isEmpty()) {
            int count = rollBonusCount(level, attachedBellows());
            if (count > 0) {
                bonus.setCount(count);
                spawnOutput(level, bonus, x, z, facing, 0.13D);
            }
        }

        int xp = calculateExperience(level, result.getCount(), experience);
        ExperienceOrb.award(level,
                new net.minecraft.world.phys.Vec3(x, worldPosition.getY() + 0.4D, z), xp);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.55D,
                worldPosition.getZ() + 0.5D,
                5, 0.2D, 0.1D, 0.2D, 0.02D);
        // TC4's client event emitted five independently varied lava pops for
        // every completed operation. Broadcasting them from the server keeps
        // the same audible burst for every nearby player.
        for (int i = 0; i < 5; i++) {
            level.playSound(null, worldPosition, SoundEvents.LAVA_POP,
                    SoundSource.BLOCKS,
                    0.1F + level.random.nextFloat() * 0.1F,
                    0.9F + level.random.nextFloat() * 0.15F);
        }
    }

    private void spawnOutput(ServerLevel level, ItemStack stack, double x,
            double z, Direction facing, double speed) {
        ItemEntity item = new ItemEntity(level, x,
                worldPosition.getY() + 0.4D, z, stack);
        double sideX = facing.getStepX() == 0
                ? (level.random.nextFloat() - level.random.nextFloat()) * 0.03D
                : facing.getStepX() * speed;
        double sideZ = facing.getStepZ() == 0
                ? (level.random.nextFloat() - level.random.nextFloat()) * 0.03D
                : facing.getStepZ() * speed;
        item.setDeltaMovement(sideX, 0.0D, sideZ);
        level.addFreshEntity(item);
    }

    private int firstSmeltable(ServerLevel level) {
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty() && recipe(level, items.get(slot)) != null) {
                return slot;
            }
        }
        return -1;
    }

    private boolean canSmelt(ItemStack stack) {
        return level instanceof ServerLevel server && recipe(server, stack) != null;
    }

    @Nullable private static SmeltingRecipe recipe(ServerLevel level,
            ItemStack stack) {
        return level.getRecipeManager().getRecipeFor(
                RecipeType.SMELTING, new SimpleContainer(stack), level)
                .orElse(null);
    }

    int calcCookTime() {
        return (speedyTime > 0 ? SPEEDY_COOK_TIME : NORMAL_COOK_TIME)
                - BELLOWS_REDUCTION * attachedBellows();
    }

    private int attachedBellows() {
        if (level == null) return 0;
        int bellows = 0;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;
            BlockPos position = worldPosition.relative(direction, 2);
            BlockState state = level.getBlockState(position);
            if (state.is(ModBlocks.ARCANE_BELLOWS.get())
                    && state.getValue(ArcaneBellowsBlock.FACING)
                    == direction.getOpposite()
                    && !level.hasNeighborSignal(position)) bellows++;
        }
        return Math.min(3, bellows);
    }

    public static int rollBonusCount(ServerLevel level, int bellows) {
        int result = 0;
        if (bellows <= 0) return level.random.nextInt(4) == 0 ? 1 : 0;
        for (int index = 0; index < Math.min(3, bellows); index++) {
            if (level.random.nextFloat() < 0.44F) result++;
        }
        return result;
    }

    public static int calculateExperience(ServerLevel level, int outputCount,
            float experience) {
        if (experience == 0.0F) return 0;
        float exact = outputCount * experience;
        int result = net.minecraft.util.Mth.floor(exact);
        if (result < net.minecraft.util.Mth.ceil(exact)
                && level.random.nextFloat() < exact - result) result++;
        return result;
    }

    private static ItemStack smeltingBonus(ItemStack source) {
        if (source.is(Tags.Items.ORES_GOLD)
                || source.is(ModItems.NATIVE_GOLD_CLUSTER.get())) {
            return new ItemStack(Items.GOLD_NUGGET);
        }
        if (source.is(Tags.Items.ORES_IRON)
                || source.is(ModItems.NATIVE_IRON_CLUSTER.get())) {
            return new ItemStack(Items.IRON_NUGGET);
        }
        if (source.is(Tags.Items.ORES_COPPER)
                || source.is(ModItems.NATIVE_COPPER_CLUSTER.get())) {
            return new ItemStack(ModItems.COPPER_NUGGET.get());
        }
        if (source.is(ModItems.NATIVE_TIN_CLUSTER.get())) {
            return new ItemStack(ModItems.TIN_NUGGET.get());
        }
        if (source.is(ModItems.NATIVE_SILVER_CLUSTER.get())) {
            return new ItemStack(ModItems.ARCANE_RECIPE_COMPONENTS
                    .get("silver_nugget").get());
        }
        if (source.is(ModItems.NATIVE_LEAD_CLUSTER.get())) {
            return new ItemStack(ModItems.LEAD_NUGGET.get());
        }
        if (source.is(ModItems.CINNABAR_ORE.get())) {
            return new ItemStack(ModItems.QUICKSILVER_NUGGET.get());
        }
        if (source.is(Items.CHICKEN)) return edibleBonus("chicken_nugget");
        if (source.is(Items.BEEF)) return edibleBonus("beef_nugget");
        if (source.is(Items.PORKCHOP)) return edibleBonus("pork_nugget");
        if (source.is(ItemTags.FISHES)) return edibleBonus("fish_nugget");
        return ItemStack.EMPTY;
    }

    private static ItemStack edibleBonus(String id) {
        var item = ModItems.ARCANE_RECIPE_COMPONENTS.get(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
    }

    private Direction outputDirection() {
        if (level != null) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockState state = level.getBlockState(worldPosition.relative(direction));
                if (state.getBlock() instanceof InfernalFurnaceBlock
                        && state.getValue(InfernalFurnaceBlock.PART) == 10) {
                    return direction;
                }
            }
        }
        return Direction.SOUTH;
    }

    @Nullable private InfernalFurnaceBlockEntity core() {
        if (level == null) return null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockEntity(worldPosition.relative(direction))
                    instanceof InfernalFurnaceBlockEntity furnace
                    && furnace.getBlockState().getValue(InfernalFurnaceBlock.PART) == 0) {
                return furnace;
            }
        }
        return null;
    }

    @Nullable private Direction outwardDirection() {
        if (level == null) return null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(worldPosition.relative(direction));
            if (state.getBlock() instanceof InfernalFurnaceBlock
                    && state.getValue(InfernalFurnaceBlock.PART) == 0) {
                return direction.getOpposite();
            }
        }
        return null;
    }

    private boolean nozzleSide(Direction side) {
        Direction outward = outwardDirection();
        return getBlockState().getValue(InfernalFurnaceBlock.PART) == 10
                && outward != null && side == outward;
    }

    @Override public boolean isConnectable(Direction side) { return nozzleSide(side); }
    @Override public boolean canInputFrom(Direction side) { return nozzleSide(side); }
    @Override public boolean canOutputTo(Direction side) { return false; }
    @Override public void setSuction(@Nullable String aspect, int amount) {}
    @Override public @Nullable String suctionType(Direction side) {
        return nozzleSide(side) ? "ignis" : null;
    }
    @Override public int suctionAmount(Direction side) {
        InfernalFurnaceBlockEntity core = core();
        return nozzleSide(side) && core != null && core.speedyTime < 40
                ? ESSENTIA_SUCTION : 0;
    }
    @Override public @Nullable String essentiaType(Direction side) { return null; }
    @Override public int essentiaAmount(Direction side) { return 0; }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) { return 0; }
    @Override public int addEssentia(String aspect, int amount, Direction side) { return 0; }
    @Override public boolean renderExtendedTube() { return false; }

    public int cookTime() { return furnaceCookTime; }
    public int maxCookTime() { return furnaceMaxCookTime; }
    public int speedyTime() { return speedyTime; }
    public List<ItemStack> inventoryView() { return List.copyOf(items); }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CookTime", furnaceCookTime);
        tag.putInt("SpeedyTime", speedyTime);
        net.minecraft.world.ContainerHelper.saveAllItems(tag, items);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        furnaceCookTime = tag.getInt("CookTime");
        speedyTime = tag.getInt("SpeedyTime");
        net.minecraft.world.ContainerHelper.loadAllItems(tag, items);
    }
}
