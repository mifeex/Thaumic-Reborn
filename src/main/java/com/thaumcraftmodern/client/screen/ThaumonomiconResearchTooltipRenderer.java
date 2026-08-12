package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.research.ResearchDefinition;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

/**
 * Exact TC4 research-node tooltip renderer.
 *
 * <p>This intentionally does not use the vanilla tooltip layout: the classic
 * half-scale text, fixed offsets, aspect row and z-order are visual contracts.</p>
 */
final class ThaumonomiconResearchTooltipRenderer {
    private static final float RESEARCH_TOOLTIP_SMALL_SCALE = 0.5F;
    private static final float RESEARCH_TOOLTIP_WIDTH_DIVISOR = 1.9F;
    private static final int RESEARCH_TOOLTIP_X_OFFSET = 6;
    private static final int RESEARCH_TOOLTIP_Y_OFFSET = -4;
    private static final int RESEARCH_TOOLTIP_PADDING = 3;
    private static final int RESEARCH_TOOLTIP_BACKGROUND = 0xC0000000;
    private static final int RESEARCH_TOOLTIP_SUBTITLE = 0xFF9090FF;
    private static final int RESEARCH_TOOLTIP_WARP = 0xFFAA55FF;
    private static final int RESEARCH_TOOLTIP_MISSING = 0xFF705050;
    private static final int RESEARCH_TOOLTIP_READY = 0xFF87D1AB;
    private static final int RESEARCH_TOOLTIP_HAS_NOTES = 0xFFFFAA00;
    private static final int RESEARCH_TOOLTIP_BLOCKED = 0xFFDC141C;
    private static final int RESEARCH_TOOLTIP_TITLE = 0xFFFFFFFF;
    private static final int RESEARCH_TOOLTIP_SPECIAL_TITLE = 0xFFFFFF80;
    private static final int RESEARCH_TOOLTIP_LOCKED_TITLE = 0xFF808040;
    private static final int RESEARCH_TOOLTIP_LOCKED_SPECIAL_TITLE = 0xFF808080;
    private static final float RESEARCH_TOOLTIP_Z = 400.0F;

    private final Font font;
    private final Predicate<ResearchDefinition> canAfford;
    private final Predicate<String> hasNotes;
    private final BooleanSupplier hasMaterials;
    private final Predicate<String> knowsAspect;
    private final ToIntFunction<String> aspectAmount;

    ThaumonomiconResearchTooltipRenderer(
            Font font,
            Predicate<ResearchDefinition> canAfford,
            Predicate<String> hasNotes,
            BooleanSupplier hasMaterials,
            Predicate<String> knowsAspect,
            ToIntFunction<String> aspectAmount
    ) {
        this.font = font;
        this.canAfford = canAfford;
        this.hasNotes = hasNotes;
        this.hasMaterials = hasMaterials;
        this.knowsAspect = knowsAspect;
        this.aspectAmount = aspectAmount;
    }

    private boolean canAffordResearch(ResearchDefinition research) {
        return canAfford.test(research);
    }

    private boolean hasResearchNotes(String researchId) {
        return hasNotes.test(researchId);
    }

    private boolean hasScribingMaterials() {
        return hasMaterials.getAsBoolean();
    }

