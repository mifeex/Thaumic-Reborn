package com.thaumcraftmodern.wand;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WandAssemblyRecipeVariantsTest {
    private static final Path RECIPES = Path.of(
            "src/main/resources/data/thaumic_reborn/recipes"
    );

    @Test
    void everyClassicCastingToolCombinationHasAViewerRecipe()
            throws Exception {
        try (var files = Files.list(RECIPES)) {
            long variants = files
                    .filter(path -> path.toString().endsWith(".json"))
                    .filter(WandAssemblyRecipeVariantsTest::isVariant)
                    .count();
            assertEquals(161, variants);
        }
        assertVariant(
                "wand_greatwood_gold.json",
                "thaumic_reborn:arcane_wand_assembly",
                "greatwood",
                "gold"
        );
        assertVariant(
                "staff_silverwood_staff_thaumium.json",
                "thaumic_reborn:arcane_wand_assembly",
                "silverwood_staff",
                "thaumium"
        );
        assertVariant(
                "sceptre_greatwood_gold.json",
                "thaumic_reborn:arcane_sceptre_assembly",
                "greatwood",
                "gold"
        );
    }

    private static boolean isVariant(Path path) {
        try {
            JsonObject json = JsonParser.parseString(
                    Files.readString(path)
            ).getAsJsonObject();
            return json.has("rod") && json.has("cap")
                    && json.get("type").getAsString()
                            .contains("assembly");
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void assertVariant(
            String file,
            String type,
            String rod,
            String cap
    ) throws Exception {
        JsonObject json = JsonParser.parseString(
                Files.readString(RECIPES.resolve(file))
        ).getAsJsonObject();
        assertEquals(type, json.get("type").getAsString());
        assertEquals(rod, json.get("rod").getAsString());
        assertEquals(cap, json.get("cap").getAsString());
    }
}
