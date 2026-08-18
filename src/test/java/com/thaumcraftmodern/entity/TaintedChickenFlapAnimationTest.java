package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaintedChickenFlapAnimationTest {
    @Test
    void restingChickenHasNoPerpetualWingFlap() {
        TaintedChickenFlapAnimation animation =
                new TaintedChickenFlapAnimation();

        for (int tick = 0; tick < 100; tick++) {
            animation.tick(true);
        }

        assertEquals(0.0F, animation.sample(0.5F), 0.0001F);
    }

    @Test
    void airborneFlapSettlesAfterLanding() {
        TaintedChickenFlapAnimation animation =
                new TaintedChickenFlapAnimation();

        animation.tick(false);
        animation.tick(false);
        assertTrue(animation.sample(1.0F) > 0.0F);

        for (int tick = 0; tick < 5; tick++) {
            animation.tick(true);
        }

        assertEquals(0.0F, animation.sample(1.0F), 0.0001F);
    }
}
