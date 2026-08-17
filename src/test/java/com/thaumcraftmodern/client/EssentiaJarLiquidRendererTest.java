package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EssentiaJarLiquidRendererTest {
    @Test
    void usesFullAnimatedSpriteAndExactAspectRgb() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EssentiaJarBlockEntityRenderer.java"));

        assertTrue(renderer.contains("AspectRegistryRuntime.find(aspect)"));
        assertTrue(renderer.contains("TextureAtlas.LOCATION_BLOCKS"));
        assertTrue(renderer.contains("sprite.getU0()"));
        assertTrue(renderer.contains("sprite.getU1()"));
        assertTrue(renderer.contains("sprite.getV0()"));
        assertTrue(renderer.contains("sprite.getV1()"));
        assertTrue(renderer.contains(".color(red, green, blue, 255)"));
        assertTrue(renderer.contains("ClassicJarLiquidRenderType.get()"));

        String renderType = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ClassicJarLiquidRenderType.java"));
        assertTrue(renderType.contains("DefaultVertexFormat.POSITION_COLOR_TEX"));
        assertTrue(renderType.contains("POSITION_COLOR_TEX_SHADER"));
        assertTrue(renderType.contains("TRANSLUCENT_TRANSPARENCY"));
        assertTrue(renderType.contains("NO_CULL"));
        assertTrue(!renderer.contains("LightTexture"));
        assertTrue(!renderer.contains(".normal("));

        assertAspectColor("aer", "FFFF7E");
        assertAspectColor("terra", "56C000");
        assertAspectColor("ignis", "FF5A01");
        assertAspectColor("aqua", "3CD4FC");
        assertAspectColor("ordo", "D5D4EC");
        assertAspectColor("perditio", "404040");
        assertAspectColor("spiritus", "EBEBFB");
        assertAspectColor("corpus", "EE478D");
    }

    private static void assertAspectColor(String aspect, String color)
            throws Exception {
        String definition = Files.readString(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/aspects/"
                        + aspect + ".json"));
        assertTrue(definition.contains("\"color\": \"" + color + "\""));
    }
}
