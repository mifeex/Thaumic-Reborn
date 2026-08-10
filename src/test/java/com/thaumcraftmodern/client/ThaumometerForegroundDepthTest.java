package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumometerForegroundDepthTest {
    @Test
    void foregroundLayerDoesNotHideOptifineTranslucentWorldPasses()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "ClientRenderEvents.java"
        ));

        assertTrue(source.contains(
                "THAUMOMETER_DEPTH_FAR = 0.05D"
        ));
        assertTrue(source.contains(
                "renderThaumometerInForeground("
        ));
        assertTrue(source.contains(
                "GL11.glDepthRange(0.0D, 1.0D);"
        ));
        assertFalse(source.contains("GL11.glClear("));
        assertFalse(source.contains("RenderSystem.disableDepthTest()"));

        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ClassicThaumometerItemRenderer.java"
        ));
        assertTrue(renderer.contains("flush(buffers, frameRenderType);"));
        assertTrue(renderer.contains("GL11.glDepthMask(false);"));
        assertTrue(renderer.contains("flush(buffers, screenRenderType);"));
        assertTrue(renderer.contains("GL11.glDepthMask(previousDepthMask);"));

        String model = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/textures/models/"
                        + "scanner.obj"
        ));
        assertTrue(model.contains("v  -1.2500 0.1950 -1.2500"));
        assertFalse(model.contains("1.3100"));
    }
}
