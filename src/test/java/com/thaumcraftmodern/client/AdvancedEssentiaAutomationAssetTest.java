package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.essentia.AdvancedBufferSideRole;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AdvancedEssentiaAutomationAssetTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn");

    @Test
    void reversibleControllerTextureIsATransparentGameSizedPng()
            throws Exception {
        BufferedImage texture = ImageIO.read(ASSETS.resolve(
                "textures/block/pipe_reverse_controller.png").toFile());
        assertEquals(32, texture.getWidth());
        assertEquals(32, texture.getHeight());
        assertTrue(texture.getColorModel().hasAlpha());
        assertEquals(0, texture.getRGB(0, 0) >>> 24);
    }

    @Test
    void reversibleRendererHidesTheInactiveHeadArrow() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EssentiaTubeBlockEntityRenderer.java"));
        assertTrue(renderer.contains(
                "if (!tube.reversibleArrowVisible()) return;"));
    }

    @Test
    void improvedBufferKeepsOriginalBodyAndSmallRoleLamps() throws Exception {
        JsonObject reverseItem = read(
                "models/item/reversible_essentia_tube.json");
        assertEquals("minecraft:block/block",
                reverseItem.get("parent").getAsString());
        assertEquals(6, reverseItem.getAsJsonArray("elements").size());
        assertEquals("thaumic_reborn:block/pipe_3",
                reverseItem.getAsJsonObject("textures")
                        .get("joint").getAsString());
        assertEquals("thaumic_reborn:block/pipe_reverse_arrow",
                reverseItem.getAsJsonObject("textures")
                        .get("arrow").getAsString());
        for (int element = 2; element < 6; element++) {
            JsonObject arrow = reverseItem.getAsJsonArray("elements")
                    .get(element).getAsJsonObject();
            for (var coordinate : List.of(
                    arrow.getAsJsonArray("from"),
                    arrow.getAsJsonArray("to")
            )) {
                for (int axis : new int[]{0, 2}) {
                    double value = coordinate.get(axis).getAsDouble();
                    assertTrue(value >= 6.99D && value <= 9.01D,
                            "GUI arrow must stay within the tube silhouette");
                }
            }
        }
        JsonObject reverseBlock = read(
                "models/block/reversible_essentia_tube.json");
        JsonObject reverseJoint = reverseBlock.getAsJsonArray("elements")
                .get(0).getAsJsonObject();
        assertEquals("[6.5,6.5,6.5]",
                reverseJoint.getAsJsonArray("from").toString());
        assertEquals("[9.5,9.5,9.5]",
                reverseJoint.getAsJsonArray("to").toString());
        assertEquals("thaumic_reborn:block/pipe_3",
                reverseBlock.getAsJsonObject("textures")
                        .get("joint").getAsString());
        BufferedImage reverseArrow = ImageIO.read(ASSETS.resolve(
                "textures/block/pipe_reverse_arrow.png").toFile());
        assertEquals(16, reverseArrow.getWidth());
        assertEquals(16, reverseArrow.getHeight());
        assertEquals(0, reverseArrow.getRGB(0, 0) >>> 24);
        assertTrue((reverseArrow.getRGB(7, 4) >>> 24) > 0,
                "Reverse arrow must have an opaque cyan centre");

        JsonObject buffer = read(
                "models/block/advanced_essentia_buffer.json");
        assertEquals(7, buffer.getAsJsonArray("elements").size());
        assertEquals("thaumic_reborn:block/advanced_buffer_indicator",
                buffer.getAsJsonObject("textures")
                        .get("indicator").getAsString());
        JsonObject body = buffer.getAsJsonArray("elements").get(0)
                .getAsJsonObject();
        assertEquals("[4,4,4]", body.getAsJsonArray("from").toString());
        assertEquals("[12,12,12]", body.getAsJsonArray("to").toString());
        for (int element = 1; element <= 6; element++) {
            JsonObject lamp = buffer.getAsJsonArray("elements").get(element)
                    .getAsJsonObject();
            String from = lamp.getAsJsonArray("from").toString();
            String to = lamp.getAsJsonArray("to").toString();
            assertTrue(from.contains("7") && to.contains("9"),
                    "Role lamp must remain a 2x2 centre marker");
            JsonObject face = lamp.getAsJsonObject("faces")
                    .entrySet().iterator().next().getValue()
                    .getAsJsonObject();
            assertEquals("[0,0,16,16]",
                    face.getAsJsonArray("uv").toString());
        }
        BufferedImage indicator = ImageIO.read(ASSETS.resolve(
                "textures/block/advanced_buffer_indicator.png").toFile());
        assertEquals(16, indicator.getWidth());
        assertEquals(16, indicator.getHeight());
        assertTrue((indicator.getRGB(1, 1) & 0xFF)
                        > (indicator.getRGB(14, 14) & 0xFF),
                "Role tint mask must fade from the light upper-left corner "
                        + "to the dark lower-right corner");
        assertEquals(0x2B83B6, AdvancedBufferSideRole.INPUT.indicatorColor());
        assertEquals(0x159B7E,
                AdvancedBufferSideRole.MAIN_OUTPUT.indicatorColor());
        assertEquals(0xC65D12,
                AdvancedBufferSideRole.RESERVE_OUTPUT.indicatorColor());
        assertEquals(0x8F414B,
                AdvancedBufferSideRole.BLOCKED.indicatorColor());
    }

    private static JsonObject read(String path) throws Exception {
        return JsonParser.parseString(Files.readString(ASSETS.resolve(path)))
                .getAsJsonObject();
    }
}
