package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.menu.DeconstructionTableMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Pixel-coordinate port of TC4 GuiDeconstructionTable. */
public final class DeconstructionTableScreen
        extends AbstractContainerScreen<DeconstructionTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/gui/gui_decontable.png"
    );
    private static final int ASPECT_X = 64;
    private static final int ASPECT_Y = 48;
    private static final int ASPECT_SIZE = 16;

    public DeconstructionTableScreen(
            DeconstructionTableMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        ClassicScreenBackground.render(graphics, width, height);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        AspectDefinition aspect = aspect();
        if (aspect != null && insideAspect(mouseX, mouseY)) {
            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            Component.translatable(
                                            "aspect.thaumic_reborn."
                                                    + aspect.id()
                                    )
                                    .withStyle(ChatFormatting.AQUA),
                            Component.translatable("tc.aspect." + aspect.id())
                                    .withStyle(ChatFormatting.GRAY)
                    ),
                    mouseX,
                    mouseY - 8
            );
        }
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        RenderSystem.enableBlend();
        graphics.blit(
                TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                imageHeight,
                256,
                256
        );
        int progress = menu.breakTimeScaled(46);
        if (progress > 0) {
            graphics.blit(
                    TEXTURE,
                    leftPos + 93,
                    topPos + 15 + 46 - progress,
                    176,
                    46 - progress,
                    9,
                    progress,
                    256,
                    256
            );
        }
        AspectDefinition aspect = aspect();
        if (aspect != null) {
            ClassicUiRender.drawAspect(
                    graphics,
                    new ResourceLocation(aspect.icon()),
                    leftPos + ASPECT_X,
                    topPos + ASPECT_Y,
                    ASPECT_SIZE,
                    aspect.color()
            );
        }
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        // TC4 intentionally draws no foreground labels.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (aspect() == null || !insideAspect(mouseX, mouseY)) {
            return handled;
        }
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    DeconstructionTableMenu.CLAIM_ASPECT_BUTTON
            );
        }
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(
                    ModSounds.HH_OFF.get(),
                    0.2F,
                    1.0F + minecraft.player.getRandom().nextFloat() * 0.1F
            );
        }
        return true;
    }

    private AspectDefinition aspect() {
        return AspectRegistryRuntime.find(menu.aspectId()).orElse(null);
    }

    private boolean insideAspect(double mouseX, double mouseY) {
        double relativeX = mouseX - (leftPos + ASPECT_X);
        double relativeY = mouseY - (topPos + ASPECT_Y);
        return relativeX >= 0
                && relativeY >= 0
                && relativeX < ASPECT_SIZE
                && relativeY < ASPECT_SIZE;
    }
}
