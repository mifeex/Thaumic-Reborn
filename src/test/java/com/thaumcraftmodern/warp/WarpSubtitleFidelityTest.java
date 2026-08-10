package com.thaumcraftmodern.warp;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WarpSubtitleFidelityTest {
    @Test
    void warpWhispersUsePurpleItalicBottomHudText() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/warp/WarpEvents.java"
        ));
        assertTrue(source.contains("new WarpFeedbackPacket("));
        assertTrue(source.contains("WarpFeedbackPacket.VISUAL_NONE"));
        assertFalse(source.contains("player.sendSystemMessage("));
        assertFalse(source.contains("player.displayClientMessage("));

        String overlay = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "ClientScanOverlay.java"
        ));
        assertTrue(overlay.contains("public static void showWarp("));
        assertTrue(overlay.contains("ChatFormatting.DARK_PURPLE"));
        assertTrue(overlay.contains("ChatFormatting.ITALIC"));
        assertTrue(overlay.contains("messageKey.startsWith(\"warp.text.\")"));
    }
}
