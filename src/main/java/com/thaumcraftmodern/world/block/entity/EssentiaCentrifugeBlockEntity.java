package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/** One-point, 39-tick TC4 compound-aspect centrifuge. */
public final class EssentiaCentrifugeBlockEntity extends BlockEntity implements EssentiaTransport {
    public static final int PROCESS_TICKS = 39;
    private @Nullable String input;
    private @Nullable String output;
    private int process;
    private int count;
    private float rotation;
    private float rotationSpeed;

    public EssentiaCentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_CENTRIFUGE.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel, BlockPos pos,
            BlockState state, EssentiaCentrifugeBlockEntity centrifuge) {
        if (!(rawLevel instanceof ServerLevel level) || level.hasNeighborSignal(pos)) return;
        if (centrifuge.output == null && centrifuge.input == null && ++centrifuge.count % 5 == 0) centrifuge.draw(level);
        if (centrifuge.process > 0) centrifuge.process--;
        if (centrifuge.output == null && centrifuge.input != null && centrifuge.process == 0) centrifuge.process(level);
    }

    public static void clientTick(net.minecraft.world.level.Level level, BlockPos pos,
            BlockState state, EssentiaCentrifugeBlockEntity centrifuge) {
        boolean powered = level.hasNeighborSignal(pos);
        if (centrifuge.input != null && !powered && centrifuge.rotationSpeed < 20.0F) {
            centrifuge.rotationSpeed = Math.min(20.0F, centrifuge.rotationSpeed + 2.0F);
        } else if ((centrifuge.input == null || powered) && centrifuge.rotationSpeed > 0.0F) {
            centrifuge.rotationSpeed = Math.max(0.0F, centrifuge.rotationSpeed - 0.5F);
        }
        int previous = (int) centrifuge.rotation;
        centrifuge.rotation = (centrifuge.rotation + centrifuge.rotationSpeed) % 360.0F;
        if (centrifuge.rotation % 180.0F <= 20.0F && previous % 180 >= 160
                && centrifuge.rotationSpeed > 0.0F) {
            level.playLocalSound(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
                    com.thaumcraftmodern.registry.ModSounds.PUMP.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    private void draw(ServerLevel level) {
        EssentiaTransport remote = EssentiaConnections.neighbour(level, worldPosition, Direction.DOWN).orElse(null);
        if (remote == null || !remote.canOutputTo(Direction.UP)) return;
        String aspect = remote.essentiaType(Direction.UP);
        if (!isCompound(aspect) || remote.essentiaAmount(Direction.UP) <= 0
                || remote.suctionAmount(Direction.UP) >= suctionAmount(Direction.DOWN)
                || suctionAmount(Direction.DOWN) < remote.minimumSuction()) return;
        if (remote.takeEssentia(aspect, 1, Direction.UP) == 1) {
            input = aspect;
            process = PROCESS_TICKS;
            EssentiaSync.changed(this);
        }
    }

    private void process(ServerLevel level) {
        List<String> components = AspectRegistryRuntime.find(input).map(d -> d.components()).orElse(List.of());
        output = components.size() == 2 ? components.get(level.random.nextInt(2)) : null;
        input = null;
        EssentiaSync.changed(this);
    }

    private static boolean isCompound(@Nullable String aspect) {
        return aspect != null && AspectRegistryRuntime.find(aspect).map(d -> d.isCompound()).orElse(false);
    }

    public @Nullable String inputAspect() { return input; }
    public @Nullable String outputAspect() { return output; }
    public int processTicks() { return process; }
    public float rotation(float partialTick) { return rotation + rotationSpeed * partialTick; }

    @Override public boolean isConnectable(Direction side) { return side == Direction.UP || side == Direction.DOWN; }
    @Override public boolean canInputFrom(Direction side) { return side == Direction.DOWN; }
    @Override public boolean canOutputTo(Direction side) { return side == Direction.UP; }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return null; }
    @Override public int suctionAmount(Direction side) {
        if (side != Direction.DOWN || level != null && level.hasNeighborSignal(worldPosition)) return 0;
        return input == null ? 128 : 64;
    }
    @Override public @Nullable String essentiaType(Direction side) { return output; }
    @Override public int essentiaAmount(Direction side) { return output == null ? 0 : 1; }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) {
        if (!canOutputTo(side) || amount != 1 || !Objects.equals(output, aspect)) return 0;
        output = null; EssentiaSync.changed(this); return 1;
    }
    @Override public int addEssentia(String aspect, int amount, Direction side) {
        if (!canInputFrom(side) || input != null || amount <= 0 || !isCompound(aspect)) return 0;
        input = aspect; process = PROCESS_TICKS; EssentiaSync.changed(this); return 1;
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (input != null) tag.putString("aspectIn", input);
        if (output != null) tag.putString("aspectOut", output);
        tag.putInt("Process", process);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        input = blank(tag.getString("aspectIn")); output = blank(tag.getString("aspectOut"));
        process = Math.max(0, Math.min(PROCESS_TICKS, tag.getInt("Process")));
    }
    private static @Nullable String blank(String value) { return value == null || value.isBlank() ? null : value; }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { if (packet.getTag() != null) load(packet.getTag()); }
}
