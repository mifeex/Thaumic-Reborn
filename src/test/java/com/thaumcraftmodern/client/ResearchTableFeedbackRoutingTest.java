package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableFeedbackRoutingTest {
    @Test
    void tableFeedbackUsesItsBottomCornerQueueInsteadOfChatOrActionBar()
            throws Exception {
        String menu = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/menu/ResearchTableMenu.java"
        ));
        String overlay = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientResearchTableOverlay.java"
        ));
        String screen = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ResearchTableScreen.java"
        ));

        assertFalse(menu.contains("displayClientMessage("));
        assertFalse(menu.contains("sendSystemMessage("));
        assertTrue(menu.contains("new ResearchTableFeedbackPacket(message, success)"));
        assertTrue(overlay.contains("private static final Deque<Entry> QUEUE"));
        assertTrue(overlay.contains("screenWidth - minecraft.font.width(line) - 12"));
        assertTrue(screen.contains("ClientResearchTableOverlay.renderScreen("));
    }
}
