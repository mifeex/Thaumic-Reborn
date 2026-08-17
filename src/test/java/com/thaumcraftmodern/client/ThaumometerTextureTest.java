package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumometerTextureTest {
    @Test
    void scannerModelTextureKeepsTheClassicResolutionAndAlpha() throws IOException {
        assertTexture(
                "/assets/thaumic_reborn/textures/models/scanner.png",
                256,
                256
        );
        assertTexture(
                "/assets/thaumic_reborn/textures/item/scanner.png",
                256,
                256
        );
    }

    @Test
    void scanScreenKeepsTheClassicResolutionAndAlpha() throws IOException {
        assertTexture(
                "/assets/thaumic_reborn/textures/models/scanscreen.png",
                128,
                128
        );
        assertTexture(
                "/assets/thaumic_reborn/textures/item/scanscreen.png",
                128,
                128
        );
    }

    private static void assertTexture(
            String resourcePath,
            int expectedWidth,
            int expectedHeight
    ) throws IOException {
        try (InputStream stream =
                     ThaumometerTextureTest.class.getResourceAsStream(resourcePath)) {
            assertNotNull(stream, "Missing texture " + resourcePath);
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Unreadable texture " + resourcePath);
            assertEquals(expectedWidth, image.getWidth());
            assertEquals(expectedHeight, image.getHeight());
            assertTrue(image.getColorModel().hasAlpha());
        }
    }
}
