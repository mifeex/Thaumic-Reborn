package com.thaumcraftmodern.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ThaumiumToolRecipePatternTest {
    private static final Path RECIPES = Path.of(
            "src/main/resources/data/thaumic_reborn/recipes"
    );

    @Test
    void toolsUseClassicShapedPatterns() throws Exception {
        assertPattern("thaumium_pickaxe", "III", " S ", " S ");
        assertPattern("thaumium_axe", "II", "SI", "S ");
        assertPattern("thaumium_sword", "I", "I", "S");
        assertPattern("thaumium_shovel", "I", "S", "S");
        assertPattern("thaumium_hoe", "II", "S ", "S ");
    }

    private static void assertPattern(String recipeId, String... expected)
            throws Exception {
        JsonObject recipe = JsonParser.parseString(Files.readString(
                RECIPES.resolve(recipeId + ".json")
        )).getAsJsonObject();
        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        JsonArray pattern = recipe.getAsJsonArray("pattern");
        assertEquals(
                List.of(expected),
                pattern.asList().stream().map(element -> element.getAsString()).toList()
        );
    }
}
