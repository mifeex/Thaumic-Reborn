package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.crucible.EssentiaStore;
import com.thaumcraftmodern.crucible.ItemAspectRegistry;
import com.thaumcraftmodern.essentia.AlembicStorage;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.AlchemicalFurnaceBlock;
import com.thaumcraftmodern.world.menu.AlchemicalFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AlchemicalFurnaceBlockEntity extends BlockEntity
        implements WorldlyContainer, MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int MAX_ESSENTIA = 50;
    /** TC4 TileAlchemyFurnace scans the five blocks directly above it. */
    public static final int MAX_ALEMBIC_STACK = 5;
    private static final int[] BOTTOM_SLOTS = {FUEL_SLOT};
    private static final int[] SIDE_SLOTS = {INPUT_SLOT};
    private static final int[] TOP_SLOTS = {};

    private final NonNullList<ItemStack> items =
            NonNullList.withSize(2, ItemStack.EMPTY);
    private final EssentiaStore essentia = new EssentiaStore();
    private int burnTime;
    private int currentBurnTime;
    private int cookTime;
    private int smeltTime = 100;
    private int count;
    private boolean speedBoost;
    private int clientEssentiaAmount;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> currentBurnTime;
                case 2 -> cookTime;
                case 3 -> smeltTime;
                case 4 -> level != null && level.isClientSide
                        ? clientEssentiaAmount : essentia.total();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> currentBurnTime = value;
                case 2 -> cookTime = value;
                case 3 -> smeltTime = value;
                case 4 -> clientEssentiaAmount = Math.max(0,
                        Math.min(MAX_ESSENTIA, value));
                default -> { }
            }
        }
        @Override public int getCount() { return 5; }
    };

    public AlchemicalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMICAL_FURNACE.get(), pos, state);
    }

    public static void serverTick(Level rawLevel, BlockPos pos,
            BlockState state, AlchemicalFurnaceBlockEntity furnace) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        boolean wasBurning = furnace.burnTime > 0;
        boolean dirty = false;
        furnace.count++;
        if (furnace.burnTime > 0) furnace.burnTime--;

        furnace.pushToAlembics(level);

        if (furnace.burnTime == 0 && furnace.canSmelt()) {
            ItemStack fuel = furnace.items.get(FUEL_SLOT);
            int value = fuelBurnTime(fuel);
            furnace.currentBurnTime = furnace.burnTime = value;
            if (value > 0) {
                furnace.speedBoost = fuel.is(ModItems.ALUMENTUM.get());
                ItemStack remainder = fuel.getCraftingRemainingItem();
                fuel.shrink(1);
                if (fuel.isEmpty()) furnace.items.set(FUEL_SLOT, remainder);
                dirty = true;
            }
        }
        if (furnace.burnTime > 0 && furnace.canSmelt()) {
            furnace.cookTime++;
            if (furnace.cookTime >= furnace.smeltTime) {
                furnace.cookTime = 0;
                furnace.smeltOne();
                dirty = true;
            }
        } else if (furnace.cookTime != 0) {
            furnace.cookTime = 0;
            dirty = true;
        }
        boolean burning = furnace.burnTime > 0;
        if (wasBurning != burning) {
            level.setBlock(pos, state.setValue(AlchemicalFurnaceBlock.LIT, burning),
                    Block.UPDATE_ALL);
            dirty = true;
        }
        if (dirty) furnace.sync();
    }

    private void pushToAlembics(ServerLevel level) {
        if (essentia.isEmpty() || count % (speedBoost ? 20 : 40) != 0) return;
        List<String> excluded = new ArrayList<>();
        for (int y = 1; y <= MAX_ALEMBIC_STACK; y++) {
            if (!(level.getBlockEntity(worldPosition.above(y)) instanceof AlembicStorage alembic)) break;
            String stored = alembic.storedAspect();
            if (stored != null && alembic.storedAmount() < alembic.capacity()
                    && (alembic.filterAspect() == null
                    || alembic.filterAspect().equals(stored))
                    && essentia.amount(stored) > 0
                    && alembic.acceptFromFurnace(stored, 1) == 1) {
                essentia.remove(stored, 1);
                excluded.add(stored);
                sync();
            }
        }
        for (int y = 1; y <= MAX_ALEMBIC_STACK; y++) {
            if (!(level.getBlockEntity(worldPosition.above(y)) instanceof AlembicStorage alembic)) break;
            if (alembic.storedAspect() != null && alembic.storedAmount() != 0) continue;
            String selected = alembic.filterAspect();
            if (selected != null && essentia.amount(selected) <= 0) continue;
            if (selected == null) selected = randomAspect(level.random, excluded);
            if (selected == null) continue;
            if (alembic.acceptFromFurnace(selected, 1) == 1) {
                essentia.remove(selected, 1);
                sync();
                break;
            }
        }
    }

    private @Nullable String randomAspect(RandomSource random, List<String> excluded) {
        List<String> candidates = essentia.view().entrySet().stream()
                .filter(entry -> entry.getValue() > 0 && !excluded.contains(entry.getKey()))
                .map(Map.Entry::getKey).toList();
        return candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
    }

    private boolean canSmelt() {
        ItemStack input = items.get(INPUT_SLOT);
        Map<String, Integer> aspects = ItemAspectRegistry.aspects(input).orElse(null);
        if (input.isEmpty() || aspects == null || aspects.isEmpty()) return false;
        int total = aspects.values().stream().mapToInt(Integer::intValue).sum();
        if (total > MAX_ESSENTIA - essentia.total()) return false;
        smeltTime = Math.max(1, total * 10);
        return true;
    }

    private void smeltOne() {
        ItemStack input = items.get(INPUT_SLOT);
        Map<String, Integer> aspects = ItemAspectRegistry.aspects(input).orElse(null);
        if (aspects == null || aspects.isEmpty()) return;
        aspects.forEach(essentia::add);
        input.shrink(1);
        if (input.isEmpty()) items.set(INPUT_SLOT, ItemStack.EMPTY);
        sync();
    }

    public static int fuelBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.is(ModItems.ALUMENTUM.get())) return 6400;
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    public ContainerData data() { return data; }
    public int essentiaAmount() { return essentia.total(); }
    public Map<String, Integer> essentia() { return essentia.view(); }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition,
                getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) Containers.dropContents(level, worldPosition, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.put("Aspects", essentia.save());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("CurrentBurnTime", currentBurnTime);
        tag.putInt("CookTime", cookTime);
        tag.putInt("SmeltTime", smeltTime);
        tag.putBoolean("SpeedBoost", speedBoost);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        essentia.load(tag.getCompound("Aspects"));
        burnTime = Math.max(0, tag.getInt("BurnTime"));
        currentBurnTime = Math.max(0, tag.getInt("CurrentBurnTime"));
        cookTime = Math.max(0, tag.getInt("CookTime"));
        smeltTime = Math.max(1, tag.getInt("SmeltTime"));
        speedBoost = tag.getBoolean("SpeedBoost");
    }

    @Override public Component getDisplayName() {
        return Component.translatable("container.thaumic_reborn.alchemical_furnace");
    }

    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AlchemicalFurnaceMenu(id, inventory, this, data);
    }

    @Override public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? BOTTOM_SLOTS
                : side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
    }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.DOWN && canPlaceItem(slot, stack);
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side != Direction.DOWN
                || slot != FUEL_SLOT
                || stack.is(Items.BUCKET);
    }
    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }
    @Override public void clearContent() { items.clear(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT ? ItemAspectRegistry.aspects(stack).isPresent()
                : slot == FUEL_SLOT && fuelBurnTime(stack) > 0;
    }
}
