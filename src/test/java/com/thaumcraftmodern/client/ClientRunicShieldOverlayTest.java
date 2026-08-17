package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientRunicShieldOverlayTest {
    @Test
    void creativeModeDoesNotRenderRunicShieldHearts() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientRunicShieldOverlay.java"));
        assertTrue(source.contains("minecraft.player.isCreative()"));
    }

    @Test
    void chargeIsNormalizedToTheClassicTenRuneBar() {
        assertEquals(0, ClientRunicShieldOverlay.visibleIconCount(0, 19));
        assertEquals(1, ClientRunicShieldOverlay.visibleIconCount(1, 19));
        assertEquals(3, ClientRunicShieldOverlay.visibleIconCount(4, 19));
        assertEquals(5, ClientRunicShieldOverlay.visibleIconCount(9, 19));
        assertEquals(10, ClientRunicShieldOverlay.visibleIconCount(19, 19));
    }

    @Test
    void barStartsAtTheVanillaHealthOrigin() {
        assertEquals(229, ClientRunicShieldOverlay.barLeft(640));
        assertEquals(321, ClientRunicShieldOverlay.barTop(360));
    }

    @Test
    void classicParticleAtlasIsAvailableAtOriginalResolution() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(
                "/assets/thaumic_reborn/textures/misc/particles.png"
        )) {
            assertNotNull(stream, "Missing original TC4 particle atlas");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Unreadable original TC4 particle atlas");
            assertEquals(256, image.getWidth());
            assertEquals(256, image.getHeight());
        }
    }
}
