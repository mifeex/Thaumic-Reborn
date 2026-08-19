package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CrimsonCultBehaviorTest {
    @Test
    void altarRitualUsesOriginalTc4Limits() {
        assertEquals(32.0D, CrimsonCultBehavior.FOLLOW_RANGE);
        assertEquals(0, CrimsonCultBehavior.TARGET_CHECK_INTERVAL_TICKS);
        assertEquals(60, CrimsonCultBehavior.UNSEEN_MEMORY_TICKS);
        assertEquals(10.0D, CrimsonCultBehavior.ALERT_VERTICAL_RANGE);
        assertEquals(40, CrimsonCultBehavior.RITUAL_CHECK_INTERVAL_TICKS);
        assertEquals(
                256.0D,
                CrimsonCultBehavior.RITUAL_MAX_DISTANCE_SQUARED
        );
        assertEquals(8, CrimsonCultBehavior.CLERIC_HOME_RADIUS);
        assertEquals(16, CrimsonCultBehavior.KNIGHT_HOME_RADIUS);
        assertEquals(3, CrimsonCultBehavior.RITUALIST_ALERT_CHANCE);
        assertEquals(2.0D, CrimsonCultBehavior.CLERIC_RANGED_MIN_DISTANCE);
        assertEquals(16.0D, CrimsonCultBehavior.PRAETOR_RANGED_MIN_DISTANCE);
        assertEquals(24.0F, CrimsonCultBehavior.RANGED_MAX_DISTANCE);
        assertEquals(1.0D, CrimsonCultBehavior.RANGED_MOVE_SPEED);
        assertEquals(20,
                CrimsonCultBehavior.CLERIC_RANGED_MIN_COOLDOWN_TICKS);
        assertEquals(30,
                CrimsonCultBehavior.PRAETOR_RANGED_MIN_COOLDOWN_TICKS);
        assertEquals(40, CrimsonCultBehavior.RANGED_MAX_COOLDOWN_TICKS);
        assertEquals(0.66F, CrimsonCultBehavior.CLERIC_ORB_ROLL_THRESHOLD);
        assertEquals(3, CrimsonCultBehavior.CLERIC_FIREBALL_COUNT);
        assertEquals(0.66F, CrimsonCultBehavior.ORB_VELOCITY);
        assertEquals(3.0F, CrimsonCultBehavior.ORB_INACCURACY);
        assertEquals(240, CrimsonCultBehavior.RED_ORB_LIFETIME_TICKS);
        assertEquals(1.0F, CrimsonCultBehavior.RED_ORB_DAMAGE_MULTIPLIER);
    }

    @Test
    void ritualistsAnswerGroupAggroWithOriginalOneInThreeChance() {
        assertFalse(CrimsonCultBehavior.shouldAlertRitualist(
                2
        ));
        assertTrue(CrimsonCultBehavior.shouldAlertRitualist(
                0
        ));
    }

    @Test
    void everyCrimsonCultistVariantTreatsEveryOtherVariantAsAnAlly() {
        LegacyMobKind[] cultists = {
                LegacyMobKind.CRIMSON_KNIGHT,
                LegacyMobKind.CRIMSON_INQUISITOR,
                LegacyMobKind.CRIMSON_CLERIC,
                LegacyMobKind.CRIMSON_PRAETOR
        };
        for (LegacyMobKind first : cultists) {
            for (LegacyMobKind second : cultists) {
                assertTrue(CrimsonCultBehavior.areCrimsonAllies(first, second),
                        first + " should be allied to " + second);
            }
        }
        assertFalse(CrimsonCultBehavior.areCrimsonAllies(
                LegacyMobKind.CRIMSON_KNIGHT,
                LegacyMobKind.ELDRITCH_GUARDIAN
        ));
    }
}
