package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ScribingToolsInkFidelityTest {
    @Test
    void finalInkUseLeavesAnEmptyRefillableInkwell() {
        assertEquals(100, ScribingToolsInk.nextDamage(99, 100));
        assertEquals(100, ScribingToolsInk.nextDamage(100, 100));
    }
}
