package com.thaumcraftmodern.worldgen.outerlands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Half;
import org.junit.jupiter.api.Test;

class OuterLandsMissingCornerStairTest {
    @Test
    void perpendicularRunsOfTheSameHalfFormCorner() {
        assertTrue(OuterLandsStairTopology.formsMissingCorner(
                Direction.EAST,
                Direction.NORTH,
                Half.BOTTOM,
                Direction.SOUTH,
                Direction.WEST,
                Half.BOTTOM
        ));
    }

    @Test
    void parallelRunDoesNotFillGap() {
        assertFalse(OuterLandsStairTopology.formsMissingCorner(
                Direction.EAST,
                Direction.NORTH,
                Half.BOTTOM,
                Direction.WEST,
                Direction.NORTH,
                Half.BOTTOM
        ));
    }

    @Test
    void mixedTopAndBottomStairsDoNotJoin() {
        assertFalse(OuterLandsStairTopology.formsMissingCorner(
                Direction.EAST,
                Direction.NORTH,
                Half.BOTTOM,
                Direction.SOUTH,
                Direction.WEST,
                Half.TOP
        ));
    }
}
