package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorldContentBlockPresentationTest {
    @Test
    void ancientStoneUsesOriginalSquareStoneSprite() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(
                "/assets/thaumic_reborn/textures/block/ancient_stone.png"
        )) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertNotNull(image);
            assertEquals(16, image.getWidth());
            assertEquals(16, image.getHeight());
        }
    }

    @Test
    void taintFibresItemUsesClassicPurpleTint() {
        assertEquals(
                0x6D4189,
                WorldContentClientEvents.CLASSIC_TAINT_FIBRES_ITEM_COLOR
        );
    }

    @Test
    void obsidianBaseHasAnInventoryModel() {
        assertNotNull(getClass().getResource(
                "/assets/thaumic_reborn/models/item/obsidian_tile.json"
        ));
    }

    @Test
    void silverwoodAuraNodeUsesClassicKnotTextures() throws Exception {
        String modelPath =
                "/assets/thaumic_reborn/models/block/silverwood_node.json";
        try (InputStream stream = getClass().getResourceAsStream(modelPath)) {
            assertNotNull(stream);
            String model = new String(
                    stream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
            assertTrue(model.contains(
                    "thaumic_reborn:block/silverwood_node_side"
            ));
            assertTrue(model.contains(
                    "thaumic_reborn:block/silverwood_node_top"
            ));
            assertTrue(!model.contains(
                    "thaumic_reborn:block/silverwood_log\""
            ));
        }

        assertClassicSprite(
                "/assets/thaumic_reborn/textures/block/"
                        + "silverwood_node_side.png"
        );
        assertClassicSprite(
                "/assets/thaumic_reborn/textures/block/"
                        + "silverwood_node_top.png"
        );
    }

    @Test
    void infusedStoneMasksHaveAnOpaqueStoneBacking() throws Exception {
        String modelPath =
                "/assets/thaumic_reborn/models/block/infused_stone.json";
        try (InputStream stream = getClass().getResourceAsStream(modelPath)) {
            assertNotNull(stream);
            JsonObject model = JsonParser.parseReader(
                    new java.io.InputStreamReader(
                            stream,
                            StandardCharsets.UTF_8
                    )
            ).getAsJsonObject();

            assertEquals(
                    "minecraft:cutout_mipped",
                    model.get("render_type").getAsString()
            );
            assertEquals(
                    "minecraft:block/stone",
                    model.getAsJsonObject("textures")
                            .get("backing")
                            .getAsString()
            );
            var elements = model.getAsJsonArray("elements");
            assertEquals(3, elements.size());
            JsonObject backing = elements.get(2).getAsJsonObject();
            assertEquals(0.08D, backing.getAsJsonArray("from")
                    .get(0).getAsDouble());
            assertEquals(15.92D, backing.getAsJsonArray("to")
                    .get(0).getAsDouble());
            for (String face : new String[]{
                    "down", "up", "north", "south", "west", "east"
            }) {
                JsonObject faceModel = backing.getAsJsonObject("faces")
                        .getAsJsonObject(face);
                assertEquals(
                        "#backing",
                        faceModel.get("texture").getAsString()
                );
                assertTrue(!faceModel.has("cullface"));
            }
        }
    }

    private static void assertClassicSprite(String path) throws Exception {
        try (InputStream stream =
                     WorldContentBlockPresentationTest.class
                             .getResourceAsStream(path)) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertNotNull(image);
            assertEquals(16, image.getWidth());
            assertEquals(16, image.getHeight());
        }
    }
}
