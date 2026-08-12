package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.essentia.EssentiaAirHandler;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.mirror.LinkedMirrorBlockEntity;
import com.thaumcraftmodern.mirror.MirrorLink;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.MagicMirrorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** TC4 essentia mirror: an air-source bridge, never a pipe endpoint. */
public final class EssentiaMirrorBlockEntity extends LinkedMirrorBlockEntity
        implements EssentiaTransport {
    public static final int REMOTE_SOURCE_RANGE = 8;
    public static final int EFFECT_TICKS = 15;

    private @Nullable BlockPos effectSource;
    private int effectColor = 0xFFFFFF;
    private long effectUntil;

    public EssentiaMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_MIRROR.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel,
            BlockPos pos, BlockState state, EssentiaMirrorBlockEntity mirror) {
        if (rawLevel instanceof ServerLevel) mirror.linkTick();
    }

    /** Called only by the shared air-source scan, matching IAspectSource in TC4. */
    public boolean takeFromAir(String aspect) {
        if (!(level instanceof ServerLevel local) || !validReciprocalLink()) {
            return false;
        }
        MirrorLink destination = link();
        ServerLevel remoteLevel = destination == null ? null
                : destination.level(local.getServer());
        if (remoteLevel == null || !remoteLevel.hasChunkAt(destination.position())
                || !(remoteLevel.getBlockEntity(destination.position())
                        instanceof EssentiaMirrorBlockEntity remote)) {
            return false;
        }

        Direction remoteFacing = remote.getBlockState().getValue(
                MagicMirrorBlock.FACING
        );
        BlockPos source = EssentiaAirHandler.drain(
                remoteLevel,
                remote.getBlockPos(),
                aspect,
                remoteFacing,
                REMOTE_SOURCE_RANGE,
                true
        );
        if (source == null) {
            return false;
        }
        remote.setEffect(source, AspectRegistryRuntime.find(aspect)
                .map(AspectDefinition::color)
                .orElse(0xFFFFFF));
        return true;
    }

    private void setEffect(BlockPos source, int color) {
        effectSource = source.immutable();
        effectColor = color;
        effectUntil = level == null ? EFFECT_TICKS
                : level.getGameTime() + EFFECT_TICKS;
        sync();
    }

    public @Nullable BlockPos effectSource() {
        return effectSource;
    }

    public int effectColor() {
        return effectColor;
    }

    public long effectUntil() {
        return effectUntil;
    }

    /* Essentia mirrors are deliberately invisible to pipe connectivity. */
    @Override public boolean isConnectable(Direction side) { return false; }
    @Override public boolean canInputFrom(Direction side) { return false; }
    @Override public boolean canOutputTo(Direction side) { return false; }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return null; }
    @Override public int suctionAmount(Direction side) { return 0; }
    @Override public @Nullable String essentiaType(Direction side) { return null; }
    @Override public int essentiaAmount(Direction side) { return 0; }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) {
        return 0;
    }
    @Override public int addEssentia(String aspect, int amount, Direction side) {
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (effectSource != null) {
            tag.putLong("EssentiaEffectSource", effectSource.asLong());
        }
        tag.putInt("EssentiaEffectColor", effectColor);
        tag.putLong("EssentiaEffectUntil", effectUntil);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        effectSource = tag.contains("EssentiaEffectSource", Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong("EssentiaEffectSource")) : null;
        effectColor = tag.contains("EssentiaEffectColor", Tag.TAG_INT)
                ? tag.getInt("EssentiaEffectColor") : 0xFFFFFF;
        effectUntil = tag.getLong("EssentiaEffectUntil");
    }
}
