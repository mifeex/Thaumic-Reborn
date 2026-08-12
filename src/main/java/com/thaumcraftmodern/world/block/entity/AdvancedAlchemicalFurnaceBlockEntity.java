package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.crucible.EssentiaStore;
import com.thaumcraftmodern.crucible.ItemAspectRegistry;
import com.thaumcraftmodern.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.ArcaneBellowsBlock;
import com.thaumcraftmodern.world.block.AdvancedAlchemicalFurnaceBlock;
import com.thaumcraftmodern.visnet.VisMachineAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** TC4 TileAlchemyFurnaceAdvanced and its four source-only nozzles. */
public final class AdvancedAlchemicalFurnaceBlockEntity extends BlockEntity
        implements EssentiaTransport {
    public static final int MAX_ESSENTIA = 500;
    public static final int MAX_POWER = 500;
    private final EssentiaStore essentia = new EssentiaStore();
    private int heat;
    private int entropyPower;
    private int waterPower;
    private int processed;
    private int count;

    public AdvancedAlchemicalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(), pos, state);
    }

    public static void serverTick(Level rawLevel, BlockPos pos, BlockState state,
            AdvancedAlchemicalFurnaceBlockEntity furnace) {
        if (!(rawLevel instanceof ServerLevel level) || furnace.isNozzle()) return;
        furnace.count++;
        if (furnace.processed > 0) furnace.processed--;
        if (furnace.count % 5 != 0) return;
        int oldLight = heatLight(furnace.heat);
        int oldHeat = furnace.heat;
        int oldEntropy = furnace.entropyPower;
        int oldWater = furnace.waterPower;
        if (furnace.heat > 0) furnace.heat--;
        if (furnace.heat <= MAX_POWER)
            furnace.heat += VisMachineAccess.consumeNearest(level, pos, PrimalAspect.IGNIS, 50);
        if (furnace.entropyPower <= MAX_POWER)
            furnace.entropyPower += VisMachineAccess.consumeNearest(level, pos, PrimalAspect.PERDITIO, 50);
        if (furnace.waterPower <= MAX_POWER)
            furnace.waterPower += VisMachineAccess.consumeNearest(level, pos, PrimalAspect.AQUA, 50);
        if (oldLight != heatLight(furnace.heat)) level.setBlock(pos,
                state.setValue(AdvancedAlchemicalFurnaceBlock.LIGHT, heatLight(furnace.heat)),
                Block.UPDATE_ALL);
        if (oldHeat != furnace.heat || oldEntropy != furnace.entropyPower
                || oldWater != furnace.waterPower) furnace.sync(false);
    }

    /** Processes exactly one dropped item, as the original collision handler did. */
    public boolean process(ItemStack stack) {
        if (isNozzle() || processed != 0 || stack.isEmpty()) return false;
        Map<String, Integer> aspects = ItemAspectRegistry.aspects(stack).orElse(null);
        if (aspects == null || aspects.isEmpty()) return false;
        int amount = aspects.values().stream().mapToInt(Integer::intValue).sum();
        if (amount + essentia.total() > MAX_ESSENTIA
                || !hasProcessingPower(amount, heat, entropyPower, waterPower))
            return false;
        heat -= amount * 2;
        entropyPower -= amount;
        waterPower -= amount;
        processed = processingDelayForHeat(heat, attachedBellows());
        aspects.forEach(essentia::add);
        if (level instanceof ServerLevel server) server.setBlock(worldPosition,
                getBlockState().setValue(AdvancedAlchemicalFurnaceBlock.LIGHT,
                        heatLight(heat)), Block.UPDATE_ALL);
        sync(true);
        return true;
    }

    public static boolean hasProcessingPower(int essentiaAmount, int heat,
            int entropyPower, int waterPower) {
        return essentiaAmount > 0 && essentiaAmount * 2 <= heat
                && essentiaAmount <= entropyPower && essentiaAmount <= waterPower;
    }

    public static int processingDelayForHeat(int heatAfterProcessing) {
        return processingDelayForHeat(heatAfterProcessing, 0);
    }

    /**
     * Thaumic Bases inherited TC4's alchemical-furnace bellows multiplier:
     * each working bellows removes 12.5% of the processing delay. TC4 checked
     * every adjacent direction, so the physical maximum was six bellows.
     */
    public static int processingDelayForHeat(int heatAfterProcessing, int bellows) {
        int base = (int) (5.0F + Math.max(0.0F,
                (1.0F - heatAfterProcessing / (float) MAX_POWER) * 100.0F));
        int workingBellows = Math.max(0, Math.min(Direction.values().length, bellows));
        return Math.max(1, (int) (base * (1.0F - 0.125F * workingBellows)));
    }

    public static int heatLight(int heat) {
        if (heat <= 100) return 0;
        return Math.max(0, Math.min(15, (int) (heat / (float) MAX_POWER * 12.0F)));
    }

    public boolean isNozzle() {
        return getBlockState().hasProperty(AdvancedAlchemicalFurnaceBlock.PART)
                && getBlockState().getValue(AdvancedAlchemicalFurnaceBlock.PART)
                == AdvancedAlchemicalFurnaceBlock.LOWER_NOZZLE;
    }

    public int heat() { return heat; }
    public int entropyPower() { return entropyPower; }
    public int waterPower() { return waterPower; }
    /** Animated fire and flux are transient processing effects, not tank gauges. */
    public boolean isProcessing() {
        AdvancedAlchemicalFurnaceBlockEntity source = source();
        return source != null && source.processed > 0;
    }

    /** Finds bellows attached to any outside face of the 3x2x3 multiblock. */
    public int attachedBellows() {
        if (level == null || isNozzle()) return 0;
        Set<BlockPos> found = new HashSet<>();
        for (int y = 0; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (y == 1 && x == 0 && z == 0) continue;
                    BlockPos shell = worldPosition.offset(x, y, z);
                    for (Direction outward : Direction.values()) {
                        BlockPos candidate = shell.relative(outward);
                        int relativeX = candidate.getX() - worldPosition.getX();
                        int relativeY = candidate.getY() - worldPosition.getY();
                        int relativeZ = candidate.getZ() - worldPosition.getZ();
                        if (relativeX >= -1 && relativeX <= 1
                                && relativeY >= 0 && relativeY <= 1
                                && relativeZ >= -1 && relativeZ <= 1) continue;
                        BlockState state = level.getBlockState(candidate);
                        if (state.is(ModBlocks.ARCANE_BELLOWS.get())
                                && state.getValue(ArcaneBellowsBlock.FACING)
                                == outward.getOpposite()
                                && !level.hasNeighborSignal(candidate)) {
                            found.add(candidate);
                            if (found.size() == Direction.values().length)
                                return Direction.values().length;
                        }
                    }
                }
            }
        }
        return found.size();
    }
    public int essentiaAmount() {
        AdvancedAlchemicalFurnaceBlockEntity source = source();
        return source == null ? 0 : source.essentia.total();
    }
    public Map<String, Integer> essentia() {
        AdvancedAlchemicalFurnaceBlockEntity source = source();
        return source == null ? Map.of() : source.essentia.view();
    }

    public void importEssentia(Map<String, Integer> stored) {
        if (isNozzle() || stored == null || stored.isEmpty()) return;
        int room = MAX_ESSENTIA - essentia.total();
        for (Map.Entry<String, Integer> entry : stored.entrySet()) {
            int accepted = Math.min(room, Math.max(0, entry.getValue()));
            essentia.add(entry.getKey(), accepted);
            room -= accepted;
            if (room == 0) break;
        }
        sync(true);
    }

    private @Nullable AdvancedAlchemicalFurnaceBlockEntity source() {
        if (!isNozzle()) return this;
        if (level == null) return null;
        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
            if (blockEntity instanceof AdvancedAlchemicalFurnaceBlockEntity furnace
                    && !furnace.isNozzle()) return furnace;
        }
        return null;
    }

    private @Nullable Direction outputSide() {
        if (!isNozzle() || level == null) return null;
        for (Direction towardCore : Direction.values()) {
            BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(towardCore));
            if (blockEntity instanceof AdvancedAlchemicalFurnaceBlockEntity furnace
                    && !furnace.isNozzle()) return towardCore.getOpposite();
        }
        return null;
    }

    @Override public boolean isConnectable(Direction side) { return side == outputSide(); }
    @Override public boolean canInputFrom(Direction side) { return false; }
    @Override public boolean canOutputTo(Direction side) { return isConnectable(side); }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return null; }
    @Override public int suctionAmount(Direction side) { return 0; }
    @Override public @Nullable String essentiaType(Direction side) {
        AdvancedAlchemicalFurnaceBlockEntity source = source();
        return source == null || source.essentia.isEmpty()
                ? null : source.essentia.view().keySet().iterator().next();
    }
    @Override public int essentiaAmount(Direction side) {
        String aspect = essentiaType(side);
        AdvancedAlchemicalFurnaceBlockEntity source = source();
        return source == null || aspect == null ? 0 : source.essentia.amount(aspect);
    }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) {
        AdvancedAlchemicalFurnaceBlockEntity source = source();
        if (!canOutputTo(side) || source == null || !source.essentia.remove(aspect, amount)) return 0;
        source.sync(true);
        return amount;
    }
    @Override public int addEssentia(String aspect, int amount, Direction side) { return 0; }
    @Override public boolean canReturnEssentia() { return false; }

    private void sync(boolean comparators) {
        setChanged();
        if (level == null) return;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        if (comparators) for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos nozzle = worldPosition.relative(direction);
            level.updateNeighbourForOutputSignal(nozzle, level.getBlockState(nozzle).getBlock());
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Aspects", essentia.save());
        tag.putInt("Heat", heat);
        tag.putInt("EntropyPower", entropyPower);
        tag.putInt("WaterPower", waterPower);
        tag.putInt("Processed", processed);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        essentia.load(tag.getCompound("Aspects"));
        heat = Math.max(0, tag.getInt("Heat"));
        entropyPower = Math.max(0, tag.getInt("EntropyPower"));
        waterPower = Math.max(0, tag.getInt("WaterPower"));
        processed = Math.max(0, tag.getInt("Processed"));
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
    @Override public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 2, 2));
    }
}
