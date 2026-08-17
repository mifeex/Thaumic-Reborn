package com.thaumcraftmodern.worldgen.outerlands;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;

/** TC4 Outer Lands spawn boundary: a maze cell with a nearby solid ceiling. */
public final class OuterLandsSpawnRules {
    public static final int CEILING_SEARCH_DISTANCE = 16;

    private OuterLandsSpawnRules() {
    }

    public static boolean isOuterLands(ServerLevelAccessor level) {
        return level.getLevel().dimension().equals(
                OuterLandsDimensions.OUTER_LANDS
        );
    }

    public static boolean isEnclosedMazePosition(
            ServerLevelAccessor level,
            BlockPos position
    ) {
        if (!isOuterLands(level)) {
            return true;
        }
        if (!OuterLandsMaze.at(
                level.getLevel().getSeed(),
                position.getX() >> 4,
                position.getZ() >> 4
        ).exists()) {
            return false;
        }
        return hasCeiling(level, position, CEILING_SEARCH_DISTANCE);
    }

    /** Literal equivalent of TC4 TeleporterThaumcraft.hasCeiling. */
    public static boolean hasCeiling(
            ServerLevelAccessor level,
            BlockPos position,
            int distance
    ) {
        for (int offset = 2; offset <= distance; offset++) {
            if (level.getBlockState(position.above(offset)).blocksMotion()) {
                return true;
            }
        }
        return false;
    }
}
