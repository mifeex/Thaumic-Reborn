package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.EldritchNothingBlock;
import com.thaumcraftmodern.world.block.EldritchRunedStoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/** Places TC4 GenCommon decoCommon's rare meta-10 runed traps. */
public final class OuterLandsRunedStones {
    private static final long CANDIDATE_SALT = 0x52756e6543616e64L;
    private static final long CRAB_SALT = 0x437261624578636cL;
    private static final long DECO_BRANCH_SALT = 0x4465636f4272616eL;
    private static final long RUNED_SALT = 0x52756e656453746fL;
    private static final long PATTERN_SALT = 0x5061747465726e73L;

    private OuterLandsRunedStones() {
    }

    static int populate(
            ServerLevelAccessor level,
            ChunkPos chunk,
            long worldSeed,
            OuterLandsCell cell
    ) {
        if (cell.feature() == 7) {
            return 0;
        }
        int placed = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 1;
                y <= OuterLandsLabyrinthGenerator.BASE_Y + 11; y++) {
            for (int x = chunk.getMinBlockX();
                    x <= chunk.getMinBlockX() + 15; x++) {
                for (int z = chunk.getMinBlockZ();
                        z <= chunk.getMinBlockZ() + 15; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!state.is(ModBlocks.ANCIENT_STONE.get())
                            || roll(worldSeed, cursor, CANDIDATE_SALT, 25) != 0
                            || cell.feature() == 0
                            && roll(worldSeed, cursor, CRAB_SALT, 50) == 0
                            || roll(worldSeed, cursor, DECO_BRANCH_SALT, 3) != 0
                            || roll(worldSeed, cursor, RUNED_SALT, 8) != 0
                            || !isExposed(level, cursor)
                            || adjacentEldritchFeature(level, cursor)) {
                        continue;
                    }
                    int pattern = roll(
                            worldSeed, cursor, PATTERN_SALT, 4
                    );
                    level.setBlock(
                            cursor,
                            ModBlocks.ELDRITCH_RUNED_STONE.get()
                                    .defaultBlockState().setValue(
                                            EldritchRunedStoneBlock.PATTERN,
                                            pattern
                                    ),
                            2
                    );
                    placed++;
                }
            }
        }
        return placed;
    }

    private static boolean isExposed(
            ServerLevelAccessor level, BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(
                    position.relative(direction)
            );
            if (neighbor.isAir() || EldritchNothingBlock.isNothing(neighbor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean adjacentEldritchFeature(
            ServerLevelAccessor level, BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(
                    position.relative(direction)
            );
            if (neighbor.is(ModBlocks.ELDRITCH_CRAB_VENT.get())
                    || neighbor.is(ModBlocks.ELDRITCH_RUNED_STONE.get())
                    || neighbor.is(ModBlocks.ELDRITCH_LOCK.get())) {
                return true;
            }
        }
        return false;
    }

    private static int roll(
            long worldSeed,
            BlockPos position,
            long salt,
            int bound
    ) {
        long value = worldSeed ^ position.asLong() ^ salt;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        return Math.floorMod((int) (value ^ value >>> 32), bound);
    }
}
