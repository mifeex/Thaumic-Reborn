package com.thaumcraftmodern.worldgen.outerlands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class OuterLandsPortalCornerVoidTest {
    @Test
    void closedCornerVoidStartsAtTheFirstBlockAboveTheFloor() {
        OuterLandsCell portal = new OuterLandsCell(
                false, false, false, false, 1
        );

        assertTrue(OuterLandsLabyrinthGenerator
                .isClassicPortalVoidWall(portal, 2, 3, 1));
        assertTrue(OuterLandsLabyrinthGenerator
                .isClassicPortalVoidWall(portal, 3, 2, 1));
        assertFalse(OuterLandsLabyrinthGenerator
                .isClassicPortalVoidWall(portal, 2, 3, 0));
        assertFalse(OuterLandsLabyrinthGenerator
                .isClassicPortalVoidWall(portal, 3, 3, 1));
    }

    @Test
    void passageOpeningStillWinsOverTheVoidLayer() {
        OuterLandsCell northOpen = new OuterLandsCell(
                true, false, false, false, 1
        );

        assertFalse(OuterLandsLabyrinthGenerator
                .isClassicPortalVoidWall(northOpen, 4, 2, 1));
        assertTrue(OuterLandsLabyrinthGenerator
                .isClassicPortalVoidWall(northOpen, 3, 2, 1));
        assertTrue(OuterLandsLabyrinthGenerator
                .isClassicPortalVoidWall(northOpen, 4, 2, 10));
    }
}
