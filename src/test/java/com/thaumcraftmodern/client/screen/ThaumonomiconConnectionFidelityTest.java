package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconConnectionFidelityTest {
    private static final Path SCREEN = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconScreen.java");
    private static final Path BROWSER_MODEL = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconBrowserModel.java");

    @Test
    void researchLinksUseExactTunableTc4LineStrip() throws Exception {
        String source = Files.readString(SCREEN);
        String modelSource = Files.readString(BROWSER_MODEL);
        for (String control : new String[]{
                "public static final float RESEARCH_CONNECTION_WIDTH",
                "public static final float RESEARCH_CONNECTION_ALPHA",
                "public static final float RESEARCH_CONNECTION_POINT_SPACING",
                "public static final float RESEARCH_CONNECTION_MAJOR_STEP_SCALE",
                "public static final float RESEARCH_CONNECTION_STEP_DECAY_SCALE",
                "public static final float RESEARCH_CONNECTION_WIGGLE_AMPLITUDE",
                "public static final float RESEARCH_CONNECTION_WIGGLE_X_PERIOD",
                "public static final float RESEARCH_CONNECTION_WIGGLE_Y_PERIOD"
        }) {
            assertTrue(source.contains(control), control);
        }
        assertTrue(source.contains("RenderType.guiOverlay()"));
        assertTrue(source.contains("drawConnectionSegment("));
        assertTrue(source.contains(
                "minecraft.getWindow().getGuiScale()"
        ));
        assertTrue(source.contains(
                "float halfWidth = renderedWidth / 2.0F"
        ));
        int firstLeft = source.indexOf(
                "x1 - normalX, y1 - normalY"
        );
        int firstRight = source.indexOf(
                "x1 + normalX, y1 + normalY"
        );
        int secondRight = source.indexOf(
                "x2 + normalX, y2 + normalY"
        );
        int secondLeft = source.indexOf(
                "x2 - normalX, y2 - normalY"
        );
        assertTrue(firstLeft < firstRight);
        assertTrue(firstRight < secondRight);
        assertTrue(secondRight < secondLeft);
        assertTrue(source.contains("alpha *= phase"));
        assertTrue(source.contains("Math.sin("));
        assertTrue(source.contains("graphics.flush()"));
        assertTrue(modelSource.contains(
                "for (String siblingId : definition.siblings())"
        ));
        assertTrue(modelSource.contains(
                "sibling.parents().contains(definition.id())"
        ));
        assertTrue(modelSource.contains("if (drawn.add(edge))"));
        assertFalse(source.contains("RESEARCH_CONNECTION_SHADOW"));
        assertFalse(source.contains("VertexFormat.Mode.TRIANGLE_STRIP"));
        assertFalse(source.contains("VertexFormat.Mode.LINE_STRIP"));
        assertFalse(source.contains("graphics.fill(x, y, x + 1, y + 1"));
    }
}
