package com.thaumcraftmodern.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Worldgen-bootstrap settings that must be available before a level's
 * per-world SERVER config is loaded.
 */
public final class ThaumicOverworldConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue GENERATE_TC4_BIOMES;
    private static final ForgeConfigSpec.IntValue MAGICAL_FOREST_WEIGHT;
    private static final ForgeConfigSpec.IntValue TAINTED_LANDS_WEIGHT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("worldGeneration");
        GENERATE_TC4_BIOMES = builder
                .comment(
                        "Enable whole Magical Forest and Tainted Land surface regions in standard Overworld generation.",
                        "Eerie remains dynamic and is painted by sinister aura nodes.",
                        "This is a startup setting because Minecraft builds its climate table before loading a world's server config."
                )
                .define("generateBiomes", true);
        MAGICAL_FOREST_WEIGHT = builder
                .comment("Whole-region occurrence weight. TC4 default biome weight: 5.")
                .defineInRange("magicalForestWeight", 5, 0, 100);
        TAINTED_LANDS_WEIGHT = builder
                .comment("Whole-region occurrence weight. TC4 default biome weight: 2.")
                .defineInRange("taintedLandsWeight", 2, 0, 100);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumicOverworldConfig() {
    }

    public static boolean generateTc4Biomes() {
        try {
            return GENERATE_TC4_BIOMES.get();
        } catch (IllegalStateException configNotLoadedYet) {
            return true;
        }
    }

    public static int magicalForestWeight() {
        return integer(MAGICAL_FOREST_WEIGHT, 5);
    }

    public static int taintedLandsWeight() {
        return integer(TAINTED_LANDS_WEIGHT, 2);
    }

    private static int integer(
            ForgeConfigSpec.IntValue value,
            int fallback
    ) {
        try {
            return value.get();
        } catch (IllegalStateException configNotLoadedYet) {
            return fallback;
        }
    }
}
