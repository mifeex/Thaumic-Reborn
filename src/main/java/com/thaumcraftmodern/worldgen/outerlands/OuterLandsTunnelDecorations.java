package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.EldritchNothingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Keeps air-space decorations on the labyrinth side of the void shell. */
final class OuterLandsTunnelDecorations {
    private OuterLandsTunnelDecorations() {
    }

    static boolean isInteriorAir(LevelAccessor level, BlockPos position) {
        if (!level.getBlockState(position).isAir()) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (EldritchNothingBlock.isNothing(
                    level.getBlockState(position.relative(direction))
            )) {
                return false;
            }
        }
        return true;
    }

    static int removeEscaped(
            LevelAccessor level,
            ChunkPos chunk,
            OuterLandsCell cell
    ) {
        if (cell.feature() != 13 && cell.feature() != 14) {
            return 0;
        }
        int removed = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
            for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
                for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 1;
                        y <= OuterLandsLabyrinthGenerator.BASE_Y + 8; y++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    boolean generatedDecoration = cell.feature() == 14
                            ? state.is(Blocks.COBWEB)
                            : state.is(ModBlocks.TAINT_FIBRES.get());
                    if (!generatedDecoration || !touchesOuterVoid(level, cursor)) {
                        continue;
                    }
                    level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                    removed++;
                }
            }
        }
        return removed;
    }

    private static boolean touchesOuterVoid(
            LevelAccessor level,
            BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            if (EldritchNothingBlock.isNothing(
                    level.getBlockState(position.relative(direction))
            )) {
                return true;
            }
        }
        return false;
    }
}
