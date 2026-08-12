package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.TemporaryHoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** Invisible, non-colliding TC4 portable-hole placeholder. */
public final class TemporaryHoleBlock extends BaseEntityBlock {
    private static final DustParticleOptions HOLE_SPARK =
            new DustParticleOptions(new Vector3f(0.25F, 0.0F, 0.25F), 0.65F);

    public TemporaryHoleBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                         CollisionContext context) { return Shapes.empty(); }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                                  CollisionContext context) { return Shapes.empty(); }
    @Override
    public VoxelShape getBlockSupportShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        // The passage has no collision, but attached blocks must continue to
        // regard the temporarily displaced wall/floor as a valid support.
        return Shapes.block();
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TemporaryHoleBlockEntity(pos, state);
    }
    @Override
    public void animateTick(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random
    ) {
        if (random.nextInt(3) == 0) {
            level.addParticle(
                    HOLE_SPARK,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel ? createTickerHelper(type,
                ModBlockEntities.TEMPORARY_HOLE.get(), TemporaryHoleBlockEntity::serverTick) : null;
    }
}
