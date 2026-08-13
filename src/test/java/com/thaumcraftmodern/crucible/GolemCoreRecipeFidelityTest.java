package com.thaumcraftmodern.crucible;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GolemCoreRecipeFidelityTest {
    private static final Path CRUCIBLE = Path.of(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/crucible_recipes");

    @Test void allSevenOriginalCrucibleCoreRecipesAreActiveAndFaithful()
            throws Exception {
        assertRecipe("coregather", "blank_golem_core", "gather_golem_core",
                Map.of("lucrum", 5, "terra", 5));
        assertRecipe("corefill", "blank_golem_core", "fill_golem_core",
                Map.of("fames", 5, "vacuos", 5));
        assertRecipe("coreempty", "blank_golem_core", "empty_golem_core",
                Map.of("lucrum", 5, "vacuos", 5));
        assertRecipe("coreharvest", "blank_golem_core", "harvest_golem_core",
                Map.of("meto", 5, "messis", 5));
        assertRecipe("coreguard", "blank_golem_core", "guard_golem_core",
                Map.of("telum", 5, "vinculum", 5));
        assertRecipe("corebutcher", "guard_golem_core", "butcher_golem_core",
                Map.of("corpus", 5, "bestia", 5));
        assertRecipe("coreliquid", "blank_golem_core", "liquid_golem_core",
                Map.of("aqua", 5, "vacuos", 5));
    }

    @Test void blankAndFiveAdvancedOriginalCoreRecipesArePresent() {
        Path data = Path.of("src/main/resources/data/thaumcraftmodern");
        assertTrue(Files.isRegularFile(data.resolve("recipes/core_blank.json")));
        for (String id : new String[]{"core_use", "core_alchemy", "core_lumber",
                "core_sorting", "core_fishing"}) {
            assertTrue(Files.isRegularFile(data.resolve(
                    "thaumcraft/infusion_recipes/" + id + ".json")), id);
        }
    }

    @Test void advancedCoreResearchPagesUseTheirInfusionRecipes()
            throws Exception {
        Path research = Path.of(
                "src/main/resources/data/thaumcraftmodern/thaumcraft/research/legacy");
        for (String id : new String[]{"coreuse", "corealchemy", "corelumber",
                "coresorting", "corefishing"}) {
            JsonObject json = JsonParser.parseString(Files.readString(
                    research.resolve(id + ".json"))).getAsJsonObject();
            boolean hasInfusionPage = false;
            for (var rawPage : json.getAsJsonArray("pages")) {
                JsonObject page = rawPage.getAsJsonObject();
                if ("infusion".equals(page.get("type").getAsString())) {
                    hasInfusionPage = true;
                    assertTrue(page.has("output"), id);
                    assertTrue(page.has("central"), id);
                    assertTrue(!page.getAsJsonArray("components").isEmpty(), id);
                    assertTrue(!page.getAsJsonArray("aspect_costs").isEmpty(), id);
                }
            }
            assertTrue(hasInfusionPage, id);
        }
    }

    private static void assertRecipe(String id, String catalyst, String output,
            Map<String, Integer> aspects) throws Exception {
        JsonObject json = JsonParser.parseString(
                Files.readString(CRUCIBLE.resolve(id + ".json"))).getAsJsonObject();
        assertEquals("thaumcraftmodern:" + catalyst,
                json.getAsJsonObject("catalyst").get("item").getAsString(), id);
        assertEquals("thaumcraftmodern:" + output,
                json.getAsJsonObject("output").get("item").getAsString(), id);
        assertTrue(!json.has("inactive") || !json.get("inactive").getAsBoolean(), id);
        for (var entry : aspects.entrySet()) {
            assertEquals(entry.getValue().intValue(),
                    json.getAsJsonObject("aspects").get(entry.getKey()).getAsInt(), id);
        }
    }
}
