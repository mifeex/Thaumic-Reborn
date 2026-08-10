package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.ClassicUiRender;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Shared TC4 result presentation used by Thaumonomicon recipe pages.
 *
 * <p>The original Crucible and Infusion pages use the same 56x17 legacy
 * overlay region at 2x scale, with the result item centered inside it.</p>
 */
final class ThaumonomiconRecipeOutputRenderer {
    static final int WIDTH = 112;
    static final int HEIGHT = 34;
    static final int ITEM_OFFSET_X = 48;
    static final int ITEM_OFFSET_Y = 8;

    private static final ResourceLocation BOOK_OVERLAY =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/gui_researchbook_overlay.png"
            );

    private ThaumonomiconRecipeOutputRenderer() {
    }

    static boolean render(
            GuiGraphics graphics,
            Font font,
            ItemStack output,
            int left,
            int top,
            int mouseX,
            int mouseY
    ) {
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                BOOK_OVERLAY,
                left,
                top,
                WIDTH,
                HEIGHT,
                0,
                6,
                WIDTH,
                HEIGHT,
                512,
                512,
                0xFFFFFFFF
        );

        int itemX = left + ITEM_OFFSET_X;
        int itemY = top + ITEM_OFFSET_Y;
        graphics.renderItem(output, itemX, itemY);
        graphics.renderItemDecorations(font, output, itemX, itemY);
        return contains(
                itemX,
                itemY,
                16,
                16,
                mouseX,
                mouseY
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
