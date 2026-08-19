package com.thaumcraftmodern.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Modern terrain gate shared by every procedural Thaumcraft tree.
 *
 * <p>The legacy generators only inspected their central soil block. Modern
 * terrain can expose one-block cave roofs, flooded banks and overhangs, so a
 * natural tree also requires a dry planting cell and two consecutive support
 * blocks below every load-bearing trunk/root point.</p>
 */
final class TreeSitePolicy {
    private static final int SUPPORT_DEPTH = 2;

    private TreeSitePolicy() {
    }

    static boolean hasDrySupportedSoil(
            WorldGenLevel level,
            BlockPos origin
    ) {
        return hasDrySupportedSoil(level, origin, new int[][]{{0, 0}});
    }

    static boolean hasDrySupportedBase(
            WorldGenLevel level,
            BlockPos origin
    ) {
        return hasDrySupportedBase(level, origin, new int[][]{{0, 0}}, false);
    }

    static boolean hasDrySupportedSoil(
            WorldGenLevel level,
            BlockPos origin,
            int[][] footprint
    ) {
        return hasDrySupportedBase(level, origin, footprint, true);
    }

    static boolean hasDryDirtCoverage(
            WorldGenLevel level,
            BlockPos origin,
            int radius,
            int minimumPercent
    ) {
        int width = radius * 2 + 1;
        int total = width * width;
        int required = (total * minimumPercent + 99) / 100;
        int supported = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos plantingCell = origin.offset(x, 0, z);
                BlockPos soil = plantingCell.below();
                BlockState soilState = level.getBlockState(soil);
                if (level.getFluidState(plantingCell).isEmpty()
                        && level.getFluidState(soil).isEmpty()
                        && soilState.is(BlockTags.DIRT)
                        && soilState.isFaceSturdy(
                                level,
                                soil,
                                Direction.UP
                        )) {
                    supported++;
                }
            }
        }
        return supported >= required;
    }

    private static boolean hasDrySupportedBase(
            WorldGenLevel level,
            BlockPos origin,
            int[][] footprint,
            boolean requireDirt
    ) {
        if (!level.getFluidState(origin).isEmpty()) {
            return false;
        }
        for (int[] offset : footprint) {
            BlockPos soil = origin.offset(offset[0], -1, offset[1]);
            BlockState soilState = level.getBlockState(soil);
            if (!level.getFluidState(soil).isEmpty()
                    || (requireDirt && !soilState.is(BlockTags.DIRT))
                    || !soilState.isFaceSturdy(level, soil, Direction.UP)) {
                return false;
            }
            for (int depth = 1; depth < SUPPORT_DEPTH; depth++) {
                BlockPos support = soil.below(depth);
                BlockState supportState = level.getBlockState(support);
                if (!level.getFluidState(support).isEmpty()
                        || supportState.isAir()
                        || supportState.getCollisionShape(level, support)
                                .isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
