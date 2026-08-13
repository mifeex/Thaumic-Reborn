package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class TravelingTrunkPresentationFidelityTest {
    @Test
    void rendererSeatsSixteenPixelModelOnModernGroundPlane()
            throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "TravelingTrunkRenderer.java"
        ));
        int scale = renderer.indexOf(
                "poses.scale(inverse * scale, .5F / inverse * scale, inverse * scale)"
        );
        int translation = renderer.indexOf(
                "poses.translate(0F, .5F, 0F)", scale
        );
        assertTrue(scale >= 0);
        assertTrue(translation > scale);
    }

    @Test
    void woodenBodyAndLidHaveDamageDeathAndContainerSoundsOnly()
            throws Exception {
        String entity = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/entity/"
                        + "TravelingTrunkEntity.java"
        ));
        assertTrue(entity.contains("SoundEvents.WOOD_STEP"));
        assertTrue(entity.contains("SoundEvents.WOOD_HIT"));
        assertTrue(entity.contains("SoundEvents.WOOD_BREAK"));
        assertFalse(entity.contains("SoundEvents.CHEST_CLOSE, .1F"));
        assertTrue(entity.contains(".9F + random.nextFloat() * .1F"));
        assertTrue(entity.contains(
                "open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE"
        ));
        assertTrue(entity.contains("previous != open"));
    }
}
