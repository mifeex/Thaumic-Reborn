package com.thaumcraftmodern.construction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThaumatoriumResearchRecipeTest {
    @Test
    void matchesClassicCompoundPageDimensionsAndCost() {
        ThaumatoriumResearchRecipe.Snapshot recipe =
                ThaumatoriumResearchRecipe.snapshot();

        assertEquals("thaumic_reborn:thaumatorium_construct",
                ThaumatoriumResearchRecipe.ID.toString());
        assertEquals(1, recipe.width());
        assertEquals(3, recipe.height());
        assertEquals(1, recipe.depth());
        assertEquals(3, recipe.cells().size());
        assertEquals("ignis", recipe.costs().get(0).aspectId());
        assertEquals(15, recipe.costs().get(0).amount());
        assertEquals("ordo", recipe.costs().get(1).aspectId());
        assertEquals(30, recipe.costs().get(1).amount());
        assertEquals("aqua", recipe.costs().get(2).aspectId());
        assertEquals(30, recipe.costs().get(2).amount());
    }
}
