package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableFeedbackRoutingTest {
    @Test
    void successfulCombinationReusesThaumometerAspectNotification()
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

        assertTrue(menu.contains("new ScanFeedbackPacket("));
        assertTrue(menu.contains("new ScanFeedbackPacket.AspectGain("));
        assertTrue(menu.contains("result.newlyDiscovered()"));
        assertTrue(menu.contains("displayClientMessage("));
        assertTrue(menu.contains(
                "message.thaumic_reborn.scan.aspect_discovered"
        ));
        String failedCombination = menu.substring(
                menu.indexOf("if (!result.combined())"),
                menu.indexOf("playCombinationResultSound(player, true)")
        );
        assertTrue(!failedCombination.contains("sendFeedback("));
        assertTrue(failedCombination.contains("KnowledgeSync.send("));
        assertTrue(menu.contains("new ResearchTableFeedbackPacket(message, success)"));
        assertTrue(overlay.contains("private static final Deque<Entry> QUEUE"));
        assertTrue(overlay.contains("screenWidth - minecraft.font.width(line) - 12"));
        assertTrue(screen.contains("ClientResearchTableOverlay.renderScreen("));
    }
}
