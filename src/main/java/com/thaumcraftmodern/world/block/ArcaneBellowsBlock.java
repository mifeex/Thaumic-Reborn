package com.thaumcraftmodern.world.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import com.thaumcraftmodern.world.block.entity.ArcaneBellowsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

/** Placement and redstone contract used by classic Arcane Bellows consumers. */
public final class ArcaneBellowsBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public ArcaneBellowsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new ArcaneBellowsBlockEntity(position, state);
    }

    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            net.minecraft.world.level.Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type,
                ModBlockEntities.ARCANE_BELLOWS.get(), ArcaneBellowsBlockEntity::clientTick)
                : null;
    }
    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,
            BlockGetter level, BlockPos position,
            net.minecraft.world.phys.shapes.CollisionContext context) {
        return box(1.6, 0, 1.6, 14.4, 16, 14.4);
    }

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING,
                context.getClickedFace().getOpposite());
    }

    @Override protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
