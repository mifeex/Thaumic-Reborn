package com.thaumcraftmodern.worldgen;

/**
 * User-facing Magical Forest density and climate rules kept independent from
 * Minecraft registry bootstrap so they can be regression-tested directly.
 */
public final class MagicalForestGenerationPolicy {
    /*
     * Deliberate density adjustment from TC4's 10 / 14 / 10 profile.
     * Fewer total attempts leave breathing room between magic oaks, while
     * stronger rare-tree rolls make Silverwood and Greatwood landmarks more
     * visible despite modern large-canopy clearance failures.
     */
    static final int TREE_ATTEMPTS = 9;
    static final int SILVERWOOD_CHANCE = 9;
    static final int SILVERWOOD_SITE_ATTEMPTS = 12;
    static final int SILVERWOOD_GROUND_RADIUS = 2;
    static final int SILVERWOOD_MIN_GROUND_PERCENT = 80;
    static final int GREATWOOD_CHANCE_AFTER_SILVERWOOD = 7;
    static final int BOULDER_VARIANTS = 3;
    static final int GIANT_MUSHROOM_GRID_SIZE = 4;
    static final int GIANT_MUSHROOM_CHANCE = 40;
    static final int MANA_POD_ATTEMPTS = 10;
    static final int VISHROOM_ATTEMPTS = 8;
    static final int SHIMMERLEAF_ATTEMPTS = 18;
    static final int FLOWER_ATTEMPTS = 2;
    static final int GRASS_ATTEMPTS = 12;
    static final int FERN_CHANCE = 4;
    static final int NORMAL_MUSHROOM_ATTEMPTS = 6;
    static final int REED_ATTEMPTS = 6;

    private MagicalForestGenerationPolicy() {
    }

    public static boolean supportsClimate(
            float baseTemperature,
            boolean coldTagged
    ) {
        return !coldTagged
                && baseTemperature >= 0.45F
                && baseTemperature <= 1.20F;
    }

    static int silverwoodClearanceRadius(int y, int height) {
        return y >= height - 1 ? 3 : 1;
    }
}
