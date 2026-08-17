package com.thaumcraftmodern.infusion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RestoredInfusionRecipeCoverageTest {
    private static final Path RECIPES = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes");
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy");

    @Test
    void everySupportedTc4InfusionHasAnExecutableRecipeAndMatchingBookPage()
            throws Exception {
        for (Spec spec : specs()) {
            JsonObject recipe = json(RECIPES.resolve(spec.id() + ".json"));
            assertEquals(spec.research(), recipe.get("research").getAsString(), spec.id());
            assertEquals(spec.instability(), recipe.get("instability").getAsInt(), spec.id());
            assertEquals(spec.central(), item(recipe.getAsJsonObject("central")), spec.id());
            assertEquals(spec.components(), items(recipe.getAsJsonArray("components")), spec.id());
            assertEquals(spec.essentia(), costs(recipe.getAsJsonObject("essentia")), spec.id());
            assertEquals(spec.output(), item(recipe.getAsJsonObject("result")), spec.id());

            JsonObject research = json(RESEARCH.resolve(spec.research() + ".json"));
            assertFalse(research.get("inactive").getAsBoolean(), spec.research());
            JsonObject page = infusionPage(research, spec.output());
            assertEquals(spec.central(), page.get("central").getAsString(), spec.research());
            assertEquals(spec.components(), pageItems(page.getAsJsonArray("components")),
                    spec.research());
            assertEquals(spec.essentia(), pageCosts(page.getAsJsonArray("aspect_costs")),
                    spec.research());
            assertEquals(instabilityLabel(spec.instability()),
                    page.get("instability").getAsString(), spec.research());
        }
    }

    private static List<Spec> specs() {
        return List.of(
                spec("wand_cap_silver", "cap_silver", 4,
                        "thaumic_reborn:inert_silver_wand_cap",
                        List.of("thaumic_reborn:salis_mundus", "thaumic_reborn:salis_mundus"),
                        "thaumic_reborn:silver_wand_cap", map("potentia", 8, "auram", 4)),
                spec("wand_cap_thaumium", "cap_thaumium", 5,
                        "thaumic_reborn:inert_thaumium_wand_cap",
                        List.of("thaumic_reborn:salis_mundus", "thaumic_reborn:salis_mundus",
                                "thaumic_reborn:salis_mundus"),
                        "thaumic_reborn:thaumium_wand_cap", map("potentia", 12, "auram", 6)),
                spec("wand_cap_void", "cap_void", 8,
                        "thaumic_reborn:inert_void_wand_cap",
                        List.of("thaumic_reborn:salis_mundus", "thaumic_reborn:salis_mundus",
                                "thaumic_reborn:salis_mundus", "thaumic_reborn:salis_mundus"),
                        "thaumic_reborn:void_wand_cap",
                        map("potentia", 18, "vacuos", 18, "alienis", 18, "auram", 18)),
                spec("focus_hellbat", "focushellbat", 3,
                        "minecraft:magma_cream",
                        List.of("minecraft:quartz", "thaumic_reborn:fire_shard",
                                "minecraft:quartz", "thaumic_reborn:air_shard",
                                "minecraft:quartz", "thaumic_reborn:entropy_shard"),
                        "thaumic_reborn:focus_hellbat",
                        map("ignis", 25, "aer", 15, "bestia", 15, "perditio", 25)),
                spec("focus_portable_hole", "focusportablehole", 3,
                        "minecraft:ender_pearl",
                        List.of("minecraft:quartz", "thaumic_reborn:water_shard",
                                "minecraft:quartz", "thaumic_reborn:air_shard",
                                "minecraft:quartz", "thaumic_reborn:entropy_shard"),
                        "thaumic_reborn:focus_portable_hole",
                        map("iter", 25, "alienis", 10, "permutatio", 10,
                                "perditio", 25)),
                spec("focus_warding", "focuswarding", 4,
                        "minecraft:nether_star",
                        List.of("thaumic_reborn:quicksilver",
                                "thaumic_reborn:water_shard", "minecraft:quartz",
                                "thaumic_reborn:order_shard",
                                "thaumic_reborn:quicksilver",
                                "thaumic_reborn:water_shard", "minecraft:quartz",
                                "thaumic_reborn:order_shard"),
                        "thaumic_reborn:focus_warding",
                        map("terra", 25, "tutamen", 25, "ordo", 25,
                                "cognitio", 10)),
                elementalRod("wand_rod_obsidian", "rod_obsidian", "minecraft:obsidian",
                        "thaumic_reborn:earth_shard", "thaumic_reborn:obsidian_wand_rod",
                        map("terra", 12, "praecantatio", 6, "tenebrae", 6)),
                elementalRod("wand_rod_ice", "rod_ice", "minecraft:packed_ice",
                        "thaumic_reborn:water_shard", "thaumic_reborn:ice_wand_rod",
                        map("aqua", 12, "praecantatio", 6, "gelum", 6)),
                elementalRod("wand_rod_quartz", "rod_quartz", "minecraft:quartz_block",
                        "thaumic_reborn:order_shard", "thaumic_reborn:quartz_wand_rod",
                        map("ordo", 12, "praecantatio", 6, "vitreus", 6)),
                elementalRod("wand_rod_reed", "rod_reed", "minecraft:sugar_cane",
                        "thaumic_reborn:air_shard", "thaumic_reborn:reed_wand_rod",
                        map("aer", 12, "praecantatio", 6, "motus", 6)),
                elementalRod("wand_rod_blaze", "rod_blaze", "minecraft:blaze_rod",
                        "thaumic_reborn:fire_shard", "thaumic_reborn:blaze_wand_rod",
                        map("ignis", 12, "praecantatio", 6, "bestia", 6)),
                elementalRod("wand_rod_bone", "rod_bone", "minecraft:bone",
                        "thaumic_reborn:entropy_shard", "thaumic_reborn:bone_wand_rod",
                        map("perditio", 12, "praecantatio", 6, "exanimis", 6)),
                spec("wand_rod_silverwood", "rod_silverwood", 5,
                        "thaumic_reborn:silverwood_log",
                        List.of("thaumic_reborn:balanced_shard", "thaumic_reborn:air_shard",
                                "thaumic_reborn:fire_shard", "thaumic_reborn:water_shard",
                                "thaumic_reborn:earth_shard", "thaumic_reborn:order_shard",
                                "thaumic_reborn:entropy_shard"),
                        "thaumic_reborn:silverwood_wand_rod",
                        map("aer", 9, "ignis", 9, "aqua", 9, "terra", 9, "ordo", 9,
                                "perditio", 9, "praecantatio", 9)),
                spec("wand_rod_primal_staff", "rod_primal_staff", 8,
                        "thaumic_reborn:silverwood_wand_rod",
                        List.of("thaumic_reborn:primal_charm", "thaumic_reborn:obsidian_wand_rod",
                                "thaumic_reborn:ice_wand_rod", "thaumic_reborn:quartz_wand_rod",
                                "thaumic_reborn:primal_charm", "thaumic_reborn:reed_wand_rod",
                                "thaumic_reborn:blaze_wand_rod", "thaumic_reborn:bone_wand_rod"),
                        "thaumic_reborn:primal_staff_rod",
                        map("aer", 32, "ignis", 32, "aqua", 32, "terra", 32, "ordo", 32,
                                "perditio", 32, "praecantatio", 64)),
                spec("advanced_node_stabilizer", "nodestabilizeradv", 10,
                        "thaumic_reborn:node_stabilizer",
                        List.of("thaumic_reborn:nitor", "minecraft:redstone_block",
                                "thaumic_reborn:alumentum", "minecraft:redstone_block",
                                "thaumic_reborn:nitor", "minecraft:redstone_block",
                                "thaumic_reborn:alumentum", "minecraft:redstone_block"),
                        "thaumic_reborn:advanced_node_stabilizer",
                        map("auram", 32, "praecantatio", 16, "ordo", 16, "potentia", 16)),
                spec("essentia_reservoir", "essentiareservoir", 6,
                        "thaumic_reborn:essentia_buffer",
                        List.of("thaumic_reborn:void_metal_ingot", "thaumic_reborn:warded_jar",
                                "thaumic_reborn:warded_jar", "thaumic_reborn:void_metal_ingot",
                                "thaumic_reborn:warded_jar", "thaumic_reborn:warded_jar"),
                        "thaumic_reborn:essentia_reservoir",
                        map("aqua", 8, "vacuos", 8, "praecantatio", 8, "permutatio", 8)),
                spec("sanity_checker", "sanitycheck", 4, "thaumic_reborn:thaumometer",
                        List.of("thaumic_reborn:mirrored_glass", "thaumic_reborn:zombie_brain",
                                "minecraft:diamond"), "thaumic_reborn:sanity_checker",
                        map("cognitio", 24, "sensus", 24, "alienis", 8)),
                spec("wand_recharge_pedestal", "wandped", 3,
                        "thaumic_reborn:arcane_pedestal",
                        List.of("minecraft:gold_ingot", "minecraft:diamond",
                                "thaumic_reborn:primal_charm", "minecraft:diamond"),
                        "thaumic_reborn:wand_recharge_pedestal",
                        map("auram", 10, "praecantatio", 15, "permutatio", 15)),
                spec("compound_recharge_focus", "wandpedfoc", 4, "minecraft:comparator",
                        List.of("thaumic_reborn:order_shard", "thaumic_reborn:vis_filter",
                                "thaumic_reborn:order_shard", "thaumic_reborn:vis_filter",
                                "thaumic_reborn:order_shard", "thaumic_reborn:vis_filter",
                                "thaumic_reborn:order_shard", "thaumic_reborn:vis_filter"),
                        "thaumic_reborn:compound_recharge_focus",
                        map("ordo", 10, "praecantatio", 15, "permutatio", 10)),
                spec("brain_jar", "jarbrain", 4,
                        "thaumic_reborn:warded_jar",
                        List.of("thaumic_reborn:zombie_brain", "minecraft:spider_eye",
                                "minecraft:water_bucket", "minecraft:spider_eye"),
                        "thaumic_reborn:brain_jar",
                        map("cognitio", 10, "sensus", 10, "exanimis", 20)),
                spec("sinister_lodestone", "sinstone", 5,
                        "minecraft:flint",
                        List.of("thaumic_reborn:quicksilver", "thaumic_reborn:order_shard",
                                "thaumic_reborn:salis_mundus",
                                "thaumic_reborn:entropy_shard"),
                        "thaumic_reborn:sinister_lodestone",
                        map("sensus", 8, "tenebrae", 8, "alienis", 8, "auram", 8))
        );
    }

    private static Spec elementalRod(String id, String research, String central,
            String shard, String output, Map<String, Integer> essentia) {
        return spec(id, research, 3, central,
                List.of("thaumic_reborn:balanced_shard", shard), output, essentia);
    }

    private static Spec spec(String id, String research, int instability, String central,
            List<String> components, String output, Map<String, Integer> essentia) {
        return new Spec(id, research, instability, central, components, output, essentia);
    }

    private static Map<String, Integer> map(Object... entries) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((String) entries[index], (Integer) entries[index + 1]);
        }
        return result;
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static String item(JsonObject ingredient) {
        return ingredient.get("item").getAsString();
    }

    private static List<String> items(JsonArray array) {
        List<String> result = new ArrayList<>();
        array.forEach(element -> result.add(item(element.getAsJsonObject())));
        return result;
    }

    private static List<String> pageItems(JsonArray array) {
        List<String> result = new ArrayList<>();
        array.forEach(element -> result.add(
                element.getAsJsonObject().get("item").getAsString()));
        return result;
    }

    private static Map<String, Integer> costs(JsonObject object) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        object.entrySet().forEach(entry -> result.put(
                entry.getKey(), entry.getValue().getAsInt()));
        return result;
    }

    private static Map<String, Integer> pageCosts(JsonArray array) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        array.forEach(element -> {
            JsonObject cost = element.getAsJsonObject();
            result.put(cost.get("id").getAsString(), cost.get("amount").getAsInt());
        });
        return result;
    }

    private static JsonObject infusionPage(JsonObject research, String output) {
        for (var element : research.getAsJsonArray("pages")) {
            JsonObject page = element.getAsJsonObject();
            if ("infusion".equals(page.get("type").getAsString())
                    && output.equals(page.get("output").getAsString())) {
                return page;
            }
        }
        throw new AssertionError("Missing infusion page for " + output);
    }

    private static String instabilityLabel(int instability) {
        return List.of("negligible", "minor", "moderate", "high", "very_high", "dangerous")
                .get(Math.min(5, Math.max(0, instability / 2)));
    }

    private record Spec(String id, String research, int instability, String central,
                        List<String> components, String output,
                        Map<String, Integer> essentia) {
    }
}
