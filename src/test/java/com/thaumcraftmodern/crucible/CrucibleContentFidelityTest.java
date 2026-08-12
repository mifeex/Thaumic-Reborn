package com.thaumcraftmodern.crucible;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleContentFidelityTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void requiredResearchActivationMatchesImplementedVertical()
            throws IOException {
        assertFalse(research("crucible").get("inactive").getAsBoolean());
        assertFalse(research("nitor").get("inactive").getAsBoolean());
        assertFalse(research("alumentum").get("inactive").getAsBoolean());
        assertFalse(research("thaumium").get("inactive").getAsBoolean());
        assertFalse(research("distilessentia").get("inactive").getAsBoolean());
        assertFalse(research("tubes").get("inactive").getAsBoolean());
        assertFalse(research("tubefilter").get("inactive").getAsBoolean());
        assertFalse(research("jarlabel").get("inactive").getAsBoolean());
        assertFalse(research("alchemicalduplication").get("inactive").getAsBoolean());
        assertFalse(research("alchemicalmanufacture").get("inactive").getAsBoolean());
        assertFalse(research("entropicprocessing").get("inactive").getAsBoolean());
        assertFalse(research("etherealbloom").get("inactive").getAsBoolean());
    }

    @Test
    void secondaryAlchemyResearchUsesOriginalTc4Icons() throws IOException {
        assertResearchIcon("alchemicalduplication", "r_alchmult.png");
        assertResearchIcon("alchemicalmanufacture", "r_alchman.png");
        assertResearchIcon("entropicprocessing", "r_alchent.png");
    }

    private static void assertResearchIcon(String researchId, String fileName)
            throws IOException {
        assertEquals(
                "thaumcraftmodern:textures/misc/" + fileName,
                research(researchId).get("icon_resource").getAsString()
        );
        assertTrue(Files.isRegularFile(ROOT.resolve(
                "assets/thaumcraftmodern/textures/misc/" + fileName
        )));
    }

    @Test
    void classicRecipeCostsAndOutputsRemainDataDriven()
            throws IOException {
        JsonObject alumentum = crucibleRecipe("alumentum");
        assertEquals(
                "thaumcraftmodern:alumentum",
                alumentum.getAsJsonObject("output").get("item").getAsString()
        );
        assertEquals(
                3,
                alumentum.getAsJsonObject("aspects")
                        .get("perditio").getAsInt()
        );
        JsonObject nitor = crucibleRecipe("nitor");
        assertEquals(
                3,
                nitor.getAsJsonObject("aspects").get("lux").getAsInt()
        );
        assertTrue(Files.isRegularFile(ROOT.resolve(
                "data/thaumcraftmodern/recipes/salis_mundus.json"
        )));

        JsonObject thaumium = crucibleRecipe("thaumium");
        assertEquals("forge:ingots/iron",
                thaumium.getAsJsonObject("catalyst").get("tag").getAsString());
        assertEquals("thaumcraftmodern:thaumium_ingot",
                thaumium.getAsJsonObject("output").get("item").getAsString());
        assertEquals(4,
                thaumium.getAsJsonObject("aspects")
                        .get("praecantatio").getAsInt());
        JsonObject thaumiumPage = research("thaumium")
                .getAsJsonArray("pages").get(1).getAsJsonObject();
        assertEquals("recipe", thaumiumPage.get("type").getAsString());
        assertEquals("thaumcraftmodern:thaumium",
                thaumiumPage.get("recipe").getAsString());

        JsonObject duplication = crucibleRecipe("altgunpowder");
        assertEquals(2, duplication.getAsJsonObject("output").get("count").getAsInt());
        assertEquals(4, duplication.getAsJsonObject("aspects").get("ignis").getAsInt());
        JsonObject manufacture = crucibleRecipe("altweb");
        assertEquals("minecraft:cobweb",
                manufacture.getAsJsonObject("output").get("item").getAsString());
        JsonObject entropic = crucibleRecipe("altbonemeal");
        assertEquals(4, entropic.getAsJsonObject("output").get("count").getAsInt());
    }

    @Test
    void originalCrucibleOutputAssetsArePresent() {
        for (String asset : new String[]{
                "textures/item/alumentum.png",
                "textures/item/nitor.png",
                "textures/item/shard_balanced.png",
                "textures/item/salis_mundus.png",
                "sounds/spill.ogg",
                "sounds/bubble1.ogg"
        }) {
            assertTrue(Files.isRegularFile(
                    ROOT.resolve("assets/thaumcraftmodern").resolve(asset)
            ), asset);
        }
    }

    @Test
    void oreDictionaryCatalystsUseModernIngredientAlternatives()
            throws IOException {
        assertCatalystTag("nitor", "forge:dusts/glowstone");
        assertCatalystTag("altglowstone", "forge:dusts/glowstone");
        assertCatalystTag("thaumium", "forge:ingots/iron");
        assertCatalystTag("transiron", "forge:nuggets/iron");
        assertCatalystTag("transtin", "forge:nuggets/tin");
        assertCatalystTag("transsilver", "forge:nuggets/silver");
        assertCatalystTag("translead", "forge:nuggets/lead");

        assertEquals("minecraft:packed_ice",
                crucibleRecipe("altice").getAsJsonObject("catalyst")
                        .get("item").getAsString());
        assertEquals("minecraft:clay",
                crucibleRecipe("golemclay").getAsJsonObject("catalyst")
                        .get("item").getAsString());
    }

    @Test
    void allSixPrimalCrystalsRemainValidAspectSpecificCatalysts()
            throws IOException {
        String[] names = {
                "air", "fire", "water", "earth", "order", "entropy"
        };
        String[] aspects = {
                "aer", "ignis", "aqua", "terra", "ordo", "perditio"
        };
        for (int index = 0; index < names.length; index++) {
            JsonObject recipe = crucibleRecipe("balanced_" + names[index]);
            assertEquals("thaumcraftmodern:" + names[index] + "_shard",
                    recipe.getAsJsonObject("catalyst")
                            .get("item").getAsString());
            assertEquals("thaumcraftmodern:balanced_shard",
                    recipe.getAsJsonObject("output")
                            .get("item").getAsString());
            assertFalse(recipe.getAsJsonObject("aspects").has(aspects[index]));
            assertEquals(5, recipe.getAsJsonObject("aspects").size());
        }
    }

    @Test
    void thaumonomiconCyclesThroughAllSixBalancedCrystalRecipes()
            throws IOException {
        JsonArray pages = research("crucible").getAsJsonArray("pages");
        assertRecipeCycle(pages.get(3).getAsJsonObject(),
                "balanced_air", "balanced_fire", "balanced_water");
        assertRecipeCycle(pages.get(5).getAsJsonObject(),
                "balanced_earth", "balanced_order", "balanced_entropy");
    }

    private static void assertRecipeCycle(JsonObject page, String... ids) {
        assertFalse(page.has("recipe"));
        JsonArray recipes = page.getAsJsonArray("recipes");
        assertEquals(ids.length, recipes.size());
        for (int index = 0; index < ids.length; index++) {
            assertEquals("thaumcraftmodern:" + ids[index],
                    recipes.get(index).getAsString());
        }
    }

    private static void assertCatalystTag(String recipeId, String tag)
            throws IOException {
        assertEquals(tag, crucibleRecipe(recipeId)
                .getAsJsonObject("catalyst").get("tag").getAsString());
    }

    private static JsonObject research(String id) throws IOException {
        return json(ROOT.resolve(
                "data/thaumcraftmodern/thaumcraft/research/legacy/"
                        + id + ".json"
        ));
    }

    private static JsonObject crucibleRecipe(String id) throws IOException {
        return json(ROOT.resolve(
                "data/thaumcraftmodern/thaumcraft/crucible_recipes/"
                        + id + ".json"
        ));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
