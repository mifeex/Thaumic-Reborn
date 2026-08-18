package com.thaumcraftmodern.world.block.entity;

import com.mojang.authlib.GameProfile;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.crucible.ItemAspectRegistry;
import com.thaumcraftmodern.focus.FocusUpgradeType;
import com.thaumcraftmodern.focus.WandFocusType;
import com.thaumcraftmodern.item.ElementalPickaxeItem;
import com.thaumcraftmodern.item.WandFocusItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEnchantments;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.visnet.VisMachineAccess;
import com.thaumcraftmodern.world.menu.ArcaneBoreMenu;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

/** Full TC4 TileArcaneBore mining, upgrade, power and output state machine. */
public final class ArcaneBoreBlockEntity extends BlockEntity
        implements net.minecraft.world.Container, MenuProvider {
    public static final int MAX_RADIUS = 2;
    public static final int MAX_DEPTH = 64;
    public static final int MAX_SPEEDY_TIME = 20;
    public static final int MAX_VIS_PER_TICK = 100;
    public static final int VIS_PER_FAST_BLOCK = 5;
    public static final int DATA_COUNT = 4;
    private static final GameProfile BORE_PROFILE = new GameProfile(
            UUID.nameUUIDFromBytes("FakeThaumcraftBore".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            "FakeThaumcraftBore");

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final EnumMap<PrimalAspect, Integer> repairVis = new EnumMap<>(PrimalAspect.class);
    private final EnumMap<PrimalAspect, Integer> repairCost = new EnumMap<>(PrimalAspect.class);
    private Direction orientation = Direction.UP;
    private Direction baseOrientation = Direction.UP;
    private int spiral;
    private float currentRadius;
    private float radiusIncrement;
    private int digDelay;
    private boolean hasTarget;
    private BlockPos digTarget;
    private float speedyTime;
    private long repairCounter;
    private int fortune;
    private int speed;
    private int area;
    private int rotX;
    private int rotZ;
    private int targetRotX;
    private int targetRotZ;
    private int speedX;
    private int speedZ;
    private float aimX;
    private float aimZ;
    private float targetAimX;
    private float targetAimZ;
    private int topRotation;
    private int beamTicks;
    private FakePlayer fakePlayer;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> area;
                case 1 -> speed;
                case 2 -> fortune;
                case 3 -> Math.round(speedyTime * 10.0F);
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) area = value;
            else if (index == 1) speed = value;
            else if (index == 2) fortune = value;
            else if (index == 3) speedyTime = value / 10.0F;
        }
        @Override public int getCount() { return DATA_COUNT; }
    };

    public ArcaneBoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_BORE.get(), pos, state);
        setOrientation(Direction.UP);
    }

    public static void serverTick(Level rawLevel, BlockPos pos, BlockState state,
            ArcaneBoreBlockEntity bore) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        bore.ensureFakePlayer(level);
        bore.rechargeSpeed(level);
        bore.updateRotation();
        if (bore.isPowered(level) && bore.hasFocus() && bore.hasPickaxe()
                && bore.canUsePickaxe()) bore.updateMining(level);
        bore.updatePickaxeLifecycle(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state,
            ArcaneBoreBlockEntity bore) {
        bore.updateRotation();
        if (bore.beamTicks > 0) {
            bore.beamTicks--;
            bore.topRotation = (bore.topRotation + Math.max(1, bore.beamTicks / 6)) % 360;
            if (level.getGameTime() % 25L == 0L) {
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D,
                        pos.getZ() + 0.5D, ModSounds.RUMBLE.get(),
                        SoundSource.BLOCKS, 0.25F, 0.9F + level.random.nextFloat() * 0.2F, false);
            }
            bore.easeAim();
        } else {
            if (bore.topRotation % 90 != 0) bore.topRotation += Math.min(10, 90 - bore.topRotation % 90);
            bore.aimX *= 0.9F;
            bore.aimZ *= 0.9F;
        }
    }

    private void ensureFakePlayer(ServerLevel level) {
        if (fakePlayer == null) fakePlayer = FakePlayerFactory.get(level, BORE_PROFILE);
    }

    private void rechargeSpeed(ServerLevel level) {
        if (speedyTime >= MAX_SPEEDY_TIME) return;
        int drained = VisMachineAccess.consumeNearest(level, worldPosition,
                PrimalAspect.PERDITIO, MAX_VIS_PER_TICK);
        if (drained > 0) speedyTime += drained / (float) VIS_PER_FAST_BLOCK;
        ArcaneBoreBaseBlockEntity base = base();
        if (speedyTime < MAX_SPEEDY_TIME && base != null
                && base.drawPerditio(level)) speedyTime += MAX_SPEEDY_TIME;
        speedyTime = Math.min(MAX_SPEEDY_TIME, speedyTime);
    }

    private void updateMining(ServerLevel level) {
        if (fakePlayer == null || rotX != targetRotX || rotZ != targetRotZ) return;
        if (--digDelay > 0) return;
        boolean dug = false;
        if (hasTarget && digTarget != null) {
            hasTarget = false;
            BlockState state = level.getBlockState(digTarget);
            if (!state.isAir()) dug = mineBlock(level, digTarget, state);
        }
        findNextBlock(level);
        if (dug && speedyTime > 0.0F) speedyTime -= 1.0F;
    }

    private void findNextBlock(ServerLevel level) {
        if (radiusIncrement == 0.0F) radiusIncrement = (MAX_RADIUS + area) / 360.0F;
        BlockPos lane;
        do {
            spiral = (spiral + 2) % 360;
            currentRadius += radiusIncrement;
            int radius = MAX_RADIUS + area;
            if (currentRadius > radius || currentRadius < -radius) radiusIncrement *= -1.0F;
            double angle = spiral / 180.0D * Math.PI;
            double ox = currentRadius * Math.sin(angle);
            double oy = currentRadius * Math.cos(angle);
            double yaw = Math.PI * 0.5D * orientation.getStepX();
            double yawX = ox * Math.cos(yaw);
            double yawZ = -ox * Math.sin(yaw);
            double pitch = Math.PI * 0.5D * orientation.getStepY();
            double pitchY = oy * Math.cos(pitch) + yawZ * Math.sin(pitch);
            double pitchZ = yawZ * Math.cos(pitch) - oy * Math.sin(pitch);
            lane = BlockPos.containing(worldPosition.getX() + 0.5D + orientation.getStepX() + yawX,
                    worldPosition.getY() + 0.5D + orientation.getStepY() + pitchY,
                    worldPosition.getZ() + 0.5D + orientation.getStepZ() + pitchZ);
        } while (lane.equals(lastLane));
        lastLane = lane;

        BlockPos scan = lane.relative(orientation, 2);
        for (int depth = 0; depth < MAX_DEPTH; depth++, scan = scan.relative(orientation)) {
            BlockState state = level.getBlockState(scan);
            if (state.getDestroySpeed(level, scan) < 0.0F) break;
            if (!isDiggable(level, scan, state)) continue;
            BlockPos target = rayTarget(level, scan);
            BlockState targetState = level.getBlockState(target);
            if (!isDiggable(level, target, targetState)
                    || targetState.getDestroySpeed(level, target) < 0.0F) continue;
            digTarget = target.immutable();
            digDelay = digDelay(targetState, target);
            hasTarget = true;
            beamTicks = 64;
            updateAimTarget(target);
            sync();
            break;
        }
    }

    private BlockPos lastLane = BlockPos.ZERO;

    private BlockPos rayTarget(ServerLevel level, BlockPos scan) {
        Vec3 start = Vec3.atCenterOf(worldPosition).add(orientation.getStepX(),
                orientation.getStepY(), orientation.getStepZ());
        BlockHitResult hit = level.clip(new ClipContext(start, Vec3.atCenterOf(scan),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, fakePlayer));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : scan;
    }

    private static boolean isDiggable(ServerLevel level, BlockPos pos, BlockState state) {
        return !state.isAir() && !state.getCollisionShape(level, pos).isEmpty();
    }

    private int digDelay(BlockState state, BlockPos target) {
        int delay = Math.max(10 - speed,
                (int) (state.getDestroySpeed(level, target) * 2.0F) - speed * 2);
        return speedyTime < 1.0F ? delay * 4 : delay;
    }

    private boolean mineBlock(ServerLevel level, BlockPos target, BlockState state) {
        ItemStack pickaxe = getItem(1);
        fakePlayer.setPos(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D);
        fakePlayer.getInventory().selected = 0;
        fakePlayer.getInventory().setItem(0, pickaxe);
        int xp = ForgeHooks.onBlockBreakEvent(level, GameType.SURVIVAL, fakePlayer, target);
        if (xp < 0) return false;
        BlockEntity targetEntity = level.getBlockEntity(target);
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(
                state, level, target, targetEntity, fakePlayer, pickaxe));
        boolean silk = EnchantmentHelper.getItemEnchantmentLevel(
                Enchantments.SILK_TOUCH, pickaxe) > 0
                || focusUpgrade(FocusUpgradeType.SILK_TOUCH) > 0;
        int dropFortune = Math.max(fortune, EnchantmentHelper.getItemEnchantmentLevel(
                Enchantments.BLOCK_FORTUNE, pickaxe));
        if (!level.removeBlock(target, false)) return false;
        for (ItemEntity existing : level.getEntitiesOfClass(ItemEntity.class,
                new AABB(target).inflate(1.0D))) {
            if (!existing.getItem().isEmpty()) drops.add(existing.getItem().copy());
            existing.discard();
        }
        if (!silk && xp > 0) state.getBlock().popExperience(level, target, xp);
        level.levelEvent(2001, target, Block.getId(state));
        for (ItemStack drop : drops) ejectOrStore(level,
                nativeClusterResult(level, drop, silk, dropFortune));
        pickaxe.hurtAndBreak(1, fakePlayer, broken -> { });
        if (pickaxe.isEmpty()) setItem(1, ItemStack.EMPTY); else setChanged();
        placeTunnelLight(level);
        return true;
    }

    private ItemStack nativeClusterResult(ServerLevel level, ItemStack drop,
            boolean silk, int dropFortune) {
        if (silk || drop.isEmpty() || !nativeClustersEnabled()
                || level.random.nextFloat() >= 0.2F + dropFortune * 0.075F) return drop;
        var id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(drop.getItem());
        if (id == null) return drop;
        String path = id.getPath();
        Item replacement = path.contains("iron") ? ModItems.NATIVE_IRON_CLUSTER.get()
                : path.contains("gold") ? ModItems.NATIVE_GOLD_CLUSTER.get()
                : path.contains("copper") ? ModItems.NATIVE_COPPER_CLUSTER.get()
                : path.contains("tin") ? ModItems.NATIVE_TIN_CLUSTER.get()
                : path.contains("silver") ? ModItems.NATIVE_SILVER_CLUSTER.get()
                : path.contains("lead") ? ModItems.NATIVE_LEAD_CLUSTER.get() : null;
        return replacement == null ? drop : new ItemStack(replacement, drop.getCount());
    }

    private boolean nativeClustersEnabled() {
        return getItem(1).getItem() instanceof ElementalPickaxeItem
                || focusUpgrade(FocusUpgradeType.DOWSING) > 0;
    }

    private void ejectOrStore(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return;
        ArcaneBoreBaseBlockEntity base = base();
        Direction out = base != null ? base.output() : orientation.getOpposite();
        BlockPos origin = base != null ? base.getBlockPos() : worldPosition;
        ItemStack remaining = stack.copy();
        BlockEntity destination = level.getBlockEntity(origin.relative(out));
        if (destination != null) {
            IItemHandler handler = destination.getCapability(ForgeCapabilities.ITEM_HANDLER,
                    out.getOpposite()).orElse(null);
            if (handler != null) remaining = ItemHandlerHelper.insertItemStacked(
                    handler, remaining, false);
        }
        if (remaining.isEmpty()) return;
        ItemEntity item = new ItemEntity(level,
                origin.getX() + 0.5D + out.getStepX() * 0.66D,
                origin.getY() + 0.4D + baseOrientation.getOpposite().getStepY() * 0.66D,
                origin.getZ() + 0.5D + out.getStepZ() * 0.66D, remaining);
        item.setDeltaMovement(0.075D * out.getStepX(), 0.025D,
                0.075D * out.getStepZ());
        level.addFreshEntity(item);
    }

    private void placeTunnelLight(ServerLevel level) {
        ArcaneBoreBaseBlockEntity base = base();
        if (base == null) return;
        boolean lamp = false;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(base.getBlockPos().relative(side))
                    .is(ModBlocks.ARCANE_LAMP.get())) { lamp = true; break; }
        }
        if (!lamp) return;
        int distance = level.random.nextInt(32) * 2;
        BlockPos target = worldPosition.relative(orientation, distance + 1);
        int pattern = distance / 2 % 4;
        if (orientation.getStepX() != 0) target = target.offset(0, 0,
                pattern == 0 ? 3 : pattern == 2 ? -3 : 0);
        else target = target.offset(pattern == 0 ? 3 : pattern == 2 ? -3 : 0, 0, 0);
        if (pattern == 3 && orientation.getStepY() == 0) target = target.below(2);
        if (level.getBlockState(target).isAir() && level.getMaxLocalRawBrightness(target) < 15) {
            level.setBlock(target, ModBlocks.ARCANE_LAMP_LIGHT.get().defaultBlockState(), 3);
        }
    }

    private void updatePickaxeLifecycle(ServerLevel level) {
        if (!hasPickaxe() || fakePlayer == null) return;
        ItemStack pickaxe = getItem(1);
        repairCounter++;
        fakePlayer.tickCount = (int) repairCounter;
        pickaxe.inventoryTick(level, fakePlayer, 0, true);
        int repairLevel = Math.min(2, EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.REPAIR.get(), pickaxe));
        if (repairLevel <= 0 || !(pickaxe.getItem() instanceof
                com.thaumicreborn.api.equipment.ThaumicRepairable)) return;
        if (repairCounter % 40L == 0L && pickaxe.isDamaged()) {
            calculateRepairCost(pickaxe, repairLevel);
            boolean enough = repairCost.entrySet().stream().allMatch(entry ->
                    repairVis.getOrDefault(entry.getKey(), 0) >= entry.getValue());
            if (enough && !repairCost.isEmpty()) {
                repairCost.forEach((aspect, amount) -> repairVis.merge(aspect, -amount, Integer::sum));
                pickaxe.setDamageValue(Math.max(0, pickaxe.getDamageValue() - repairLevel));
                sync();
            }
        }
        if (repairCounter % 5L == 0L) {
            for (var entry : repairCost.entrySet()) {
                int missing = entry.getValue() - repairVis.getOrDefault(entry.getKey(), 0);
                if (missing > 0) repairVis.merge(entry.getKey(), VisMachineAccess.consumeNearest(
                        level, worldPosition, entry.getKey(), missing), Integer::sum);
            }
        }
    }

    private void calculateRepairCost(ItemStack stack, int repairLevel) {
        repairCost.clear();
        EnumMap<PrimalAspect, Integer> primals = new EnumMap<>(PrimalAspect.class);
        ItemAspectRegistry.aspects(stack).orElse(Map.of()).forEach((aspect, amount) ->
                reduceAspect(aspect, amount, primals));
        primals.forEach((aspect, amount) -> {
            int cost = (int) Math.sqrt(amount * 2.0D) * repairLevel;
            if (cost > 0) repairCost.put(aspect, cost);
        });
    }

    private static void reduceAspect(String id, int amount,
            EnumMap<PrimalAspect, Integer> result) {
        try { result.merge(PrimalAspect.fromId(id), amount, Math::addExact); return; }
        catch (IllegalArgumentException ignored) { }
        AspectDefinition definition = AspectRegistryRuntime.find(id).orElse(null);
        if (definition == null || definition.components().size() != 2) return;
        reduceAspect(definition.components().get(0), amount, result);
        reduceAspect(definition.components().get(1), amount, result);
    }

    private boolean isPowered(ServerLevel level) {
        ArcaneBoreBaseBlockEntity base = base();
        return level.hasNeighborSignal(worldPosition)
                || base != null && level.hasNeighborSignal(base.getBlockPos());
    }

    private @Nullable ArcaneBoreBaseBlockEntity base() {
        return level != null && level.getBlockEntity(
                worldPosition.relative(baseOrientation.getOpposite()))
                instanceof ArcaneBoreBaseBlockEntity base ? base : null;
    }

    public void configurePlacement(Direction baseOrientation, Direction orientation) {
        this.baseOrientation = baseOrientation;
        setOrientation(orientation);
    }

    public void setOrientation(Direction direction) {
        orientation = direction == null ? Direction.UP : direction;
        switch (orientation) {
            case DOWN -> { targetRotZ = 180; targetRotX = 0; }
            case UP -> { targetRotZ = 0; targetRotX = 0; }
            case NORTH -> { targetRotZ = 90; targetRotX = 270; }
            case SOUTH -> { targetRotZ = 90; targetRotX = 90; }
            case WEST -> { targetRotZ = 90; targetRotX = 0; }
            case EAST -> { targetRotZ = 90; targetRotX = 180; }
        }
        speedX = speedZ = 0;
        lastLane = BlockPos.ZERO;
        hasTarget = false;
        radiusIncrement = 0.0F;
        aimX = aimZ = targetAimX = targetAimZ = 0.0F;
        sync();
    }

    private void updateRotation() {
        if (rotX < targetRotX) { rotX += speedX; speedX = rotX < targetRotX ? speedX + 1 : speedX / 3; }
        else if (rotX > targetRotX) { rotX += speedX; speedX = rotX > targetRotX ? speedX - 1 : speedX / 3; }
        else speedX = 0;
        if (rotZ < targetRotZ) { rotZ += speedZ; speedZ = rotZ < targetRotZ ? speedZ + 1 : speedZ / 3; }
        else if (rotZ > targetRotZ) { rotZ += speedZ; speedZ = rotZ > targetRotZ ? speedZ - 1 : speedZ / 3; }
        else speedZ = 0;
    }

    private void updateAimTarget(BlockPos target) {
        double xd = worldPosition.getX() + 0.5D - (target.getX() + 0.5D);
        double yd = worldPosition.getY() + 0.5D - (target.getY() + 0.5D);
        double zd = worldPosition.getZ() + 0.5D - (target.getZ() + 0.5D);
        double horizontal = Math.sqrt(xd * xd + zd * zd);
        float rx = (float) (Math.atan2(zd, xd) * 180.0D / Math.PI);
        float rz = (float) (-(Math.atan2(yd, horizontal) * 180.0D / Math.PI)) + 90.0F;
        targetAimX = Mth.wrapDegrees(rotX) + rx;
        if (orientation == Direction.EAST) targetAimX = Mth.wrapDegrees(targetAimX);
        targetAimZ = rz - rotZ;
        if (orientation.get3DDataValue() <= 1) targetAimZ += 180.0F;
    }

    private void easeAim() {
        aimX += (targetAimX - aimX) / 6.0F;
        aimZ += (targetAimZ - aimZ) / 6.0F;
    }

    private void refreshUpgrades() {
        fortune = speed = area = 0;
        ItemStack focus = getItem(0);
        if (hasFocus()) {
            fortune = WandFocusItem.upgradeLevel(focus, FocusUpgradeType.TREASURE);
            speed = WandFocusItem.upgradeLevel(focus, FocusUpgradeType.POTENCY);
            area = WandFocusItem.upgradeLevel(focus, FocusUpgradeType.ENLARGE);
        }
        ItemStack pickaxe = getItem(1);
        if (hasPickaxe()) {
            fortune = Math.max(fortune, EnchantmentHelper.getItemEnchantmentLevel(
                    Enchantments.BLOCK_FORTUNE, pickaxe));
            speed += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY,
                    pickaxe);
        }
    }

    public boolean hasFocus() {
        return getItem(0).getItem() instanceof WandFocusItem focus
                && focus.type() == WandFocusType.EXCAVATION;
    }
    public boolean hasPickaxe() { return getItem(1).getItem() instanceof PickaxeItem; }
    private boolean canUsePickaxe() {
        ItemStack stack = getItem(1);
        return stack.isDamageableItem() && stack.getDamageValue() + 1 < stack.getMaxDamage();
    }
    private int focusUpgrade(FocusUpgradeType upgrade) {
        return hasFocus() ? WandFocusItem.upgradeLevel(getItem(0), upgrade) : 0;
    }

    public Direction orientation() { return orientation; }
    public Direction baseOrientation() { return baseOrientation; }
    public int rotX() { return rotX; }
    public int rotZ() { return rotZ; }
    public float aimX() { return aimX; }
    public float aimZ() { return aimZ; }
    public int topRotation() { return topRotation; }
    public int beamTicks() { return beamTicks; }
    public @Nullable BlockPos digTarget() { return digTarget; }
    public ContainerData data() { return data; }

    private void sync() {
        refreshUpgrades();
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(
                worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("orientation", orientation.get3DDataValue());
        tag.putInt("baseOrientation", baseOrientation.get3DDataValue());
        tag.putFloat("SpeedyTime", speedyTime);
        tag.putInt("spiral", spiral);
        tag.putFloat("radius", currentRadius);
        tag.putInt("rotX", rotX); tag.putInt("rotZ", rotZ);
        tag.putInt("targetRotX", targetRotX); tag.putInt("targetRotZ", targetRotZ);
        tag.putInt("beamTicks", beamTicks); tag.putInt("topRotation", topRotation);
        if (digTarget != null) tag.putLong("digTarget", digTarget.asLong());
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        orientation = Direction.from3DDataValue(tag.getInt("orientation"));
        baseOrientation = Direction.from3DDataValue(tag.getInt("baseOrientation"));
        speedyTime = tag.getFloat("SpeedyTime");
        spiral = tag.getInt("spiral"); currentRadius = tag.getFloat("radius");
        rotX = tag.getInt("rotX"); rotZ = tag.getInt("rotZ");
        targetRotX = tag.getInt("targetRotX"); targetRotZ = tag.getInt("targetRotZ");
        beamTicks = tag.getInt("beamTicks"); topRotation = tag.getInt("topRotation");
        digTarget = tag.contains("digTarget") ? BlockPos.of(tag.getLong("digTarget")) : null;
        ContainerHelper.loadAllItems(tag, items);
        refreshUpgrades();
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection,
            ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }

    @Override public Component getDisplayName() {
        return Component.translatable("container.thaumic_reborn.arcane_bore");
    }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ArcaneBoreMenu(id, inventory, this, data);
    }
    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) sync(); return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack); if (stack.getCount() > 1) stack.setCount(1); sync();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 64.0D;
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && stack.getItem() instanceof WandFocusItem focus
                && focus.type() == WandFocusType.EXCAVATION
                || slot == 1 && stack.getItem() instanceof PickaxeItem;
    }
    @Override public void clearContent() { items.clear(); sync(); }
}
