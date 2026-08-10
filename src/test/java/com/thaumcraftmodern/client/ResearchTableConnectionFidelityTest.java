package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableConnectionFidelityTest {
    @Test
    void connectionsUseClassicGpuLineInsteadOfPixelStaircase()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/"
                        + "ResearchTableScreen.java"
        ));
        assertTrue(source.contains("drawClassicConnection("));
        assertTrue(source.contains("RenderSystem.lineWidth(3.0F);"));
        assertTrue(source.contains("VertexFormat.Mode.DEBUG_LINE_STRIP"));
        assertTrue(source.contains("GlStateManager.DestFactor.ONE"));
        assertFalse(source.contains("int steps = Math.max("));
    }
}
