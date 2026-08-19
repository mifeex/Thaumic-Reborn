package com.thaumcraftmodern.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumicBiomeRegionMaskTest {
    @Test
    void defaultWeightsSelectWholeBiomeScaleRegions() {
        long seed = 0x4A61756DL;

        assertTrue(maximumHorizontalRun(
                seed,
                ThaumicBiomeRegionMask.Selection.MAGICAL_FOREST,
                2_400,
                4
        ) >= 72);
        assertTrue(maximumHorizontalRun(
                seed,
                ThaumicBiomeRegionMask.Selection.TAINTED_LANDS,
                2_400,
                8
        ) >= 96);
    }

    @Test
    void zeroWeightsNeverSelectThaumicRegions() {
        for (int x = -500; x <= 500; x += 25) {
            for (int z = -500; z <= 500; z += 25) {
                assertEquals(
                        ThaumicBiomeRegionMask.Selection.NONE,
                        ThaumicBiomeRegionMask.select(x, z, 77L, 0, 0)
                );
            }
        }
    }

    @Test
    void seedChangesTheRegionalLayout() {
        int changed = 0;
        for (int x = -1200; x <= 1200; x += 40) {
            for (int z = -1200; z <= 1200; z += 40) {
                if (ThaumicBiomeRegionMask.select(x, z, 11L, 5, 2)
                        != ThaumicBiomeRegionMask.select(
                                x, z, 12L, 5, 2
                        )) {
                    changed++;
                }
            }
        }
        assertNotEquals(0, changed);
    }

    @Test
    void regionScaleMatchesTheRestoredLegacyContract() {
        assertEquals(
                TaintedLandsGenerationPolicy.PATCH_NOISE_SCALE_QUARTS,
                ThaumicBiomeRegionMask.TAINTED_REGION_SIZE_QUARTS
        );
        assertEquals(120, ThaumicBiomeRegionMask.MAGICAL_REGION_SIZE_QUARTS);
    }

    private static int maximumHorizontalRun(
            long seed,
            ThaumicBiomeRegionMask.Selection wanted,
            int radius,
            int step
    ) {
        int maximum = 0;
        for (int z = -radius; z <= radius; z += step) {
            int run = 0;
            for (int x = -radius; x <= radius; x += step) {
                if (ThaumicBiomeRegionMask.select(x, z, seed, 5, 2)
                        == wanted) {
                    run += step;
                    maximum = Math.max(maximum, run);
                } else {
                    run = 0;
                }
            }
        }
        return maximum;
    }
}
