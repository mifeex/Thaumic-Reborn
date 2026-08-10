package com.thaumcraftmodern.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RestoredItemScanFidelityTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path SCANS = ROOT.resolve(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/scans"
    );

    @Test
    void legacyDyeItemsRetainIndependentSensusScans() throws IOException {
        Set<String> targets = Set.of(
                "black_dye", "blue_dye", "brown_dye", "cyan_dye",
                "gray_dye", "green_dye", "light_blue_dye",
                "light_gray_dye", "lime_dye", "magenta_dye", "orange_dye",
                "pink_dye", "purple_dye", "red_dye", "white_dye",
                "yellow_dye", "lapis_lazuli", "ink_sac", "cocoa_beans",
                "bone_meal"
        );

        for (String target : targets) {
            JsonObject scan = json(SCANS.resolve(
                    "vanilla_dyes/" + target + ".json"
            ));
            assertEquals("item", scan.get("type").getAsString());
            assertEquals("minecraft:" + target, scan.get("target").getAsString());
            assertEquals(Map.of("sensus", 1), aspects(scan));
        }

        assertEquals(
                targets.size(),
                targets.stream().map(target -> "item:minecraft:" + target)
                        .distinct().count(),
                "every split 1.20.1 dye identity must have its own scan key"
        );
    }

    @Test
    void directlyRegisteredThaumcraftObjectsMatchOriginalAspects()
            throws IOException {
        assertScan("thaumometer.json", "item", "thaumcraftmodern:thaumometer",
                Map.of("sensus", 3, "metallum", 2, "vitreus", 1,
                        "praecantatio", 1));
        assertScan("scribing_tools.json", "item",
                "thaumcraftmodern:scribing_tools",
                Map.of("aqua", 1, "tenebrae", 1, "instrumentum", 1));
        assertScan("thaumium_block.json", "block",
                "thaumcraftmodern:thaumium_block",
                Map.of("metallum", 8, "praecantatio", 2));
        assertScan("flesh_block.json", "block",
                "thaumcraftmodern:flesh_block",
                Map.of("corpus", 4, "lux", 1, "praecantatio", 1));
    }

    @Test
    void vanillaToolScansMatchTheTwentyFiveDirectTc4Registrations()
            throws IOException {
        assertToolSet("iron", "metallum", 3, 3, 2, 2,
                Map.of("pickaxe", 3, "axe", 3, "shovel", 2, "sword", 3,
                        "hoe", 2));
        assertToolSet("diamond", "vitreus", 5, 4, 3, 3,
                Map.of("pickaxe", 4, "axe", 4, "shovel", 3, "sword", 4,
                        "hoe", 3));
        assertToolSet("golden", "metallum", 4, 4, 4, 4,
                Map.of("pickaxe", 2, "axe", 2, "shovel", 1, "sword", 2,
                        "hoe", 1));
        assertToolSet("stone", "terra", 3, 3, 2, 2,
                Map.of("pickaxe", 2, "axe", 2, "shovel", 1, "sword", 2,
                        "hoe", 1));
        assertToolSet("wooden", "arbor", 3, 3, 2, 2,
                Map.of("pickaxe", 1, "axe", 1, "shovel", 1, "sword", 1,
                        "hoe", 1));
    }

    @Test
    void vanillaArmorIncludesClassicMaterialAndProtectionAspects()
            throws IOException {
        assertScan("vanilla_armor/leather_helmet.json", "item",
                "minecraft:leather_helmet", Map.of("bestia", 7, "corpus", 3,
                        "tutamen", 1));
        assertScan("vanilla_armor/leather_chestplate.json", "item",
                "minecraft:leather_chestplate", Map.of("bestia", 12, "corpus", 6,
                        "tutamen", 3));
        assertScan("vanilla_armor/leather_leggings.json", "item",
                "minecraft:leather_leggings", Map.of("bestia", 10, "corpus", 5,
                        "tutamen", 2));
        assertScan("vanilla_armor/leather_boots.json", "item",
                "minecraft:leather_boots", Map.of("bestia", 6, "corpus", 3,
                        "tutamen", 1));

        assertArmorSet("iron", "metallum", 15, 24, 21, 12, 2, 6, 5, 2);
        assertArmorSet("golden", "metallum", 22, 36, 31, 18, 2, 5, 3, 1);
        assertArmorSet("diamond", "vitreus", 30, 48, 42, 24, 3, 8, 6, 3);
        assertArmorSet("netherite", "vitreus", 31, 49, 43, 25, 4, 9, 7, 4);

        assertScan("legacy/object_163_new_itemstack_items.field_151020_u_1_32767.json",
                "item", "minecraft:chainmail_helmet",
                Map.of("metallum", 8, "tutamen", 2));
        assertScan("legacy/object_164_new_itemstack_items.field_151023_v_1_32767.json",
                "item", "minecraft:chainmail_chestplate",
                Map.of("metallum", 12, "tutamen", 5));
        assertScan("legacy/object_165_new_itemstack_items.field_151022_w_1_32767.json",
                "item", "minecraft:chainmail_leggings",
                Map.of("metallum", 11, "tutamen", 4));
        assertScan("legacy/object_166_new_itemstack_items.field_151029_x_1_32767.json",
                "item", "minecraft:chainmail_boots",
                Map.of("metallum", 7, "tutamen", 1));
    }

    @Test
    void netheriteToolsAreDiamondToolAspectsPlusOneEach() throws IOException {
        for (String tool : List.of("pickaxe", "axe", "shovel", "hoe", "sword")) {
            Map<String, Integer> diamond = aspects(json(SCANS.resolve(
                    "vanilla_tools/diamond_" + tool + ".json")));
            Map<String, Integer> expected = new LinkedHashMap<>();
            diamond.forEach((aspect, amount) -> expected.put(aspect, amount + 1));
            assertScan("vanilla_tools/netherite_" + tool + ".json", "item",
                    "minecraft:netherite_" + tool, expected);
        }
    }

    private static void assertArmorSet(String material, String materialAspect,
            int helmetMaterial, int chestMaterial, int legsMaterial, int bootsMaterial,
            int helmetProtection, int chestProtection, int legsProtection,
            int bootsProtection) throws IOException {
        assertArmor(material, "helmet", materialAspect, helmetMaterial,
                helmetProtection);
        assertArmor(material, "chestplate", materialAspect, chestMaterial,
                chestProtection);
        assertArmor(material, "leggings", materialAspect, legsMaterial,
                legsProtection);
        assertArmor(material, "boots", materialAspect, bootsMaterial,
                bootsProtection);
    }

    private static void assertArmor(String material, String slot,
            String materialAspect, int materialAmount, int protection)
            throws IOException {
        assertScan("vanilla_armor/" + material + "_" + slot + ".json", "item",
                "minecraft:" + material + "_" + slot,
                Map.of(materialAspect, materialAmount, "tutamen", protection));
    }

    private static void assertToolSet(String material, String materialAspect,
            int pickaxeMaterial, int axeMaterial, int shovelMaterial, int hoeMaterial,
            Map<String, Integer> instrumentum) throws IOException {
        assertTool(material, "pickaxe", materialAspect, pickaxeMaterial,
                instrumentum.get("pickaxe"), 0, 0);
        assertTool(material, "axe", materialAspect, axeMaterial,
                instrumentum.get("axe"), 0, 0);
        assertTool(material, "shovel", materialAspect, shovelMaterial,
                instrumentum.get("shovel"), 0, 0);
        assertTool(material, "sword", materialAspect,
                material.equals("diamond") ? 4 : pickaxeMaterial,
                instrumentum.get("sword"), instrumentum.get("sword"), 0);
        assertTool(material, "hoe", materialAspect, hoeMaterial,
                instrumentum.get("hoe"), 0,
                material.equals("diamond") ? 2 : 1);
    }

    private static void assertTool(String material, String tool,
            String materialAspect, int materialAmount, int instrumentum,
            int telum, int meto) throws IOException {
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("instrumentum", instrumentum);
        if (telum > 0) expected.put("telum", telum);
        expected.put(materialAspect, materialAmount);
        if (meto > 0) expected.put("meto", meto);
        assertScan("vanilla_tools/" + material + "_" + tool + ".json", "item",
                "minecraft:" + material + "_" + tool, expected);
    }

    @Test
    void newlyMappedLegacyResourcesStayActiveAndUnique() throws IOException {
        Map<String, JsonObject> active = new LinkedHashMap<>();
        try (var paths = Files.walk(SCANS)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json"))
                    .toList()) {
                JsonObject scan = json(path);
                if (scan.has("inactive") && scan.get("inactive").getAsBoolean()) {
                    continue;
                }
                String key = scan.get("type").getAsString() + ":"
                        + scan.get("target").getAsString();
                assertFalse(active.containsKey(key), "duplicate active scan: " + key);
                active.put(key, scan);
            }
        }

        List<String> restored = List.of(
                "block:thaumcraftmodern:tallow_candle",
                "item:thaumcraftmodern:quicksilver_nugget",
                "item:thaumcraftmodern:cultist_knight_helmet",
                "item:thaumcraftmodern:cultist_knight_chestplate",
                "item:thaumcraftmodern:cultist_knight_leggings",
                "item:thaumcraftmodern:cultist_cleric_hood",
                "item:thaumcraftmodern:cultist_cleric_robe",
                "item:thaumcraftmodern:cultist_cleric_leggings",
                "item:thaumcraftmodern:cultist_praetor_helmet",
                "item:thaumcraftmodern:cultist_praetor_chestplate",
                "item:thaumcraftmodern:cultist_praetor_leggings",
                "item:thaumcraftmodern:cultist_boots",
                "item:thaumcraftmodern:native_iron_cluster",
                "item:thaumcraftmodern:native_copper_cluster",
                "item:thaumcraftmodern:native_tin_cluster",
                "item:thaumcraftmodern:native_silver_cluster",
                "item:thaumcraftmodern:native_lead_cluster",
                "item:thaumcraftmodern:native_gold_cluster",
                "item:thaumcraftmodern:beef_nugget",
                "item:thaumcraftmodern:chicken_nugget",
                "item:thaumcraftmodern:pork_nugget",
                "item:thaumcraftmodern:fish_nugget",
                "item:thaumcraftmodern:apprentice_ring_aer",
                "block:thaumcraftmodern:arcane_ear",
                "entity:thaumcraftmodern:primal_orb",
                "entity:thaumcraftmodern:straw_golem",
                "item_tag:thaumcraftmodern:thaumcraft_banners",
                "block_tag:thaumcraftmodern:eldritch_structure_blocks"
        );
        restored.forEach(key -> assertTrue(active.containsKey(key),
                () -> "missing restored original scan: " + key));
    }

    private static void assertScan(
            String file,
            String type,
            String target,
            Map<String, Integer> expectedAspects
    ) throws IOException {
        JsonObject scan = json(SCANS.resolve(file));
        assertEquals(type, scan.get("type").getAsString());
        assertEquals(target, scan.get("target").getAsString());
        assertEquals(expectedAspects, aspects(scan));
    }

    private static Map<String, Integer> aspects(JsonObject scan) {
        Map<String, Integer> result = new LinkedHashMap<>();
        JsonArray aspects = scan.getAsJsonArray("aspects");
        aspects.forEach(element -> {
            JsonObject aspect = element.getAsJsonObject();
            result.merge(
                    aspect.get("id").getAsString(),
                    aspect.get("amount").getAsInt(),
                    Integer::sum
            );
        });
        return result;
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
