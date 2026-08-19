package com.thaumcraftmodern.research;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceptreThaumonomiconFidelityTest {
    @Test
    void recipePageCyclesTheThreeOriginalConcreteExamples() throws Exception {
        JsonObject research = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy/sceptre.json"
        ))).getAsJsonObject();
        JsonObject page = research.getAsJsonArray("pages").get(1).getAsJsonObject();
        assertEquals(List.of(
                "thaumic_reborn:sceptre_wood_iron",
                "thaumic_reborn:sceptre_greatwood_gold",
                "thaumic_reborn:sceptre_silverwood_thaumium"
        ), page.getAsJsonArray("recipes").asList().stream()
                .map(element -> element.getAsString()).toList());

        for (String recipe : List.of(
                "sceptre_wood_iron.json",
                "sceptre_greatwood_gold.json",
                "sceptre_silverwood_thaumium.json"
        )) {
            JsonObject definition = JsonParser.parseString(Files.readString(
                    Path.of("src/main/resources/data/thaumic_reborn/recipes")
                            .resolve(recipe))).getAsJsonObject();
            assertTrue(definition.has("rod"));
            assertTrue(definition.has("cap"));
        }

        String assembly = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/arcane/ArcaneWandAssemblyRecipe.java"
        ));
        assertTrue(assembly.contains("if (lockedRodId == null || lockedCapId == null)"));
        assertTrue(assembly.contains("amounts.put(primal, cost)"));
    }
}
