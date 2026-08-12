package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconNavigationHistoryTest {
    @Test
    void nestedResearchReturnsToExactSourcePagesInLifoOrder() {
        ThaumonomiconNavigationHistory history =
                new ThaumonomiconNavigationHistory();
        history.push("tubes", 4, "alchemy");
        history.push("tubefilter", 2, "alchemy");

        assertEquals(2, history.depth());
        assertEquals(new ThaumonomiconNavigationHistory.Location(
                "tubefilter", 2, "alchemy"), history.pop().orElseThrow());
        assertEquals(new ThaumonomiconNavigationHistory.Location(
                "tubes", 4, "alchemy"), history.pop().orElseThrow());
        assertTrue(history.pop().isEmpty());
    }

    @Test
    void screenLinksOnlyCompletedResearchAndUsesSamePopForButtonAndEscape()
            throws Exception {
        String screen = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ThaumonomiconScreen.java"))
                + Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ThaumonomiconOpenBookRenderer.java"))
                + Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ThaumonomiconNavigationController.java"));
        assertTrue(screen.contains(".filter(research -> isCompleted(research.id()))"));
        assertTrue(screen.contains("research.iconItem().equals(itemId.toString())"));
        assertTrue(screen.contains("researchProducesItem(research, itemId)"));
        assertTrue(screen.contains("renderItemLinkTooltip("));
        assertTrue(screen.contains("thaumonomicon.open_item_page"));
        assertTrue(screen.contains("leaveResearchLevel(\"button\")"));
        assertTrue(screen.contains("leaveResearchLevel(\"escape\")"));
        assertTrue(screen.contains("pagePair = previous.pagePair()"));
        assertTrue(screen.contains("selectedCategoryId = result.categoryId()"));
        assertTrue(screen.contains("pagePair = Math.max(0, pagePair - 2)"));
        assertTrue(screen.contains("pagePair += 2"));
    }
}
