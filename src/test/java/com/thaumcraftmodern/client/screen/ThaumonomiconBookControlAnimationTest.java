package com.thaumcraftmodern.client.screen;

import net.minecraft.util.Mth;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconBookControlAnimationTest {
    @Test
    void usesExactTc4PlayerTickSineScale() {
        for (int ticks = 0; ticks < 80; ticks++) {
            float expected = 1.0F
                    + Mth.sin((float) ticks / 3.0F) * 0.2F
                    + 0.1F;
            assertEquals(expected,
                    ThaumonomiconOpenBookRenderer.controlScale(ticks),
                    0.000001F);
        }
    }

    @Test
    void animatesReturnPreviousAndNextAroundTheirCenters() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/"
                        + "ThaumonomiconOpenBookRenderer.java"
        ));
        assertTrue(renderer.contains(
                "graphics, left, top, ThaumonomiconBookLayout.BACK,"));
        assertTrue(renderer.contains(
                "graphics, left, top, ThaumonomiconBookLayout.PREVIOUS,"));
        assertTrue(renderer.contains(
                "graphics, left, top, ThaumonomiconBookLayout.NEXT,"));
        assertTrue(renderer.contains(
                "x + region.width() / 2.0F"));
        assertTrue(renderer.contains(
                "y + region.height() / 2.0F"));
        assertTrue(renderer.contains(
                "graphics.pose().scale(scale, scale, 1.0F)"));
    }
}
