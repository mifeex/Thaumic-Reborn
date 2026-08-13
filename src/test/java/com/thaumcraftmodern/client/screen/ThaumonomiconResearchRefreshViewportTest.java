package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumonomiconResearchRefreshViewportTest {
    @Test
    void knowledgeRefreshPreservesCurrentTreeViewport() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ThaumonomiconScreen.java"
        ));
        String refreshMethod = source.substring(
                source.indexOf("public void refreshResearchData()"),
                source.indexOf("private void rebuildBrowserModel()")
        );

        assertTrue(refreshMethod.contains("clampTreePan();"));
        assertFalse(refreshMethod.contains("centerSelectedCategory();"));
    }
}
