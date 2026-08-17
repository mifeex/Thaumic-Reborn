package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.arcane.ArcaneVisCost;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.menu.ArcaneWorkbenchMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/gui/gui_arcaneworkbench.png"
    );
    private static final int[][] ASPECT_POSITIONS = {
            {72, 21},
            {24, 43},
            {24, 102},
            {72, 124},
            {120, 102},
            {120, 43}
    };

    public ArcaneWorkbenchScreen(
            ArcaneWorkbenchMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = 190;
        imageHeight = 234;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClassicScreenBackground.render(graphics, width, height);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        graphics.flush();
        RenderSystem.disableBlend();
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        Map<String, Integer> costCentivis =
                menu.displayCostCentivis(minecraft.player);
        ItemStack wand = menu.wandStack();
        long timeMillis = System.currentTimeMillis();
        for (int index = 0; index < ArcaneVisCost.PRIMALS.size(); index++) {
            String primal = ArcaneVisCost.PRIMALS.get(index);
            int centivis = costCentivis.getOrDefault(primal, 0);
            if (centivis <= 0) {
                continue;
            }
            AspectDefinition definition = AspectRegistryRuntime.find(primal).orElse(null);
            if (definition == null) {
                continue;
            }
            int[] position = ASPECT_POSITIONS[index];
            boolean available = WandVisService.visCentivis(wand, primal) >= centivis;
            int color = AspectAvailabilityColor.resolve(
                    definition.color(),
                    available,
                    timeMillis
            );
            ClassicUiRender.drawAspectVisTag(
                    graphics,
                    font,
                    new ResourceLocation(definition.icon()),
                    leftPos + position[0] - 8,
                    topPos + position[1] - 8,
                    16,
                    color,
                    centivis
            );
        }

        if (!costCentivis.isEmpty()
                && WandVisService.isWand(wand)
                && !menu.hasEnoughVis(minecraft.player)) {
            Component message = Component.translatable(
                    "screen.thaumic_reborn.arcane_workbench.insufficient_vis"
            ).withStyle(ChatFormatting.RED);
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + 168, topPos + 46, 0.0F);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            graphics.drawCenteredString(font, message, 0, 0, 0xEE6D6E);
            graphics.pose().popPose();
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // The classic TC4 screen deliberately has no foreground labels.
    }

}
