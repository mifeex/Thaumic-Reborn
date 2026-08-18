package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.EldritchCrabVentBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/** Places the wall burrows collected by TC4's GenCommon.crabSpawner pass. */
public final class OuterLandsCrabVents {
    private OuterLandsCrabVents() {
    }

    static int populate(
            ServerLevelAccessor level,
            ChunkPos chunk,
            long worldSeed,
            OuterLandsCell cell
    ) {
        int chance = switch (cell.feature()) {
            case 7 -> 25;
            case 12 -> 50;
            case 0 -> 1250;
            default -> 0;
        };
        if (chance == 0) {
            return 0;
        }

        int placed = 0;
        int changed = 0;
        boolean crusted = cell.feature() != 0;
        boolean existingVent = false;
        BlockPos fallback = null;
        Direction fallbackOpening = null;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 1;
                y <= OuterLandsLabyrinthGenerator.BASE_Y + 11; y++) {
            for (int x = minX; x <= minX + 15; x++) {
                for (int z = minZ; z <= minZ + 15; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(ModBlocks.ELDRITCH_CRAB_VENT.get())) {
                        existingVent = true;
                        if (state.getValue(EldritchCrabVentBlock.CRUSTED)
                                != crusted) {
                            level.setBlock(cursor, state.setValue(
                                    EldritchCrabVentBlock.CRUSTED, crusted
                            ), 2);
                            changed++;
                        }
                        continue;
                    }
                    if (!eligibleStone(state, cell.feature())
                            || adjacentVent(level, cursor)) {
                        continue;
                    }
                    Direction opening = singleOpening(level, cursor);
                    if (opening == null) {
                        continue;
                    }
                    if (cell.feature() == 7 && fallback == null) {
                        fallback = cursor.immutable();
                        fallbackOpening = opening;
                    }
                    if (Math.floorMod(mix(worldSeed, x, y, z), chance) != 0) {
                        continue;
                    }
                    level.setBlock(cursor, ModBlocks.ELDRITCH_CRAB_VENT.get()
                            .defaultBlockState().setValue(
                                    EldritchCrabVentBlock.FACING, opening
                            ).setValue(
                                    EldritchCrabVentBlock.CRUSTED, crusted
                            ), 2);
                    placed++;
                    changed++;
                }
            }
        }
        /*
         * A TC4 crab nest has many 1-in-25 candidates and therefore visibly
         * contains burrows in practice. Chunk-local modern generation can
         * expose fewer candidates at seams, so retain the original rolls but
         * guarantee one real burrow when an otherwise valid nest rolled zero.
         * The existing-block guard keeps chunk migration idempotent.
         */
        if (cell.feature() == 7 && !existingVent && placed == 0
                && fallback != null) {
            level.setBlock(fallback, ModBlocks.ELDRITCH_CRAB_VENT.get()
                    .defaultBlockState().setValue(
                            EldritchCrabVentBlock.FACING, fallbackOpening
                    ).setValue(
                            EldritchCrabVentBlock.CRUSTED, true
                    ), 2);
            placed++;
            changed++;
        }
        return changed;
    }

    private static boolean eligibleStone(BlockState state, int feature) {
        if (feature == 7) {
            return state.is(ModBlocks.ANCIENT_STONE.get())
                    || state.is(ModBlocks.ANCIENT_CRUST.get());
        }
        return feature == 12
                ? state.is(ModBlocks.ANCIENT_CRUST.get())
                : state.is(ModBlocks.ANCIENT_STONE.get());
    }

    private static Direction singleOpening(
            ServerLevelAccessor level, BlockPos position
    ) {
        Direction opening = null;
        for (Direction direction : Direction.values()) {
            if (!level.getBlockState(position.relative(direction)).isAir()) {
                continue;
            }
            if (opening != null) {
                return null;
            }
            opening = direction;
        }
        return opening;
    }

    private static boolean adjacentVent(
            ServerLevelAccessor level, BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(position.relative(direction))
                    .is(ModBlocks.ELDRITCH_CRAB_VENT.get())) {
                return true;
            }
        }
        return false;
    }

    private static int mix(long seed, int x, int y, int z) {
        long value = seed ^ x * 341873128712L ^ z * 132897987541L
                ^ y * 42317861L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        return (int) (value ^ value >>> 32);
    }
}