    void render(
            GuiGraphics graphics,
            ResearchDefinition research,
            boolean completed,
            boolean unlocked,
            int mouseX,
            int mouseY
    ) {
        /*
         * ItemRenderer can defer node icons until the shared buffer is
         * flushed. Submit those icons first, then render the complete custom
         * tooltip on the same high layer vanilla tooltips use. This keeps the
         * background, text, and embedded aspect costs above every tree icon.
         */
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, RESEARCH_TOOLTIP_Z);
        try {
        String title = Component.translatable(research.titleKey()).getString();
        String subtitle = Component.translatable(
                research.subtitleKey()
        ).getString();
        int tooltipX = mouseX + RESEARCH_TOOLTIP_X_OFFSET;
        int tooltipY = mouseY + RESEARCH_TOOLTIP_Y_OFFSET;

        if (!unlocked) {
            renderLockedResearchTooltip(
                    graphics,
                    research,
                    title,
                    tooltipX,
                    tooltipY
            );
            return;
        }

        boolean secondary = !completed && research.purchasable();
        boolean primary = !secondary && !completed;
        ResearchTooltipLine state = primary
                ? primaryResearchTooltipLine(research)
                : secondary
                ? secondaryResearchTooltipLine(research)
                : null;
        String warp = research.completionWarp() > 0
                ? Component.translatable(
                        "tooltip.thaumcraftmodern.research_completion_warp",
                        research.completionWarp()
                ).getString()
                : "";

        int tooltipWidth = Math.max(
                font.width(title),
                halfScaleWidth(subtitle)
        );
        if (state != null) {
            tooltipWidth = Math.max(
                    tooltipWidth,
                    halfScaleWidth(state.text())
            );
        }
        if (!warp.isEmpty()) {
            tooltipWidth = Math.max(tooltipWidth, halfScaleWidth(warp));
        }
        int contentHeight = font.wordWrapHeight(title, tooltipWidth) + 5;
        int extraHeight = secondary ? 29 : state == null ? 0 : 9;
        if (!warp.isEmpty()) {
            extraHeight += 9;
        }

        graphics.fill(
                tooltipX - RESEARCH_TOOLTIP_PADDING,
                tooltipY - RESEARCH_TOOLTIP_PADDING,
                tooltipX + tooltipWidth + RESEARCH_TOOLTIP_PADDING,
                tooltipY + contentHeight + 6 + extraHeight,
                RESEARCH_TOOLTIP_BACKGROUND
        );
        drawHalfScaleResearchText(
                graphics,
                subtitle,
                tooltipX,
                tooltipY + contentHeight - 1,
                RESEARCH_TOOLTIP_SUBTITLE
        );
        if (!warp.isEmpty()) {
            drawHalfScaleResearchText(
                    graphics,
                    warp,
                    tooltipX,
                    tooltipY + contentHeight + 8,
                    RESEARCH_TOOLTIP_WARP
            );
            contentHeight += 9;
        }
        if (!secondary && state != null) {
            drawHalfScaleResearchText(
                    graphics,
                    state.text(),
                    tooltipX,
                    tooltipY + contentHeight + 8,
                    state.color()
            );
        } else if (secondary && state != null) {
            ThaumonomiconAspectCostRenderer.renderMaskedRow(
                    graphics,
                    font,
                    research.purchaseCost(),
                    aspectId -> knowsAspect.test(aspectId),
                    aspectId -> aspectAmount.applyAsInt(aspectId),
                    tooltipX,
                    tooltipY + contentHeight + 8,
                    Util.getMillis()
            );
            drawHalfScaleResearchText(
                    graphics,
                    state.text(),
                    tooltipX,
                    tooltipY + contentHeight + 27,
                    state.color()
            );
        }
        graphics.drawString(
                font,
                title,
                tooltipX,
                tooltipY,
                researchTitleColor(research, unlocked),
                false
        );
        } finally {
            graphics.flush();
            graphics.pose().popPose();
        }
    }

    private void renderLockedResearchTooltip(
            GuiGraphics graphics,
            ResearchDefinition research,
            String title,
            int tooltipX,
            int tooltipY
    ) {
        String missing = Component.translatable("tc.researchmissing")
                .getString();
        int tooltipWidth = Math.max(
                font.width(title),
                (int) (font.width(missing) / 1.5F)
        );
        int missingHeight = font.wordWrapHeight(missing, tooltipWidth * 2);
        graphics.fill(
                tooltipX - RESEARCH_TOOLTIP_PADDING,
                tooltipY - RESEARCH_TOOLTIP_PADDING,
                tooltipX + tooltipWidth + RESEARCH_TOOLTIP_PADDING,
                tooltipY + missingHeight + 10,
                RESEARCH_TOOLTIP_BACKGROUND
        );
        drawHalfScaleResearchTextWrapped(
                graphics,
                missing,
                tooltipX,
                tooltipY + 12,
                tooltipWidth * 2,
                RESEARCH_TOOLTIP_MISSING
        );
        graphics.drawString(
                font,
                title,
                tooltipX,
                tooltipY,
                researchTitleColor(research, false),
                false
        );
    }

    private ResearchTooltipLine primaryResearchTooltipLine(
            ResearchDefinition research
    ) {
        if (research.inactive()) {
            return new ResearchTooltipLine(
                    Component.translatable(
                            "screen.thaumcraftmodern.thaumonomicon.content_inactive"
                    ).getString(),
                    RESEARCH_TOOLTIP_BLOCKED
            );
        }
        if (research.purchasable()) {
            return secondaryResearchTooltipLine(research);
        }
        if (hasResearchNotes(research.id())) {
            return new ResearchTooltipLine(
                    Component.translatable("tc.research.hasnote").getString(),
                    RESEARCH_TOOLTIP_HAS_NOTES
            );
        }
        if (hasScribingMaterials()) {
            return new ResearchTooltipLine(
                    Component.translatable("tc.research.getprim").getString(),
                    RESEARCH_TOOLTIP_READY
            );
        }
        return new ResearchTooltipLine(
                Component.translatable("tc.research.shortprim").getString(),
                RESEARCH_TOOLTIP_BLOCKED
        );
    }

    private ResearchTooltipLine secondaryResearchTooltipLine(
            ResearchDefinition research
    ) {
        boolean affordable = canAffordResearch(research);
        return new ResearchTooltipLine(
                Component.translatable(
                        affordable ? "tc.research.purchase" : "tc.research.short"
                ).getString(),
                affordable
                        ? RESEARCH_TOOLTIP_READY
                        : RESEARCH_TOOLTIP_BLOCKED
        );
    }

    private static int researchTitleColor(
            ResearchDefinition research,
            boolean unlocked
    ) {
        if (unlocked) {
            return research.specialFrame()
                    ? RESEARCH_TOOLTIP_SPECIAL_TITLE
                    : RESEARCH_TOOLTIP_TITLE;
        }
        return research.specialFrame()
                ? RESEARCH_TOOLTIP_LOCKED_SPECIAL_TITLE
                : RESEARCH_TOOLTIP_LOCKED_TITLE;
    }

    private int halfScaleWidth(String text) {
        return (int) Math.ceil(
                font.width(text) / RESEARCH_TOOLTIP_WIDTH_DIVISOR
        );
    }

    private void drawHalfScaleResearchText(
            GuiGraphics graphics,
            String text,
            int x,
            int y,
            int color
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 1.0F);
        graphics.pose().scale(
                RESEARCH_TOOLTIP_SMALL_SCALE,
                RESEARCH_TOOLTIP_SMALL_SCALE,
                1.0F
        );
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void drawHalfScaleResearchTextWrapped(
            GuiGraphics graphics,
            String text,
            int x,
            int y,
            int width,
            int color
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 1.0F);
        graphics.pose().scale(
                RESEARCH_TOOLTIP_SMALL_SCALE,
                RESEARCH_TOOLTIP_SMALL_SCALE,
                1.0F
        );
        graphics.drawWordWrap(font, Component.literal(text), 0, 0, width, color);
        graphics.pose().popPose();
    }

    private record ResearchTooltipLine(String text, int color) {
    }
}
