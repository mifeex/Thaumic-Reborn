package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.world.block.entity.EldritchNothingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Sparse render anchor used only on visible faces of eldritch nothing. */
public final class EldritchNothingAnchorBlock extends BaseEntityBlock {
    private static final VoxelShape COLLISION = Block.box(2, 2, 2, 14, 14, 14);

    public EldritchNothingAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos,
            Entity entity) {
        if (!level.isClientSide
                && entity.tickCount > 20
                && (!(entity instanceof Player player)
                || !player.getAbilities().flying)) {
            entity.hurt(level.damageSources().fellOutOfWorld(), 8.0F);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
            Block neighbor, BlockPos neighborPos, boolean moving) {
        if (!level.isClientSide) {
            EldritchNothingBlock.setExposure(
                    level,
                    pos,
                    state,
                    EldritchNothingBlock.shouldExpose(level, pos)
            );
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EldritchNothingBlockEntity(pos, state);
    }
}
