package com.thaumcraftmodern.client.screen;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconDecompositionTest {
    private static final Path ROOT = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen"
    );

    @Test
    void screenDelegatesBookPagesTooltipNavigationAndNodeHitTesting()
            throws Exception {
        String screen = Files.readString(ROOT.resolve(
                "ThaumonomiconScreen.java"
        ));

        assertTrue(screen.contains("openBookRenderer.render("));
        assertTrue(screen.contains("researchTooltipRenderer.render("));
        assertTrue(screen.contains("navigation.openRoot(research)"));
        assertTrue(screen.contains("navigation.openLinked("));
        assertTrue(screen.contains(
                "ThaumonomiconResearchInteraction\n"
                        + "                    .researchAt("
        ));
        assertFalse(screen.contains("private void renderPage("));
        assertFalse(screen.contains("private void renderInfusionDisplay("));
        assertFalse(screen.contains("private void renderResearchTooltip("));
    }
}
