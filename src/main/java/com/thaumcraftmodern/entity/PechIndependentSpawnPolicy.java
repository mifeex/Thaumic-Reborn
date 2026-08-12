package com.thaumcraftmodern.entity;

/** Pure constants and rolls for the Pech spawn path outside the monster cap. */
final class PechIndependentSpawnPolicy {
    static final int CHECK_INTERVAL_TICKS = 200;
    static final int RARE_ROLL_BOUND = 20;
    static final int MIN_PLAYER_DISTANCE = 24;
    static final int MAX_PLAYER_DISTANCE = 64;
    static final int POSITION_ATTEMPTS = 8;
    static final int MIN_GROUP_SIZE = 1;
    static final int MAX_GROUP_SIZE = 2;

    private PechIndependentSpawnPolicy() {
    }

    static boolean isCheckTick(long gameTime) {
        return gameTime % CHECK_INTERVAL_TICKS == 0;
    }

    static boolean winsRareRoll(int rareRoll) {
        return rareRoll == 0;
    }

    static int groupSize(int groupRoll) {
        return MIN_GROUP_SIZE + Math.floorMod(
                groupRoll,
                MAX_GROUP_SIZE - MIN_GROUP_SIZE + 1
        );
    }
}
