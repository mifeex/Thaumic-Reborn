package com.thaumcraftmodern.entity;

/** Pure constants and rolls for the Pech spawn path outside the monster cap. */
final class PechIndependentSpawnPolicy {
    static final int CHECK_INTERVAL_TICKS = 200;
    static final int RARE_ROLL_BOUND = 20;
    static final int MIN_PLAYER_DISTANCE = 24;
    static final int MAX_PLAYER_DISTANCE = 64;
    /*
     * A biome edge can cut most of the vanilla 24-64 block spawn annulus out
     * of the Magical Forest. Eight blind points made the fallback practically
     * inert there, so inspect enough loaded columns to find the valid part of
     * the same annulus without relaxing any spawn rule.
     */
    static final int POSITION_ATTEMPTS = 48;
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

    static boolean isWithinPlayerSpawnAnnulus(int offsetX, int offsetZ) {
        long distanceSquared = (long) offsetX * offsetX
                + (long) offsetZ * offsetZ;
        return distanceSquared >= (long) MIN_PLAYER_DISTANCE
                        * MIN_PLAYER_DISTANCE
                && distanceSquared <= (long) MAX_PLAYER_DISTANCE
                        * MAX_PLAYER_DISTANCE;
    }
}
