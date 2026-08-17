package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.aspect.AspectCostProvider;
import com.thaumcraftmodern.arcane.ArcaneRecipe;
import com.thaumcraftmodern.arcane.ArcaneShapedRecipe;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.construction.AdvancedAlchemicalFurnaceResearchRecipe;
import com.thaumcraftmodern.construction.InfernalFurnaceResearchRecipe;
import com.thaumcraftmodern.construction.InfusionAltarResearchRecipe;
import com.thaumcraftmodern.construction.ThaumatoriumResearchRecipe;
import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import com.thaumcraftmodern.crucible.CrucibleRecipeRegistry;
import com.thaumcraftmodern.infusion.InfusionRecipeDefinition;
import com.thaumcraftmodern.nodejar.NodeJarResearchRecipe;
import com.thaumcraftmodern.nodejar.NodeJarStructure;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.research.InfusionDisplayDefinition;
import com.thaumcraftmodern.research.ResearchDefinition;
import com.thaumcraftmodern.research.ResearchPageDefinition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Pixel-exact renderer for a single Thaumonomicon research page.
 *
 * <p>The layout code is intentionally kept together: its constants and draw
 * order are part of the TC4 visual contract.</p>
 */
final class ThaumonomiconPageRenderer {
    private static final ResourceLocation BOOK_OVERLAY =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/gui_researchbook_overlay.png"
            );
    private static final ResourceLocation CLASSIC_BOOK_FONT =
            new ResourceLocation("minecraft", "uniform");
    private static final int PAGE_CONTENT_BOTTOM = 174;
    private static final int RECIPE_LAYOUT_WIDTH = 112;
    private static final int RECIPE_GRID_OFFSET_X = 4;
    private static final int RECIPE_GRID_OFFSET_Y = 42;
    private static final int RECIPE_GRID_SIZE = 104;
    private static final int RECIPE_SLOT_OFFSET_X = 16;
    private static final int RECIPE_SLOT_OFFSET_Y = 54;
    private static final int RECIPE_SLOT_STEP = 32;
    private static final int PAGE_ASPECT_COST_BOTTOM = 196;
    private static final int RECIPE_ASPECT_COST_BOTTOM = 174;
    private static final int COMPOUND_PAGE_X_OFFSET = 11;
    private static final int COMPOUND_PAGE_Y_OFFSET = -2;
    private static final int COMPOUND_TITLE_CENTER_X = 56;
    private static final int COMPOUND_ASPECT_COST_BOTTOM = 198;
    private static final int COMPOUND_COST_GLYPH_Y = 174;
    private static final int COMPOUND_COST_GLYPH_SIZE = 24;
    private static final int COMPOUND_STRUCTURE_CENTER_X = 64;
    private static final int COMPOUND_STRUCTURE_BASE_Y = 108;
    private static final int COMPOUND_LAYER_Y = 50;
    private static final int INFUSION_LAYOUT_WIDTH = 112;
    private static final int INFUSION_PAGE_Y_OFFSET = -20;
    private static final int INFUSION_OUTPUT_Y_OFFSET = 10;
    private static final int INFUSION_MATRIX_Y_OFFSET = 22;
    private static final int INFUSION_CENTRAL_Y = 78;
    private static final int INFUSION_COMPONENT_CENTER_Y = 86;
    private static final int INFUSION_COMPONENT_RADIUS = 40;
    private static final int INFUSION_RECIPE_CONTENT_BOTTOM = 156;
    private static final int INFUSION_SECTION_GAP = 5;
    private static final int INFUSION_INSTABILITY_Y = 193;

    private Minecraft minecraft;
    private Font font;
    private int top;
    private ResearchDefinition openResearch;
    private List<ThaumonomiconItemLinkRegion> itemLinkRegions = List.of();

    void beginFrame(
            Minecraft minecraft,
            Font font,
            int top,
            ResearchDefinition openResearch,
            List<ThaumonomiconItemLinkRegion> itemLinkRegions
    ) {
        this.minecraft = minecraft;
        this.font = font;
        this.top = top;
        this.openResearch = openResearch;
        this.itemLinkRegions = itemLinkRegions;
    }

    private static boolean contains(
            int x,
            int y,
            double width,
            double height,
            double mouseX,
            double mouseY
    ) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    void renderPage(
            GuiGraphics graphics,
            int pageIndex,
            int x,
            int y,
            int pageWidth,
            int mouseX,
            int mouseY
    ) {
        if (openResearch == null || pageIndex < 0 || pageIndex >= openResearch.pages().size()) {
            return;
        }
        ResearchPageDefinition page = openResearch.pages().get(pageIndex);
        int pageY = page.type() == ResearchPageDefinition.Type.INFUSION
                ? y + INFUSION_PAGE_Y_OFFSET
                : y;
        if (page.type() != ResearchPageDefinition.Type.INFUSION) {
            renderPageTitle(
                    graphics,
                    Component.translatable(page.titleKey()),
                    x + pageWidth / 2,
                    pageY,
                    pageWidth
            );
        }

        if (page.type() == ResearchPageDefinition.Type.TEXT) {
            int aspectCostHeight =
                    ThaumonomiconAspectCostRenderer.requiredHeight(
                            page.aspectCosts(),
                            pageWidth
                    );
            ThaumonomiconMarkupRenderer.render(
                    graphics,
                    font,
                    Component.translatable(page.bodyKey()).getString(),
                    x,
                    y + 15,
                    pageWidth,
                    top + PAGE_CONTENT_BOTTOM
                            - Math.max(0, aspectCostHeight - 16)
            );
        } else if (page.type() == ResearchPageDefinition.Type.RECIPE) {
            renderRecipe(graphics, page, x, y + 22, pageWidth, mouseX, mouseY);
        } else if (page.type() == ResearchPageDefinition.Type.COMPOUND_CRAFTING) {
            renderCompoundCrafting(
                    graphics,
                    page,
                    x + COMPOUND_PAGE_X_OFFSET,
                    y + COMPOUND_PAGE_Y_OFFSET,
                    mouseX,
                    mouseY
            );
        } else if (page.type() == ResearchPageDefinition.Type.INFUSION) {
            renderInfusionDisplay(
                    graphics,
                    page,
                    x,
                    pageY + 8,
                    pageWidth,
                    mouseX,
                    mouseY
            );
        } else {
            renderFittedText(
                    graphics,
                    Component.translatable(
                            "screen.thaumic_reborn.thaumonomicon.content_inactive"
                    ),
                    x,
                    y + 15,
                    pageWidth,
                    top + PAGE_CONTENT_BOTTOM
            );
        }

        if (page.type() != ResearchPageDefinition.Type.RECIPE
                && page.type() != ResearchPageDefinition.Type.COMPOUND_CRAFTING
                && page.type() != ResearchPageDefinition.Type.INFUSION
                && !page.aspectCosts().isEmpty()) {
            String hoveredAspect = ThaumonomiconAspectCostRenderer.render(
                    graphics,
                    font,
                    page.aspectCosts(),
                    x,
                    pageWidth,
                    y + PAGE_ASPECT_COST_BOTTOM,
                    mouseX,
                    mouseY
            );
            renderAspectTooltip(graphics, hoveredAspect, mouseX, mouseY);
        }
    }

    private void renderInfusionDisplay(
            GuiGraphics graphics,
            ResearchPageDefinition page,
            int x,
            int y,
            int pageWidth,
            int mouseX,
            int mouseY
    ) {
        InfusionDisplayDefinition display = page.infusionDisplay();
        if (display == null) {
            return;
        }
        RunicAugmentationPreview runicPreview = RunicAugmentationPreview.atTime(
                page, display, Util.getMillis());
        List<InfusionDisplayDefinition.ComponentStack> displayedComponents =
                runicPreview == null ? display.components()
                        : runicPreview.components();
        List<AspectCost> displayedCosts = runicPreview == null
                ? page.aspectCosts() : runicPreview.costs();
        InfusionDisplayDefinition.Instability displayedInstability =
                runicPreview == null ? display.instability()
                        : runicPreview.instability();
        int layoutX = x + (pageWidth - INFUSION_LAYOUT_WIDTH) / 2;
        int centerX = layoutX + INFUSION_LAYOUT_WIDTH / 2;

        /*
         * TC4 GuiResearchRecipe.drawInfusionPage uses the same 512px book
         * overlay through a legacy 256px UV space. The matrix region below
         * corresponds to (200,77,60,44) at the original on-page size.
         */
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                BOOK_OVERLAY,
                layoutX,
                y + 45 + INFUSION_MATRIX_Y_OFFSET,
                112,
                88,
                400,
                154,
                112,
                88,
                512,
                512,
                0xFFFFFFFF
        );

        long durabilityPreviewTime = Util.getMillis();
        ItemStack central = ThaumonomiconDurabilityPreview.atTime(
                infusionStack(display.centralItem(), 1),
                durabilityPreviewTime
        );
        ItemStack output = infusionStack(display.outputItem(), 1);
        if (isDamageableTransformation(central, output)) {
            output.setDamageValue(InfusionRecipeDefinition.transferredDamage(
                    central.getDamageValue(), output.getMaxDamage()));
        }
        if (runicPreview != null) {
            if (runicPreview.inputHardening() > 0) {
                central.getOrCreateTag().putByte("RS.HARDEN",
                        (byte) runicPreview.inputHardening());
            }
            output.getOrCreateTag().putByte("RS.HARDEN",
                    (byte) runicPreview.outputHardening());
        }
        ThaumonomiconRecipeOutputRenderer.render(
                graphics,
                font,
                output,
                layoutX,
                y + 6 + INFUSION_OUTPUT_Y_OFFSET,
                mouseX,
                mouseY
        );
        registerItemLink(output,
                layoutX + ThaumonomiconRecipeOutputRenderer.ITEM_OFFSET_X,
                y + 6 + INFUSION_OUTPUT_Y_OFFSET
                        + ThaumonomiconRecipeOutputRenderer.ITEM_OFFSET_Y,
                16, 16);
        int centralX = centerX - 8;
        int centralY = y + INFUSION_CENTRAL_Y + INFUSION_MATRIX_Y_OFFSET;
        renderLinkedItemExact(graphics, central, centralX, centralY);

        int componentCount = displayedComponents.size();
        for (int index = 0; index < componentCount; index++) {
            double angle = -Math.PI / 2.0D
                    + Math.PI * 2.0D * index / componentCount;
            int itemX = centerX
                    + (int) (Math.cos(angle) * INFUSION_COMPONENT_RADIUS)
                    - 8;
            int itemY = y
                    + INFUSION_COMPONENT_CENTER_Y
                    + INFUSION_MATRIX_Y_OFFSET
                    + (int) (Math.sin(angle) * INFUSION_COMPONENT_RADIUS)
                    - 8;
            InfusionDisplayDefinition.ComponentStack component =
                    displayedComponents.get(index);
            ItemStack stack = infusionStack(component, index);
            renderLinkedItem(graphics, stack, itemX, itemY);
        }

        int aspectHeight = ThaumonomiconAspectCostRenderer.requiredHeight(
                displayedCosts, pageWidth
        );
        int minimumAspectTop = y
                + INFUSION_RECIPE_CONTENT_BOTTOM
                + INFUSION_SECTION_GAP;
        int bottomAlignedAspectTop = y
                + INFUSION_INSTABILITY_Y
                - INFUSION_SECTION_GAP
                - aspectHeight;
        int aspectTop = Math.max(minimumAspectTop, bottomAlignedAspectTop);
        int aspectBottom = aspectTop + aspectHeight;
        String hoveredAspect = ThaumonomiconAspectCostRenderer.render(
                graphics,
                font,
                displayedCosts,
                x,
                pageWidth,
                aspectBottom,
                mouseX,
                mouseY
        );
        renderAspectTooltip(graphics, hoveredAspect, mouseX, mouseY);

        Component instability = classicBookText(Component.translatable(
                "screen.thaumic_reborn.thaumonomicon.instability",
                Component.translatable(
                        displayedInstability.translationKey()
                )
        ));
        graphics.drawCenteredString(
                font,
                instability,
                x + pageWidth / 2,
                Math.max(
                        y + INFUSION_INSTABILITY_Y,
                        aspectBottom + INFUSION_SECTION_GAP
                ),
                displayedInstability.color()
        );
    }

    private static ItemStack infusionStack(String itemId, int count) {
        ItemStack stack = BuiltInRegistries.ITEM
                .getOptional(new ResourceLocation(itemId))
                .map(item -> item.getDefaultInstance())
                .orElseGet(Items.BARRIER::getDefaultInstance);
        stack.setCount(Math.max(1, count));
        return stack;
    }

    private static ItemStack infusionStack(
            InfusionDisplayDefinition.ComponentStack component,
            int slotIndex
    ) {
        if (!component.isTag()) {
            ItemStack stack = infusionStack(component.item(), component.count());
            if (!component.potion().isBlank()) {
                stack.getOrCreateTag().putString("Potion", component.potion());
            }
            return stack;
        }
        ItemStack stack = cyclingIngredient(
                Ingredient.of(ItemTags.create(new ResourceLocation(component.tag()))),
                slotIndex
        ).copy();
        if (stack.isEmpty()) {
            return Items.BARRIER.getDefaultInstance();
        }
        stack.setCount(component.count());
        return stack;
    }

    private void renderPageTitle(
            GuiGraphics graphics,
            Component title,
            int centerX,
            int y,
            int maxWidth
    ) {
        Component classicTitle = classicBookText(title);
        int titleWidth = font.width(classicTitle);
        float scale = titleWidth <= maxWidth
                ? 1.0F
                : maxWidth / (float) titleWidth;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(
                font,
                classicTitle,
                -titleWidth / 2,
                0,
                0x505050,
                false
        );
        graphics.pose().popPose();
    }

    private static Component classicBookText(Component text) {
        return text.copy().withStyle(style -> style.withFont(CLASSIC_BOOK_FONT));
    }

    private void renderFittedText(
            GuiGraphics graphics,
            Component text,
            int x,
            int y,
            int pageWidth,
            int bottom
    ) {
        float selectedScale = 0.65F;
        List<FormattedCharSequence> selectedLines = List.of();
        for (float scale : new float[]{1.0F, 0.9F, 0.8F, 0.72F, 0.65F}) {
            List<FormattedCharSequence> lines = font.split(text, Math.max(1, (int) (pageWidth / scale)));
            if (lines.size() * 10.0F * scale <= bottom - y) {
                selectedScale = scale;
                selectedLines = lines;
                break;
            }
            selectedLines = lines;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(selectedScale, selectedScale, 1.0F);
        int maxLines = Math.max(1, (int) ((bottom - y) / (10.0F * selectedScale)));
        for (int index = 0; index < selectedLines.size() && index < maxLines; index++) {
            graphics.drawString(font, selectedLines.get(index), 0, index * 10, 0x342117, false);
        }
        graphics.pose().popPose();
    }

    private void renderRecipe(
            GuiGraphics graphics,
            ResearchPageDefinition page,
            int x,
            int y,
            int pageWidth,
            int mouseX,
            int mouseY
    ) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        ResourceLocation requestedId = ResourceLocation.tryParse(
                selectedRecipeId(page)
        );
        if (requestedId == null) {
            return;
        }
        CrucibleRecipeDefinition crucibleRecipe =
                CrucibleRecipeRegistry.all().stream()
                        .filter(candidate -> candidate.id().equals(requestedId))
                        .findFirst()
                        .orElse(null);
        if (crucibleRecipe != null) {
            renderCrucibleRecipe(
                    graphics,
                    crucibleRecipe,
                    x,
                    y,
                    pageWidth,
                    mouseX,
                    mouseY
            );
            return;
        }
        Recipe<?> recipe = minecraft.level.getRecipeManager()
                .byKey(requestedId)
                .orElse(null);
        if (recipe == null) {
            graphics.drawString(
                    font,
                    Component.translatable("screen.thaumic_reborn.thaumonomicon.recipe_missing"),
                    x,
                    y,
                    0x8A1F1F,
                    false
            );
            return;
        }
        if (!(recipe instanceof ArcaneRecipe)
                && !(recipe instanceof CraftingRecipe)) {
            renderTransformationRecipe(
                    graphics,
                    recipe,
                    x,
                    y,
                    pageWidth,
                    mouseX,
                    mouseY
            );
            return;
        }

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int recipeLeft = x + (pageWidth - RECIPE_LAYOUT_WIDTH) / 2;
        int gridX = recipeLeft + RECIPE_GRID_OFFSET_X;
        int gridY = y + RECIPE_GRID_OFFSET_Y;
        /*
         * TC4 addressed this 512px atlas through a legacy 256px UV space.
         * Its 52x52 call was made under a 2x pose, so it occupied 104x104
         * screen pixels while sampling the physical 104x104 region below.
         */
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                BOOK_OVERLAY,
                gridX,
                gridY,
                RECIPE_GRID_SIZE,
                RECIPE_GRID_SIZE,
                120,
                30,
                RECIPE_GRID_SIZE,
                RECIPE_GRID_SIZE,
                512,
                512,
                0xFFFFFFFF
        );
        int recipeWidth = craftingRecipeWidth(recipe);
        for (int index = 0; index < ingredients.size() && index < 9; index++) {
            ItemStack displayed = cyclingIngredient(ingredients.get(index), index);
            if (displayed.isEmpty()) {
                continue;
            }
            int itemX = recipeLeft
                    + RECIPE_SLOT_OFFSET_X
                    + (index % recipeWidth) * RECIPE_SLOT_STEP;
            int itemY = y
                    + RECIPE_SLOT_OFFSET_Y
                    + (index / recipeWidth) * RECIPE_SLOT_STEP;
            renderLinkedItem(graphics, displayed, itemX, itemY);
        }

        ItemStack result = recipe.getResultItem(minecraft.level.registryAccess());
        ThaumonomiconRecipeOutputRenderer.render(
                graphics,
                font,
                result,
                recipeLeft,
                y + 2,
                mouseX,
                mouseY
        );
        registerItemLink(result,
                recipeLeft + ThaumonomiconRecipeOutputRenderer.ITEM_OFFSET_X,
                y + 2 + ThaumonomiconRecipeOutputRenderer.ITEM_OFFSET_Y,
                16, 16);

        String hoveredAspect = ThaumonomiconAspectCostRenderer.render(
                graphics,
                font,
                aspectCostsFor(page, recipe),
                x,
                pageWidth,
                y + RECIPE_ASPECT_COST_BOTTOM,
                mouseX,
                mouseY
        );
        renderAspectTooltip(graphics, hoveredAspect, mouseX, mouseY);
    }

    /**
     * Shaped recipes expose a compact width x height ingredient list rather
     * than a padded 3x3 list. Preserve that width when drawing the book grid;
     * shapeless recipes retain the conventional three-column flow.
     */
    static int craftingRecipeWidth(Recipe<?> recipe) {
        if (recipe instanceof ShapedRecipe shaped) {
            return Math.max(1, Math.min(3, shaped.getWidth()));
        }
        if (recipe instanceof ArcaneShapedRecipe shaped) {
            return Math.max(1, Math.min(3, shaped.width()));
        }
        return 3;
    }

    private static String selectedRecipeId(ResearchPageDefinition page) {
        List<String> recipes = page.recipeIds();
        if (recipes.isEmpty()) {
            return page.recipeId();
        }
        int index = Math.floorMod(
                (int) (System.currentTimeMillis() / 1000L),
                recipes.size()
        );
        return recipes.get(index);
    }

    /**
     * TC4 used its SMELTING page for input/output transformations that do not
     * have an Arcane Workbench, normal crafting-grid, Crucible, or Infusion
     * presentation. The original vertical smoky arrow is preserved here.
     */
    private void renderTransformationRecipe(
            GuiGraphics graphics,
            Recipe<?> recipe,
            int x,
            int y,
            int pageWidth,
            int mouseX,
            int mouseY
    ) {
        ItemStack input = recipe.getIngredients().stream()
                .map(ingredient -> cyclingIngredient(ingredient, 0))
                .filter(stack -> !stack.isEmpty())
                .findFirst()
                .orElse(ItemStack.EMPTY);
        ItemStack output = recipe.getResultItem(
                minecraft.level.registryAccess()
        );
        if (input.isEmpty() || output.isEmpty()) {
            return;
        }

        int layoutX = ThaumonomiconTransformationRecipeLayout.left(
                x,
                pageWidth
        );
        if (recipe instanceof AbstractCookingRecipe) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("recipe.type.smelting"),
                    layoutX
                            + ThaumonomiconTransformationRecipeLayout.WIDTH
                            / 2,
                    y,
                    0x505050
            );
        }
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                BOOK_OVERLAY,
                layoutX,
                y + ThaumonomiconTransformationRecipeLayout.OVERLAY_TOP,
                ThaumonomiconTransformationRecipeLayout.OVERLAY_WIDTH,
                ThaumonomiconTransformationRecipeLayout.OVERLAY_HEIGHT,
                ThaumonomiconTransformationRecipeLayout.OVERLAY_SOURCE_X,
                ThaumonomiconTransformationRecipeLayout.OVERLAY_SOURCE_Y,
                ThaumonomiconTransformationRecipeLayout.OVERLAY_WIDTH,
                ThaumonomiconTransformationRecipeLayout.OVERLAY_HEIGHT,
                512,
                512,
                0xFFFFFFFF
        );
        renderTransformationItem(
                graphics,
                input,
                layoutX + ThaumonomiconTransformationRecipeLayout.INPUT_X,
                y + ThaumonomiconTransformationRecipeLayout.INPUT_Y,
                mouseX,
                mouseY
        );
        renderTransformationItem(
                graphics,
                output,
                layoutX + ThaumonomiconTransformationRecipeLayout.OUTPUT_X,
                y + ThaumonomiconTransformationRecipeLayout.OUTPUT_Y,
                mouseX,
                mouseY
        );
    }

    private void renderTransformationItem(
            GuiGraphics graphics,
            ItemStack stack,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        renderLinkedItem(graphics, stack, x, y);
    }

    private void renderCrucibleRecipe(
            GuiGraphics graphics,
            CrucibleRecipeDefinition recipe,
            int x,
            int y,
            int pageWidth,
            int mouseX,
            int mouseY
    ) {
        int layoutX = ThaumonomiconCrucibleRecipeLayout.left(
                x,
                pageWidth
        );
        graphics.drawCenteredString(
                font,
                Component.translatable("recipe.type.crucible"),
                layoutX + ThaumonomiconCrucibleRecipeLayout.WIDTH / 2,
                y,
                0x505050
        );
        int contentY = y
                + ThaumonomiconCrucibleRecipeLayout.CONTENT_OFFSET_Y;

        ItemStack output = recipe.output();
        ThaumonomiconRecipeOutputRenderer.render(
                graphics,
                font,
                output,
                layoutX,
                contentY + ThaumonomiconCrucibleRecipeLayout.OUTPUT_TOP,
                mouseX,
                mouseY
        );
        registerItemLink(output,
                layoutX + ThaumonomiconRecipeOutputRenderer.ITEM_OFFSET_X,
                contentY + ThaumonomiconCrucibleRecipeLayout.OUTPUT_TOP
                        + ThaumonomiconRecipeOutputRenderer.ITEM_OFFSET_Y,
                16, 16);

        /*
         * Physical atlas coordinates for TC4 legacy regions
         * (0,20,56,48) and (100,84,11,13), addressed at 2x scale.
         */
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                BOOK_OVERLAY,
                layoutX,
                contentY
                        + ThaumonomiconCrucibleRecipeLayout.CAULDRON_TOP,
                112,
                96,
                0,
                40,
                112,
                96,
                512,
                512,
                0xFFFFFFFF
        );
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                BOOK_OVERLAY,
                layoutX + ThaumonomiconCrucibleRecipeLayout.ARROW_X,
                contentY + ThaumonomiconCrucibleRecipeLayout.ARROW_Y,
                22,
                26,
                200,
                168,
                22,
                26,
                512,
                512,
                0xFFFFFFFF
        );

        int catalystX = layoutX
                + ThaumonomiconCrucibleRecipeLayout.CATALYST_X;
        int catalystY = contentY
                + ThaumonomiconCrucibleRecipeLayout.CATALYST_Y;
        ItemStack catalyst = cyclingIngredient(recipe.catalyst(), 0);
        if (!recipe.catalystAspect().isBlank()
                && catalyst.is(ModItems.ESSENTIA_PHIAL.get())) {
            catalyst = com.thaumcraftmodern.item.EssentiaPhialItem.filled(
                    ModItems.ESSENTIA_PHIAL.get(), recipe.catalystAspect());
        }
        if (!catalyst.isEmpty()) {
            renderLinkedItem(graphics, catalyst, catalystX, catalystY);
        }

        List<AspectCost> costs = recipe.aspects().entrySet().stream()
                .map(entry -> new AspectCost(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(AspectCost::aspectId))
                .toList();
        String hoveredAspect =
                ThaumonomiconAspectCostRenderer.renderCrucibleGrid(
                        graphics,
                        font,
                        costs,
                        layoutX,
                        contentY,
                        mouseX,
                        mouseY
                );
        renderAspectTooltip(graphics, hoveredAspect, mouseX, mouseY);
    }

    private void renderCompoundCrafting(
            GuiGraphics graphics,
            ResearchPageDefinition page,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        CompoundRecipeView recipe = compoundRecipeView(
                ResourceLocation.tryParse(page.recipeId())
        );
        if (recipe == null) {
            graphics.drawString(
                    font,
                    Component.translatable(
                            "screen.thaumic_reborn.thaumonomicon.recipe_missing"
                    ),
                    x,
                    y,
                    0x8A1F1F,
                    false
            );
            return;
        }

        graphics.drawCenteredString(
                font,
                Component.translatable("recipe.type.construct"),
                x + COMPOUND_TITLE_CENTER_X,
                y,
                0x505050
        );

        String hoveredAspect = renderCompoundCosts(
                graphics,
                recipe,
                x,
                y,
                mouseX,
                mouseY
        );

        /*
         * TC4 addressed this 512px atlas through a 256px UV space.
         * Legacy (68,76,12,12) therefore maps to physical
         * (136,152,24,24), with the original 2x pose producing a 24px glyph.
         */
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                BOOK_OVERLAY,
                x,
                y + COMPOUND_COST_GLYPH_Y,
                COMPOUND_COST_GLYPH_SIZE,
                COMPOUND_COST_GLYPH_SIZE,
                136,
                152,
                24,
                24,
                512,
                512,
                0x66FFFFFF
        );

        int xOffset = COMPOUND_STRUCTURE_CENTER_X
                - (recipe.width() * 16 + recipe.depth() * 16) / 2;
        int yOffset = -recipe.height() * 25;
        float sizeReduction = recipe.height() > 3
                ? (recipe.height() - 3) * 0.2F
                : 0.0F;
        float structureScale = 1.0F - sizeReduction;
        float structureX = x + xOffset * (1.0F + sizeReduction);
        float structureY = y
                + COMPOUND_STRUCTURE_BASE_Y
                + yOffset * structureScale;

        List<ItemStack> displayStacks = recipe.cells();
        graphics.pose().pushPose();
        graphics.pose().translate(structureX, structureY, 0.0F);
        graphics.pose().scale(structureScale, structureScale, structureScale);

        /*
         * Legacy (0,72,64,44) on the same 512px atlas is the physical
         * (0,144,128,88) pedestal. The original inner 2x transform is
         * represented directly by the 128x88 destination below.
         */
        int pedestalY = -119
                + Math.max(3 - recipe.width(), 3 - recipe.depth()) * 8
                + recipe.width() * 4
                + recipe.depth() * 4
                + recipe.height() * COMPOUND_LAYER_Y;
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                BOOK_OVERLAY,
                -8 - xOffset,
                pedestalY,
                128,
                88,
                0,
                144,
                128,
                88,
                512,
                512,
                0x80FFFFFF
        );

        int count = 0;
        for (int layer = 0; layer < recipe.height(); layer++) {
            for (int depth = recipe.depth() - 1; depth >= 0; depth--) {
                for (int width = recipe.width() - 1; width >= 0; width--) {
                    int itemX = width * 16 + depth * 16;
                    int itemY = -width * 8
                            + depth * 8
                            + layer * COMPOUND_LAYER_Y;
                    ItemStack stack = displayStacks.get(count++);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    graphics.pose().pushPose();
                    graphics.pose().translate(
                            0.0F,
                            0.0F,
                            60.0F - layer * 10.0F
                    );
                    graphics.renderItem(stack, itemX, itemY);
                    graphics.pose().popPose();
                }
            }
        }
        graphics.pose().popPose();

        registerCompoundItemLinks(
                recipe,
                displayStacks,
                x,
                y,
                xOffset,
                yOffset,
                sizeReduction
        );

        ItemStack hoveredItem = compoundHoveredItem(
                recipe,
                displayStacks,
                x,
                y,
                xOffset,
                yOffset,
                sizeReduction,
                mouseX,
                mouseY
        );
        if (hoveredItem.isEmpty() && hoveredAspect != null) {
            renderAspectTooltip(graphics, hoveredAspect, mouseX, mouseY);
        }
    }

    private String renderCompoundCosts(
            GuiGraphics graphics,
            CompoundRecipeView recipe,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        return ThaumonomiconAspectCostRenderer.render(
                graphics,
                font,
                recipe.costs(),
                x,
                COMPOUND_TITLE_CENTER_X * 2,
                y + COMPOUND_ASPECT_COST_BOTTOM,
                mouseX,
                mouseY
        );
    }

    private static List<AspectCost> aspectCostsFor(
            ResearchPageDefinition page,
            Recipe<?> recipe
    ) {
        if (!page.aspectCosts().isEmpty()) {
            return page.aspectCosts();
        }
        return recipe instanceof AspectCostProvider provider
                ? provider.aspectCosts()
                : List.of();
    }

    private void renderAspectTooltip(
            GuiGraphics graphics,
            String aspectId,
            int mouseX,
            int mouseY
    ) {
        if (aspectId != null) {
            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            Component.translatable(
                                            "aspect.thaumic_reborn."
                                                    + aspectId
                                    )
                                    .withStyle(ChatFormatting.AQUA),
                            Component.translatable("tc.aspect." + aspectId)
                                    .withStyle(ChatFormatting.GRAY)
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    private static List<ItemStack> nodeJarDisplayStacks(
            NodeJarResearchRecipe.Snapshot recipe
    ) {
        ItemStack woodenSlab = cyclingWoodenSlab();
        return recipe.cells().stream()
                .map(kind -> switch (kind) {
                    case WOODEN_SLAB -> woodenSlab;
                    case GLASS -> Items.GLASS.getDefaultInstance();
                    // TC4's blockAiry meta 5 uses the transparent "blank"
                    // item icon, so the node cell is intentionally invisible.
                    case AURA_NODE -> ItemStack.EMPTY;
                })
                .toList();
    }

    private static CompoundRecipeView compoundRecipeView(
            ResourceLocation recipeId
    ) {
        if (NodeJarResearchRecipe.ID.equals(recipeId)) {
            NodeJarResearchRecipe.Snapshot recipe = NodeJarResearchRecipe.snapshot();
            List<AspectCost> costs = recipe.costs().stream()
                    .map(cost -> new AspectCost(cost.aspect().id(), cost.amount()))
                    .toList();
            return new CompoundRecipeView(
                    recipe.width(),
                    recipe.height(),
                    recipe.depth(),
                    costs,
                    nodeJarDisplayStacks(recipe)
            );
        }
        if (ThaumatoriumResearchRecipe.ID.equals(recipeId)) {
            ThaumatoriumResearchRecipe.Snapshot recipe =
                    ThaumatoriumResearchRecipe.snapshot();
            return new CompoundRecipeView(
                    recipe.width(),
                    recipe.height(),
                    recipe.depth(),
                    recipe.costs(),
                    recipe.cells().stream()
                            .map(cell -> switch (cell) {
                                case ALCHEMICAL_CONSTRUCT -> new ItemStack(
                                        ModItems.ALCHEMICAL_CONSTRUCT.get());
                                case ALCHEMICAL_FURNACE -> new ItemStack(
                                        ModItems.ALCHEMICAL_FURNACE.get());
                            })
                    .toList()
            );
        }
        if (InfusionAltarResearchRecipe.ID.equals(recipeId)) {
            InfusionAltarResearchRecipe.Snapshot recipe =
                    InfusionAltarResearchRecipe.snapshot();
            return new CompoundRecipeView(
                    recipe.width(),
                    recipe.height(),
                    recipe.depth(),
                    recipe.costs(),
                    recipe.cells().stream()
                            .map(cell -> switch (cell) {
                                case EMPTY -> ItemStack.EMPTY;
                                case RUNIC_MATRIX -> new ItemStack(
                                        ModItems.RUNIC_MATRIX.get());
                                case ARCANE_STONE -> new ItemStack(
                                        ModItems.ARCANE_STONE.get());
                                case ARCANE_STONE_BRICK -> new ItemStack(
                                        ModItems.ARCANE_STONE_BRICK.get());
                                case ARCANE_PEDESTAL -> new ItemStack(
                                        ModItems.ARCANE_PEDESTAL.get());
                            })
                            .toList()
            );
        }
        if (InfernalFurnaceResearchRecipe.ID.equals(recipeId)) {
            InfernalFurnaceResearchRecipe.Snapshot recipe =
                    InfernalFurnaceResearchRecipe.snapshot();
            return new CompoundRecipeView(
                    recipe.width(), recipe.height(), recipe.depth(),
                    recipe.costs(),
                    recipe.cells().stream().map(cell -> switch (cell) {
                        case EMPTY -> ItemStack.EMPTY;
                        case NETHER_BRICKS -> new ItemStack(Blocks.NETHER_BRICKS);
                        case OBSIDIAN -> new ItemStack(Blocks.OBSIDIAN);
                        case LAVA -> new ItemStack(Items.LAVA_BUCKET);
                        case IRON_BARS -> new ItemStack(Blocks.IRON_BARS);
                    }).toList()
            );
        }
        if (AdvancedAlchemicalFurnaceResearchRecipe.ID.equals(recipeId)) {
            AdvancedAlchemicalFurnaceResearchRecipe.Snapshot recipe =
                    AdvancedAlchemicalFurnaceResearchRecipe.snapshot();
            return new CompoundRecipeView(
                    recipe.width(), recipe.height(), recipe.depth(), recipe.costs(),
                    recipe.cells().stream().map(cell -> switch (cell) {
                        case EMPTY -> ItemStack.EMPTY;
                        case ADVANCED_CONSTRUCT -> new ItemStack(
                                ModItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
                        case ALCHEMICAL_FURNACE -> new ItemStack(
                                ModItems.ALCHEMICAL_FURNACE.get());
                        case ARCANE_ALEMBIC -> new ItemStack(ModItems.ARCANE_ALEMBIC.get());
                        case ALCHEMICAL_CONSTRUCT -> new ItemStack(
                                ModItems.ALCHEMICAL_CONSTRUCT.get());
                    }).toList()
            );
        }
        return null;
    }

    private static ItemStack cyclingWoodenSlab() {
        List<ItemStack> slabs = BuiltInRegistries.ITEM
                .getTag(ItemTags.WOODEN_SLABS)
                .map(tag -> tag.stream()
                        .map(holder -> holder.value().getDefaultInstance())
                        .toList())
                .orElse(List.of(Items.OAK_SLAB.getDefaultInstance()));
        if (slabs.isEmpty()) {
            return Items.OAK_SLAB.getDefaultInstance();
        }
        int index = (int) Math.floorMod(
                Util.getMillis() / 1_000L,
                slabs.size()
        );
        return slabs.get(index);
    }

    static ItemStack cyclingIngredient(Ingredient ingredient, int slotIndex) {
        ItemStack[] options = ingredient.getItems();
        if (options.length == 0) {
            return ItemStack.EMPTY;
        }
        int index = (int) Math.floorMod(
                Util.getMillis() / 1_000L + slotIndex,
                options.length
        );
        return options[index];
    }

    private static ItemStack compoundHoveredItem(
            CompoundRecipeView recipe,
            List<ItemStack> displayStacks,
            int x,
            int y,
            int xOffset,
            int yOffset,
            float sizeReduction,
            int mouseX,
            int mouseY
    ) {
        float scale = 1.0F - sizeReduction;
        int count = 0;
        for (int layer = 0; layer < recipe.height(); layer++) {
            for (int depth = recipe.depth() - 1; depth >= 0; depth--) {
                for (int width = recipe.width() - 1; width >= 0; width--) {
                    int itemX = (int) (
                            x
                                    + xOffset * (1.0F + sizeReduction)
                                    + width * 16 * scale
                                    + depth * 16 * scale
                    );
                    int itemY = (int) (
                            y
                                    + COMPOUND_STRUCTURE_BASE_Y
                                    + yOffset * scale
                                    - width * 8 * scale
                                    + depth * 8 * scale
                                    + layer * COMPOUND_LAYER_Y * scale
                    );
                    ItemStack stack = displayStacks.get(count++);
                    if (!stack.isEmpty()
                            && contains(
                                    itemX,
                                    itemY,
                                    16 * scale,
                                    16 * scale,
                                    mouseX,
                                    mouseY
                            )) {
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private void registerCompoundItemLinks(
            CompoundRecipeView recipe,
            List<ItemStack> displayStacks,
            int x,
            int y,
            int xOffset,
            int yOffset,
            float sizeReduction
    ) {
        float scale = 1.0F - sizeReduction;
        int count = 0;
        for (int layer = 0; layer < recipe.height(); layer++) {
            for (int depth = recipe.depth() - 1; depth >= 0; depth--) {
                for (int width = recipe.width() - 1; width >= 0; width--) {
                    int itemX = (int) (x
                            + xOffset * (1.0F + sizeReduction)
                            + width * 16 * scale
                            + depth * 16 * scale);
                    int itemY = (int) (y
                            + COMPOUND_STRUCTURE_BASE_Y
                            + yOffset * scale
                            - width * 8 * scale
                            + depth * 8 * scale
                            + layer * COMPOUND_LAYER_Y * scale);
                    ItemStack stack = displayStacks.get(count++);
                    registerItemLink(stack, itemX, itemY,
                            Math.max(1, Math.round(16 * scale)),
                            Math.max(1, Math.round(16 * scale)));
                }
            }
        }
    }

    private void renderLinkedItem(GuiGraphics graphics, ItemStack stack,
            int x, int y) {
        renderLinkedItemExact(graphics, durabilityPreview(stack), x, y);
    }

    private void renderLinkedItemExact(GuiGraphics graphics, ItemStack displayed,
            int x, int y) {
        graphics.renderItem(displayed, x, y);
        graphics.renderItemDecorations(font, displayed, x, y);
        registerItemLink(displayed, x, y, 16, 16);
    }

    private static boolean isDamageableTransformation(
            ItemStack central,
            ItemStack output
    ) {
        return central.isDamageableItem() && output.isDamageableItem();
    }

    private static ItemStack durabilityPreview(ItemStack stack) {
        return ThaumonomiconDurabilityPreview.atTime(stack, Util.getMillis());
    }

    private void registerItemLink(ItemStack stack, int x, int y,
            int width, int height) {
        if (!stack.isEmpty()) {
            itemLinkRegions.add(new ThaumonomiconItemLinkRegion(stack.copy(), x, y, width, height));
        }
    }

    private record CompoundRecipeView(
            int width,
            int height,
            int depth,
            List<AspectCost> costs,
            List<ItemStack> cells
    ) {
        private CompoundRecipeView {
            costs = List.copyOf(costs);
            cells = cells.stream().map(ItemStack::copy).toList();
        }
    }

}
