package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.crucible.EssentiaStore;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.EssentiaReservoirBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

/** TC4's mixed-aspect, single-port 256-point essentia reservoir. */
public final class EssentiaReservoirBlockEntity extends BlockEntity
        implements EssentiaTransport {
    public static final int CAPACITY = 256;
    public static final int SUCTION = 24;

    private final EssentiaStore contents = new EssentiaStore();
    private int ticks;
    private @Nullable String displayAspect;
    private float red = 1.0F;
    private float green = 1.0F;
    private float blue = 1.0F;
    private float redStep;
    private float greenStep;
    private float blueStep;

    public EssentiaReservoirBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_RESERVOIR.get(), pos, state);
    }

    public static void serverTick(Level rawLevel, BlockPos pos, BlockState state,
            EssentiaReservoirBlockEntity reservoir) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        if (++reservoir.ticks % 5 == 0 && reservoir.totalAmount() < CAPACITY) {
            reservoir.fill(level);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state,
            EssentiaReservoirBlockEntity reservoir) {
        int amount = reservoir.totalAmount();
        reservoir.ticks++;
        if (amount <= 0) {
            reservoir.displayAspect = null;
            reservoir.red = reservoir.green = reservoir.blue = 1.0F;
            return;
        }
        if (level.random.nextInt(500 - amount) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, ModSounds.CREAK.get(),
                    SoundSource.BLOCKS, 1.0F,
                    1.4F + level.random.nextFloat() * 0.2F, false);
        }
        if (reservoir.ticks % 20 == 0 && !reservoir.contents.isEmpty()) {
            ArrayList<String> aspects = new ArrayList<>(reservoir.contents.view().keySet());
            reservoir.displayAspect = aspects.get(
                    reservoir.ticks / 20 % aspects.size());
            int color = AspectRegistryRuntime.find(reservoir.displayAspect)
                    .map(AspectDefinition::color).orElse(0xFFFFFF);
            float targetRed = ((color >> 16) & 255) / 255.0F;
            float targetGreen = ((color >> 8) & 255) / 255.0F;
            float targetBlue = (color & 255) / 255.0F;
            reservoir.redStep = (reservoir.red - targetRed) / 20.0F;
            reservoir.greenStep = (reservoir.green - targetGreen) / 20.0F;
            reservoir.blueStep = (reservoir.blue - targetBlue) / 20.0F;
        }
        if (reservoir.displayAspect != null) {
            reservoir.red -= reservoir.redStep;
            reservoir.green -= reservoir.greenStep;
            reservoir.blue -= reservoir.blueStep;
        }
    }

    private void fill(ServerLevel level) {
        Direction side = facing();
        EssentiaTransport remote = EssentiaConnections.neighbour(
                level, worldPosition, side).orElse(null);
        if (remote == null) return;
        Direction remoteSide = side.getOpposite();
        if (!remote.canOutputTo(remoteSide)
                || remote.essentiaAmount(remoteSide) <= 0
                || remote.suctionAmount(remoteSide) >= suctionAmount(side)
                || suctionAmount(side) < remote.minimumSuction()) return;
        String aspect = remote.essentiaType(remoteSide);
        if (aspect == null) aspect = remote.essentiaType(null);
        if (aspect == null) return;
        int taken = remote.takeEssentia(aspect, 1, remoteSide);
        if (taken > 0) addEssentia(aspect, taken, side);
    }

    public Direction facing() {
        return getBlockState().hasProperty(EssentiaReservoirBlock.FACING)
                ? getBlockState().getValue(EssentiaReservoirBlock.FACING)
                : Direction.DOWN;
    }

    public int totalAmount() { return contents.total(); }
    public Map<String, Integer> contents() { return contents.view(); }
    public @Nullable String displayAspect() { return displayAspect; }
    public float red() { return red; }
    public float green() { return green; }
    public float blue() { return blue; }

    @Override public boolean isConnectable(Direction side) { return side == facing(); }
    @Override public boolean canInputFrom(Direction side) { return side == facing(); }
    @Override public boolean canOutputTo(Direction side) { return side == facing(); }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return null; }
    @Override public int suctionAmount(Direction side) {
        return totalAmount() < CAPACITY ? SUCTION : 0;
    }
    @Override public @Nullable String essentiaType(Direction side) {
        return side == null && !contents.isEmpty()
                ? contents.view().keySet().iterator().next() : null;
    }
    @Override public int essentiaAmount(Direction side) { return totalAmount(); }
    @Override public int minimumSuction() { return SUCTION; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) {
        if (!canOutputTo(side) || aspect == null || amount <= 0) return 0;
        int taken = Math.min(amount, contents.amount(aspect));
        if (taken <= 0 || !contents.remove(aspect, taken)) return 0;
        EssentiaSync.changed(this);
        return taken;
    }
    @Override public int addEssentia(String aspect, int amount, Direction side) {
        if (!canInputFrom(side) || aspect == null || aspect.isBlank() || amount <= 0) return 0;
        int accepted = Math.min(amount, CAPACITY - totalAmount());
        if (accepted <= 0) return 0;
        contents.add(aspect, accepted);
        EssentiaSync.changed(this);
        return accepted;
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Aspects", contents.save());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        contents.load(tag.getCompound("Aspects"));
        int overflow = Math.max(0, contents.total() - CAPACITY);
        if (overflow > 0) {
            for (String aspect : new ArrayList<>(contents.view().keySet())) {
                int remove = Math.min(overflow, contents.amount(aspect));
                contents.remove(aspect, remove);
                overflow -= remove;
                if (overflow == 0) break;
            }
        }
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
