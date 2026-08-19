package com.thaumcraftmodern.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimalArrowThaumonomiconFidelityTest {
    @Test
    void oneClassicRecipePageCyclesAllSixPrimalArrows() throws Exception {
        JsonObject research = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy/primalarrow.json"
        ))).getAsJsonObject();
        JsonArray pages = research.getAsJsonArray("pages");
        assertEquals(4, pages.size(),
                "TC4 PRIMALARROW has text, recipe carousel, text, text");

        JsonObject recipePage = pages.get(1).getAsJsonObject();
        assertEquals("recipe", recipePage.get("type").getAsString());
        List<String> recipes = recipePage.getAsJsonArray("recipes").asList()
                .stream().map(element -> element.getAsString()).toList();
        assertEquals(List.of(
                "thaumic_reborn:primal_arrow_aer",
                "thaumic_reborn:primal_arrow_ignis",
                "thaumic_reborn:primal_arrow_aqua",
                "thaumic_reborn:primal_arrow_terra",
                "thaumic_reborn:primal_arrow_ordo",
                "thaumic_reborn:primal_arrow_perditio"
        ), recipes);

        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ThaumonomiconPageRenderer.java"
        ));
        assertTrue(renderer.contains("recipes.size()"));
        assertTrue(renderer.contains("System.currentTimeMillis() / 1000L"));
    }
}
