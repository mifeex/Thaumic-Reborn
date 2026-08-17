package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NitorVisualFidelityTest {
    @Test
    void placedNitorIsInvisibleAndTicksTheClassicWispSource()
            throws Exception {
        String block = read("src/main/java/com/thaumcraftmodern/world/block/"
                + "NitorBlock.java");
        String tile = read("src/main/java/com/thaumcraftmodern/world/block/"
                + "entity/NitorBlockEntity.java");

        assertTrue(block.contains("return RenderShape.INVISIBLE;"));
        assertTrue(block.contains("NitorBlockEntity::clientTick"));
        assertTrue(tile.contains("LARGE_WISP_INTERVAL = 5"));
        assertTrue(tile.contains("SMALL_WISP_INTERVAL = 7"));
        assertTrue(tile.contains("+ 0.3D"));
        assertTrue(tile.contains("* 0.4D"));
        assertTrue(tile.contains("+ 0.4D"));
        assertTrue(tile.contains("* 0.2D"));
    }

    @Test
    void particlesUseTc4MotionColorAndAdditiveGlow() throws Exception {
        String particle = read("src/main/java/com/thaumcraftmodern/client/"
                + "particle/NitorWispParticle.java");
        String renderType = read("src/main/java/com/thaumcraftmodern/client/"
                + "particle/NitorParticleRenderType.java");

        assertTrue(particle.contains("LARGE_GRAVITY = -0.025F"));
        assertTrue(particle.contains("SMALL_GRAVITY = -0.02F"));
        assertTrue(particle.contains("36.0D / (random.nextDouble() * 0.3D + 0.7D)"));
        assertTrue(particle.contains("alpha = 0.5F"));
        assertTrue(particle.contains("return 0x00F000F0;"));
        assertTrue(renderType.contains("DestFactor.ONE"));
    }

    @Test
    void particleSpriteIsExactTc4AtlasCrop() throws Exception {
        BufferedImage atlas = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumic_reborn/textures/misc/"
                        + "particles.png").toFile());
        BufferedImage sprite = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumic_reborn/textures/particle/"
                        + "nitor_wisp.png").toFile());
        assertEquals(32, sprite.getWidth());
        assertEquals(32, sprite.getHeight());
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                assertEquals(atlas.getRGB(x, 224 + y), sprite.getRGB(x, y));
            }
        }

        JsonObject large = particleJson("nitor_wisp_large");
        JsonObject small = particleJson("nitor_wisp_small");
        assertEquals("thaumic_reborn:nitor_wisp",
                large.getAsJsonArray("textures").get(0).getAsString());
        assertEquals("thaumic_reborn:nitor_wisp",
                small.getAsJsonArray("textures").get(0).getAsString());
    }

    private static JsonObject particleJson(String name) throws Exception {
        return JsonParser.parseString(read(
                "src/main/resources/assets/thaumic_reborn/particles/"
                        + name + ".json")).getAsJsonObject();
    }

    private static String read(String path) throws Exception {
        return Files.readString(Path.of(path));
    }
}
