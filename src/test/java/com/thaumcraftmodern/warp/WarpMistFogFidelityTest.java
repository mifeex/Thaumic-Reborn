package com.thaumcraftmodern.warp;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WarpMistFogFidelityTest {
    @Test
    void mistUsesWorldFogInsteadOfAFullscreenDarkOverlay() throws Exception {
        String overlay = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientWarpOverlay.java"
        ));
        String fog = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientWarpFogEvents.java"
        ));

        assertTrue(overlay.contains("MIST_DURATION_TICKS = 2_400"));
        assertFalse(overlay.contains("mistUntil"));
        assertTrue(fog.contains("ViewportEvent.RenderFog"));
        assertTrue(fog.contains("event.setNearPlaneDistance("));
        assertTrue(fog.contains("event.setFarPlaneDistance("));
        assertTrue(fog.contains("event.setCanceled(true)"));
        assertTrue(fog.contains("ViewportEvent.ComputeFogColor"));
    }
}
