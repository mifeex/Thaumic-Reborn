package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.crucible.EssentiaStore;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.EssentiaTubeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/** Custom eight-points-per-aspect essentia buffer with independent side/choke state. */
public final class EssentiaBufferBlockEntity extends BlockEntity implements EssentiaTransport {
    public static final int CAPACITY_PER_ASPECT = 8;
    private final EssentiaStore contents = new EssentiaStore();
    private final boolean[] openSides = {true, true, true, true, true, true};
    private final byte[] choke = new byte[6];
    private int count;

    public EssentiaBufferBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_BUFFER.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel, BlockPos pos,
            BlockState state, EssentiaBufferBlockEntity buffer) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        if (++buffer.count % 5 == 0) {
            buffer.fill(level);
        }
    }

    private void fill(ServerLevel level) {
        for (Direction side : Direction.values()) {
            EssentiaTransport remote = EssentiaConnections.neighbour(level, worldPosition, side).orElse(null);
            if (remote == null) continue;
            Direction remoteSide = side.getOpposite();
            if (remote.essentiaAmount(remoteSide) <= 0
                    || remote.suctionAmount(remoteSide) >= suctionAmount(side)
                    || suctionAmount(side) < remote.minimumSuction()) continue;
            String aspect = remote.essentiaType(remoteSide);
            if (aspect == null) aspect = remote.essentiaType(null);
            if (aspect == null) continue;
            if (contents.amount(aspect) >= CAPACITY_PER_ASPECT) continue;
            int taken = remote.takeEssentia(aspect, 1, remoteSide);
            if (taken > 0) {
                addEssentia(aspect, taken, side);
                return;
            }
        }
    }

    public void toggleSide(Direction side) {
        openSides[side.ordinal()] = !openSides[side.ordinal()];
        if (level != null && level.getBlockEntity(worldPosition.relative(side))
                instanceof EssentiaBufferBlockEntity other) {
            other.openSides[side.getOpposite().ordinal()] = openSides[side.ordinal()];
            EssentiaSync.changed(other);
        }
        EssentiaSync.changed(this);
        refreshAdjacentTube(side);
    }

    private void refreshAdjacentTube(Direction side) {
        if (level != null && !level.isClientSide) {
            EssentiaTubeBlock.refreshConnections(
                    level, worldPosition.relative(side));
        }
    }

    public void cycleChoke(Direction side) {
        int index = side.ordinal();
        choke[index] = (byte) ((choke[index] + 1) % 3);
        EssentiaSync.changed(this);
    }

    public boolean sideOpen(Direction side) { return openSides[side.ordinal()]; }
    public int chokeMode(Direction side) { return choke[side.ordinal()]; }
    public int totalAmount() { return contents.total(); }
    public java.util.Map<String, Integer> contents() { return contents.view(); }

    /** Hook deliberately isolated for the later Arcane Bellows vertical. */
    private int attachedBellows() { return 0; }

    @Override public boolean isConnectable(Direction side) { return side != null && sideOpen(side); }
    @Override public boolean canInputFrom(Direction side) { return isConnectable(side); }
    @Override public boolean canOutputTo(Direction side) { return isConnectable(side); }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return null; }
    @Override public int suctionAmount(Direction side) {
        int mode = choke[side == null ? 0 : side.ordinal()];
        if (mode == 2) return 0;
        int bellows = attachedBellows();
        return bellows <= 0 || mode == 1 ? 1 : bellows * 32;
    }
    @Override public @Nullable String essentiaType(Direction side) {
        if (contents.isEmpty()) return null;
        ArrayList<String> aspects = new ArrayList<>(contents.view().keySet());
        if (level == null) return aspects.get(0);
        return aspects.get(level.random.nextInt(aspects.size()));
    }
    @Override public int essentiaAmount(Direction side) { return contents.total(); }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) {
        if (!canOutputTo(side) || amount <= 0) return 0;
        int taken = Math.min(amount, contents.amount(aspect));
        if (taken <= 0 || !contents.remove(aspect, taken)) return 0;
        EssentiaSync.changed(this);
        return taken;
    }
    @Override public int addEssentia(String aspect, int amount, Direction side) {
        if (!canInputFrom(side) || aspect == null || aspect.isBlank() || amount != 1
                || contents.amount(aspect) >= CAPACITY_PER_ASPECT) return 0;
        contents.add(aspect, 1);
        EssentiaSync.changed(this);
        return 1;
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Aspects", contents.save());
        byte[] open = new byte[6];
        for (int i = 0; i < 6; i++) open[i] = (byte) (openSides[i] ? 1 : 0);
        tag.putByteArray("open", open);
        tag.putByteArray("choke", choke);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        contents.load(tag.getCompound("Aspects"));
        for (String aspect : new ArrayList<>(contents.view().keySet())) {
            int overflow = contents.amount(aspect) - CAPACITY_PER_ASPECT;
            if (overflow > 0) contents.remove(aspect, overflow);
        }
        byte[] open = tag.getByteArray("open");
        if (open.length == 6) for (int i = 0; i < 6; i++) openSides[i] = open[i] != 0;
        byte[] modes = tag.getByteArray("choke");
        if (modes.length == 6) for (int i = 0; i < 6; i++) choke[i] = (byte) Math.max(0, Math.min(2, modes[i]));
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
