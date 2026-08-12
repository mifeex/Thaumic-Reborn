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
                    ThaumonomiconScreen.bookControlScale(ticks), 0.000001F);
        }
    }

    @Test
    void animatesReturnPreviousAndNextAroundTheirCenters() throws Exception {
        String screen = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/"
                        + "ThaumonomiconScreen.java"
        ));
        assertTrue(screen.contains(
                "graphics, BACK_BUTTON,"));
        assertTrue(screen.contains(
                "graphics, PREVIOUS_BUTTON,"));
        assertTrue(screen.contains(
                "graphics, NEXT_BUTTON,"));
        assertTrue(screen.contains(
                "x + region.width() / 2.0F"));
        assertTrue(screen.contains(
                "y + region.height() / 2.0F"));
        assertTrue(screen.contains(
                "graphics.pose().scale(scale, scale, 1.0F)"));
    }
}
