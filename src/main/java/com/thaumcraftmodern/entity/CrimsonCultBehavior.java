package com.thaumcraftmodern.entity;

/**
 * TC4 altar-guard and target-selection parameters shared by worldgen and the
 * modern AI.
 */
public final class CrimsonCultBehavior {
    public static final double FOLLOW_RANGE = 32.0D;
    public static final int TARGET_CHECK_INTERVAL_TICKS = 0;
    public static final int UNSEEN_MEMORY_TICKS =
            HostileAiBehavior.UNSEEN_MEMORY_TICKS;
    public static final double ALERT_VERTICAL_RANGE = 10.0D;
    public static final int RITUAL_CHECK_INTERVAL_TICKS = 40;
    public static final double RITUAL_MAX_DISTANCE_SQUARED = 256.0D;
    public static final int CLERIC_HOME_RADIUS = 8;
    public static final int KNIGHT_HOME_RADIUS = 16;
    public static final int RITUALIST_ALERT_CHANCE = 3;
    public static final double CLERIC_RANGED_MIN_DISTANCE = 2.0D;
    public static final double PRAETOR_RANGED_MIN_DISTANCE = 16.0D;
    public static final float RANGED_MAX_DISTANCE = 24.0F;
    public static final double RANGED_MOVE_SPEED = 1.0D;
    public static final int CLERIC_RANGED_MIN_COOLDOWN_TICKS = 20;
    public static final int PRAETOR_RANGED_MIN_COOLDOWN_TICKS = 30;
    public static final int RANGED_MAX_COOLDOWN_TICKS = 40;
    public static final float CLERIC_ORB_ROLL_THRESHOLD = 0.66F;
    public static final int CLERIC_FIREBALL_COUNT = 3;
    public static final float ORB_VELOCITY = 0.66F;
    public static final float ORB_INACCURACY = 3.0F;
    public static final int RED_ORB_LIFETIME_TICKS = 240;
    public static final float RED_ORB_DAMAGE_MULTIPLIER = 1.0F;

    private CrimsonCultBehavior() {
    }

    static boolean shouldAlertRitualist(
            int legacyChanceRoll
    ) {
        return legacyChanceRoll == 0;
    }

    static boolean areCrimsonAllies(
            LegacyMobKind first,
            LegacyMobKind second
    ) {
        return isCrimsonCultist(first) && isCrimsonCultist(second);
    }

    private static boolean isCrimsonCultist(LegacyMobKind kind) {
        return kind == LegacyMobKind.CRIMSON_CLERIC
                || kind == LegacyMobKind.CRIMSON_KNIGHT
                || kind == LegacyMobKind.CRIMSON_INQUISITOR
                || kind == LegacyMobKind.CRIMSON_PRAETOR;
    }
}
