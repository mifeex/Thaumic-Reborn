package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrucibleParticleFidelityTest {
    private static final Path ASSET_ROOT = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );
    private static final Path CRUCIBLE = Path.of(
            "src/main/java/com/thaumcraftmodern/world/block/entity/"
                    + "CrucibleBlockEntity.java"
    );

    @Test
    void bubbleSpritesAreExactOriginalAtlasFramesSixteenToEighteen()
            throws Exception {
        BufferedImage atlas = ImageIO.read(
                ASSET_ROOT.resolve("textures/misc/particles.png").toFile()
        );
        for (int frame = 0; frame < 3; frame++) {
            BufferedImage sprite = ImageIO.read(
                    ASSET_ROOT.resolve(
                            "textures/particle/crucible_bubble_"
                                    + frame + ".png"
                    ).toFile()
            );
            assertEquals(16, sprite.getWidth());
            assertEquals(16, sprite.getHeight());
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    assertEquals(
                            atlas.getRGB(frame * 16 + x, 16 + y),
                            sprite.getRGB(x, y),
                            "Mismatched TC4 particle pixel at frame="
                                    + frame + ", x=" + x + ", y=" + y
                    );
                }
            }
        }
    }

    @Test
    void overflowUsesEightSideFrothParticlesAndNoVanillaSplash()
            throws Exception {
        String source = Files.readString(CRUCIBLE);
        assertTrue(source.contains(
                "for (int index = 0; index < 2; index++)"
        ));
        assertEquals(5, count(source, "spawnFrothDown("));
        assertTrue(source.contains("ModParticles.CRUCIBLE_FROTH.get()"));
        assertTrue(source.contains("ModParticles.CRUCIBLE_BUBBLE.get()"));
        assertFalse(source.contains("ParticleTypes.SPLASH"));
        assertFalse(source.contains("ParticleTypes.BUBBLE_POP"));
    }

    private static int count(String source, String needle) {
        int matches = 0;
        int offset = 0;
        while ((offset = source.indexOf(needle, offset)) >= 0) {
            matches++;
            offset += needle.length();
        }
        return matches;
    }
}
