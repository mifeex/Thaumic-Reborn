package com.thaumcraftmodern.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.Map;

/**
 * Activates preserved TC4 scan records only when their modern runtime target
 * now exists. The original aspect arrays and provenance objects remain
 * untouched, so no aspect values are silently reinterpreted.
 */
final class LegacyScanMappings {
    private static final Map<String, String> ENTITIES = Map.ofEntries(
            Map.entry("Thaumcraft.Firebat", "thaumic_reborn:firebat"),
            Map.entry("Thaumcraft.Pech", "thaumic_reborn:pech"),
            Map.entry("Thaumcraft.ThaumSlime", "thaumic_reborn:thaumic_slime"),
            Map.entry("Thaumcraft.BrainyZombie", "thaumic_reborn:angry_zombie"),
            Map.entry("Thaumcraft.GiantBrainyZombie", "thaumic_reborn:furious_zombie"),
            Map.entry("Thaumcraft.Taintacle", "thaumic_reborn:taintacle"),
            Map.entry("Thaumcraft.TaintacleTiny", "thaumic_reborn:taint_tendril"),
            Map.entry("Thaumcraft.TaintSpider", "thaumic_reborn:tainted_crawler"),
            Map.entry("Thaumcraft.TaintSpore", "thaumic_reborn:taint_spore"),
            Map.entry("Thaumcraft.TaintSwarmer", "thaumic_reborn:taint_spore_swarmer"),
            Map.entry("Thaumcraft.TaintSwarm", "thaumic_reborn:taint_swarm"),
            Map.entry("Thaumcraft.TaintedPig", "thaumic_reborn:tainted_pig"),
            Map.entry("Thaumcraft.TaintedSheep", "thaumic_reborn:tainted_sheep"),
            Map.entry("Thaumcraft.TaintedCow", "thaumic_reborn:tainted_cow"),
            Map.entry("Thaumcraft.TaintedChicken", "thaumic_reborn:tainted_chicken"),
            Map.entry("Thaumcraft.TaintedVillager", "thaumic_reborn:tainted_villager"),
            Map.entry("Thaumcraft.TaintedCreeper", "thaumic_reborn:tainted_creeper"),
            Map.entry("Thaumcraft.MindSpider", "thaumic_reborn:mind_spider"),
            Map.entry("Thaumcraft.EldritchGuardian", "thaumic_reborn:eldritch_guardian"),
            Map.entry("Thaumcraft.CultistKnight", "thaumic_reborn:crimson_knight"),
            Map.entry("Thaumcraft.CultistCleric", "thaumic_reborn:crimson_cleric"),
            Map.entry("Thaumcraft.Wisp", "thaumic_reborn:wisp")
    );

