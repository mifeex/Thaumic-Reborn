package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Keeps the modern tainted-leaves bridge inside the same bounded ecology as
 * the original TC4 taint blocks, including cleanup after biome purification.
 */
public final class SpreadingTaintedLeavesBlock extends LeavesBlock {
    public SpreadingTaintedLeavesBlock(BlockBehaviour.Properties properties) {
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
    }
}
