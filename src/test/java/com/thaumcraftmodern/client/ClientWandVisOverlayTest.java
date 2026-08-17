package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientWandVisOverlayTest {
    @Test
    void classicHudTextureIsPresentAtOriginalResolution() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(
                "/assets/thaumic_reborn/textures/gui/hud.png"
        )) {
            assertNotNull(stream, "Missing original TC4 HUD texture");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Unreadable original TC4 HUD texture");
            assertEquals(256, image.getWidth());
            assertEquals(256, image.getHeight());
            assertTrue(image.getColorModel().hasAlpha());
        }
    }

    @Test
    void fillUsesTheClassicThirtyPixelScale() {
        assertEquals(0, ClientWandVisOverlay.fillPixels(0, 2_500));
        assertEquals(15, ClientWandVisOverlay.fillPixels(1_250, 2_500));
        assertEquals(30, ClientWandVisOverlay.fillPixels(2_500, 2_500));
        assertEquals(30, ClientWandVisOverlay.fillPixels(3_000, 2_500));
    }

    @Test
    void shiftReadoutMatchesClassicWholeVisDisplay() {
        assertEquals("0", ClientWandVisOverlay.formatVis(99));
        assertEquals("1", ClientWandVisOverlay.formatVis(100));
        assertEquals("25", ClientWandVisOverlay.formatVis(2_599));
    }
}
