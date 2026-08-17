package com.thaumcraftmodern.infusion;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InfusionEnchantmentResourceFidelityTest {
    private static final Path RECIPES = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes");
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy/infusionenchantment.json");

    @Test
    void allTwentyFourTc4InfusionEnchantmentsAreExecutableAndInTheBook()
            throws Exception {
        Set<Path> recipes;
        try (var paths = Files.list(RECIPES)) {
            recipes = paths.filter(path -> path.getFileName().toString()
                    .startsWith("infusion_enchant_")).collect(Collectors.toSet());
        }
        assertEquals(24, recipes.size());
        for (Path path : recipes) {
            JsonObject recipe = JsonParser.parseString(Files.readString(path))
                    .getAsJsonObject();
            assertEquals("infusionenchantment",
                    recipe.get("research").getAsString(), path.toString());
            JsonObject modifier = recipe.getAsJsonObject("result_modifier");
            assertEquals("enchantment", modifier.get("type").getAsString());
            assertTrue(modifier.get("key").getAsString().contains(":"));
            assertTrue(recipe.getAsJsonArray("components").size() > 0);
            assertTrue(recipe.getAsJsonObject("essentia").size() > 0);
        }

        JsonObject research = JsonParser.parseString(Files.readString(RESEARCH))
                .getAsJsonObject();
        assertFalse(research.get("inactive").getAsBoolean());
        int pages = 0;
        for (var element : research.getAsJsonArray("pages")) {
            JsonObject page = element.getAsJsonObject();
            if ("infusion".equals(page.get("type").getAsString())) pages++;
        }
        assertEquals(24, pages);
    }
}
