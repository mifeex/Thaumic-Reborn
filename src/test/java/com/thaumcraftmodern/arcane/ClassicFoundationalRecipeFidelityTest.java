package com.thaumcraftmodern.arcane;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClassicFoundationalRecipeFidelityTest {
    private static final Path RECIPES = Path.of(
            "src/main/resources/data/thaumic_reborn/recipes");
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn");
    private static final Path SCANS = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/scans/legacy");

    @Test
    void filterAndTubeUseTheOriginalTc4IngredientsAndCounts() throws Exception {
        JsonObject filter = recipe("filter");
        assertEquals(List.of("GWG"), pattern(filter));
        assertEquals("thaumic_reborn:silverwood_planks",
                filter.getAsJsonObject("key").getAsJsonObject("W")
                        .get("item").getAsString());
        assertEquals(2, filter.getAsJsonObject("result").get("count").getAsInt());
        assertEquals(5, filter.getAsJsonObject("vis").get("aqua").getAsInt());
        assertEquals(5, filter.getAsJsonObject("vis").get("ordo").getAsInt());

        JsonObject tube = recipe("tube");
        assertEquals(List.of(" Q ", "IGI", " B "), pattern(tube));
        assertEquals("thaumic_reborn:quicksilver_nugget",
                tube.getAsJsonObject("key").getAsJsonObject("Q")
                        .get("item").getAsString());
        assertEquals(8, tube.getAsJsonObject("result").get("count").getAsInt());
    }

    @Test
    void classicFoundationalMaterialsHaveReachableConversionRecipes()
            throws Exception {
        assertConversion("quicksilver", "thaumic_reborn:quicksilver",
                "thaumic_reborn:quicksilver_nugget");
        assertConversion("thaumium", "thaumic_reborn:thaumium_ingot",
                "thaumic_reborn:thaumium_nugget");
        assertConversion("void", "thaumic_reborn:void_metal_ingot",
                "thaumic_reborn:void_nugget");

        JsonObject leaf = recipe("quicksilver_from_shimmerleaf");
        assertEquals(List.of("#"), pattern(leaf));
        assertEquals("thaumic_reborn:quicksilver",
                leaf.getAsJsonObject("result").get("item").getAsString());

        JsonObject belt = recipe("mundane_belt");
        assertEquals(List.of(" L ", "L L", " I "), pattern(belt));
        assertEquals("thaumic_reborn:blank_belt",
                belt.getAsJsonObject("result").get("item").getAsString());

        JsonObject slab = recipe("arcane_stone_slab");
        assertEquals(List.of("KKK"), pattern(slab));
        assertEquals("thaumic_reborn:arcane_stone_brick",
                slab.getAsJsonObject("key").getAsJsonObject("K")
                        .get("item").getAsString());
        assertEquals(6, slab.getAsJsonObject("result").get("count").getAsInt());
    }

    @Test
    void quicksilverDropAndQuicksilverKeepDistinctActiveScans() throws Exception {
        JsonObject drop = json(SCANS.resolve(
                "object_237_new_itemstack_configitems.itemnugget_1_5.json"));
        JsonObject quicksilver = json(SCANS.resolve(
                "object_266_new_itemstack_configitems.itemresource_1_3.json"));
        assertEquals("thaumic_reborn:quicksilver_nugget",
                drop.get("target").getAsString());
        assertEquals("thaumic_reborn:quicksilver",
                quicksilver.get("target").getAsString());
        assertFalse(drop.get("inactive").getAsBoolean());
        assertFalse(quicksilver.get("inactive").getAsBoolean());
    }

    @Test
    void restoredComponentsUseTheirExactOriginalItemTextures() throws Exception {
        assertTexture("quicksilver_nugget",
                "e830d2ef6a9875dc757fcadd16367b73b9290b7ef7225b49cac706a99c2e6df8");
        assertTexture("vis_filter",
                "291cc37fc71023cad93377d0594b6b70fa660c8ac865fbae633d15d28510a33e");
        assertTexture("blank_belt",
                "05aa84e3353bff3e213cb6fab9d1550aae15f8108b8597f02e8fa545fbb46d3c");
        assertTexture("thaumium_nugget",
                "99452e8b9a952ab31522fdc1ea586c0b38eabdadaf3410913a7a2f1777c8ee18");
        assertTexture("thaumium_ingot",
                "c323b01aed47e6d4ab7cb8d39f37c6c905eadc5603210f3054b4482b86d51548");
        assertTexture("void_nugget",
                "f42becf5bb1b37f79cbe11c5f3b34d54363aeafda5cbe1aaa8a9e555286825c8");
        assertTexture("primal_charm",
                "be88eeb88fca1a9c94d72246cf28d7472be073105b38818a0d4c4a0fbc9bd216");
        assertTexture("mirrored_glass",
                "c0a4953d8d9a3ed843f385dc082fc4f38a75ecdff5993dec9baa0cff1af9c254");
        assertTexture("enchanted_fabric",
                "d0f63c536b135b8ff02b8e8b6ba04a651787fd564bca3c7c02af9bae3f17dc19");
        assertModelTexture("silver_nugget");
    }

    @Test
    void arcaneEarUsesItsOriginalBlockTexturesInsteadOfRecipePlaceholder()
            throws Exception {
        JsonObject item = json(ASSETS.resolve("models/item/arcane_ear.json"));
        assertEquals("thaumic_reborn:block/arcane_ear_on",
                item.get("parent").getAsString());
        JsonObject model = json(ASSETS.resolve(
                "models/block/arcane_ear_base.json"));
        assertEquals(10, model.getAsJsonArray("elements").size());
        JsonObject states = json(ASSETS.resolve("blockstates/arcane_ear.json"));
        assertTrue(states.getAsJsonObject("variants")
                .has("powered=false"));
        assertTrue(states.getAsJsonObject("variants")
                .has("powered=true"));
        for (String texture : List.of(
                "arcane_ear_bottom", "arcane_ear_top", "arcane_ear_side",
                "arcane_ear_top_on", "arcane_ear_side_on",
                "arcane_ear_bell_top", "arcane_ear_bell_side")) {
            assertTrue(Files.isRegularFile(
                    ASSETS.resolve("textures/item/" + texture + ".png")));
        }
    }

    private static void assertConversion(String suffix, String ingot,
            String nugget) throws Exception {
        JsonObject split = recipe("nuggets_" + suffix);
        assertEquals(List.of("#"), pattern(split));
        assertEquals(ingot, split.getAsJsonObject("key")
                .getAsJsonObject("#").get("item").getAsString());
        assertEquals(nugget,
                split.getAsJsonObject("result").get("item").getAsString());
        assertEquals(9, split.getAsJsonObject("result").get("count").getAsInt());

        JsonObject combine = recipe(suffix + "_from_nuggets");
        assertEquals(List.of("###", "###", "###"), pattern(combine));
        assertEquals(nugget, combine.getAsJsonObject("key")
                .getAsJsonObject("#").get("item").getAsString());
        assertEquals(ingot,
                combine.getAsJsonObject("result").get("item").getAsString());
    }

    private static void assertTexture(String item, String expectedHash)
            throws Exception {
        assertModelTexture(item);
        assertEquals(expectedHash, sha256(
                ASSETS.resolve("textures/item/" + item + ".png")));
    }

    private static void assertModelTexture(String item) throws Exception {
        JsonObject model = json(ASSETS.resolve("models/item/" + item + ".json"));
        assertEquals("thaumic_reborn:item/" + item,
                model.getAsJsonObject("textures").get("layer0").getAsString());
        assertTrue(Files.isRegularFile(
                ASSETS.resolve("textures/item/" + item + ".png")));
    }

    private static List<String> pattern(JsonObject recipe) {
        return recipe.getAsJsonArray("pattern").asList().stream()
                .map(element -> element.getAsString()).toList();
    }

    private static JsonObject recipe(String name) throws Exception {
        return json(RECIPES.resolve(name + ".json"));
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
}
