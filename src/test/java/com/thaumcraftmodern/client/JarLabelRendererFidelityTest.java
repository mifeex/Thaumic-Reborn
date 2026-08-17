package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JarLabelRendererFidelityTest {
    @Test
    void normalAndVoidJarsRenderClassicPaperAndAspectGlyph() throws Exception {
        String shared = read("src/main/java/com/thaumcraftmodern/client/render/ClassicJarLabelRenderer.java");
        String normal = read("src/main/java/com/thaumcraftmodern/client/render/EssentiaJarBlockEntityRenderer.java");
        String voidJar = read("src/main/java/com/thaumcraftmodern/client/render/VoidJarBlockEntityRenderer.java");
        String normalBlock = read("src/main/java/com/thaumcraftmodern/world/block/EssentiaJarBlock.java");
        String voidBlock = read("src/main/java/com/thaumcraftmodern/world/block/VoidJarBlock.java");
        String original = read("reference/Thaumcraft-4.2-FOREVA-master/src/main/java/"
                + "thaumcraft/client/renderers/tile/TileJarRenderer.java");
        assertTrue(shared.contains("textures/models/label.png"));
        assertTrue(shared.contains("SURFACE_OFFSET = 0.315F"));
        assertTrue(shared.contains("ASPECT_HALF_SIZE = 8.0F * 0.021F"));
        assertTrue(shared.contains("ASPECT_GRAY = 0x808080"));
        assertFalse(shared.contains("aspect.color()"));
        assertTrue(shared.contains("textures/aspects_label/"));
        assertTrue(original.contains("GlStateManager.scale(0.021F, 0.021F, 0.021F)"));
        assertTrue(original.contains("TileRenderHelper.drawTexturedQuad(8.0F"));
        assertTrue(shared.contains("AspectRegistryRuntime.find"));
        assertTrue(normal.contains("ClassicJarLabelRenderer.render"));
        assertTrue(voidJar.contains("ClassicJarLabelRenderer.render"));
        assertTrue(normalBlock.contains("player.getDirection().getOpposite()"));
        assertTrue(voidBlock.contains("player.getDirection().getOpposite()"));
    }

    @Test
    void labelMaskRemovesBlackAndKeepsTheFill() throws Exception {
        BufferedImage mask = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumic_reborn/textures/"
                        + "aspects_label/ignis.png").toFile());

        assertEquals(0, mask.getRGB(13, 0) >>> 24);
        assertEquals(255, mask.getRGB(9, 7) >>> 24);
        assertEquals(0xFFFFFF, mask.getRGB(9, 7) & 0xFFFFFF);
    }

    private static String read(String path) throws Exception {
        return Files.readString(Path.of(path));
    }
}
