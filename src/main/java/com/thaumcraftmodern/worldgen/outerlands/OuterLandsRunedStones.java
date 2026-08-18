package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.AncientStoneBlock;
import com.thaumcraftmodern.world.block.CrystalClusterBlock;
import com.thaumcraftmodern.world.block.EldritchCrystalBlock;
import com.thaumcraftmodern.world.block.EldritchNothingBlock;
import com.thaumcraftmodern.world.block.EldritchRunedStoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/** Restores TC4 GenCommon.decoCommon's glowing, glyphed and runed variants. */
public final class OuterLandsRunedStones {
    private static final long CANDIDATE_SALT = 0x52756e6543616e64L;
    private static final long CRAB_SALT = 0x437261624578636cL;
    private static final long DECO_BRANCH_SALT = 0x4465636f4272616eL;
    private static final long RUNED_SALT = 0x52756e656453746fL;
    private static final long PATTERN_SALT = 0x5061747465726e73L;
    private static final long CRYSTAL_SALT = 0x4372797374616c73L;
    private static final long CRUST_GLOW_SALT = 0x4372757374476c6fL;

    private OuterLandsRunedStones() {
    }

    static int populate(
            ServerLevelAccessor level,
            ChunkPos chunk,
            long worldSeed,
            OuterLandsCell cell
    ) {
        int placed = replaceLegacyCrystalPlaceholders(level, chunk);
        placed += replaceLegacyCrossLights(level, chunk, cell);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 1;
                y <= OuterLandsLabyrinthGenerator.BASE_Y + 11; y++) {
            for (int x = chunk.getMinBlockX();
                    x <= chunk.getMinBlockX() + 15; x++) {
                for (int z = chunk.getMinBlockZ();
                        z <= chunk.getMinBlockZ() + 15; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(ModBlocks.ANCIENT_CRUST.get())) {
                        if (roll(
                                worldSeed, cursor, CRUST_GLOW_SALT, 25
                        ) == 0) {
                            level.setBlock(
                                    cursor,
                                    ModBlocks.ELDRITCH_GLOWING_CRUST.get()
                                            .defaultBlockState(),
                                    2
                            );
                            placed++;
                        }
                        continue;
                    }
                    if (!state.is(ModBlocks.ANCIENT_STONE.get())
                            || cell.feature() == 7
                            || roll(worldSeed, cursor, CANDIDATE_SALT, 25) != 0
                            || cell.feature() == 0
                            && roll(worldSeed, cursor, CRAB_SALT, 50) == 0
                            || !hasValidExposure(level, cursor)
                            || adjacentEldritchFeature(level, cursor)) {
                        continue;
                    }
                    int branch = roll(
                            worldSeed, cursor, DECO_BRANCH_SALT, 3
                    );
                    BlockState replacement;
                    boolean glowing = branch != 0;
                    if (glowing) {
                        replacement = ModBlocks.ELDRITCH_GLOWING_CRUST.get()
                                .defaultBlockState();
                    } else if (roll(
                            worldSeed, cursor, RUNED_SALT, 8
                    ) != 0) {
                        replacement = ModBlocks.ELDRITCH_GLYPHED_STONE.get()
                                .defaultBlockState();
                    } else {
                        int pattern = roll(
                                worldSeed, cursor, PATTERN_SALT, 4
                        );
                        replacement = ModBlocks.ELDRITCH_RUNED_STONE.get()
                                .defaultBlockState().setValue(
                                        EldritchRunedStoneBlock.PATTERN,
                                        pattern
                                );
                    }
                    level.setBlock(
                            cursor,
                            replacement,
                            2
                    );
                    if (glowing && roll(
                            worldSeed, cursor, CRYSTAL_SALT, 12
                    ) == 0) {
                        growEldritchCrystal(level, cursor);
                    }
                    placed++;
                }
            }
        }
        return placed;
    }

    /**
     * Repairs chunks decorated before the metadata-7 crystal was ported. The
     * old placeholder is only replaced when it is attached to generated
     * glowing crust, so player-placed balanced clusters remain untouched.
     */
    private static int replaceLegacyCrystalPlaceholders(
            ServerLevelAccessor level,
            ChunkPos chunk
    ) {
        int changed = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 1;
                y <= OuterLandsLabyrinthGenerator.BASE_Y + 11; y++) {
            for (int x = chunk.getMinBlockX();
                    x <= chunk.getMinBlockX() + 15; x++) {
                for (int z = chunk.getMinBlockZ();
                        z <= chunk.getMinBlockZ() + 15; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!state.is(ModBlocks.BALANCED_CRYSTAL_CLUSTER.get())) {
                        continue;
                    }
                    Direction facing = state.getValue(
                            CrystalClusterBlock.FACING
                    );
                    BlockPos support = cursor.relative(facing.getOpposite());
                    if (!level.getBlockState(support).is(
                            ModBlocks.ELDRITCH_GLOWING_CRUST.get()
                    )) {
                        continue;
                    }
                    level.setBlock(
                            cursor,
                            ModBlocks.ELDRITCH_CRYSTAL_CLUSTER.get()
                                    .defaultBlockState().setValue(
                                            EldritchCrystalBlock.FACING,
                                            facing
                                    ),
                            2
                    );
                    changed++;
                }
            }
        }
        return changed;
    }

    private static int replaceLegacyCrossLights(
            ServerLevelAccessor level,
            ChunkPos chunk,
            OuterLandsCell cell
    ) {
        if (cell.feature() >= 1 && cell.feature() <= 8
                || !cell.north() || !cell.south()
                || !cell.west() || !cell.east()) {
            return 0;
        }
        int changed = 0;
        for (int y : new int[]{
                OuterLandsLabyrinthGenerator.BASE_Y + 2,
                OuterLandsLabyrinthGenerator.BASE_Y + 8
        }) {
            BlockPos position = new BlockPos(
                    chunk.getMinBlockX() + 8,
                    y,
                    chunk.getMinBlockZ() + 8
            );
            BlockState state = level.getBlockState(position);
            if (!state.is(ModBlocks.ANCIENT_STONE.get())
                    || state.getValue(AncientStoneBlock.VARIANT) != 3) {
                continue;
            }
            level.setBlock(
                    position,
                    ModBlocks.ELDRITCH_GLOWING_CRUST.get()
                            .defaultBlockState(),
                    2
            );
            changed++;
        }
        return changed;
    }

    private static boolean hasValidExposure(
            ServerLevelAccessor level, BlockPos position
    ) {
        int exposed = 0;
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(
                    position.relative(direction)
            );
            if (neighbor.isAir()) {
                exposed++;
            }
        }
        return exposed > 0
                && (exposed == 1 || !isBedrockShowing(level, position));
    }

    private static boolean isBedrockShowing(
            ServerLevelAccessor level, BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPosition = position.relative(direction);
            if (level.getBlockState(neighborPosition)
                    .isCollisionShapeFullBlock(level, neighborPosition)) {
                continue;
            }
            BlockState opposite = level.getBlockState(
                    position.relative(direction.getOpposite())
            );
            if (opposite.is(ModBlocks.ANCIENT_SEAL.get())
                    || EldritchNothingBlock.isNothing(opposite)) {
                return true;
            }
        }
        return false;
    }

    private static boolean adjacentEldritchFeature(
            ServerLevelAccessor level, BlockPos position
    ) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    BlockState neighbor = level.getBlockState(
                            position.offset(x, y, z)
                    );
                    if (isEldritchFeature(neighbor)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isEldritchFeature(BlockState state) {
        return state.is(ModBlocks.ELDRITCH_GLOWING_CRUST.get())
                || state.is(ModBlocks.ELDRITCH_GLYPHED_STONE.get())
                || state.is(ModBlocks.ELDRITCH_RUNED_STONE.get())
                || state.is(ModBlocks.ELDRITCH_CRAB_VENT.get())
                || state.is(ModBlocks.ELDRITCH_LOCK.get())
                || state.is(ModBlocks.ELDRITCH_CAPSTONE.get())
                || state.is(ModBlocks.ELDRITCH_ALTAR_PART.get());
    }

    private static void growEldritchCrystal(
            ServerLevelAccessor level, BlockPos support
    ) {
        for (Direction direction : Direction.values()) {
            BlockPos crystalPosition = support.relative(direction);
            if (!level.getBlockState(crystalPosition).isAir()) {
                continue;
            }
            BlockState crystal = ModBlocks.ELDRITCH_CRYSTAL_CLUSTER.get()
                    .defaultBlockState().setValue(
                            EldritchCrystalBlock.FACING,
                            direction
                    );
            if (crystal.canSurvive(level, crystalPosition)) {
                level.setBlock(crystalPosition, crystal, 2);
            }
            return;
        }
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
