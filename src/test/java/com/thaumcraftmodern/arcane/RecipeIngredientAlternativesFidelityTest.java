package com.thaumcraftmodern.arcane;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RecipeIngredientAlternativesFidelityTest {
    private static final Path RECIPES = Path.of(
            "src/main/resources/data/thaumic_reborn/recipes"
    );
    private static final Path ELEMENTAL_SHARDS = Path.of(
            "src/main/resources/data/thaumic_reborn/tags/items/elemental_shards.json"
    );

    @Test
    void everyClassicWildcardShardSlotUsesAllSixPrimalShards()
            throws Exception {
        List<String> expected = List.of(
                "thaumic_reborn:air_shard",
                "thaumic_reborn:fire_shard",
                "thaumic_reborn:water_shard",
                "thaumic_reborn:earth_shard",
                "thaumic_reborn:order_shard",
                "thaumic_reborn:entropy_shard"
        );
        JsonObject tag = json(ELEMENTAL_SHARDS);
        assertEquals(expected, tag.getAsJsonArray("values").asList().stream()
                .map(element -> element.getAsString()).toList());

        assertIngredientTag("thaumometer", "S");
        assertIngredientTag("arcane_stone1", "C");
        assertIngredientTag("infusion_matrix", "B");
    }

    @Test
    void loopGeneratedPrimalArrowRecipesKeepTheirMatchingShardMetadata()
            throws Exception {
        Map<String, String> variants = Map.of(
                "aer", "air",
                "ignis", "fire",
                "aqua", "water",
                "terra", "earth",
                "ordo", "order",
                "perditio", "entropy"
        );
        for (Map.Entry<String, String> variant : variants.entrySet()) {
            JsonObject recipe = recipe("primal_arrow_" + variant.getKey());
            assertEquals(
                    "thaumic_reborn:" + variant.getValue() + "_shard",
                    recipe.getAsJsonObject("key").getAsJsonObject("S")
                            .get("item").getAsString(),
                    variant.getKey()
            );
        }
    }

    private static void assertIngredientTag(String recipeId, String symbol)
            throws Exception {
        assertEquals(
                "thaumic_reborn:elemental_shards",
                recipe(recipeId).getAsJsonObject("key")
                        .getAsJsonObject(symbol).get("tag").getAsString(),
                recipeId
        );
    }

    private static JsonObject recipe(String id) throws Exception {
        return json(RECIPES.resolve(id + ".json"));
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
