package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.research.ResearchDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** Pixel-exact open-book frame, page placement, controls and item tooltip. */
final class ThaumonomiconOpenBookRenderer {
    private static final ResourceLocation BOOK = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/gui/gui_researchbook.png"
    );
    private static final int BOOK_RENDER_WIDTH = 333;
    private static final int BOOK_RENDER_HEIGHT = 235;
    private static final int PAGE_WIDTH = 139;
    private final ThaumonomiconPageRenderer pageRenderer =
            new ThaumonomiconPageRenderer();

    void render(
            GuiGraphics graphics,
            Minecraft minecraft,
            Font font,
            int screenWidth,
            int screenHeight,
            int left,
            int top,
            ResearchDefinition research,
            int pagePair,
            List<ThaumonomiconItemLinkRegion> itemLinkRegions,
            Function<ItemStack, List<Component>> tooltipProvider,
            Predicate<ItemStack> hasResearchLink,
            int mouseX,
            int mouseY
    ) {
        itemLinkRegions.clear();
        // gui_researchbook.png is a 512px atlas. Only its first 362px form the
        // open book; the rest contains navigation sprites and must be cropped.
        int bookLeft = (screenWidth - BOOK_RENDER_WIDTH) / 2;
        int bookTop = (screenHeight - BOOK_RENDER_HEIGHT) / 2;
        ClassicUiRender.drawScaledTexture(
                graphics,
                BOOK,
                bookLeft,
                bookTop,
                BOOK_RENDER_WIDTH,
                BOOK_RENDER_HEIGHT,
                0,
                0,
                512,
                362,
                512,
                512
        );

        pageRenderer.beginFrame(
                minecraft,
                font,
                top,
                research,
                itemLinkRegions
        );
        pageRenderer.renderPage(
                graphics, pagePair, left - 15, top - 6,
                PAGE_WIDTH, mouseX, mouseY
        );
        pageRenderer.renderPage(
                graphics, pagePair + 1, left + 137, top - 6,
                PAGE_WIDTH, mouseX, mouseY
        );
        renderControls(
                graphics, minecraft, font, left, top,
                research, pagePair, mouseX, mouseY
        );
        renderItemLinkTooltip(
                graphics, font, itemLinkRegions, tooltipProvider,
                hasResearchLink, mouseX, mouseY
        );
    }

    private static void renderItemLinkTooltip(
            GuiGraphics graphics,
            Font font,
            List<ThaumonomiconItemLinkRegion> itemLinkRegions,
            Function<ItemStack, List<Component>> tooltipProvider,
            Predicate<ItemStack> hasResearchLink,
            int mouseX,
            int mouseY
    ) {
        ThaumonomiconItemLinkRegion hovered = itemLinkRegions.stream()
                .filter(region -> region.contains(mouseX, mouseY))
                .findFirst()
                .orElse(null);
        if (hovered == null) {
            return;
        }
        /* Preserve Screen.getTooltipFromItem and all Forge-added lines. */
        List<Component> tooltip = new ArrayList<>(
                tooltipProvider.apply(hovered.stack())
        );
        if (hasResearchLink.test(hovered.stack())) {
            tooltip.add(Component.translatable(
                    "screen.thaumic_reborn.thaumonomicon.open_item_page"
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        graphics.renderTooltip(
                font,
                tooltip,
                Optional.empty(),
                mouseX,
                mouseY
        );
    }

    private static void renderControls(
            GuiGraphics graphics,
            Minecraft minecraft,
            Font font,
            int left,
            int top,
            ResearchDefinition research,
            int pagePair,
            int mouseX,
            int mouseY
    ) {
        int playerTicks = minecraft != null && minecraft.player != null
                ? minecraft.player.tickCount : 0;
        float controlScale = controlScale(playerTicks);
        drawControl(
                graphics, left, top, ThaumonomiconBookLayout.BACK,
                76, 404, 40, 24, controlScale
        );
        if (pagePair > 0) {
            drawControl(
                    graphics, left, top, ThaumonomiconBookLayout.PREVIOUS,
                    0, 368, 24, 16, controlScale
            );
        }
        if (pagePair + 2 < research.pages().size()) {
            drawControl(
                    graphics, left, top, ThaumonomiconBookLayout.NEXT,
                    24, 368, 24, 16, controlScale
            );
        }

        if (ThaumonomiconBookLayout.BACK.contains(
                left, top, mouseX, mouseY
        )) {
            graphics.renderTooltip(
                    font,
                    Component.translatable(
                            "screen.thaumic_reborn.thaumonomicon.back"
                    ),
                    mouseX,
                    mouseY
            );
        } else if (pagePair > 0 && ThaumonomiconBookLayout.PREVIOUS.contains(
                left, top, mouseX, mouseY
        )) {
            graphics.renderTooltip(
                    font,
                    Component.translatable(
                            "screen.thaumic_reborn.thaumonomicon.previous_page"
                    ),
                    mouseX,
                    mouseY
            );
        } else if (pagePair + 2 < research.pages().size()
                && ThaumonomiconBookLayout.NEXT.contains(
                        left, top, mouseX, mouseY
                )) {
            graphics.renderTooltip(
                    font,
                    Component.translatable(
                            "screen.thaumic_reborn.thaumonomicon.next_page"
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    static float controlScale(int playerTicks) {
        float bob = Mth.sin((float) playerTicks / 3.0F) * 0.2F + 0.1F;
        return 1.0F + bob;
    }

    private static void drawControl(
            GuiGraphics graphics,
            int left,
            int top,
            ThaumonomiconBookLayout.Region region,
            int sourceX,
            int sourceY,
            int sourceWidth,
            int sourceHeight,
            float scale
    ) {
        int x = left + region.x();
        int y = top + region.y();
        graphics.pose().pushPose();
        graphics.pose().translate(
                x + region.width() / 2.0F,
                y + region.height() / 2.0F,
                0.0F
        );
        graphics.pose().scale(scale, scale, 1.0F);
        ClassicUiRender.drawScaledTexture(
                graphics,
                BOOK,
                -region.width() / 2,
                -region.height() / 2,
                region.width(),
                region.height(),
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight,
                512,
                512
        );
        graphics.pose().popPose();
    }
}
