package com.thaumcraftmodern.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ImplementedEldritchResearchPagesTest {
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/research/legacy"
    );
    private static final Path INFUSION = Path.of(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/infusion_recipes"
    );
    private static final Path MODERN_ITEMS = Path.of(
            "src/main/resources/assets/thaumcraftmodern/textures/item"
    );
    private static final Path CLASSIC_ITEMS = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                    + "assets/thaumcraft/textures/items"
    );

    @Test
    void implementedEldritchObjectsHaveActiveClassicResearchIcons()
            throws IOException {
        JsonObject oculus = read(RESEARCH.resolve("oculus.json"));
        assertFalse(oculus.get("inactive").getAsBoolean());
        assertEquals("thaumcraftmodern:eldritch_eye",
                oculus.get("icon").getAsString());

        JsonObject pearl = read(RESEARCH.resolve("primpearl.json"));
        assertFalse(pearl.get("inactive").getAsBoolean());
        assertEquals("thaumcraftmodern:primordial_pearl",
                pearl.get("icon").getAsString());
        assertEquals("criterion",
                pearl.getAsJsonObject("reveal_when")
                        .get("type").getAsString());
        assertEquals("thaumcraftmodern:legacy_clue/primpearl",
                pearl.getAsJsonObject("reveal_when")
                        .get("id").getAsString());

        assertArrayEquals(
                Files.readAllBytes(CLASSIC_ITEMS.resolve("eldritch_object.png")),
                Files.readAllBytes(MODERN_ITEMS.resolve("eldritch_eye.png"))
        );
        assertArrayEquals(
                Files.readAllBytes(CLASSIC_ITEMS.resolve("eldritch_object_3.png")),
                Files.readAllBytes(MODERN_ITEMS.resolve("primordial_pearl.png"))
        );
        assertArrayEquals(
                Files.readAllBytes(CLASSIC_ITEMS.resolve(
                        "eldritch_object_3.png.mcmeta")),
                Files.readAllBytes(MODERN_ITEMS.resolve(
                        "primordial_pearl.png.mcmeta"))
        );
    }

    @Test
    void oculusBookPageAndExecutableInfusionMatchTc4() throws IOException {
        JsonObject research = read(RESEARCH.resolve("oculus.json"));
        JsonObject page = research.getAsJsonArray("pages")
                .get(1).getAsJsonObject();
        assertEquals("infusion", page.get("type").getAsString());
        assertEquals("thaumcraftmodern:eldritch_eye",
                page.get("output").getAsString());
        assertEquals("minecraft:ender_eye",
                page.get("central").getAsString());
        assertComponents(page.getAsJsonArray("components"));
        assertEssentia(page.getAsJsonArray("aspect_costs"));
        assertEquals("very_high", page.get("instability").getAsString());

        JsonObject recipe = read(INFUSION.resolve("eldritch_eye.json"));
        assertEquals("oculus", recipe.get("research").getAsString());
        assertEquals(5, recipe.get("instability").getAsInt());
        assertEquals("minecraft:ender_eye",
                recipe.getAsJsonObject("central").get("item").getAsString());
        assertComponents(recipe.getAsJsonArray("components"));
        JsonObject essentia = recipe.getAsJsonObject("essentia");
        assertEquals(64, essentia.get("alienis").getAsInt());
        assertEquals(16, essentia.get("vacuos").getAsInt());
        assertEquals(16, essentia.get("tenebrae").getAsInt());
        assertEquals(16, essentia.get("iter").getAsInt());
        assertEquals("thaumcraftmodern:eldritch_eye",
                recipe.getAsJsonObject("result").get("item").getAsString());
    }

    private static void assertComponents(JsonArray components) {
        assertEquals(2, components.size());
        assertEquals("thaumcraftmodern:void_seed",
                components.get(0).getAsJsonObject().get("item").getAsString());
        assertEquals("minecraft:gold_ingot",
                components.get(1).getAsJsonObject().get("item").getAsString());
    }

    private static void assertEssentia(JsonArray costs) {
        assertEquals(4, costs.size());
        assertCost(costs, 0, "alienis", 64);
        assertCost(costs, 1, "vacuos", 16);
        assertCost(costs, 2, "tenebrae", 16);
        assertCost(costs, 3, "iter", 16);
    }

    private static void assertCost(
            JsonArray costs,
            int index,
            String id,
            int amount
    ) {
        JsonObject cost = costs.get(index).getAsJsonObject();
        assertEquals(id, cost.get("id").getAsString());
        assertEquals(amount, cost.get("amount").getAsInt());
    }

    private static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
