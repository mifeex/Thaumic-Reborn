package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.worldgen.ModWorldgenKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bounded modern form of TC4's BlockTaint/BlockTaintFibres random-tick
 * conversion. It gives generated intermediate trees time-dependent stages
 * without changing biomes or loading neighbouring chunks.
 */
public final class TaintEcology {
    private TaintEcology() {
    }

    static void randomTick(
            ServerLevel level,
            BlockPos source,
            BlockState sourceState,
            RandomSource random
    ) {
        attemptBiomeSpread(level, source, random);
        if (!TaintBiomeService.isTainted(level, source)) {
            decayOutsideTaint(level, source, sourceState, random);
            return;
        }
        BlockPos target = source.offset(
                random.nextInt(3) - 1,
                random.nextInt(5) - 3,
                random.nextInt(3) - 1
        );
        if (!level.isLoaded(target)) {
            return;
        }
        BlockState targetState = level.getBlockState(target);
        int adjacent = adjacentTaint(level, target);
        if (targetState.is(BlockTags.LEAVES)
                && !targetState.is(ModBlocks.TAINTED_LEAVES.get())
                && adjacent >= 1) {
            BlockState taintedLeaves =
                    ModBlocks.TAINTED_LEAVES.get().defaultBlockState();
            if (targetState.hasProperty(
                    net.minecraft.world.level.block.LeavesBlock.PERSISTENT
            )) {
                taintedLeaves = taintedLeaves.setValue(
                        net.minecraft.world.level.block.LeavesBlock.PERSISTENT,
                        targetState.getValue(
                                net.minecraft.world.level.block.LeavesBlock
                                        .PERSISTENT
                        )
                );
            }
            setTaintBlock(level, target, taintedLeaves, random);
            return;
        }
        if ((targetState.is(BlockTags.LOGS)
                || targetState.is(ModBlocks.TAINTED_LEAVES.get()))
                && adjacent >= 2) {
            setTaintBlock(
                    level,
                    target,
                    ModBlocks.CRUSTED_TAINT.get().defaultBlockState(),
                    random
            );
            return;
        }
        if ((targetState.is(BlockTags.DIRT)
                || targetState.is(BlockTags.SAND)
                || targetState.is(BlockTags.BASE_STONE_OVERWORLD))
                && adjacent >= 3) {
            setTaintBlock(
                    level,
                    target,
                    ModBlocks.TAINTED_SOIL.get().defaultBlockState(),
                    random
            );
            return;
        }
        if ((targetState.isAir() || targetState.canBeReplaced())
                && adjacentSolid(level, target)) {
            placeTaintGrowth(level, target, random);
        }
    }

    private static void attemptBiomeSpread(
            ServerLevel level,
            BlockPos source,
            RandomSource random
    ) {
        BlockPos target = source.offset(
                random.nextInt(3) - 1,
                0,
                random.nextInt(3) - 1
        );
        int spreadBound = TaintBiomeService.spreadChanceBound();
        if (spreadBound <= 0
                || !level.isLoaded(target)
                || TaintBiomeService.isTainted(level, target)
                || random.nextInt(spreadBound) != 0
                || adjacentTaint(level, source) < 2) {
            return;
        }
        if (TaintBiomeService.taintColumn(level, target)) {
            playRoots(level, source, random);
        }
    }

    private static void decayOutsideTaint(
            ServerLevel level,
            BlockPos source,
            BlockState sourceState,
            RandomSource random
    ) {
        if (sourceState.is(ModBlocks.CRUSTED_TAINT.get())) {
            if (random.nextInt(20) == 0) {
                level.setBlock(
                        source,
                        ModBlocks.FLUX_GOO.get().defaultBlockState(),
                        3
                );
            }
        } else if (sourceState.is(ModBlocks.TAINTED_SOIL.get())) {
            if (random.nextInt(10) == 0) {
                level.setBlock(source, Blocks.DIRT.defaultBlockState(), 3);
            }
        } else {
            level.removeBlock(source, false);
        }
    }

    private static int adjacentTaint(
            ServerLevel level,
            BlockPos position
    ) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (isTaint(level.getBlockState(position.relative(direction)))) {
                count++;
            }
        }
        return count;
    }

    private static boolean isTaint(BlockState state) {
        return state.is(ModBlocks.CRUSTED_TAINT.get())
                || state.is(ModBlocks.TAINTED_SOIL.get())
                || state.is(ModBlocks.TAINTED_LEAVES.get())
                || state.is(ModBlocks.TAINT_FIBRES.get())
                || state.is(ModBlocks.SHORT_TAINTED_GRASS.get())
                || state.is(ModBlocks.TALL_TAINTED_GRASS.get())
                || state.is(ModBlocks.SPORE_STALK.get())
                || state.is(ModBlocks.MATURE_SPORE_STALK.get());
    }

    private static boolean adjacentSolid(
            ServerLevel level,
            BlockPos position
    ) {
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

    private static void placeTaintGrowth(
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        if (random.nextInt(10) == 0
                && level.isEmptyBlock(position.above())
                && level.getBlockState(position.below()).isFaceSturdy(
                        level,
                        position.below(),
                        Direction.UP
                )) {
            BlockState plant;
            int plantRoll = random.nextInt(10);
            if (plantRoll < 9) {
                plant = ModBlocks.SHORT_TAINTED_GRASS.get()
                        .defaultBlockState();
            } else if (random.nextInt(12) < 10) {
                plant = ModBlocks.TALL_TAINTED_GRASS.get()
                        .defaultBlockState();
            } else {
                plant = ModBlocks.SPORE_STALK.get().defaultBlockState();
            }
            if (plant.canSurvive(level, position)) {
                setTaintBlock(level, position, plant, random);
                return;
            }
        }
        placeFibres(level, position);
    }

    public static void placeFibres(
            ServerLevel level,
            BlockPos position
    ) {
        MultifaceBlock fibres = (MultifaceBlock) ModBlocks.TAINT_FIBRES.get();
        BlockState state = level.getBlockState(position);
        for (Direction direction : Direction.values()) {
            BlockState withFace = fibres.getStateForPlacement(
                    state,
                    level,
                    position,
                    direction
            );
            if (withFace != null) {
                state = withFace;
            }
        }
        if (state.is(fibres)) {
            level.setBlock(position, state, 3);
            playRoots(level, position, level.random);
        }
    }

    private static void setTaintBlock(
            ServerLevel level,
            BlockPos position,
            BlockState state,
            RandomSource random
    ) {
        level.setBlock(position, state, 3);
        playRoots(level, position, random);
    }

    private static void playRoots(
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        level.playSound(
                null,
                position,
                ModSounds.ROOTS.get(),
                SoundSource.BLOCKS,
                0.1F,
                0.9F + random.nextFloat() * 0.2F
        );
    }
}
