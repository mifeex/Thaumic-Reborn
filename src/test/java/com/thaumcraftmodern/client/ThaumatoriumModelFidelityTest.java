package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumatoriumModelFidelityTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn");

    @Test
    void originalZUpModelIsCenteredAndMadeYUpForForge() throws Exception {
        String model = Files.readString(
                ASSETS.resolve("models/block/thaumatorium_lower.json"));
        String adapted = Files.readString(
                ASSETS.resolve("textures/models/thaumatorium_block.obj"));
        String original = Files.readString(
                ASSETS.resolve("textures/models/thaumatorium.obj"));

        assertTrue(model.contains(
                "thaumic_reborn:textures/models/thaumatorium_block.obj"));
        assertTrue(adapted.contains("v 0.062500 0.125000 0.937500"));
        assertTrue(adapted.contains("v 0.500000 2.000000 0.288700"));
        assertTrue(original.contains("v  -0.4375 -0.4375 0.1250"));
        assertFalse(original.contains("Forge block-space adapter"));
    }

    @Test
    void westFacingOriginalIsRotatedToEachMachineFacing() throws Exception {
        JsonObject variants = JsonParser.parseString(Files.readString(
                ASSETS.resolve("blockstates/thaumatorium.json")))
                .getAsJsonObject().getAsJsonObject("variants");

        assertEquals(90, lower(variants, "north").get("y").getAsInt());
        assertEquals(180, lower(variants, "east").get("y").getAsInt());
        assertEquals(270, lower(variants, "south").get("y").getAsInt());
        assertFalse(lower(variants, "west").has("y"));
    }

    private static JsonObject lower(JsonObject variants, String facing) {
        return variants.getAsJsonObject("facing=" + facing + ",half=lower");
    }
}
