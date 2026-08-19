package com.thaumcraftmodern.warp;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarpGainNotificationFidelityTest {
    @Test
    void gainFeedbackUsesRightHandWarpLaneAndHidesTemporaryType() throws Exception {
        String warp = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientWarpOverlay.java"
        ));
        String notifications = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientScanOverlay.java"
        ));

        assertTrue(warp.contains("ClientScanOverlay.showWarp(key)"));
        assertFalse(warp.contains("displayClientMessage"));
        assertTrue(warp.contains("case WarpFeedbackPacket.NORMAL,"));
        assertTrue(warp.contains("WarpFeedbackPacket.TEMPORARY ->"));
        assertTrue(warp.contains("? \"tc.removewarpsticky\""));
        assertTrue(warp.contains(": \"tc.addwarpsticky\""));
        assertTrue(notifications.contains(
                "messageKey.startsWith(\"tc.addwarp\")"
        ));
        assertTrue(notifications.contains(
                "messageKey.startsWith(\"tc.removewarp\")"
        ));
    }
}
