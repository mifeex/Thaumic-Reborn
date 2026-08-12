package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.essentia.JarRedstoneSignal;
import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** TC4 void jar: displays 64 points and destroys further matching essentia. */
public final class VoidJarBlockEntity extends BlockEntity implements EssentiaTransport {
    public static final int CAPACITY = 64;
    private static final int TRANSFER_INTERVAL = 5;
    private static final int OVERFLOW_HOLD_TICKS = TRANSFER_INTERVAL + 1;
    private @Nullable String aspect;
    private @Nullable String filter;
    private Direction filterFacing = Direction.NORTH;
    private int amount;
    private int count;
    private int overflowTicks;

    public VoidJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_JAR.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel, BlockPos pos,
            BlockState state, VoidJarBlockEntity jar) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        if (jar.overflowTicks > 0) {
            int previousSignal = jar.comparatorSignal();
            --jar.overflowTicks;
            jar.notifyRedstoneChange(previousSignal);
        }
        if (++jar.count % TRANSFER_INTERVAL == 0) jar.fill(level);
    }

    private void fill(ServerLevel level) {
        EssentiaTransport remote = EssentiaConnections.neighbour(level, worldPosition, Direction.UP).orElse(null);
        if (remote == null || !remote.canOutputTo(Direction.DOWN)) return;
        String wanted = filter != null ? filter : amount > 0 ? aspect : remote.essentiaType(Direction.DOWN);
        if (wanted == null || remote.suctionAmount(Direction.DOWN) >= suctionAmount(Direction.UP)
                || suctionAmount(Direction.UP) < remote.minimumSuction()) return;
        int taken = remote.takeEssentia(wanted, 1, Direction.DOWN);
        if (taken > 0) addEssentia(wanted, taken, Direction.UP);
    }

    public @Nullable String aspect() { return aspect; }
    public @Nullable String filter() { return filter; }
    public int amount() { return amount; }
    public Direction filterFacing() { return filterFacing; }
    public void setFilter(@Nullable String value, Direction facing) {
        filter = value == null || value.isBlank() ? null : value;
        if (facing.getAxis().isHorizontal()) filterFacing = facing;
        if (amount == 0) aspect = filter;
        EssentiaSync.changed(this);
    }
    public void emptyContents() {
        int previousSignal = comparatorSignal();
        amount = 0;
        overflowTicks = 0;
        aspect = filter;
        EssentiaSync.changed(this);
        notifyRedstoneChange(previousSignal);
    }
    public boolean isOverflowing() { return overflowTicks > 0; }
    public int comparatorSignal() {
        return JarRedstoneSignal.forVoidJar(amount, isOverflowing());
    }

    @Override public boolean isConnectable(Direction side) { return side == Direction.UP; }
    @Override public boolean canInputFrom(Direction side) { return side == Direction.UP; }
    @Override public boolean canOutputTo(Direction side) { return side == Direction.UP; }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return filter != null ? filter : aspect; }
    @Override public int suctionAmount(Direction side) { return filter != null && amount < CAPACITY ? 48 : 32; }
    @Override public @Nullable String essentiaType(Direction side) { return aspect; }
    @Override public int essentiaAmount(Direction side) { return amount; }
    @Override public int minimumSuction() { return filter != null ? 48 : 32; }
    @Override public int takeEssentia(String requestedAspect, int requested, Direction side) {
        if (!canOutputTo(side) || requested <= 0 || !Objects.equals(aspect, requestedAspect) || amount < requested) return 0;
        int previousSignal = comparatorSignal();
        amount -= requested;
        overflowTicks = 0;
        if (amount == 0) aspect = filter;
        EssentiaSync.changed(this);
        notifyRedstoneChange(previousSignal);
        return requested;
    }
    @Override public int addEssentia(String incoming, int requested, Direction side) {
        if (!canInputFrom(side) || !acceptsAspect(incoming, requested)) return 0;
        int previousSignal = comparatorSignal();
        boolean visibleChange = amount < CAPACITY;
        boolean destroysExcess = (long) amount + requested > CAPACITY;
        aspect = incoming;
        amount = Math.min(CAPACITY, amount + requested);
        if (destroysExcess) overflowTicks = OVERFLOW_HOLD_TICKS;
        if (visibleChange || destroysExcess) EssentiaSync.changed(this);
        notifyRedstoneChange(previousSignal);
        return requested;
    }
    public boolean acceptsAspect(@Nullable String incoming, int requested) {
        return incoming != null && requested > 0
                && AspectRegistryRuntime.find(incoming).isPresent()
                && (filter == null || filter.equals(incoming))
                && (amount == 0 || Objects.equals(aspect, incoming));
    }
    private void notifyRedstoneChange(int previousSignal) {
        if (level != null && !level.isClientSide
                && previousSignal != comparatorSignal()) {
            getBlockState().updateNeighbourShapes(
                    level, worldPosition, Block.UPDATE_ALL);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.updateNeighbourForOutputSignal(
                    worldPosition, getBlockState().getBlock());
        }
    }
    @Override public boolean renderExtendedTube() { return true; }

    public CompoundTag saveForItem() { CompoundTag tag = new CompoundTag(); writePayload(tag); return tag; }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); writePayload(tag); }
    private void writePayload(CompoundTag tag) {
        if (aspect != null) tag.putString("Aspect", aspect);
        if (filter != null) tag.putString("AspectFilter", filter);
        tag.putInt("Facing", filterFacing.ordinal());
        tag.putInt("Amount", amount);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        aspect = blank(tag.getString("Aspect"));
        filter = blank(tag.getString("AspectFilter"));
        Direction[] values = Direction.values();
        int ordinal = tag.getInt("Facing");
        filterFacing = ordinal >= 0 && ordinal < values.length && values[ordinal].getAxis().isHorizontal()
                ? values[ordinal] : Direction.NORTH;
        amount = Mth.clamp(tag.getInt("Amount"), 0, CAPACITY);
        if (amount == 0) aspect = filter;
        if (filter != null && aspect != null && !filter.equals(aspect)) { amount = 0; aspect = filter; }
    }
    private static @Nullable String blank(String value) { return value == null || value.isBlank() ? null : value; }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { if (packet.getTag() != null) load(packet.getTag()); }
}
