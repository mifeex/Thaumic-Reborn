package com.thaumcraftmodern.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClassicMagicalWoodCraftingFidelityTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumcraftmodern");
    private static final Path DATA = Path.of(
            "src/main/resources/data/thaumcraftmodern");

    @Test
    void originalTc4235WoodRecipesKeepTheirShapesAndCounts() throws Exception {
        assertRecipe("greatwood_planks", List.of("W"), 4);
        assertRecipe("silverwood_planks", List.of("W"), 4);
        assertRecipe("greatwood_stairs", List.of("K  ", "KK ", "KKK"), 4);
        assertRecipe("silverwood_stairs", List.of("K  ", "KK ", "KKK"), 4);
        assertRecipe("greatwood_slab", List.of("KKK"), 6);
        assertRecipe("silverwood_slab", List.of("KKK"), 6);

        for (String wood : List.of("greatwood", "silverwood")) {
            JsonObject smelting = recipe(wood + "_log_charcoal");
            assertEquals("minecraft:smelting", smelting.get("type").getAsString());
            assertEquals("minecraft:charcoal", smelting.get("result").getAsString());
            assertEquals(0.5F, smelting.get("experience").getAsFloat());
        }
    }

    @Test
    void stairsAndSlabsUseTheExactOriginalPlankTextures() throws Exception {
        assertEquals(
                "5abcfb58238aebb17f1967c125c125a982521e2beaa6a568295b75288e11c2f1",
                sha256(ASSETS.resolve("textures/block/greatwood_planks.png")));
        assertEquals(
                "2f610cb70be968d0a3f3c937b28387a558ff963c9e5a572b12de0ea6b515eb0c",
                sha256(ASSETS.resolve("textures/block/silverwood_planks.png")));

        for (String wood : List.of("greatwood", "silverwood")) {
            JsonObject stairs = json(ASSETS.resolve(
                    "blockstates/" + wood + "_stairs.json"));
            assertEquals(40, stairs.getAsJsonObject("variants").size());
            for (String model : List.of(
                    wood + "_stairs", wood + "_stairs_inner",
                    wood + "_stairs_outer", wood + "_slab", wood + "_slab_top")) {
                String source = Files.readString(ASSETS.resolve(
                        "models/block/" + model + ".json"));
                assertTrue(source.contains(
                        "thaumcraftmodern:block/" + wood + "_planks"));
            }
            for (String item : List.of(wood + "_stairs", wood + "_slab")) {
                assertTrue(Files.exists(ASSETS.resolve(
                        "models/item/" + item + ".json")));
            }
        }
    }

    @Test
    void allFourClassicDecorativeBlocksAreInTagsAndCreativeTab()
            throws Exception {
        String tab = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"));
        for (String item : List.of(
                "GREATWOOD_STAIRS", "GREATWOOD_SLAB",
                "SILVERWOOD_STAIRS", "SILVERWOOD_SLAB")) {
            assertTrue(tab.contains("output.accept(ModItems." + item + ".get());"));
        }
        for (String tag : List.of(
                "blocks/stairs", "blocks/wooden_stairs",
                "items/stairs", "items/wooden_stairs",
                "blocks/slabs", "blocks/wooden_slabs",
                "items/slabs", "items/wooden_slabs")) {
            JsonArray values = json(Path.of(
                    "src/main/resources/data/minecraft/tags/" + tag + ".json"))
                    .getAsJsonArray("values");
            String shape = tag.endsWith("stairs") ? "stairs" : "slab";
            assertTrue(values.asList().stream().anyMatch(value -> value.getAsString()
                    .equals("thaumcraftmodern:greatwood_" + shape)));
            assertTrue(values.asList().stream().anyMatch(value -> value.getAsString()
                    .equals("thaumcraftmodern:silverwood_" + shape)));
        }
    }

    private static void assertRecipe(String name, List<String> pattern,
            int count) throws Exception {
        JsonObject recipe = recipe(name);
        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals(pattern, recipe.getAsJsonArray("pattern").asList().stream()
                .map(element -> element.getAsString()).toList());
        assertEquals(count, recipe.getAsJsonObject("result").get("count").getAsInt());
    }

    private static JsonObject recipe(String name) throws Exception {
        return json(DATA.resolve("recipes/" + name + ".json"));
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
}
