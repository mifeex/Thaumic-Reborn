package com.thaumcraftmodern.client.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class PechHeldItemFidelityTest {
    @Test
    void pechUsesItsOriginalHeldItemTransformWithoutVanillaOffset()
            throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "PechRenderer.java"));
        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "PechModel.java"));

        assertTrue(renderer.contains("class PechHeldItemLayer"));
        assertFalse(renderer.contains("new ItemInHandLayer"));
        assertTrue(renderer.contains("Axis.XP.rotationDegrees(-90.0F)"));
        assertTrue(renderer.contains("Axis.YP.rotationDegrees(180.0F)"));
        assertTrue(renderer.contains(
                "ItemDisplayContext.THIRD_PERSON_RIGHT_HAND"));
        assertTrue(model.contains("0.3375D"));
        assertFalse(renderer.contains("-0.625"));
    }
}
