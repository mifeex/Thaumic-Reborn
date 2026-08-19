package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MagicalForestGenerationPolicyTest {
    @Test
    void magicalForestUsesTheTunedOpenCanopyDecoratorCounts() {
        assertEquals(9, MagicalForestGenerationPolicy.TREE_ATTEMPTS);
        assertEquals(9, MagicalForestGenerationPolicy.SILVERWOOD_CHANCE);
        assertEquals(
                12,
                MagicalForestGenerationPolicy.SILVERWOOD_SITE_ATTEMPTS
        );
        assertEquals(
                2,
                MagicalForestGenerationPolicy.SILVERWOOD_GROUND_RADIUS
        );
        assertEquals(
                80,
                MagicalForestGenerationPolicy.SILVERWOOD_MIN_GROUND_PERCENT
        );
        assertEquals(
                7,
                MagicalForestGenerationPolicy
                        .GREATWOOD_CHANCE_AFTER_SILVERWOOD
        );
        assertEquals(3, MagicalForestGenerationPolicy.BOULDER_VARIANTS);
        assertEquals(
                4,
                MagicalForestGenerationPolicy.GIANT_MUSHROOM_GRID_SIZE
        );
        assertEquals(
                40,
                MagicalForestGenerationPolicy.GIANT_MUSHROOM_CHANCE
        );
        assertEquals(10, MagicalForestGenerationPolicy.MANA_POD_ATTEMPTS);
        assertEquals(8, MagicalForestGenerationPolicy.VISHROOM_ATTEMPTS);
        assertEquals(18, MagicalForestGenerationPolicy.SHIMMERLEAF_ATTEMPTS);
        assertEquals(2, MagicalForestGenerationPolicy.FLOWER_ATTEMPTS);
        assertEquals(12, MagicalForestGenerationPolicy.GRASS_ATTEMPTS);
        assertEquals(4, MagicalForestGenerationPolicy.FERN_CHANCE);
        assertEquals(
                6,
                MagicalForestGenerationPolicy.NORMAL_MUSHROOM_ATTEMPTS
        );
        assertEquals(6, MagicalForestGenerationPolicy.REED_ATTEMPTS);
    }

    @Test
    void magicalForestUsesCalmTemperateAndWarmUnderlyingClimate() {
        assertFalse(MagicalForestGenerationPolicy
                .supportsClimate(0.8F, true));
        assertFalse(MagicalForestGenerationPolicy
                .supportsClimate(0.2F, false));
        assertTrue(MagicalForestGenerationPolicy
                .supportsClimate(0.6F, false));
        assertTrue(MagicalForestGenerationPolicy
                .supportsClimate(1.0F, false));
        assertFalse(MagicalForestGenerationPolicy
                .supportsClimate(1.5F, false));
    }

    @Test
    void silverwoodOnlyReservesItsTrunkAndInnerCrown() {
        assertEquals(1, MagicalForestGenerationPolicy
                .silverwoodClearanceRadius(1, 9));
        assertEquals(1, MagicalForestGenerationPolicy
                .silverwoodClearanceRadius(7, 9));
        assertEquals(3, MagicalForestGenerationPolicy
                .silverwoodClearanceRadius(8, 9));
        assertEquals(3, MagicalForestGenerationPolicy
                .silverwoodClearanceRadius(12, 9));
    }
}
