package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.menu.TravelingTrunkMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class TravelingTrunkScreen extends AbstractContainerScreen<TravelingTrunkMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/gui/guitrunkbase.png");

    public TravelingTrunkScreen(TravelingTrunkMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 200;
        inventoryLabelY = 106;
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.trunk() != null) {
            int health = Math.max(0, Math.min(39, Math.round(
                    menu.trunk().getHealth() / menu.trunk().getMaxHealth() * 39F)));
            graphics.blit(TEXTURE, leftPos + 134, topPos + 2, 176, 16,
                    health, 6, 256, 256);
        }
        if (menu.rows() == 4) {
            graphics.blit(TEXTURE, leftPos + 80, topPos, 206, 0,
                    imageWidth, 27, 256, 256);
        }
        if (menu.isStaying()) {
            graphics.blit(TEXTURE, leftPos + 112, topPos, 176, 0,
                    10, 10, 256, 256);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= leftPos + 112 && mouseX < leftPos + 122
                && mouseY >= topPos && mouseY < topPos + 10 && minecraft != null
                && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, TravelingTrunkMenu.STAY_BUTTON);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
