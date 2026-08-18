package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** TC4's indestructible, star-filled and lethal Outer Lands void layer. */
public final class EldritchNothingBlock extends Block {
    public static final BooleanProperty EXPOSED = BooleanProperty.create("exposed");
    private static final VoxelShape COLLISION = Block.box(2, 2, 2, 14, 14, 14);

    public EldritchNothingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(EXPOSED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return COLLISION;
    }

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity
    ) {
        if (!level.isClientSide
                && entity.tickCount > 20
                && (!(entity instanceof Player player)
                || !player.getAbilities().flying)) {
            entity.hurt(level.damageSources().fellOutOfWorld(), 8.0F);
        }
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighbor,
            BlockPos neighborPos,
            boolean moving
    ) {
        if (!level.isClientSide) {
            setExposure(level, pos, state, shouldExpose(level, pos));
        }
    }

    public static boolean shouldExpose(BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacent = level.getBlockState(adjacentPos);
            if (!isNothing(adjacent)
                    && !adjacent.isSolidRender(level, adjacentPos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean setExposure(
            Level level,
            BlockPos pos,
            BlockState state,
            boolean exposed
    ) {
        if (!isNothing(state)) {
            return false;
        }
        BlockState replacement = exposed
                ? ModBlocks.ELDRITCH_NOTHING_ANCHOR.get().defaultBlockState()
                : ModBlocks.ELDRITCH_NOTHING.get().defaultBlockState();
        if (state.equals(replacement)) {
            return false;
        }
        level.setBlock(pos, replacement, 2);
        return true;
    }

    public static boolean isNothing(BlockState state) {
        return state.is(ModBlocks.ELDRITCH_NOTHING.get())
                || state.is(ModBlocks.ELDRITCH_NOTHING_ANCHOR.get());
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(EXPOSED);
    }
}
