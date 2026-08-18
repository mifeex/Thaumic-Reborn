package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Original base nozzle and 128-suction Perditio transport contract. */
public final class ArcaneBoreBaseBlockEntity extends BlockEntity
        implements EssentiaTransport {
    public static final String ASPECT = "perditio";
    private Direction output = Direction.NORTH;

    public ArcaneBoreBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_BORE_BASE.get(), pos, state);
    }

    public static void serverTick(Level rawLevel, BlockPos pos, BlockState state,
            ArcaneBoreBaseBlockEntity base) {
        // Pulling is initiated by the attached bore, exactly like TileArcaneBoreBase.
    }

    public Direction output() { return output; }

    public void setOutput(Direction output) {
        this.output = output == null ? Direction.NORTH : output;
        sync();
    }

    public boolean drawPerditio(ServerLevel level) {
        for (Direction side : Direction.values()) {
            var remote = EssentiaConnections.neighbour(level, worldPosition, side).orElse(null);
            Direction remoteSide = side.getOpposite();
            if (remote == null) continue;
            // TC4 returns immediately on a connected transport that cannot output.
            if (!remote.canOutputTo(remoteSide)) return false;
            if (remote.suctionAmount(remoteSide) < suctionAmount(side)
                    && remote.takeEssentia(ASPECT, 1, remoteSide) == 1) return true;
        }
        return false;
    }

    @Override public boolean isConnectable(Direction side) { return true; }
    @Override public boolean canInputFrom(Direction side) { return true; }
    @Override public boolean canOutputTo(Direction side) { return false; }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return ASPECT; }
    @Override public int suctionAmount(Direction side) { return side != output ? 128 : 0; }
    @Override public @Nullable String essentiaType(Direction side) { return null; }
    @Override public int essentiaAmount(Direction side) { return 0; }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) { return 0; }
    @Override public int addEssentia(String aspect, int amount, Direction side) { return 0; }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("orientation", output.get3DDataValue());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        output = Direction.from3DDataValue(tag.getInt("orientation"));
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection,
            ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
