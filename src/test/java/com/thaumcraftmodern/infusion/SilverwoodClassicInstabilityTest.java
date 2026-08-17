package com.thaumcraftmodern.infusion;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SilverwoodClassicInstabilityTest {
    @Test
    void silverwoodRodUsesClassicOriginalInstability() throws Exception {
        var recipe = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes/wand_rod_silverwood.json"
        ))).getAsJsonObject();
        assertEquals(5, recipe.get("instability").getAsInt());

        var research = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy/rod_silverwood.json"
        ))).getAsJsonObject();
        assertEquals("moderate", research.getAsJsonArray("pages")
                .get(1).getAsJsonObject().get("instability").getAsString());
    }
}
