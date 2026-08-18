package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.EldritchNothingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/** Keeps render anchors only on void blocks that are actually visible. */
final class OuterLandsEldritchNothingExposure {
    private OuterLandsEldritchNothingExposure() {
    }

    static int refresh(LevelAccessor level, ChunkPos chunk) {
        int changed = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
            for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
                for (int y = OuterLandsLabyrinthGenerator.BASE_Y - 1;
                        y <= OuterLandsLabyrinthGenerator.BASE_Y + 13; y++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!EldritchNothingBlock.isNothing(state)) {
                        continue;
                    }
                    boolean exposed = EldritchNothingBlock.shouldExpose(
                            level,
                            cursor
                    );
                    BlockState expected = exposed
                            ? ModBlocks.ELDRITCH_NOTHING_ANCHOR.get()
                                    .defaultBlockState()
                            : ModBlocks.ELDRITCH_NOTHING.get()
                                    .defaultBlockState();
                    if (state.equals(expected)) {
                        continue;
                    }
                    level.setBlock(
                            cursor,
                            expected,
                            2
                    );
                    changed++;
                }
            }
        }
        return changed;
    }
}
