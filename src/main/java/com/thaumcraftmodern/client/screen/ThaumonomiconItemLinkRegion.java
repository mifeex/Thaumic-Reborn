package com.thaumcraftmodern.client.screen;

import net.minecraft.world.item.ItemStack;

/** Screen-space hit region for an item rendered on a research page. */
record ThaumonomiconItemLinkRegion(
        ItemStack stack,
        int x,
        int y,
        int width,
        int height
) {
    boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }
}
