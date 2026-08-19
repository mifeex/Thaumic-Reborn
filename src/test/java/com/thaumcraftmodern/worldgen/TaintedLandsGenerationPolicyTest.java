package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

class TaintedLandsGenerationPolicyTest {
    @Test
    void taintedLandUsesTheExactTc4DecoratorCounts() {
        assertEquals(2, TaintedLandsGenerationPolicy.BIOME_WEIGHT);
        assertEquals(2, TaintedLandsGenerationPolicy.FLOWER_ATTEMPTS);
        assertEquals(2, TaintedLandsGenerationPolicy.GRASS_ATTEMPTS);
        assertEquals(3, TaintedLandsGenerationPolicy.TAINT_BLOB_VARIANTS);
        assertEquals(
                10,
                TaintedLandsGenerationPolicy.SURFACE_FIBRE_ATTEMPTS
        );
        assertEquals(
                8,
                TaintedLandsGenerationPolicy.SPREAD_FIBRE_ATTEMPTS
        );
        assertEquals(3, TaintedLandsGenerationPolicy.INFECTED_TREE_ATTEMPTS);
        assertEquals(3, TaintedLandsGenerationPolicy.INFECTED_TREE_STAGES);
        assertEquals(
                18,
                TaintedLandsGenerationPolicy.TAINTED_SOIL_PATCH_ATTEMPTS
        );
        assertEquals(
                14,
                TaintedLandsGenerationPolicy.TAINTED_PLANT_ATTEMPTS
        );
        assertEquals(3, TaintedLandsGenerationPolicy.SPORE_STALK_ATTEMPTS);
        assertEquals(12, TaintedLandsGenerationPolicy.TAINTED_NODE_RARITY);
        assertEquals(1, TaintedLandsGenerationPolicy.TAINTACLE_WEIGHT);
        assertEquals(1, TaintedLandsGenerationPolicy.TAINTACLE_MINIMUM);
        assertEquals(1, TaintedLandsGenerationPolicy.TAINTACLE_MAXIMUM);
    }

    @Test
    void defaultPatchUsesTheRestoredNegativeTail() {
        assertEquals(
                -0.91D,
                TaintedLandsGenerationPolicy.patchThreshold(2),
                0.000_001D
        );
        assertTrue(
                TaintedLandsGenerationPolicy.PATCH_NOISE_SCALE_QUARTS > 96
        );
    }

    @Test
    void colorsMatchTc4BiomeGenTaint() {
        assertEquals(0x6D4189, TaintedLandsGenerationPolicy.GRASS_COLOR);
        assertEquals(0x4F8A55, TaintedLandsGenerationPolicy.FOLIAGE_COLOR);
        assertEquals(0x7C44FF, TaintedLandsGenerationPolicy.SKY_COLOR);
        assertEquals(0xCC1188, TaintedLandsGenerationPolicy.WATER_COLOR);
    }

    @Test
    void infectedTreesUseDedicatedGreenPurpleTaintedLeaves()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyVegetationFeature.java"
        ));
        assertTrue(source.contains(
                "BlockState leaves = ModBlocks.TAINTED_LEAVES"
        ));
        assertTrue(source.contains(
                ".setValue(LeavesBlock.PERSISTENT, true)"
        ));
        assertFalse(source.contains(
                "BlockState leaves = Blocks.OAK_LEAVES"
        ));
    }
}
