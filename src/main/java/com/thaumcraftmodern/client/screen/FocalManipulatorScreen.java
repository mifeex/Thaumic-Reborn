package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.focus.FocusUpgradeCost;
import com.thaumcraftmodern.focus.FocusUpgradeType;
import com.thaumcraftmodern.item.WandFocusItem;
import com.thaumcraftmodern.world.menu.FocalManipulatorMenu;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** TC4 gui_wandtable presentation with five ranks and mouse-bound choices. */
public final class FocalManipulatorScreen extends AbstractContainerScreen<FocalManipulatorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/gui/gui_wandtable.png");
    private static final int START_X = 48;
    private static final int START_Y = 88;
    private static final int START_WIDTH = 96;
    private static final int START_HEIGHT = 8;
    private static final int OPTIONS_X = 48;
    private static final int OPTIONS_Y = 104;
    private static final int OPTION_SIZE = 16;
    private int selectedUpgrade = -1;

    public FocalManipulatorScreen(FocalManipulatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 192;
        imageHeight = 233;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (!(menu.focus().getItem() instanceof WandFocusItem focusItem)) {
            selectedUpgrade = -1;
            return;
        }
        short[] applied = WandFocusItem.appliedUpgrades(menu.focus());
        for (int index = 0; index < applied.length; index++) if (applied[index] >= 0)
            drawUpgrade(graphics, FocusUpgradeType.byId(applied[index]), leftPos + 56 + index * 16,
                    topPos + 32, 16);

        int rank = displayRank();
        List<FocusUpgradeType> choices = rank > 0
                ? focusItem.type().upgradesAtRank(rank) : List.of();
        reconcileSelection(choices);
        for (int index = 0; index < choices.size(); index++) {
            FocusUpgradeType choice = choices.get(index);
            int x = leftPos + OPTIONS_X + index * OPTION_SIZE;
            int y = topPos + OPTIONS_Y;
            if (choice.id() == selectedUpgrade)
                graphics.blit(TEXTURE, x, y, 200, 0, OPTION_SIZE, OPTION_SIZE, 256, 256);
            drawUpgrade(graphics, choice, x, y, OPTION_SIZE);
        }

        if (selectedUpgrade >= 0 && rank > 0) {
            int xp = rank * 8;
            graphics.blit(TEXTURE, leftPos + 108, topPos + 59,
                    200, 16, 16, 16, 256, 256);
            boolean enoughExperience = minecraft.player != null
                    && (minecraft.player.getAbilities().instabuild
                    || minecraft.player.experienceLevel >= xp);
            graphics.drawString(font, Integer.toString(xp), leftPos + 125, topPos + 64,
                    enoughExperience ? 10092429 : 16151160, false);

            EnumMap<PrimalAspect, Integer> costs = displayedCosts(rank);
            drawAspectCosts(graphics, costs);
            if (canStart(choices, enoughExperience))
                graphics.blit(TEXTURE, leftPos + START_X, topPos + START_Y,
                        8, 240, START_WIDTH, START_HEIGHT, 256, 256);
        }
        drawProgress(graphics);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // The classic screen deliberately has no container or inventory labels.
    }

    private void reconcileSelection(List<FocusUpgradeType> choices) {
        if (menu.activeUpgrade() >= 0) {
            selectedUpgrade = menu.activeUpgrade();
            return;
        }
        if (selectedUpgrade >= 0 && choices.stream().noneMatch(type -> type.id() == selectedUpgrade))
            selectedUpgrade = -1;
    }

    private int displayRank() {
        return menu.activeUpgrade() >= 0 && menu.activeRank() > 0
                ? menu.activeRank() : WandFocusItem.nextRank(menu.focus());
    }

    private EnumMap<PrimalAspect, Integer> displayedCosts(int rank) {
        if (menu.activeUpgrade() < 0)
            return FocusUpgradeCost.primalCost(FocusUpgradeType.byId(selectedUpgrade), rank);
        EnumMap<PrimalAspect, Integer> costs = new EnumMap<>(PrimalAspect.class);
        for (int index = 0; index < PrimalAspect.ordered().size(); index++) {
            int amount = menu.remainingPrimal(index);
            if (amount > 0) costs.put(PrimalAspect.ordered().get(index), amount);
        }
        return costs;
    }

    private void drawAspectCosts(GuiGraphics graphics, EnumMap<PrimalAspect, Integer> costs) {
        int row = 0;
        int y = topPos + 68 - costs.size() * 5 / 2;
        for (PrimalAspect primal : PrimalAspect.ordered()) {
            int amount = costs.getOrDefault(primal, 0);
            if (amount <= 0) continue;
            AspectDefinition aspect = AspectRegistryRuntime.find(primal.id()).orElse(null);
            if (aspect == null) continue;
            ClassicUiRender.drawAspectVisRow(graphics, font,
                    Component.translatable("aspect.thaumic_reborn." + primal.id()),
                    amount, leftPos + 49, y + row * 5, aspect.color());
            row++;
        }
    }

    private void drawProgress(GuiGraphics graphics) {
        if (menu.activeUpgrade() < 0 || menu.totalCost() <= 0) return;
        int start = 0;
        for (int index = 0; index < PrimalAspect.ordered().size(); index++) {
            int amount = menu.remainingPrimal(index);
            if (amount <= 0) continue;
            AspectDefinition aspect = AspectRegistryRuntime.find(
                    PrimalAspect.ordered().get(index).id()).orElse(null);
            if (aspect == null) continue;
            int width = amount * START_WIDTH / menu.totalCost();
            if (width <= 0) continue;
            int color = aspect.color();
            RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255.0F,
                    ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, 0.9F);
            graphics.blit(TEXTURE, leftPos + START_X + start, topPos + START_Y,
                    112 + start, 240, width, START_HEIGHT, 256, 256);
            start += width;
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private boolean canStart(List<FocusUpgradeType> choices, boolean enoughExperience) {
        return menu.activeUpgrade() < 0 && enoughExperience && selectedUpgrade >= 0
                && choices.stream().anyMatch(type -> type.id() == selectedUpgrade);
    }

    private void drawUpgrade(GuiGraphics graphics, FocusUpgradeType type, int x, int y, int size) {
        graphics.blit(new ResourceLocation(type.icon()), x, y, 0, 0, size, size, size, size);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && menu.focus().getItem() instanceof WandFocusItem focusItem) {
            int rank = displayRank();
            List<FocusUpgradeType> choices = rank > 0
                    ? focusItem.type().upgradesAtRank(rank) : List.of();
            if (menu.activeUpgrade() < 0 && selectedUpgrade >= 0
                    && inside(mouseX, mouseY, START_X, START_Y, START_WIDTH, START_HEIGHT)
                    && minecraft.player != null
                    && (minecraft.player.getAbilities().instabuild
                    || minecraft.player.experienceLevel >= rank * 8)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selectedUpgrade);
                return true;
            }
            if (menu.activeUpgrade() < 0) {
                for (int index = 0; index < choices.size(); index++) {
                    if (!inside(mouseX, mouseY, OPTIONS_X + index * OPTION_SIZE,
                            OPTIONS_Y, OPTION_SIZE, OPTION_SIZE)) continue;
                    int id = choices.get(index).id();
                    selectedUpgrade = selectedUpgrade == id ? -1 : id;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
        if (!(menu.focus().getItem() instanceof WandFocusItem focusItem)) return;
        int rank = displayRank();
        if (rank < 1) return;
        List<FocusUpgradeType> choices = focusItem.type().upgradesAtRank(rank);
        for (int index = 0; index < choices.size(); index++) {
            if (!inside(mouseX, mouseY, OPTIONS_X + index * OPTION_SIZE,
                    OPTIONS_Y, OPTION_SIZE, OPTION_SIZE)) continue;
            FocusUpgradeType choice = choices.get(index);
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable(choice.nameKey()),
                    Component.translatable(choice.textKey())), mouseX, mouseY);
            return;
        }
        if (selectedUpgrade < 0) return;
        if (inside(mouseX, mouseY, 48, 48, 36, 36))
            graphics.renderTooltip(font, Component.translatable("wandtable.text1"), mouseX, mouseY);
        else if (inside(mouseX, mouseY, 108, 58, 36, 16))
            graphics.renderTooltip(font, Component.translatable("wandtable.text2"), mouseX, mouseY);
        else if (menu.activeUpgrade() < 0
                && inside(mouseX, mouseY, START_X, START_Y, START_WIDTH, START_HEIGHT))
            graphics.renderTooltip(font, Component.translatable("wandtable.text3"), mouseX, mouseY);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        double relativeX = mouseX - (leftPos + x);
        double relativeY = mouseY - (topPos + y);
        return relativeX >= 0 && relativeY >= 0 && relativeX < width && relativeY < height;
    }
}
