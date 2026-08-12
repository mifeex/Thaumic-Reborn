package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.item.EssentiaCrystalItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.EssentiaCrystallizerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** TC4 crystallizer: one essentia point, 200 progress, server-owned output. */
public final class EssentiaCrystallizerBlockEntity extends BlockEntity implements EssentiaTransport {
    public static final int PROGRESS_MAX = 200;
    private @Nullable String aspect;
    private int progress;
    private int count;
    private float spin;
    private float spinIncrement;

    public EssentiaCrystallizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_CRYSTALLIZER.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel, BlockPos pos,
            BlockState state, EssentiaCrystallizerBlockEntity machine) {
        if (!(rawLevel instanceof ServerLevel level)
                || EssentiaCrystallizerBlock.alignToConnectedTransport(
                        level, pos, state)
                || level.hasNeighborSignal(pos)) return;
        if (++machine.count % 5 == 0) {
            if (machine.aspect == null) { machine.fill(level); machine.progress = 0; }
            else machine.progress++;
        }
        if (machine.aspect != null && machine.progress >= PROGRESS_MAX) machine.eject(level);
    }

    public static void clientTick(net.minecraft.world.level.Level level, BlockPos pos,
            BlockState state, EssentiaCrystallizerBlockEntity machine) {
        boolean powered = level.hasNeighborSignal(pos);
        machine.spin = (machine.spin + machine.spinIncrement) % 360.0F;
        if (machine.aspect != null && !powered && machine.spinIncrement < 20.0F) {
            machine.spinIncrement = Math.min(20.0F, machine.spinIncrement + 0.1F);
        } else if ((machine.aspect == null || powered) && machine.spinIncrement > 0.0F) {
            machine.spinIncrement = Math.max(0.0F, machine.spinIncrement - 0.2F);
        }
    }

    private Direction inputSide() {
        return getBlockState().hasProperty(EssentiaCrystallizerBlock.FACING)
                ? getBlockState().getValue(EssentiaCrystallizerBlock.FACING) : Direction.DOWN;
    }

    private void fill(ServerLevel level) {
        Direction input = inputSide();
        EssentiaTransport remote = EssentiaConnections.neighbour(level, worldPosition, input).orElse(null);
        if (remote == null || !remote.canOutputTo(input.getOpposite())) return;
        String incoming = remote.essentiaType(input.getOpposite());
        if (incoming == null || remote.essentiaAmount(input.getOpposite()) <= 0
                || remote.suctionAmount(input.getOpposite()) >= suctionAmount(input)
                || suctionAmount(input) < remote.minimumSuction()) return;
        int taken = remote.takeEssentia(incoming, 1, input.getOpposite());
        if (taken > 0) addEssentia(incoming, taken, input);
    }

    private void eject(ServerLevel level) {
        ItemStack output = EssentiaCrystalItem.create(ModItems.ESSENTIA_CRYSTAL.get(), aspect);
        Direction direction = inputSide().getOpposite();
        BlockEntity target = level.getBlockEntity(worldPosition.relative(direction));
        if (target != null) {
            IItemHandler handler = target.getCapability(ForgeCapabilities.ITEM_HANDLER, inputSide()).orElse(null);
            if (handler != null) output = insert(handler, output, false);
        }
        if (!output.isEmpty()) {
            ItemEntity entity = new ItemEntity(level,
                    worldPosition.getX() + 0.5D + direction.getStepX() * 0.65D,
                    worldPosition.getY() + 0.5D + direction.getStepY() * 0.65D,
                    worldPosition.getZ() + 0.5D + direction.getStepZ() * 0.65D,
                    output);
            entity.setDeltaMovement(direction.getStepX() * 0.04D,
                    direction.getStepY() * 0.04D, direction.getStepZ() * 0.04D);
            level.addFreshEntity(entity);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 0.25F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        aspect = null; progress = 0; EssentiaSync.changed(this);
    }

    private static ItemStack insert(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }

    public @Nullable String aspect() { return aspect; }
    public int progress() { return progress; }
    public float spin(float partialTick) { return spin + spinIncrement * partialTick; }
    @Override public boolean isConnectable(Direction side) { return side == inputSide(); }
    @Override public boolean canInputFrom(Direction side) { return side == inputSide(); }
    @Override public boolean canOutputTo(Direction side) { return false; }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return null; }
    @Override public int suctionAmount(Direction side) {
        if (level != null && level.hasNeighborSignal(worldPosition)) return 0;
        return side == inputSide() && aspect == null ? 128 : 64;
    }
    @Override public @Nullable String essentiaType(Direction side) { return aspect; }
    @Override public int essentiaAmount(Direction side) { return aspect == null ? 0 : 1; }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) { return 0; }
    @Override public int addEssentia(String incoming, int amount, Direction side) {
        if (!canInputFrom(side) || this.aspect != null || amount <= 0) return 0;
        this.aspect = incoming; progress = 0; EssentiaSync.changed(this); return 1;
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); if (aspect != null) tag.putString("Aspect", aspect); tag.putInt("Progress", progress);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); String saved = tag.getString("Aspect"); aspect = saved.isBlank() ? null : saved;
        progress = Math.max(0, Math.min(PROGRESS_MAX, tag.getInt("Progress")));
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { if (packet.getTag() != null) load(packet.getTag()); }
}
