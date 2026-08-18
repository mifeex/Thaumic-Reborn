package com.thaumcraftmodern.warp;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WarpEventVignetteFidelityTest {
    @Test
    void genericWarpEventKeepsHeartbeatWithoutMandatoryPurpleVignette()
            throws Exception {
        String overlay = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientWarpOverlay.java"
        ));

        assertTrue(overlay.contains(
                "packet.visual() == WarpFeedbackPacket.VISUAL_EVENT"
        ));
        assertTrue(overlay.contains("ModSounds.HEARTBEAT.get()"));
        assertFalse(overlay.contains("vignetteUntil"));
        assertFalse(overlay.contains("0x430044"));
    }
}
