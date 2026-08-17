package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTablePlacementPreviewTest {
    @Test
    void draggedAspectRendersAbovePaletteAmounts() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ResearchTableScreen.java"
        ));
        assertTrue(source.contains("DRAGGED_ASPECT_Z = 20.0F"));
        int start = source.indexOf("private void renderDraggedAspect(");
        int end = source.indexOf("private int cellX(", start);
        String method = source.substring(start, end);
        assertTrue(method.contains(
                "graphics.pose().translate(0.0F, 0.0F, DRAGGED_ASPECT_Z)"
        ));
        assertTrue(method.indexOf("pushPose()") < method.indexOf("drawAspect("));
        assertTrue(method.indexOf("drawAspect(") < method.indexOf("popPose()"));
    }

    @Test
    void draggedAspectHasNoOrangeHexTargetFrame() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ResearchTableScreen.java"
        ));
        assertFalse(source.contains("SELECTED_HEX"));
        assertFalse(source.contains("textures/gui/hex2.png"));
        int start = source.indexOf("private void renderDropTargetHighlights(");
        int end = source.indexOf("@Override\n    protected void renderLabels", start);
        String method = source.substring(start, end);

        assertFalse(method.contains("SELECTED_HEX"));
        assertFalse(method.contains("validatePlacement"));
        assertTrue(source.contains("renderConnections(graphics, puzzle, knowledge)"));
        assertTrue(source.contains(
                "renderHoveredEmptyCellOutline(graphics, mouseX, mouseY)"
        ));
        assertTrue(source.contains("textures/gui/hex_hover_white.png"));
        int hoverStart = source.indexOf("private void renderHoveredEmptyCellOutline(");
        int hoverEnd = source.indexOf("private HexResearchPuzzle.Cell hoveredPuzzleCellByCenter(", hoverStart);
        String hoverMethod = source.substring(hoverStart, hoverEnd);
        assertTrue(hoverMethod.contains("puzzle.aspectAt(hovered).isEmpty()"));
        assertTrue(source.contains("color = lightenColor(color)"));
        assertTrue(source.contains("Math.round((255 - red) * 0.15F)"));
        assertTrue(source.contains("0xA0FFFFFF"));
        assertFalse(source.contains("drawThinHexOutline("));
        assertFalse(source.contains("drawOnePixelLine("));
    }

    @Test
    void whiteHoverHexPreservesTheOrdinaryHexAlphaMask() throws Exception {
        BufferedImage ordinary = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumic_reborn/textures/gui/hex1.png"
        ).toFile());
        BufferedImage hovered = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumic_reborn/textures/gui/hex_hover_white.png"
        ).toFile());
        assertNotNull(ordinary);
        assertNotNull(hovered);
        assertEquals(ordinary.getWidth(), hovered.getWidth());
        assertEquals(ordinary.getHeight(), hovered.getHeight());
        for (int y = 0; y < ordinary.getHeight(); y++) {
            for (int x = 0; x < ordinary.getWidth(); x++) {
                int ordinaryAlpha = ordinary.getRGB(x, y) >>> 24;
                int hoveredPixel = hovered.getRGB(x, y);
                assertEquals(ordinaryAlpha, hoveredPixel >>> 24);
                if (ordinaryAlpha != 0) {
                    assertEquals(0xFFFFFF, hoveredPixel & 0xFFFFFF);
                }
            }
        }
    }
}
