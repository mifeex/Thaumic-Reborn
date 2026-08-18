package com.thaumcraftmodern.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void ecologyFallbackCanPopulateTaintedLandInDaylight() {
        assertTrue(TaintedBiomeSpawnPolicy.usesEcologyLifecycleRules(
                LegacyMobKind.TAINT_SPORE_SWARMER
        ));
        assertTrue(TaintedBiomeSpawnPolicy.usesEcologyLifecycleRules(
                LegacyMobKind.TAINT_SWARM
        ));
        assertTrue(TaintedBiomeSpawnPolicy.usesEcologyLifecycleRules(
                LegacyMobKind.TAINTED_SHEEP
        ));
        assertTrue(TaintedBiomeSpawnPolicy.usesEcologyLifecycleRules(
                LegacyMobKind.TAINTED_COW
        ));
        assertTrue(TaintedBiomeSpawnPolicy.usesEcologyLifecycleRules(
                LegacyMobKind.TAINTED_CHICKEN
        ));
        assertTrue(TaintedBiomeSpawnPolicy.usesEcologyLifecycleRules(
                LegacyMobKind.THAUMIC_SLIME
        ));
        assertTrue(LegacyMobKind.THAUMIC_SLIME.tainted());
        assertFalse(TaintedBiomeSpawnPolicy.usesEcologyLifecycleRules(
                LegacyMobKind.TAINTACLE
        ));
    }

    @Test
    void onlyTaintedVillagersRequireANearbyVillage() throws Exception {
        assertTrue(TaintedBiomeSpawnPolicy.requiresNearbyVillage(
                LegacyMobKind.TAINTED_VILLAGER
        ));
        assertFalse(TaintedBiomeSpawnPolicy.requiresNearbyVillage(
                LegacyMobKind.TAINTED_COW
        ));
        assertEquals(2, TaintedBiomeSpawnPolicy.VILLAGE_PROXIMITY_SECTIONS);

        String mob = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/com/thaumcraftmodern/entity/"
                        + "LegacyThaumcraftMob.java"));
        assertTrue(mob.contains("requiresNearbyVillage(sample.kind)"));
        assertTrue(mob.contains("isCloseToVillage("));
        assertTrue(mob.contains("VILLAGE_PROXIMITY_SECTIONS"));
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
