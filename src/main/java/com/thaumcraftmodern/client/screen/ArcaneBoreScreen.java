package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.focus.FocusUpgradeType;
import com.thaumcraftmodern.item.ElementalPickaxeItem;
import com.thaumcraftmodern.item.WandFocusItem;
import com.thaumcraftmodern.world.menu.ArcaneBoreMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/** Pixel-coordinate port of TC4 GuiArcaneBore. */
public final class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/gui/gui_arcanebore.png");

    public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth = 176; imageHeight = 141;
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick) {
        ClassicScreenBackground.render(graphics, width, height);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick,
            int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        ItemStack pickaxe = menu.getSlot(1).getItem();
        if (!pickaxe.isEmpty() && pickaxe.isDamageableItem()
                && pickaxe.getDamageValue() + 1 >= pickaxe.getMaxDamage()) {
            graphics.blit(TEXTURE, leftPos + 74, topPos + 18, 184, 0, 16, 16, 256, 256);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 112, topPos + 8, 505);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, Component.translatable("gui.thaumic_reborn.arcane_bore.width",
                1 + (menu.area() + 2) * 2), 0, 0, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("gui.thaumic_reborn.arcane_bore.speed",
                menu.speed()), 0, 10, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable(
                "gui.thaumic_reborn.arcane_bore.properties"), 0, 24, 0xFFFFFF, false);
        int offset = 0;
        ItemStack focus = menu.getSlot(0).getItem();
        boolean nativeClusters = pickaxe.getItem() instanceof ElementalPickaxeItem
                || focus.getItem() instanceof WandFocusItem
                && WandFocusItem.upgradeLevel(focus, FocusUpgradeType.DOWSING) > 0;
        if (nativeClusters) {
            graphics.drawString(font, Component.translatable(
                    "gui.thaumic_reborn.arcane_bore.native_clusters"), 4, 34, 0xC0C0C0, false);
            offset += 9;
        }
        if (menu.fortune() > 0) {
            graphics.drawString(font, Component.translatable(
                    "enchantment.minecraft.fortune").append(" " + menu.fortune()),
                    4, 34 + offset, 0xEECACA, false); offset += 9;
        }
        boolean silk = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, pickaxe) > 0
                || focus.getItem() instanceof WandFocusItem
                && WandFocusItem.upgradeLevel(focus, FocusUpgradeType.SILK_TOUCH) > 0;
        if (silk) graphics.drawString(font, Component.translatable(
                "enchantment.minecraft.silk_touch"), 4, 34 + offset, 0x8080FF, false);
        graphics.pose().popPose();
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { }
}
