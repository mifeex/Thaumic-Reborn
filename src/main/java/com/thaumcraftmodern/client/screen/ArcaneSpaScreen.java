package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.ArcaneSpaBlockEntity;
import com.thaumcraftmodern.world.menu.ArcaneSpaMenu;
import com.thaumcraftmodern.registry.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/** Pixel-coordinate port of TC4 GuiSpa using the original 256x256 texture. */
public final class ArcaneSpaScreen extends AbstractContainerScreen<ArcaneSpaMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/gui/gui_spa.png");

    public ArcaneSpaScreen(ArcaneSpaMenu menu, Inventory inventory,
            Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick) {
        ClassicScreenBackground.render(graphics, width, height);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (inside(mouseX, mouseY, 104, 10, 10, 55)
                && menu.fluidAmount() > 0) {
            FluidStack stack = new FluidStack(menu.fluid(), menu.fluidAmount());
            graphics.renderComponentTooltip(font, List.of(
                    stack.getDisplayName(),
                    Component.literal(menu.fluidAmount() + " mB")
                            .withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        }
        if (inside(mouseX, mouseY, 88, 34, 10, 10)) {
            graphics.renderTooltip(font, Component.translatable(
                    menu.mixing() ? "text.spa.mix.true" : "text.spa.mix.false"),
                    mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick,
            int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0,
                imageWidth, imageHeight, 256, 256);
        graphics.blit(TEXTURE, leftPos + 89, topPos + 35,
                208, menu.mixing() ? 16 : 32, 8, 8, 256, 256);
        drawFluid(graphics);
        graphics.blit(TEXTURE, leftPos + 106, topPos + 11,
                232, 0, 10, 55, 256, 256);
        RenderSystem.disableBlend();
    }

    private void drawFluid(GuiGraphics graphics) {
        int amount = menu.fluidAmount();
        Fluid fluid = menu.fluid();
        if (amount <= 0 || fluid == null) return;
        int fill = Math.max(0, Math.min(48,
                amount * 48 / ArcaneSpaBlockEntity.CAPACITY));
        if (fill == 0) return;

        FluidStack stack = new FluidStack(fluid, amount);
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation still = extensions.getStillTexture(stack);
        if (still == null) return;
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
        int color = extensions.getTintColor(stack);
        RenderSystem.setShaderColor(
                (color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F,
                (color >>> 24 & 255) / 255.0F);
        graphics.blit(leftPos + 107, topPos + 15 + 48 - fill,
                0, 8, fill, sprite);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // TC4 intentionally draws no foreground labels.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!inside(mouseX, mouseY, 89, 35, 8, 8)) return handled;
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId, ArcaneSpaMenu.TOGGLE_MIX_BUTTON);
        }
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(ModSounds.CAMERA_CLACK.get(), 0.4F, 1.0F);
        }
        return true;
    }

    private boolean inside(double mouseX, double mouseY, int x, int y,
            int width, int height) {
        double relativeX = mouseX - (leftPos + x);
        double relativeY = mouseY - (topPos + y);
        return relativeX >= 0 && relativeY >= 0
                && relativeX < width && relativeY < height;
    }
}
