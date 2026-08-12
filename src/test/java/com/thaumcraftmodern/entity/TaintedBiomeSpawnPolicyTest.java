package com.thaumcraftmodern.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TaintedBiomeSpawnPolicyTest {
    @Test
    void groundedTaintCreaturesRequireHostileRulesInsideTaintedLand() {
        assertTrue(TaintedBiomeSpawnPolicy.allows(
                LegacyMobKind.TAINTED_CRAWLER,
                true,
                true,
                true
        ));
        assertFalse(TaintedBiomeSpawnPolicy.allows(
                LegacyMobKind.TAINTED_CRAWLER,
                false,
                true,
                true
        ));
        assertFalse(TaintedBiomeSpawnPolicy.allows(
                LegacyMobKind.TAINTED_CRAWLER,
                true,
                true,
                false
        ));
        assertFalse(TaintedBiomeSpawnPolicy.allows(
                LegacyMobKind.TAINTED_CRAWLER,
                true,
                false,
                true
        ));
    }

    @Test
    void flyingTaintCreaturesNeedNoGroundButRespectPeaceful() {
        assertTrue(TaintedBiomeSpawnPolicy.allows(
                LegacyMobKind.TAINT_SWARM,
                true,
                true,
                false
        ));
        assertFalse(TaintedBiomeSpawnPolicy.allows(
                LegacyMobKind.TAINT_SWARM,
                true,
                false,
                true
        ));
    }

    @Test
    void taintacleUsesClassicTaintedSurfacePredicate() throws Exception {
        String policy = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/com/thaumcraftmodern/entity/"
                        + "TaintedBiomeSpawnPolicy.java"));
        assertTrue(policy.contains("TAINT_FIBRES"));
        assertTrue(policy.contains("CRUSTED_TAINT"));
        assertTrue(policy.contains("validTaintacleGround"));
    }
}
