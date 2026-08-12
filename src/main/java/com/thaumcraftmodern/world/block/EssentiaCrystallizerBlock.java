package com.thaumcraftmodern.world.block;

import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.EssentiaCrystallizerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public final class EssentiaCrystallizerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public EssentiaCrystallizerBlock(Properties properties) {
        super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { builder.add(FACING); }
    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction fallback = context.getClickedFace().getOpposite();
        Direction connected = findConnectedInput(context.getLevel(),
                context.getClickedPos(), fallback);
        return defaultBlockState().setValue(FACING,
                connected == null ? fallback : connected);
    }
    @Override public void neighborChanged(BlockState state, Level level,
            BlockPos pos, Block neighbour, BlockPos neighbourPos, boolean moving) {
        super.neighborChanged(state, level, pos, neighbour, neighbourPos, moving);
        if (!level.isClientSide) alignToConnectedTransport(level, pos, state);
    }
    public static boolean alignToConnectedTransport(Level level, BlockPos pos,
            BlockState state) {
        if (connectedOutput(level, pos, state.getValue(FACING))) return false;
        Direction connected = findConnectedInput(level, pos, null);
        if (connected != null && connected != state.getValue(FACING)) {
            level.setBlock(pos, state.setValue(FACING, connected), UPDATE_ALL);
            return true;
        }
        return false;
    }
    private static @Nullable Direction findConnectedInput(Level level,
            BlockPos pos, @Nullable Direction preferred) {
        if (preferred != null && connectedOutput(level, pos, preferred)) {
            return preferred;
        }
        for (Direction side : Direction.values()) {
            if (side != preferred && connectedOutput(level, pos, side)) return side;
        }
        return null;
    }
    private static boolean connectedOutput(Level level, BlockPos pos,
            Direction side) {
        return level.getBlockEntity(pos.relative(side)) instanceof EssentiaTransport remote
                && remote.isConnectable(side.getOpposite())
                && remote.canOutputTo(side.getOpposite());
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EssentiaCrystallizerBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type, ModBlockEntities.ESSENTIA_CRYSTALLIZER.get(), EssentiaCrystallizerBlockEntity::serverTick)
                : createTickerHelper(type, ModBlockEntities.ESSENTIA_CRYSTALLIZER.get(), EssentiaCrystallizerBlockEntity::clientTick);
    }
}
