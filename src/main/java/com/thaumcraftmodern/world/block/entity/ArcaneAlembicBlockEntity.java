package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.essentia.AlembicStorage;
import com.thaumcraftmodern.essentia.ArcaneAlembicFacingRules;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.essentia.EssentiaTransportRegistry;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.ArcaneAlembicBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ArcaneAlembicBlockEntity extends BlockEntity
        implements EssentiaTransport, AlembicStorage {
    public static final int CAPACITY = 32;

    private @Nullable String aspect;
    private @Nullable String filter;
    private int amount;

    public ArcaneAlembicBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_ALEMBIC.get(), pos, state);
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(ArcaneAlembicBlock.FACING)
                ? state.getValue(ArcaneAlembicBlock.FACING) : Direction.NORTH;
    }

    @Override
    public @Nullable String storedAspect() {
        return aspect;
    }

    @Override
    public @Nullable String filterAspect() {
        return filter;
    }

    @Override
    public int storedAmount() {
        return amount;
    }

    @Override
    public int capacity() {
        return CAPACITY;
    }

    public void setFilter(@Nullable String filter) {
        this.filter = filter == null || filter.isBlank() ? null : filter;
        EssentiaSync.changed(this);
    }

    public void emptyContents() {
        amount = 0;
        aspect = null;
        EssentiaSync.changed(this);
    }

    public int comparatorSignal() {
        return amount <= 0
                ? 0
                : Mth.floor(amount / (float) CAPACITY * 14.0F) + 1;
    }

    /** Direct container extraction used by classic phials, independent of pipe faces. */
    public boolean takeFromContainer(String aspect, int requested) {
        if (requested <= 0 || amount < requested
                || !Objects.equals(this.aspect, aspect)) return false;
        amount -= requested;
        if (amount == 0) this.aspect = null;
        EssentiaSync.changed(this);
        return true;
    }

    @Override
    public int acceptFromFurnace(String aspect, int requested) {
        if (requested <= 0 || filter != null && !filter.equals(aspect)
                || amount > 0 && !Objects.equals(this.aspect, aspect)) return 0;
        int accepted = Math.min(requested, CAPACITY - amount);
        if (accepted <= 0) return 0;
        this.aspect = aspect;
        amount += accepted;
        EssentiaSync.changed(this);
        return accepted;
    }

    @Override
    public boolean isConnectable(Direction side) {
        return ArcaneAlembicFacingRules.isPipeConnectable(facing(), side);
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return isConnectable(side);
    }

    @Override
    public void setSuction(@Nullable String aspect, int amount) {
    }

    @Override
    public @Nullable String suctionType(Direction side) {
        return null;
    }

    @Override
    public int suctionAmount(Direction side) {
        return 0;
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
        return 0;
    }

    @Override
    public int takeEssentia(String aspect, int requested, Direction side) {
        return canOutputTo(side) && takeFromContainer(aspect, requested)
                ? requested : 0;
    }

    @Override
    public int addEssentia(String aspect, int amount, Direction side) {
        return 0;
    }

    @Override
    public boolean canReturnEssentia() {
        return EssentiaTransportRegistry.canReturnEssentia(
                BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()));
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (aspect != null) tag.putString("Aspect", aspect);
        if (filter != null) tag.putString("AspectFilter", filter);
        tag.putInt("Amount", amount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspect = blankToNull(tag.getString("Aspect"));
        filter = blankToNull(tag.getString("AspectFilter"));
        amount = Math.min(CAPACITY, Math.max(0, tag.getInt("Amount")));
        if (amount == 0) aspect = null;
        if (filter != null && aspect != null && !filter.equals(aspect)) {
            amount = 0;
            aspect = null;
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
