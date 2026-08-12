package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Solid TC4 taint states share the same slow ecology tick.
 */
public final class SpreadingTaintBlock extends Block {
    public SpreadingTaintBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        TaintEcology.randomTick(level, position, state, random);
        if (!state.is(ModBlocks.CRUSTED_TAINT.get())
                || !level.getBlockState(position).is(this)) {
            return;
        }
        if (spawnSwarmer(level, position, random)) {
            return;
        }
        convertSurroundedTaintToFlux(level, position);
    }

    private static boolean spawnSwarmer(
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        if (!ThaumcraftModernServerConfig.spawnTaintCreatures()
                || !level.isEmptyBlock(position.above())
                || random.nextInt(200) != 0
                || !level.getEntitiesOfClass(
                        LegacyThaumcraftMob.class,
                        new AABB(position).inflate(16.0D),
                        mob -> mob.kind() == LegacyMobKind.TAINT_SPORE_SWARMER
                ).isEmpty()) {
            return false;
        }
        LegacyThaumcraftMob swarmer =
                ModEntities.TAINT_SPORE_SWARMER.get().create(level);
        if (swarmer == null) {
            return false;
        }
        level.removeBlock(position, false);
        swarmer.moveTo(
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D,
                random.nextFloat() * 360.0F,
                0.0F
        );
        swarmer.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(position),
                MobSpawnType.MOB_SUMMONED,
                null,
                null
        );
        level.addFreshEntity(swarmer);
        level.playSound(
                null,
                position,
                ModSounds.ROOTS.get(),
                SoundSource.BLOCKS,
                0.1F,
                0.9F + random.nextFloat() * 0.2F
        );
        return true;
    }

    private static boolean convertSurroundedTaintToFlux(
            ServerLevel level,
            BlockPos position
    ) {
        if (!level.getBlockState(position.above())
                .is(ModBlocks.CRUSTED_TAINT.get())) {
            return false;
        }
        for (net.minecraft.core.Direction direction
                : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(position.relative(direction))
                    .is(ModBlocks.CRUSTED_TAINT.get())) {
                return false;
            }
        }
        level.setBlock(
                position,
                ModBlocks.FLUX_GOO.get().defaultBlockState(),
                3
        );
        return true;
    }
}
