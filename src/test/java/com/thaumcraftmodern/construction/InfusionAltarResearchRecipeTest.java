package com.thaumcraftmodern.construction;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class InfusionAltarResearchRecipeTest {
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy/infusion.json"
    );

    @Test
    void matchesClassicCompoundPageDimensionsCostAndCells() {
        InfusionAltarResearchRecipe.Snapshot recipe =
                InfusionAltarResearchRecipe.snapshot();

        assertEquals("thaumic_reborn:infusion_altar_construct",
                InfusionAltarResearchRecipe.ID.toString());
        assertEquals(3, recipe.width());
        assertEquals(3, recipe.height());
        assertEquals(3, recipe.depth());
        assertEquals(27, recipe.cells().size());
        assertEquals(
                List.of("ignis", "terra", "ordo", "aer", "perditio", "aqua"),
                recipe.costs().stream().map(cost -> cost.aspectId()).toList()
        );
        recipe.costs().forEach(cost -> assertEquals(25, cost.amount()));
        assertEquals(InfusionAltarResearchRecipe.Cell.RUNIC_MATRIX,
                recipe.cells().get(4));
        assertEquals(InfusionAltarResearchRecipe.Cell.ARCANE_PEDESTAL,
                recipe.cells().get(22));
        assertEquals(4, recipe.cells().stream()
                .filter(cell -> cell == InfusionAltarResearchRecipe.Cell.ARCANE_STONE)
                .count());
        assertEquals(4, recipe.cells().stream()
                .filter(cell -> cell == InfusionAltarResearchRecipe.Cell.ARCANE_STONE_BRICK)
                .count());
    }

    @Test
    void infusionResearchContainsAllEightClassicPages() throws Exception {
        JsonObject research = JsonParser.parseString(Files.readString(RESEARCH))
                .getAsJsonObject();
        JsonArray pages = research.getAsJsonArray("pages");

        assertEquals(8, pages.size());
        assertEquals("text", type(pages, 0));
        assertEquals("recipe", type(pages, 1));
        assertEquals("recipe", type(pages, 2));
        assertEquals("text", type(pages, 3));
        assertEquals("compound_crafting", type(pages, 4));
        assertEquals("thaumic_reborn:infusion_altar_construct",
                pages.get(4).getAsJsonObject().get("recipe").getAsString());
        assertEquals("text", type(pages, 5));
        assertEquals("text", type(pages, 6));
        assertEquals("text", type(pages, 7));
        assertFalse(research.get("inactive").getAsBoolean());
    }

    private static String type(JsonArray pages, int index) {
        return pages.get(index).getAsJsonObject().get("type").getAsString();
    }
}
