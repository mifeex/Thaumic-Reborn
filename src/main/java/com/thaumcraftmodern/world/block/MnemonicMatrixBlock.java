package com.thaumcraftmodern.world.block;

import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.MnemonicMatrixBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** TC4 metadata 12 brainbox used as a Thaumatorium mnemonic matrix. */
public final class MnemonicMatrixBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape CLASSIC_SHAPE = box(3, 3, 3, 13, 13, 13);
    private static final int ALCHEMY_INPUT_SEARCH_RADIUS = 2;

    public MnemonicMatrixBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = adjacentConnector(context.getLevel(), context.getClickedPos());
        if (facing == null) facing = context.getNearestLookingDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override public BlockState updateShape(BlockState state, Direction changedSide,
            BlockState changedState, LevelAccessor level, BlockPos pos, BlockPos changedPos) {
        Direction facing = adjacentConnector(level, pos);
        return facing == null || facing == state.getValue(FACING)
                ? state : state.setValue(FACING, facing);
    }

    private static @Nullable Direction adjacentConnector(BlockGetter level, BlockPos pos) {
        Direction alchemyInput = nearestAlchemyInput(level, pos);
        if (alchemyInput != null) return alchemyInput;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(pos.relative(direction))
                    instanceof EssentiaTransport transport
                    && transport.isConnectable(direction.getOpposite())) {
                return direction;
            }
        }
        return null;
    }

    private static @Nullable Direction nearestAlchemyInput(
            BlockGetter level,
            BlockPos matrixPos
    ) {
        double matrixX = matrixPos.getX() + 0.5D;
        double matrixY = matrixPos.getY() + 0.5D;
        double matrixZ = matrixPos.getZ() + 0.5D;
        double nearestDistance = Double.MAX_VALUE;
        Direction nearestDirection = null;
        BlockPos.MutableBlockPos candidate = new BlockPos.MutableBlockPos();
        for (int x = -ALCHEMY_INPUT_SEARCH_RADIUS;
             x <= ALCHEMY_INPUT_SEARCH_RADIUS; x++) {
            for (int y = -ALCHEMY_INPUT_SEARCH_RADIUS;
                 y <= ALCHEMY_INPUT_SEARCH_RADIUS; y++) {
                for (int z = -ALCHEMY_INPUT_SEARCH_RADIUS;
                     z <= ALCHEMY_INPUT_SEARCH_RADIUS; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    candidate.setWithOffset(matrixPos, x, y, z);
                    BlockState state = level.getBlockState(candidate);
                    if (!isAlchemyInputOwner(state)) continue;
                    for (Direction inputFace : Direction.values()) {
                        double inputX = candidate.getX() + 0.5D
                                + inputFace.getStepX() * 0.5D;
                        double inputY = candidate.getY() + 0.5D
                                + inputFace.getStepY() * 0.5D;
                        double inputZ = candidate.getZ() + 0.5D
                                + inputFace.getStepZ() * 0.5D;
                        double dx = inputX - matrixX;
                        double dy = inputY - matrixY;
                        double dz = inputZ - matrixZ;
                        double distance = dx * dx + dy * dy + dz * dz;
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestDirection = Direction.getNearest(
                                    (float) dx,
                                    (float) dy,
                                    (float) dz
                            );
                        }
                    }
                }
            }
        }
        return nearestDirection;
    }

    private static boolean isAlchemyInputOwner(BlockState state) {
        return state.is(ModBlocks.ALCHEMICAL_CONSTRUCT.get())
                || state.is(ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get())
                || state.is(ModBlocks.THAUMATORIUM.get());
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return CLASSIC_SHAPE;
    }

    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return CLASSIC_SHAPE;
    }

    @Override public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,
            BlockState state) {
        return new MnemonicMatrixBlockEntity(pos, state);
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
