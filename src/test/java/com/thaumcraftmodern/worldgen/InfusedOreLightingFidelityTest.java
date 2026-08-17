package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class InfusedOreLightingFidelityTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void coloredOreLayerUsesWeakForgeEmissivity() throws IOException {
        JsonObject model = JsonParser.parseString(Files.readString(ROOT.resolve(
                "assets/thaumic_reborn/models/block/infused_stone.json"
        ))).getAsJsonObject();
        JsonObject faces = model.getAsJsonArray("elements")
                .get(1).getAsJsonObject().getAsJsonObject("faces");
        for (String direction : new String[]{
                "down", "up", "north", "south", "west", "east"
        }) {
            JsonObject face = faces.getAsJsonObject(direction);
            assertEquals(0, face.get("tintindex").getAsInt());
            assertEquals(4, face.getAsJsonObject("forge_data")
                    .get("block_light").getAsInt());
        }
    }

    @Test
    void stoneAndDeepslateInfusedOresEmitWeakBlockLight() throws IOException {
        String blocks = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModBlocks.java"
        ));
        for (String aspect : new String[]{
                "air", "fire", "water", "earth", "order", "entropy"
        }) {
            assertTrue(blocks.contains(
                    "infusedOre(\"" + aspect + "_infused_stone\""
            ));
            assertTrue(blocks.contains(
                    "deepslateInfusedOre(\"deepslate_" + aspect
                            + "_infused_stone\""
            ));
        }
        assertTrue(blocks.contains(".lightLevel(state -> 4)"));
    }
}
