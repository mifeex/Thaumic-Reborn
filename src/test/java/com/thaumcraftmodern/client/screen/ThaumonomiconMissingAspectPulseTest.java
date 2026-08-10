package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ThaumonomiconMissingAspectPulseTest {
    @Test
    void matchesTc4SixHundredMillisecondFade() {
        assertEquals(0.75F, ThaumonomiconAspectCostRenderer.missingAspectAlpha(0L),
                0.0001F);
        assertEquals(1.0F, ThaumonomiconAspectCostRenderer.missingAspectAlpha(150L),
                0.0001F);
        assertEquals(0.75F, ThaumonomiconAspectCostRenderer.missingAspectAlpha(300L),
                0.0001F);
        assertEquals(0.5F, ThaumonomiconAspectCostRenderer.missingAspectAlpha(450L),
                0.0001F);
        assertEquals(0.75F, ThaumonomiconAspectCostRenderer.missingAspectAlpha(600L),
                0.0001F);
    }
}