    private static final Map<String, Target> OBJECTS = Map.ofEntries(
            block("new ItemStack(ConfigBlocks.blockCustomOre, 1, 0)", "cinnabar_ore"),
            block("new ItemStack(ConfigBlocks.blockCustomOre, 1, 1)", "air_infused_stone"),
            block("new ItemStack(ConfigBlocks.blockCustomOre, 1, 2)", "fire_infused_stone"),
            block("new ItemStack(ConfigBlocks.blockCustomOre, 1, 3)", "water_infused_stone"),
            block("new ItemStack(ConfigBlocks.blockCustomOre, 1, 4)", "earth_infused_stone"),
            block("new ItemStack(ConfigBlocks.blockCustomOre, 1, 5)", "order_infused_stone"),
            block("new ItemStack(ConfigBlocks.blockCustomOre, 1, 6)", "entropy_infused_stone"),
            block("new ItemStack(ConfigBlocks.blockCustomOre, 1, 7)", "amber_ore"),
            block("new ItemStack(ConfigBlocks.blockMagicalLog, 1, 0)", "greatwood_log"),
            block("new ItemStack(ConfigBlocks.blockMagicalLog, 1, 1)", "silverwood_log"),
            block("new ItemStack(ConfigBlocks.blockMagicalLeaves, 1, 0)", "greatwood_leaves"),
            block("new ItemStack(ConfigBlocks.blockMagicalLeaves, 1, 1)", "silverwood_leaves"),
            block("new ItemStack(ConfigBlocks.blockCustomPlant, 1, 0)", "greatwood_sapling"),
            block("new ItemStack(ConfigBlocks.blockCustomPlant, 1, 1)", "silverwood_sapling"),
            block("new ItemStack(ConfigBlocks.blockCustomPlant, 1, 2)", "shimmerleaf"),
            block("new ItemStack(ConfigBlocks.blockCustomPlant, 1, 3)", "cinderpearl"),
            block("new ItemStack(ConfigBlocks.blockCustomPlant, 1, 5)", "vishroom"),
            block("new ItemStack(ConfigBlocks.blockTaint, 1, 0)", "crusted_taint"),
            block("new ItemStack(ConfigBlocks.blockTaint, 1, 1)", "tainted_soil"),
            block("new ItemStack(ConfigBlocks.blockTaintFibres, 1, 0)", "taint_fibres"),
            block("new ItemStack(ConfigBlocks.blockTaintFibres, 1, 1)", "short_tainted_grass"),
            block("new ItemStack(ConfigBlocks.blockTaintFibres, 1, 2)", "tall_tainted_grass"),
            block("new ItemStack(ConfigBlocks.blockTaintFibres, 1, 3)", "spore_stalk"),
            block("new ItemStack(ConfigBlocks.blockTaintFibres, 1, 4)", "mature_spore_stalk"),
            item("new ItemStack(ConfigItems.itemResource, 1, 3)", "quicksilver"),
            item("new ItemStack(ConfigItems.itemNugget, 1, 5)", "quicksilver_nugget"),
            item("new ItemStack(ConfigItems.itemResource, 1, 6)", "amber"),
            item("new ItemStack(ConfigItems.itemResource, 1, 11)", "tainted_goo"),
            item("new ItemStack(ConfigItems.itemResource, 1, 12)", "taint_tendril"),
            item("new ItemStack(ConfigItems.itemResource, 1, 18)", "gold_coin"),
            item("new ItemStack(ConfigItems.itemResource, 1, 17)", "void_seed"),
            item("new ItemStack(ConfigItems.itemZombieBrain)", "zombie_brain"),
            item("new ItemStack(ConfigItems.itemWispEssence, 1, 0)", "ethereal_essence"),
            item("new ItemStack(ConfigItems.itemEldritchObject, 1, 0)", "eldritch_eye"),
            item("new ItemStack(ConfigItems.itemEldritchObject, 1, 1)", "crimson_rites"),
            item("new ItemStack(ConfigItems.itemEldritchObject, 1, 2)", "runed_tablet"),
            item("new ItemStack(ConfigItems.itemEldritchObject, 1, 3)", "primordial_pearl"),
            item("new ItemStack(ConfigItems.itemLootbag, 1, 0)", "common_loot_bag"),
            item("new ItemStack(ConfigItems.itemLootbag, 1, 1)", "uncommon_loot_bag"),
            item("new ItemStack(ConfigItems.itemLootbag, 1, 2)", "rare_loot_bag")
    );

    private LegacyScanMappings() {
    }

    static JsonObject map(JsonObject original) {
        String type = GsonHelper.getAsString(original, "type", "");
        String target = GsonHelper.getAsString(original, "target", "");
        if ("legacy_entity".equals(type)) {
            String modern = ENTITIES.get(target);
            if (modern == null || !supportedEntityVariant(original, target)) {
                return original;
            }
            JsonObject mapped = original.deepCopy();
            mapped.addProperty("type", "entity");
            mapped.addProperty("target", modern);
            mapped.addProperty("inactive", false);
            return mapped;
        }
        if ("legacy_object".equals(type)) {
            Target modern = OBJECTS.get(target);
            if (modern == null) {
                return original;
            }
            JsonObject mapped = original.deepCopy();
            mapped.addProperty("type", modern.type());
            mapped.addProperty("target", modern.id());
            mapped.addProperty("inactive", false);
            return mapped;
        }
        return original;
    }

    private static boolean supportedEntityVariant(
            JsonObject json,
            String target
    ) {
        if (!"Thaumcraft.Pech".equals(target)) {
            return true;
        }
        JsonObject legacy = json.has("legacy")
                ? GsonHelper.getAsJsonObject(json, "legacy")
                : null;
        if (legacy == null || !legacy.has("nbt_conditions_raw")) {
            return true;
        }
        JsonArray conditions = GsonHelper.getAsJsonArray(
                legacy,
                "nbt_conditions_raw"
        );
        for (JsonElement condition : conditions) {
            if (condition.getAsString().contains("(byte)0")) {
                return true;
            }
        }
        return conditions.isEmpty();
    }

    private static Map.Entry<String, Target> block(
            String legacy,
            String modern
    ) {
        return Map.entry(
                legacy,
                new Target("block", "thaumic_reborn:" + modern)
        );
    }

    private static Map.Entry<String, Target> item(
            String legacy,
            String modern
    ) {
        return Map.entry(
                legacy,
                new Target("item", "thaumic_reborn:" + modern)
        );
    }

    private record Target(String type, String id) {
    }
}
