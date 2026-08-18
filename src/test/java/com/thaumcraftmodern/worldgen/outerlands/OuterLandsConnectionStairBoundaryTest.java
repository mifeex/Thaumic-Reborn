package com.thaumcraftmodern.worldgen.outerlands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OuterLandsConnectionStairBoundaryTest {
    @Test
    void ancientWallWithBackingMayBecomeTrim() {
        assertFalse(OuterLandsLabyrinthGenerator.blocksConnectionStair(
                true,
                true
        ));
    }

    @Test
    void ancientWallWithoutBackingStopsTheStairRun() {
        assertTrue(OuterLandsLabyrinthGenerator.blocksConnectionStair(
                true,
                false
        ));
    }

    @Test
    void nonWallNeverStopsTheStairRun() {
        assertFalse(OuterLandsLabyrinthGenerator.blocksConnectionStair(
                false,
                false
        ));
    }
}
