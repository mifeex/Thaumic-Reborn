package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableParchmentFidelityTest {
    @Test
    void parchmentStaysWhiteAndResearchColorBelongsToSeparateScroll()
            throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ResearchTableBlockEntityRenderer.java"
        ));
        assertTrue(renderer.contains("for (int layer = 0; layer < 6; layer++)"));
        assertTrue(renderer.contains("LightTexture.FULL_BRIGHT"));
        assertTrue(renderer.contains("0xFFFFFF"));
        assertTrue(renderer.contains("textures/models/restable2.png"));
        assertTrue(renderer.contains("model.renderScroll("));
        assertFalse(renderer.contains("notes.isEmpty() ? 1 : 5"));

        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ResearchTableModel.java"
        ));
        assertTrue(model.contains("SCROLL_TUBE"));
        assertTrue(model.contains("SCROLL_RIBBON"));
        assertTrue(model.contains("poseStack.scale(1.2F, 1.2F, 1.2F);"));
    }
}
