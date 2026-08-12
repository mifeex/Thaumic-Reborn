package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.menu.ThaumatoriumMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** TC4 Thaumatorium recipe selector, using the original 176x166 layout. */
public final class ThaumatoriumScreen
        extends AbstractContainerScreen<ThaumatoriumMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/gui_thaumatorium.png"
    );
    private static final int MAX_VISIBLE_ASPECTS = 6;
    private static final int ASPECT_PROGRESS_X_OFFSET = 2;
    private static final int ASPECT_PROGRESS_Y_OFFSET = 58;
    private static final int ASPECT_PROGRESS_WIDTH = 12;
    private static final int ASPECT_PROGRESS_HEIGHT = 4;
    private static final int RECIPE_ARROW_X = 128;
    private static final int RECIPE_PREVIOUS_Y = 16;
    private static final int RECIPE_NEXT_Y = 24;
    private static final int RECIPE_ARROW_WIDTH = 16;
    private static final int RECIPE_ARROW_HEIGHT = 8;
    private static final int RECIPE_COUNTER_RIGHT_X = RECIPE_ARROW_X + 10;
    private static final int RECIPE_COUNTER_Y = 32;

    private int index;
    private int lastSize = -1;
    private int lastRecipeRevision = -1;
    private int startAspect;

    public ThaumatoriumScreen(
            ThaumatoriumMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        refreshIndex();
    }

    private void refreshIndex() {
        List<CrucibleRecipeDefinition> recipes = menu.recipes();
        ResourceLocation displayedRecipe = menu.displayedRecipeId();
        if (displayedRecipe != null) {
            for (int recipeIndex = 0; recipeIndex < recipes.size(); recipeIndex++) {
                if (displayedRecipe.equals(recipes.get(recipeIndex).id())) {
                    index = recipeIndex;
                    startAspect = 0;
                    lastSize = recipes.size();
                    lastRecipeRevision = menu.recipeRevision();
                    return;
                }
            }
        }
        for (int recipeIndex = 0; recipeIndex < recipes.size(); recipeIndex++) {
            if (menu.selected(recipes.get(recipeIndex))) {
                index = recipeIndex;
                break;
            }
        }
        startAspect = 0;
        lastSize = recipes.size();
        lastRecipeRevision = menu.recipeRevision();
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
        renderRecipeOutputTooltip(graphics, mouseX, mouseY);
    }

    private void renderRecipeOutputTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        List<CrucibleRecipeDefinition> recipes = menu.recipes();
        if (recipes.isEmpty() || index < 0 || index >= recipes.size()
                || !menu.getCarried().isEmpty()
                || !inside(mouseX, mouseY, 112, 16, 16, 16)) {
            return;
        }
        graphics.renderTooltip(font, recipes.get(index).output(), mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
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

        List<CrucibleRecipeDefinition> recipes = menu.recipes();
        if (recipes.isEmpty()) {
            RenderSystem.disableBlend();
            return;
        }
        updateIndex(recipes);
        CrucibleRecipeDefinition recipe = recipes.get(index);
        List<Map.Entry<String, Integer>> aspects = sortedAspects(recipe);
        drawRecipeControls(graphics, recipes.size(), aspects.size());
        drawSelectionState(graphics, recipe, mouseX, mouseY);
        drawAspects(graphics, recipe, aspects);
        drawOutput(graphics, recipe, partialTick);
        drawRecipeCount(graphics, recipes.size());

        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private void updateIndex(List<CrucibleRecipeDefinition> recipes) {
        if (lastSize != recipes.size()
                || lastRecipeRevision != menu.recipeRevision()) {
            refreshIndex();
        }
        index = Mth.clamp(index, 0, recipes.size() - 1);
    }

    private void drawRecipeControls(
            GuiGraphics graphics,
            int recipeCount,
            int aspectCount
    ) {
        if (recipeCount > 1) {
            graphics.blit(TEXTURE, leftPos + RECIPE_ARROW_X,
                    topPos + RECIPE_PREVIOUS_Y,
                    index > 0 ? 192 : 176, 16,
                    RECIPE_ARROW_WIDTH, RECIPE_ARROW_HEIGHT, 256, 256);
            graphics.blit(TEXTURE, leftPos + RECIPE_ARROW_X,
                    topPos + RECIPE_NEXT_Y,
                    index < recipeCount - 1 ? 192 : 176, 24,
                    RECIPE_ARROW_WIDTH, RECIPE_ARROW_HEIGHT, 256, 256);
        }
        if (aspectCount > MAX_VISIBLE_ASPECTS) {
            startAspect = Mth.clamp(
                    startAspect,
                    0,
                    maxAspectStart(aspectCount)
            );
            graphics.blit(TEXTURE, leftPos + 32, topPos + 40,
                    startAspect > 0 ? 192 : 176,
                    32, 8, 16, 256, 256);
            graphics.blit(TEXTURE, leftPos + 136, topPos + 40,
                    startAspect < maxAspectStart(aspectCount) ? 200 : 184,
                    32, 8, 16, 256, 256);
        } else {
            startAspect = 0;
        }
    }

    private void drawSelectionState(
            GuiGraphics graphics,
            CrucibleRecipeDefinition recipe,
            int mouseX,
            int mouseY
    ) {
        if (menu.formulaCount() <= 0) {
            return;
        }
        boolean hoveringOutput = inside(
                mouseX,
                mouseY,
                112,
                16,
                16,
                16
        );
        if (hoveringOutput || menu.selected(recipe)) {
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(
                    TEXTURE,
                    leftPos + 104,
                    topPos + 8,
                    176,
                    96,
                    48,
                    48,
                    256,
                    256
            );
        }
        float ticks = minecraft != null && minecraft.player != null
                ? minecraft.player.tickCount : 0.0F;
        float alpha = 1.0F + Mth.sin(ticks / 5.0F) * 0.4F;
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(TEXTURE, leftPos + 88, topPos + 16,
                176, 56, 24, 24, 256, 256);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawAspects(
            GuiGraphics graphics,
            CrucibleRecipeDefinition recipe,
            List<Map.Entry<String, Integer>> aspects
    ) {
        int visibleCount = Math.min(
                MAX_VISIBLE_ASPECTS,
                Math.max(0, aspects.size() - startAspect)
        );
        int startX = aspectStartX(visibleCount);
        int drawn = 0;
        for (int aspectIndex = startAspect;
                aspectIndex < aspects.size() && drawn < MAX_VISIBLE_ASPECTS;
                aspectIndex++) {
            Map.Entry<String, Integer> entry = aspects.get(aspectIndex);
            AspectDefinition definition = AspectRegistryRuntime.find(entry.getKey())
                    .orElse(null);
            if (definition == null) {
                continue;
            }
            int x = leftPos + startX + 16 * drawn;
            int y = topPos + 40;
            ResourceLocation icon = ResourceLocation.tryParse(definition.icon());
            if (icon != null) {
                ClassicUiRender.drawAspectTag(
                        graphics,
                        font,
                        icon,
                        x,
                        y,
                        16,
                        definition.color(),
                        entry.getValue()
                );
            }

            graphics.blit(TEXTURE, x + 1, topPos + 57,
                    176, 8, 14, 6, 256, 256);
            if (menu.selected(recipe)) {
                int fill = progressWidth(
                        menu.reservedAmount(entry.getKey()),
                        entry.getValue()
                );
                if (fill > 0) {
                    int color = definition.color();
                    graphics.setColor(
                            ((color >> 16) & 255) / 255.0F,
                            ((color >> 8) & 255) / 255.0F,
                            (color & 255) / 255.0F,
                            1.0F
                    );
                    graphics.blit(
                            TEXTURE,
                            x + ASPECT_PROGRESS_X_OFFSET,
                            topPos + ASPECT_PROGRESS_Y_OFFSET,
                            176,
                            0,
                            fill,
                            ASPECT_PROGRESS_HEIGHT,
                            256,
                            256
                    );
                    graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }

            drawn++;
        }
    }

    static int aspectStartX(int visibleCount) {
        return 40;
    }

    static int maxAspectStart(int aspectCount) {
        return Math.max(0, aspectCount - MAX_VISIBLE_ASPECTS);
    }

    static int progressWidth(int stored, int required) {
        if (required <= 0) {
            return 0;
        }
        return Mth.clamp(
                (int) ((float) stored / required * ASPECT_PROGRESS_WIDTH),
                0,
                ASPECT_PROGRESS_WIDTH
        );
    }

    private void drawOutput(
            GuiGraphics graphics,
            CrucibleRecipeDefinition recipe,
            float partialTick
    ) {
        boolean disabled = !menu.selected(recipe)
                && !menu.canSelectOrSwitch(recipe);
        if (disabled) {
            float ticks = minecraft != null && minecraft.player != null
                    ? minecraft.player.tickCount + partialTick : 0.0F;
            float alpha = 0.6F + Mth.sin(ticks / 4.0F) * 0.3F;
            graphics.setColor(0.5F, 0.5F, 0.5F, alpha);
        }
        graphics.renderItem(recipe.output(), leftPos + 112, topPos + 16);
        graphics.renderItemDecorations(
                font,
                recipe.output(),
                leftPos + 112,
                topPos + 16
        );
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawRecipeCount(GuiGraphics graphics, int recipeCount) {
        if (recipeCount <= 0) {
            return;
        }
        String text = recipeCounterText(index, recipeCount);
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + RECIPE_COUNTER_RIGHT_X,
                topPos + RECIPE_COUNTER_Y, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, text, -font.width(text), 0,
                0xFFFFFF, false);
        graphics.pose().popPose();
    }

    static String recipeCounterText(int recipeIndex, int recipeCount) {
        if (recipeCount <= 0) {
            return "";
        }
        return (Mth.clamp(recipeIndex, 0, recipeCount - 1) + 1)
                + "/" + recipeCount;
    }

    private static List<Map.Entry<String, Integer>> sortedAspects(
            CrucibleRecipeDefinition recipe
    ) {
        return recipe.aspects().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .toList();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<CrucibleRecipeDefinition> recipes = menu.recipes();
        if (recipes.isEmpty() || index < 0 || index >= recipes.size()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        CrucibleRecipeDefinition recipe = recipes.get(index);
        if (inside(mouseX, mouseY, 112, 16, 16, 16)) {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId,
                        index
                );
            }
            return true;
        }
        if (recipes.size() > 1) {
            if (index > 0 && inside(mouseX, mouseY,
                    RECIPE_ARROW_X, RECIPE_PREVIOUS_Y,
                    RECIPE_ARROW_WIDTH, RECIPE_ARROW_HEIGHT)) {
                index--;
                startAspect = 0;
                playClickSound();
                return true;
            }
            if (index < recipes.size() - 1
                    && inside(mouseX, mouseY,
                    RECIPE_ARROW_X, RECIPE_NEXT_Y,
                    RECIPE_ARROW_WIDTH, RECIPE_ARROW_HEIGHT)) {
                index++;
                startAspect = 0;
                playClickSound();
                return true;
            }
        }
        int aspectCount = recipe.aspects().size();
        if (aspectCount > MAX_VISIBLE_ASPECTS) {
            if (startAspect > 0
                    && inside(mouseX, mouseY, 32, 40, 8, 16)) {
                startAspect--;
                playClickSound();
                return true;
            }
            if (startAspect < maxAspectStart(aspectCount)
                    && inside(mouseX, mouseY, 136, 40, 8, 16)) {
                startAspect++;
                playClickSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean inside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width
                && mouseY >= topPos + y && mouseY < topPos + y + height;
    }

    private void playClickSound() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(
                    ModSounds.CAMERA_CLACK.get(),
                    0.4F,
                    1.0F
            );
        }
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
    }
}
