package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconWarpPresentationFidelityTest {
    private static final Path SCREEN = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconScreen.java"
    );
    private static final Path TOOLTIP_RENDERER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconResearchTooltipRenderer.java"
    );
    private static final Path LOADER = Path.of(
            "src/main/java/com/thaumcraftmodern/data/"
                    + "ResearchReloadListener.java"
    );
    private static final Path GUIDE = Path.of(
            "docs/RESEARCH_CREATION_GUIDE_RU.md"
    );

    @Test
    void warpedResearchUsesClassicForbiddenAuraAndExactTooltipAmount()
            throws Exception {
        String source = Files.readString(SCREEN)
                + Files.readString(TOOLTIP_RENDERER);

        assertTrue(source.contains("textures/misc/nodes.png"));
        assertTrue(source.contains("RESEARCH_WARP_AURA_SIZE = 80"));
        assertTrue(source.contains("RESEARCH_WARP_AURA_FRAMES = 32"));
        assertTrue(source.contains("RESEARCH_WARP_AURA_STRIP = 5"));
        assertTrue(source.contains("RESEARCH_WARP_AURA_TINT = 0xA8440055"));
        assertTrue(source.contains("research.completionWarp() > 0"));
        assertTrue(source.contains(
                "RESEARCH_WARP_AURA_FRAMES - 1 - animationFrame"
        ));
        assertTrue(source.contains(
                "tooltip.thaumcraftmodern.research_completion_warp"
        ));
        assertTrue(source.contains("research.completionWarp()"));
        assertTrue(source.contains(
                "RESEARCH_TOOLTIP_SMALL_SCALE = 0.5F"
        ));
        assertTrue(source.contains(
                "RESEARCH_TOOLTIP_WIDTH_DIVISOR = 1.9F"
        ));
        assertTrue(source.contains(
                "RESEARCH_TOOLTIP_BACKGROUND = 0xC0000000"
        ));
        assertTrue(source.contains(
                "RESEARCH_TOOLTIP_SUBTITLE = 0xFF9090FF"
        ));
        assertTrue(source.contains(
                "RESEARCH_TOOLTIP_WARP = 0xFFAA55FF"
        ));
        assertTrue(source.contains(
                "RESEARCH_TOOLTIP_READY = 0xFF87D1AB"
        ));
        assertTrue(source.contains(
                "RESEARCH_TOOLTIP_HAS_NOTES = 0xFFFFAA00"
        ));
        assertTrue(source.contains(
                "RESEARCH_TOOLTIP_BLOCKED = 0xFFDC141C"
        ));
        assertTrue(source.contains(
                "RESEARCH_TOOLTIP_LOCKED_TITLE = 0xFF808040"
        ));
        assertTrue(source.contains("drawHalfScaleResearchText("));
        assertTrue(source.contains("renderMaskedRow("));
        assertTrue(source.contains("RESEARCH_TOOLTIP_X_OFFSET = 6"));
        assertTrue(source.contains("RESEARCH_TOOLTIP_Y_OFFSET = -4"));
        assertTrue(source.contains(
                "int tooltipX = mouseX + RESEARCH_TOOLTIP_X_OFFSET"
        ));
        assertTrue(source.contains(
                "int tooltipY = mouseY + RESEARCH_TOOLTIP_Y_OFFSET"
        ));
        assertTrue(source.contains("boolean primary = !secondary && !completed"));
        assertTrue(source.contains("hasResearchNotes(research.id())"));
        assertTrue(source.contains("hasScribingMaterials()"));
        assertTrue(source.contains("canAffordResearch(research)"));
    }

    @Test
    void authorFieldIsDocumentedAndOverridesLegacyFallback()
            throws Exception {
        String loader = Files.readString(LOADER);
        String guide = Files.readString(GUIDE);

        assertTrue(loader.contains("json.has(\"completion_warp\")"));
        assertTrue(loader.contains(
                "GsonHelper.getAsInt(json, \"completion_warp\")"
        ));
        assertTrue(loader.contains(
                "GsonHelper.getAsJsonArray(legacy, \"warp\").get(0)"
        ));
        assertTrue(guide.contains("\"completion_warp\": 3"));
        assertTrue(guide.contains(
                "Явное поле `completion_warp` имеет приоритет"
        ));
    }

    @Test
    void researchTooltipIsFlushedAboveDeferredNodeItemIcons()
            throws Exception {
        String source = Files.readString(TOOLTIP_RENDERER);
        int begin = source.indexOf("void render(");
        int end = source.indexOf("private void renderLockedResearchTooltip(");
        String method = source.substring(begin, end);

        assertTrue(source.contains("RESEARCH_TOOLTIP_Z = 400.0F"));
        assertTrue(method.contains("graphics.flush();"));
        assertTrue(method.contains("graphics.pose().pushPose();"));
        assertTrue(method.contains(
                "graphics.pose().translate(0.0F, 0.0F, RESEARCH_TOOLTIP_Z)"
        ));
        assertTrue(method.contains("finally"));
        assertTrue(method.contains("graphics.pose().popPose();"));
    }
}
