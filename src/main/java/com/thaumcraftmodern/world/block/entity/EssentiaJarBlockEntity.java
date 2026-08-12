package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.JarRedstoneSignal;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumicreborn.api.essentia.EssentiaTransport;
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

public final class EssentiaJarBlockEntity extends BlockEntity
        implements EssentiaTransport {
    public static final int CAPACITY = 64;
    public static final int SUCTION = 32;
    public static final int FILTERED_SUCTION = 64;

    private @Nullable String aspect;
    private @Nullable String filter;
    private Direction filterFacing = Direction.NORTH;
    private int amount;
    private int count;

    public EssentiaJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_JAR.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel, BlockPos pos,
            BlockState state, EssentiaJarBlockEntity jar) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        if (++jar.count % 5 == 0 && jar.amount < CAPACITY) {
            jar.fillFromAbove(level);
        }
    }

    private void fillFromAbove(ServerLevel level) {
        EssentiaTransport remote = EssentiaConnections.neighbour(
                level, worldPosition, Direction.UP).orElse(null);
        if (remote == null || !remote.canOutputTo(Direction.DOWN)) {
            return;
        }
        String wanted = filter != null ? filter : amount > 0 ? aspect : null;
        if (wanted == null
                && remote.essentiaAmount(Direction.DOWN) > 0
                && remote.suctionAmount(Direction.DOWN) < suctionAmount(Direction.UP)
                && suctionAmount(Direction.UP) >= remote.minimumSuction()) {
            wanted = remote.essentiaType(Direction.DOWN);
        }
        if (wanted != null
                && remote.suctionAmount(Direction.DOWN) < suctionAmount(Direction.UP)
                && suctionAmount(Direction.UP) >= remote.minimumSuction()) {
            int taken = remote.takeEssentia(wanted, 1, Direction.DOWN);
            if (taken > 0) addEssentia(wanted, taken, Direction.UP);
        }
    }

    public @Nullable String aspect() {
        return aspect;
    }

    public @Nullable String filter() {
        return filter;
    }

    public int amount() {
        return amount;
    }

    public Direction filterFacing() {
        return filterFacing;
    }

    public void setFilter(@Nullable String filter) {
        setFilter(filter, filterFacing);
    }

    public void setFilter(@Nullable String filter, Direction facing) {
        this.filter = filter == null || filter.isBlank() ? null : filter;
        if (facing.getAxis().isHorizontal()) this.filterFacing = facing;
        if (this.filter != null && amount == 0) aspect = this.filter;
        EssentiaSync.changed(this);
    }

    public void emptyContents() {
        int previousSignal = comparatorSignal();
        amount = 0;
        aspect = filter;
        EssentiaSync.changed(this);
        notifyRedstoneChange(previousSignal);
    }

    public int comparatorSignal() {
        return JarRedstoneSignal.forAmount(amount);
    }

    @Override
    public boolean isConnectable(Direction side) {
        return side == Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return side == Direction.UP;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return side == Direction.UP;
    }

    @Override
    public void setSuction(@Nullable String aspect, int amount) {
    }

    @Override
    public @Nullable String suctionType(Direction side) {
        return filter != null ? filter : aspect;
    }

    @Override
    public int suctionAmount(Direction side) {
        if (amount >= CAPACITY) return 0;
        return filter != null ? FILTERED_SUCTION : SUCTION;
    }

    @Override
    public @Nullable String essentiaType(Direction side) {
        return aspect;
    }

    @Override
    public int essentiaAmount(Direction side) {
        return amount;
    }

    @Override
    public int minimumSuction() {
        return filter != null ? FILTERED_SUCTION : SUCTION;
    }

    @Override
    public int takeEssentia(String aspect, int requested, Direction side) {
        if (!canOutputTo(side) || requested <= 0 || !Objects.equals(this.aspect, aspect)
                || amount < requested) return 0;
        int previousSignal = comparatorSignal();
        amount -= requested;
        if (amount == 0) this.aspect = filter;
        EssentiaSync.changed(this);
        notifyRedstoneChange(previousSignal);
        return requested;
    }

    @Override
    public int addEssentia(String aspect, int requested, Direction side) {
        if (!canInputFrom(side) || !acceptsAspect(aspect, requested)) return 0;
        int accepted = Math.min(requested, CAPACITY - amount);
        if (accepted <= 0) return 0;
        int previousSignal = comparatorSignal();
        this.aspect = aspect;
        amount += accepted;
        EssentiaSync.changed(this);
        notifyRedstoneChange(previousSignal);
        return accepted;
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

    public boolean acceptsAspect(@Nullable String incoming, int requested) {
        return incoming != null && requested > 0
                && AspectRegistryRuntime.find(incoming).isPresent()
                && amount <= CAPACITY - requested
                && (filter == null || filter.equals(incoming))
                && (amount == 0 || Objects.equals(aspect, incoming));
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }

    public CompoundTag saveForItem() {
        CompoundTag tag = new CompoundTag();
        writePayload(tag);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writePayload(tag);
    }

    private void writePayload(CompoundTag tag) {
        if (aspect != null) tag.putString("Aspect", aspect);
        if (filter != null) tag.putString("AspectFilter", filter);
        tag.putInt("Facing", filterFacing.ordinal());
        tag.putInt("Amount", amount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspect = blankToNull(tag.getString("Aspect"));
        filter = blankToNull(tag.getString("AspectFilter"));
        int facing = tag.getInt("Facing");
        Direction[] directions = Direction.values();
        filterFacing = facing >= 0 && facing < directions.length
                && directions[facing].getAxis().isHorizontal()
                ? directions[facing] : Direction.NORTH;
        amount = Mth.clamp(tag.getInt("Amount"), 0, CAPACITY);
        if (amount == 0 && filter != null) aspect = filter;
        if (filter != null && aspect != null && !filter.equals(aspect)) {
            amount = 0;
            aspect = filter;
        }
    }

    private static @Nullable String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection,
            ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }
}
