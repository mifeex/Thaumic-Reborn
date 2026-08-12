package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.ArcaneLampBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** TC4 Lamp of Growth: Herba buffer and shuffled spherical crop ticking. */
public final class GrowthLampBlockEntity extends BlockEntity implements EssentiaTransport {
    public static final String ASPECT = "herba";
    private static final int RANGE = 6;
    private final List<BlockPos> checklist = new ArrayList<>();
    private boolean reserve;
    private int charges = -1;
    private int drawDelay;

    public GrowthLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GROWTH_LAMP.get(), pos, state);
    }

    public static void serverTick(Level rawLevel, BlockPos pos, BlockState state,
            GrowthLampBlockEntity lamp) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        boolean wasLit = lamp.charges > 0;
        if (lamp.charges <= 0) {
            if (lamp.reserve) { lamp.charges = 100; lamp.reserve = false; }
            else if (lamp.drawEssentia(level)) lamp.charges = 100;
        }
        if (!lamp.reserve && lamp.drawEssentia(level)) lamp.reserve = true;
        if (lamp.charges == 0) lamp.charges = -1;
        if (lamp.charges > 0) lamp.updatePlant(level);
        lamp.syncIfNeeded(wasLit);
    }

    private void updatePlant(ServerLevel level) {
        if (checklist.isEmpty()) {
            for (int x = -RANGE; x <= RANGE; x++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    checklist.add(worldPosition.offset(x, RANGE, z));
                }
            }
            Collections.shuffle(checklist, new java.util.Random(level.random.nextLong()));
        }
        BlockPos column = checklist.remove(0);
        for (int y = column.getY(); y >= worldPosition.getY() - RANGE; y--) {
            BlockPos target = new BlockPos(column.getX(), y, column.getZ());
            if (worldPosition.distToCenterSqr(
                    target.getX() + 0.5D, target.getY() + 0.5D,
                    target.getZ() + 0.5D) >= RANGE * RANGE) continue;
            BlockState targetState = level.getBlockState(target);
            if (!(targetState.getBlock() instanceof BonemealableBlock growable)
                    || !growable.isValidBonemealTarget(level, target, targetState,
                    level.isClientSide)) continue;
            charges--;
            targetState.randomTick(level, target, level.random);
            setChanged();
            return;
        }
    }

    private boolean drawEssentia(ServerLevel level) {
        if (++drawDelay % 5 != 0) return false;
        Direction input = inputSide();
        EssentiaTransport remote = EssentiaConnections.neighbour(
                level, worldPosition, input).orElse(null);
        return remote != null && remote.canOutputTo(input.getOpposite())
                && remote.suctionAmount(input.getOpposite()) < suctionAmount(input)
                && remote.takeEssentia(ASPECT, 1, input.getOpposite()) == 1;
    }

    private Direction inputSide() {
        BlockState state = getBlockState();
        return state.hasProperty(ArcaneLampBlock.FACING)
                ? state.getValue(ArcaneLampBlock.FACING) : Direction.DOWN;
    }

    private void syncIfNeeded(boolean wasLit) {
        boolean lit = charges > 0;
        setChanged();
        if (level != null && wasLit != lit) {
            level.setBlock(worldPosition, getBlockState().setValue(
                    ArcaneLampBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    public int charges() { return charges; }
    public boolean hasReserve() { return reserve; }
    @Override public boolean isConnectable(Direction side) { return side == inputSide(); }
    @Override public boolean canInputFrom(Direction side) { return side == inputSide(); }
    @Override public boolean canOutputTo(Direction side) { return false; }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return ASPECT; }
    @Override public int suctionAmount(Direction side) {
        return side == inputSide() && (!reserve || charges <= 0) ? 128 : 0;
    }
    @Override public @Nullable String essentiaType(Direction side) { return null; }
    @Override public int essentiaAmount(Direction side) { return 0; }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) { return 0; }
    @Override public int addEssentia(String aspect, int amount, Direction side) { return 0; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.putBoolean("Reserve", reserve); tag.putInt("Charges", charges);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); reserve = tag.getBoolean("Reserve"); charges = tag.getInt("Charges");
    }
}
