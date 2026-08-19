package com.thaumcraftmodern.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Server-owned compatibility settings.
 */
public final class ThaumcraftModernServerConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue AUTOMATIC_SCAN_FALLBACK;
    private static final ForgeConfigSpec.BooleanValue GENERATE_ORES;
    private static final ForgeConfigSpec.BooleanValue GENERATE_TREES;
    private static final ForgeConfigSpec.BooleanValue GENERATE_PLANTS;
    private static final ForgeConfigSpec.BooleanValue GENERATE_STRUCTURES;
    private static final ForgeConfigSpec.IntValue CINNABAR_ATTEMPTS;
    private static final ForgeConfigSpec.IntValue AMBER_ATTEMPTS;
    private static final ForgeConfigSpec.IntValue INFUSED_STONE_ATTEMPTS;
    private static final ForgeConfigSpec.IntValue GREATWOOD_RARITY;
    private static final ForgeConfigSpec.IntValue SILVERWOOD_RARITY;
    private static final ForgeConfigSpec.IntValue STRUCTURE_RARITY_SCALE;
    private static final ForgeConfigSpec.IntValue TAINT_SPREAD_RATE;
    private static final ForgeConfigSpec.BooleanValue TAINT_FROM_FLUX;
    private static final ForgeConfigSpec.BooleanValue HARD_NODES;
    private static final ForgeConfigSpec.BooleanValue SPAWN_ANGRY_ZOMBIES;
    private static final ForgeConfigSpec.BooleanValue SPAWN_FIREBATS;
    private static final ForgeConfigSpec.BooleanValue SPAWN_WISPS;
    private static final ForgeConfigSpec.BooleanValue SPAWN_TAINT_CREATURES;
    private static final ForgeConfigSpec.BooleanValue SPAWN_PECH;
    private static final ForgeConfigSpec.BooleanValue SPAWN_ELDRITCH_CREATURES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("scanning");
        AUTOMATIC_SCAN_FALLBACK = builder
                .comment(
                        "Compatibility mode: infer aspects for targets without an explicit TC4-derived scan definition.",
                        "Disabled by default because inferred aspects are not faithful Thaumcraft 4 data.",
                        "Changing this does not remove scan keys already saved in player knowledge."
                )
                .define("automaticScanFallback", false);
        builder.pop();

        builder.push("worldGeneration");
        GENERATE_ORES = builder
                .comment("Generate Cinnabar, Amber and all six Infused Stone variants.")
                .define("generateOres", true);
        GENERATE_TREES = builder
                .comment("Generate Greatwood and Silverwood trees.")
                .define("generateTrees", true);
        GENERATE_PLANTS = builder
                .comment("Generate Shimmerleaf, Cinderpearl, Vishrooms and Mana Pods.")
                .define("generatePlants", true);
        GENERATE_STRUCTURES = builder
                .comment("Generate TC4 surface mounds, stone rings, hilltop stones and aura totems.")
                .define("generateStructures", true);
        CINNABAR_ATTEMPTS = builder
                .comment("Single-block Cinnabar attempts per chunk. TC4 default: 18.")
                .defineInRange("cinnabarAttemptsPerChunk", 18, 0, 128);
        AMBER_ATTEMPTS = builder
                .comment("Near-surface Amber attempts per chunk. TC4 default: 20.")
                .defineInRange("amberAttemptsPerChunk", 20, 0, 128);
        INFUSED_STONE_ATTEMPTS = builder
                .comment(
                        "Base Infused Stone vein attempts per vertical band. TC4 default: 8.",
                        "Modern generation applies 90% in stone (Y >= 0) and 80%",
                        "in deepslate (Y < 0), preserving density across the taller world."
                )
                .defineInRange("infusedStoneAttemptsPerChunk", 8, 0, 64);
        GREATWOOD_RARITY = builder
                .comment("Base chance denominator for a Greatwood attempt. TC4 default: 25.")
                .defineInRange("greatwoodRarity", 25, 1, 4096);
        SILVERWOOD_RARITY = builder
                .comment("Base chance denominator for a Silverwood attempt. TC4 default: 60.")
                .defineInRange("silverwoodRarity", 60, 1, 4096);
        STRUCTURE_RARITY_SCALE = builder
                .comment(
                        "Multiplier applied to TC4 standalone structure rarity.",
                        "1 keeps the configured boosted defaults; larger values make",
                        "mounds, rings, hilltop stones and aura totems rarer.",
                        "Village building weights are not affected."
                )
                .defineInRange("structureRarityScale", 1, 1, 64);
        TAINT_SPREAD_RATE = builder
                .comment(
                        "Taint biome spread divisor. TC4 default: 200; effective random-tick chance is 1/(value*5).",
                        "Set to 0 to disable biome spread."
                )
                .defineInRange("taintSpreadRate", 200, 0, 100000);
        TAINT_FROM_FLUX = builder
                .comment("Allow full Flux Goo to create Tainted Land. TC4 default: true.")
                .define("taintFromFlux", true);
        HARD_NODES = builder
                .comment("Allow Tainted Nodes to seed fibrous taint. TC4 default: true.")
                .define("hardModeNodes", true);
        builder.pop();

        builder.push("monsterSpawning");
        SPAWN_ANGRY_ZOMBIES = builder
                .define("spawnAngryZombies", true);
        SPAWN_FIREBATS = builder.define("spawnFirebats", true);
        SPAWN_WISPS = builder.define("spawnWisps", true);
        SPAWN_TAINT_CREATURES = builder.define("spawnTaintCreatures", true);
        SPAWN_PECH = builder.define("spawnPech", true);
        SPAWN_ELDRITCH_CREATURES = builder
                .define("spawnEldritchCreatures", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumcraftModernServerConfig() {
    }

    public static boolean automaticScanFallback() {
        try {
            return AUTOMATIC_SCAN_FALLBACK.get();
        } catch (IllegalStateException configNotLoadedYet) {
            /*
             * Unit tests and very early registry bootstrap run before Forge
             * attaches a concrete server config. Fidelity mode is the safe
             * fallback in that state.
             */
            return false;
        }
    }

    public static boolean generateOres() {
        return bool(GENERATE_ORES, true);
    }

    public static boolean generateTrees() {
        return bool(GENERATE_TREES, true);
    }

    public static boolean generatePlants() {
        return bool(GENERATE_PLANTS, true);
    }

    public static boolean generateStructures() {
        return bool(GENERATE_STRUCTURES, true);
    }

    public static boolean generateTc4Biomes() {
        return ThaumicOverworldConfig.generateTc4Biomes();
    }

    public static int cinnabarAttempts() {
        return integer(CINNABAR_ATTEMPTS, 18);
    }

    public static int amberAttempts() {
        return integer(AMBER_ATTEMPTS, 20);
    }

    public static int infusedStoneAttempts() {
        return integer(INFUSED_STONE_ATTEMPTS, 8);
    }

    public static int greatwoodRarity() {
        return integer(GREATWOOD_RARITY, 25);
    }

    public static int silverwoodRarity() {
        return integer(SILVERWOOD_RARITY, 60);
    }

    public static int structureRarityScale() {
        return integer(STRUCTURE_RARITY_SCALE, 1);
    }

    public static int magicalForestWeight() {
        return ThaumicOverworldConfig.magicalForestWeight();
    }

    public static int taintedLandsWeight() {
        return ThaumicOverworldConfig.taintedLandsWeight();
    }

    public static int taintSpreadRate() {
        return integer(TAINT_SPREAD_RATE, 200);
    }

    public static boolean taintFromFlux() {
        return bool(TAINT_FROM_FLUX, true);
    }

    public static boolean hardNodes() {
        return bool(HARD_NODES, true);
    }

    public static boolean spawnAngryZombies() {
        return bool(SPAWN_ANGRY_ZOMBIES, true);
    }

    public static boolean spawnFirebats() {
        return bool(SPAWN_FIREBATS, true);
    }

    public static boolean spawnWisps() {
        return bool(SPAWN_WISPS, true);
    }

    public static boolean spawnTaintCreatures() {
        return bool(SPAWN_TAINT_CREATURES, true);
    }

    public static boolean spawnPech() {
        return bool(SPAWN_PECH, true);
    }

    public static boolean spawnEldritchCreatures() {
        return bool(SPAWN_ELDRITCH_CREATURES, true);
    }

    private static boolean bool(
            ForgeConfigSpec.BooleanValue value,
            boolean fallback
    ) {
        try {
            return value.get();
        } catch (IllegalStateException configNotLoadedYet) {
            return fallback;
        }
    }

    private static int integer(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            return value.get();
        } catch (IllegalStateException configNotLoadedYet) {
            return fallback;
        }
    }
}
