package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.client.ClassicUiRender;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * One shared aspect icon + amount pattern for crafting, infusion/matrix and
 * wand-action pages.
 */
final class ThaumonomiconAspectCostRenderer {
    private static final ResourceLocation UNKNOWN_ASPECT =
            new ResourceLocation(
                    "thaumcraftmodern",
                    "textures/aspects/_unknown.png"
            );

    private ThaumonomiconAspectCostRenderer() {
    }

    static String render(
            GuiGraphics graphics,
            Font font,
            List<AspectCost> costs,
            int left,
            int width,
            int bottomY,
            int mouseX,
            int mouseY
    ) {
        return renderArranged(
                graphics,
                font,
                costs,
                ThaumonomiconAspectCostLayout.arrange(
                        costs.size(),
                        width,
                        bottomY
                ),
                left,
                0,
                mouseX,
                mouseY
        );
    }

    static String renderMasked(
            GuiGraphics graphics,
            Font font,
            List<AspectCost> costs,
            Predicate<String> knowsAspect,
            int left,
            int width,
            int bottomY,
            int mouseX,
            int mouseY
    ) {
        List<ThaumonomiconAspectCostLayout.Slot> slots =
                ThaumonomiconAspectCostLayout.arrange(
                        costs.size(),
                        width,
                        bottomY
                );
        String hovered = null;
        for (ThaumonomiconAspectCostLayout.Slot slot : slots) {
            AspectCost cost = costs.get(slot.index());
            int x = left + slot.x();
            int y = slot.y();
            if (knowsAspect.test(cost.aspectId())) {
                renderCost(graphics, font, cost, x, y);
                if (contains(
                        x,
                        y,
                        ThaumonomiconAspectCostLayout.ICON_SIZE,
                        ThaumonomiconAspectCostLayout.ICON_SIZE,
                        mouseX,
                        mouseY
                )) {
                    hovered = cost.aspectId();
                }
            } else {
                ClassicUiRender.drawAspect(
                        graphics,
                        UNKNOWN_ASPECT,
                        x,
                        y,
                        ThaumonomiconAspectCostLayout.ICON_SIZE,
                        0x777777
                );
            }
        }
        return hovered;
    }

    /** TC4 research-browser secondary cost row: 16px icons, no gap. */
    static void renderMaskedRow(
            GuiGraphics graphics,
            Font font,
            List<AspectCost> costs,
            Predicate<String> knowsAspect,
            ToIntFunction<String> availableAmount,
            int left,
            int top,
            long timeMillis
    ) {
        for (int index = 0; index < costs.size(); index++) {
            AspectCost cost = costs.get(index);
            int x = left + index * ThaumonomiconAspectCostLayout.ICON_SIZE;
            if (knowsAspect.test(cost.aspectId())) {
                float alpha = availableAmount.applyAsInt(cost.aspectId())
                        < cost.amount()
                        ? missingAspectAlpha(timeMillis)
                        : 1.0F;
                renderCost(graphics, font, cost, x, top, alpha);
            } else {
                ClassicUiRender.drawAspect(
                        graphics,
                        UNKNOWN_ASPECT,
                        x,
                        top,
                        ThaumonomiconAspectCostLayout.ICON_SIZE,
                        0x777777
                );
            }
        }
    }

    /** Exact TC4 research-browser pulse: one 600 ms sine cycle, alpha 0.5..1.0. */
    static float missingAspectAlpha(long timeMillis) {
        double phase = Math.floorMod(timeMillis, 600L) / 600.0D;
        return (float) (Math.sin(phase * Math.PI * 2.0D) * 0.25D + 0.75D);
    }

    static String renderCrucibleGrid(
            GuiGraphics graphics,
            Font font,
            List<AspectCost> costs,
            int left,
            int top,
            int mouseX,
            int mouseY
    ) {
        List<ThaumonomiconCrucibleRecipeLayout.Slot> slots =
                ThaumonomiconCrucibleRecipeLayout.aspectSlots(costs.size());
        String hovered = null;
        for (ThaumonomiconCrucibleRecipeLayout.Slot slot : slots) {
            AspectCost cost = costs.get(slot.index());
            int x = left + slot.x();
            int y = top + slot.y();
            renderCost(graphics, font, cost, x, y);
            if (contains(
                    x,
                    y,
                    ThaumonomiconAspectCostLayout.ICON_SIZE,
                    ThaumonomiconAspectCostLayout.ICON_SIZE,
                    mouseX,
                    mouseY
            )) {
                hovered = cost.aspectId();
            }
        }
        return hovered;
    }

    private static String renderArranged(
            GuiGraphics graphics,
            Font font,
            List<AspectCost> costs,
            List<ThaumonomiconAspectCostLayout.Slot> slots,
            int left,
            int top,
            int mouseX,
            int mouseY
    ) {
        String hovered = null;
        for (ThaumonomiconAspectCostLayout.Slot slot : slots) {
            AspectCost cost = costs.get(slot.index());
            int x = left + slot.x();
            int y = top + slot.y();
            renderCost(graphics, font, cost, x, y);
            if (contains(
                    x,
                    y,
                    ThaumonomiconAspectCostLayout.ICON_SIZE,
                    ThaumonomiconAspectCostLayout.ICON_SIZE,
                    mouseX,
                    mouseY
            )) {
                hovered = cost.aspectId();
            }
        }
        return hovered;
    }

    private static void renderCost(
            GuiGraphics graphics,
            Font font,
            AspectCost cost,
            int x,
            int y
    ) {
        renderCost(graphics, font, cost, x, y, 1.0F);
    }

    private static void renderCost(
            GuiGraphics graphics,
            Font font,
            AspectCost cost,
            int x,
            int y,
            float alpha
    ) {
        AspectDefinition definition = AspectRegistryRuntime.find(
                cost.aspectId()
        ).orElse(null);
        if (definition == null) {
            return;
        }
        ResourceLocation icon = ResourceLocation.tryParse(definition.icon());
        if (icon == null) {
            return;
        }
        ClassicUiRender.drawAspectTag(
                graphics,
                font,
                icon,
                x,
                y,
                ThaumonomiconAspectCostLayout.ICON_SIZE,
                definition.color(),
                Integer.toString(cost.amount()),
                alpha,
                0.5F
        );
    }

    static int requiredHeight(List<AspectCost> costs, int width) {
        return ThaumonomiconAspectCostLayout.requiredHeight(
                costs.size(),
                width
        );
    }

    private static boolean contains(
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }
}
