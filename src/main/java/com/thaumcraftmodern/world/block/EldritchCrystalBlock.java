package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.CrystalClusterBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** TC4 BlockCrystal metadata 7: the hidden Strange Crystals variant. */
public final class EldritchCrystalBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public EldritchCrystalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    ) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(
                FACING,
                context.getClickedFace()
        );
        return state.canSurvive(context.getLevel(), context.getClickedPos())
                ? state
                : null;
    }

    @Override
    public boolean canSurvive(
            BlockState state,
            LevelReader level,
            BlockPos position
    ) {
        // TC4 BlockCrystal.canBlockStay accepts any solid adjacent face; the
        // stored orientation controls only how the TESR is drawn.
        for (Direction direction : Direction.values()) {
            BlockPos support = position.relative(direction);
            if (level.getBlockState(support).isFaceSturdy(
                    level,
                    support,
                    direction.getOpposite()
            )) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPlace(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(state, level, position, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())
                && !state.canSurvive(level, position)) {
            level.destroyBlock(position, true);
        }
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos position,
            net.minecraft.world.level.block.Block neighbor,
            BlockPos neighborPosition,
            boolean movedByPiston
    ) {
        if (!state.canSurvive(level, position)) {
            level.destroyBlock(position, true);
            return;
        }
        super.neighborChanged(
                state,
                level,
                position,
                neighbor,
                neighborPosition,
                movedByPiston
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public VoxelShape getVisualShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(
            BlockState state,
            BlockGetter level,
            BlockPos position
    ) {
        return true;
    }

    @Override
    public float getShadeBrightness(
            BlockState state,
            BlockGetter level,
            BlockPos position
    ) {
        return 1.0F;
    }

    @Override
    public List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder builder
    ) {
        return List.of(new ItemStack(ModItems.BALANCED_SHARD.get()));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new CrystalClusterBlockEntity(position, state);
    }
}
