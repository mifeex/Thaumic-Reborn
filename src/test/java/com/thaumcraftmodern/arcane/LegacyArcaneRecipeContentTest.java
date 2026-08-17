package com.thaumcraftmodern.arcane;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyArcaneRecipeContentTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path MANIFEST = ROOT.resolve(
            "data/legacy_tc4_4_2_3_5/modern_migration/arcane_recipes.json"
    );
    private static final Path RECIPES = ROOT.resolve(
            "src/main/resources/data/thaumic_reborn/recipes"
    );
    private static final Path RESEARCH = ROOT.resolve(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research"
    );

    @Test
    void everyClassicArcaneRegistrationHasAUniqueRuntimeRecipe() throws IOException {
        JsonObject manifest = JsonParser.parseString(Files.readString(MANIFEST)).getAsJsonObject();
        assertEquals(89, manifest.get("source_registrations").getAsInt());
        assertEquals(109, manifest.get("runtime_recipes").getAsInt());
        assertEquals(107, manifest.get("research_pages_materialized").getAsInt());

        Set<String> ids = new HashSet<>();
        JsonArray recipes = manifest.getAsJsonArray("recipes");
        assertEquals(109, recipes.size());
        for (var element : recipes) {
            JsonObject record = element.getAsJsonObject();
            assertEquals("runtime", record.get("status").getAsString());
            String id = record.get("modern_id").getAsString();
            assertTrue(ids.add(id), () -> "duplicate modern recipe id: " + id);
            Path recipe = RECIPES.resolve(id.substring(id.indexOf(':') + 1) + ".json");
            assertTrue(Files.isRegularFile(recipe), () -> "missing runtime recipe: " + recipe);
            String json = Files.readString(recipe);
            assertFalse(json.contains("legacy_"), () -> "unmapped legacy identity in " + recipe);
            assertFalse(json.contains("ConfigItems"), () -> "TC4 field leaked into " + recipe);
            assertFalse(json.contains("ConfigBlocks"), () -> "TC4 field leaked into " + recipe);
        }
    }

    @Test
    void wandComponentRecipesUseOriginalCraftCosts() throws IOException {
        assertVis("wand_cap_gold", "ordo", 3);
        assertVis("wand_cap_copper", "ordo", 2);
        assertVis("wand_cap_silver_inert", "ordo", 4);
        assertVis("wand_cap_thaumium_inert", "ordo", 6);
        assertVis("wand_cap_void_inert", "perditio", 27);
        assertVis("wand_rod_greatwood", "perditio", 3);
        assertVis("wand_rod_greatwood_staff", "ordo", 8);
        assertVis("wand_rod_obsidian_staff", "ordo", 14);
        assertVis("wand_rod_silverwood_staff", "ordo", 24);
    }

    @Test
    void materializedArcaneRecipeResearchIsNotLeftGenericallyInactive()
            throws IOException {
        try (var paths = Files.walk(RESEARCH)) {
            for (Path path : paths.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".json"))
                    .toList()) {
                JsonObject research = JsonParser.parseString(
                        Files.readString(path)
                ).getAsJsonObject();
                boolean hasRuntimeRecipePage = research.has("pages")
                        && research.getAsJsonArray("pages").asList().stream()
                        .map(element -> element.getAsJsonObject())
                        .anyMatch(page -> "recipe".equals(
                                page.get("type").getAsString()
                        ));
                boolean hasStaleGenericBlock = research.has("inactive")
                        && research.get("inactive").getAsBoolean()
                        && research.has("inactive_reason")
                        && "referenced gameplay content is not implemented".equals(
                                research.get("inactive_reason").getAsString()
                        );
                assertFalse(
                        hasRuntimeRecipePage && hasStaleGenericBlock,
                        () -> "runtime recipe research is still generically inactive: "
                                + path
                );
            }
        }
    }

    private static void assertVis(String recipe, String aspect, int expected) throws IOException {
        JsonObject json = JsonParser.parseString(
                Files.readString(RECIPES.resolve(recipe + ".json"))
        ).getAsJsonObject();
        assertEquals(expected, json.getAsJsonObject("vis").get(aspect).getAsInt());
    }
}
