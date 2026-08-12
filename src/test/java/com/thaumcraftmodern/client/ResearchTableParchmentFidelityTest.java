package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableParchmentFidelityTest {
    @Test
    void tabletopPaperMatchesTheResearchNoteState()
            throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ResearchTableBlockEntityRenderer.java"
        ));
        assertTrue(renderer.contains("for (int layer = 0; layer < layers; layer++)"));
        assertTrue(renderer.contains("LightTexture.FULL_BRIGHT"));
        assertTrue(renderer.contains("0xFFFFFF"));
        assertTrue(renderer.contains("textures/models/restable2.png"));
        assertTrue(renderer.contains("model.renderScroll("));
        assertTrue(renderer.contains(
                "if (notes.getItem() instanceof DiscoveryItem)"
        ));
        assertTrue(renderer.contains("} else {\n            renderParchment("));
        assertTrue(renderer.contains(
                "notes.getItem() instanceof ResearchNotesItem ? 6 : 1"
        ));
        assertTrue(renderer.contains("DiscoveryItem.color(notes)"));
        assertFalse(renderer.contains("ResearchNotesItem.color(notes)"));
        assertFalse(renderer.contains("renderParchmentStack("));

        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ResearchTableModel.java"
        ));
        assertTrue(model.contains("SCROLL_TUBE"));
        assertTrue(model.contains("SCROLL_RIBBON"));
        assertTrue(model.contains("poseStack.scale(1.2F, 1.2F, 1.2F);"));
    }
}
