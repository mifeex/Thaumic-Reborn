package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.aspect.AspectCostProvider;
import com.thaumcraftmodern.arcane.ArcaneRecipe;
import com.thaumcraftmodern.arcane.ArcaneShapedRecipe;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.config.ThaumcraftModernClientConfig;
import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import com.thaumcraftmodern.crucible.CrucibleRecipeRegistry;
import com.thaumcraftmodern.construction.InfusionAltarResearchRecipe;
import com.thaumcraftmodern.construction.InfernalFurnaceResearchRecipe;
import com.thaumcraftmodern.construction.ThaumatoriumResearchRecipe;
import com.thaumcraftmodern.item.ResearchNotesItem;
import com.thaumcraftmodern.item.RunicShieldService;
import com.thaumcraftmodern.item.ScribingToolsItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.RequestResearchNotesPacket;
import com.thaumcraftmodern.network.packet.PurchaseResearchPacket;
import com.thaumcraftmodern.nodejar.NodeJarResearchRecipe;
import com.thaumcraftmodern.nodejar.NodeJarStructure;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.research.ResearchCategoryDefinition;
import com.thaumcraftmodern.research.ResearchCategoryRegistry;
import com.thaumcraftmodern.research.ResearchDefinition;
import com.thaumcraftmodern.research.ResearchDiagnostics;
import com.thaumcraftmodern.research.ResearchPageDefinition;
import com.thaumcraftmodern.research.InfusionDisplayDefinition;
import com.thaumcraftmodern.research.ResearchProgressService;
import com.thaumcraftmodern.research.ResearchRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.Util;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class ThaumonomiconScreen extends Screen {
    private static final ResourceLocation RESEARCH_BACKGROUND =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/gui_researchback.png"
            );
    private static final ResourceLocation RESEARCH_FRAME =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/gui_research.png"
            );
    private static final ResourceLocation RESEARCH_WARP_AURA =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/misc/nodes.png"
            );
    private static final ResourceLocation BOOK =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/gui_researchbook.png"
            );
    private static final ResourceLocation BOOK_OVERLAY =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/gui_researchbook_overlay.png"
            );
    private static final ResourceLocation CLASSIC_BOOK_FONT =
            new ResourceLocation("minecraft", "uniform");

    private static final int TREE_WIDTH = 256;
    private static final int TREE_HEIGHT = 230;
    private static final int TREE_INNER_X = 16;
    private static final int TREE_INNER_Y = 16;
    private static final int TREE_INNER_WIDTH = 224;
    private static final int TREE_INNER_HEIGHT = 197;
    private static final int TREE_SCREEN_PADDING = 4;
    private static final int BACKGROUND_SOURCE_X = 144;
    private static final int BACKGROUND_SOURCE_Y = 158;
    private static final int BOOK_PANE_WIDTH = 256;
    private static final int BOOK_PANE_HEIGHT = 181;
    private static final int BOOK_RENDER_WIDTH = 333;
    private static final int BOOK_RENDER_HEIGHT = 235;
    private static final int PAGE_WIDTH = 139;
    private static final int PAGE_CONTENT_BOTTOM = 174;
    private static final int RESEARCH_NODE_SIZE = 26;
    private static final int RESEARCH_NODE_SOURCE_X = 54;
    private static final int RESEARCH_NODE_SOURCE_Y = 230;
    private static final int RESEARCH_NODE_SOURCE_SIZE = 26;
    private static final int RESEARCH_NODE_PRIMARY_SOURCE_X = 0;
    private static final int RESEARCH_NODE_SPECIAL_SOURCE_X = 26;
    private static final int RESEARCH_NODE_ROUND_SOURCE_X = 54;
    private static final int RESEARCH_NODE_HIDDEN_SOURCE_X = 86;
    private static final int RESEARCH_NODE_SECONDARY_SOURCE_X = 110;
    private static final int RESEARCH_ICON_SIZE = 16;
    private static final int RESEARCH_ICON_OFFSET = 5;
    /** Exact TC4 GuiResearchBrowser incomplete-node brightness values. */
    private static final long RESEARCH_AVAILABLE_PULSE_MILLIS = 800L;
    private static final float RESEARCH_AVAILABLE_PULSE_BASE = 0.75F;
    private static final float RESEARCH_AVAILABLE_PULSE_AMPLITUDE = 0.25F;
    private static final int RESEARCH_LOCKED_FRAME_TINT = 0xFF4D4D4D;
    private static final int RESEARCH_LOCKED_ITEM_TINT = 0xFF1A1A1A;
    private static final int RESEARCH_LOCKED_RESOURCE_TINT = 0xFF333333;
    /** Exact TC4 GuiResearchBrowser forbidden-knowledge aura. */
    private static final int RESEARCH_WARP_AURA_SIZE = 80;
    private static final int RESEARCH_WARP_AURA_FRAMES = 32;
    private static final int RESEARCH_WARP_AURA_STRIP = 5;
    private static final int RESEARCH_WARP_AURA_FRAME_SIZE = 64;
    private static final int RESEARCH_WARP_AURA_TEXTURE_SIZE = 2048;
    private static final int RESEARCH_WARP_AURA_TINT = 0xA8440055;
    /** Exact TC4 GuiResearchBrowser research-tooltip presentation. */
    private static final float RESEARCH_TOOLTIP_SMALL_SCALE = 0.5F;
    private static final float RESEARCH_TOOLTIP_WIDTH_DIVISOR = 1.9F;
    private static final int RESEARCH_TOOLTIP_X_OFFSET = 6;
    private static final int RESEARCH_TOOLTIP_Y_OFFSET = -4;
    private static final int RESEARCH_TOOLTIP_PADDING = 3;
    private static final int RESEARCH_TOOLTIP_BACKGROUND = 0xC0000000;
    private static final int RESEARCH_TOOLTIP_SUBTITLE = 0xFF9090FF;
    private static final int RESEARCH_TOOLTIP_WARP = 0xFFAA55FF;
    private static final int RESEARCH_TOOLTIP_MISSING = 0xFF705050;
    private static final int RESEARCH_TOOLTIP_READY = 0xFF87D1AB;
    private static final int RESEARCH_TOOLTIP_HAS_NOTES = 0xFFFFAA00;
    private static final int RESEARCH_TOOLTIP_BLOCKED = 0xFFDC141C;
    private static final int RESEARCH_TOOLTIP_TITLE = 0xFFFFFFFF;
    private static final int RESEARCH_TOOLTIP_SPECIAL_TITLE = 0xFFFFFF80;
    private static final int RESEARCH_TOOLTIP_LOCKED_TITLE = 0xFF808040;
    private static final int RESEARCH_TOOLTIP_LOCKED_SPECIAL_TITLE = 0xFF808080;
    /** Above GuiGraphics item rendering, which occupies the lower GUI layers. */
    private static final float RESEARCH_TOOLTIP_Z = 400.0F;
    /** Exact TC4 {@code GuiResearchBrowser.drawLine} tuning. */
    public static final float RESEARCH_CONNECTION_WIDTH = 3.0F;
    public static final float RESEARCH_CONNECTION_ALPHA = 0.6F;
    public static final float RESEARCH_CONNECTION_POINT_SPACING = 2.0F;
    public static final float RESEARCH_CONNECTION_MAJOR_STEP_SCALE = 2.0F;
    public static final float RESEARCH_CONNECTION_STEP_DECAY_SCALE = 1.5F;
    public static final float RESEARCH_CONNECTION_WIGGLE_AMPLITUDE = 5.0F;
    public static final float RESEARCH_CONNECTION_WIGGLE_X_PERIOD = 7.0F;
    public static final float RESEARCH_CONNECTION_WIGGLE_Y_PERIOD = 5.0F;
    public static final float RESEARCH_CONNECTION_STATIC_RED = 0.1F;
    public static final float RESEARCH_CONNECTION_STATIC_GREEN = 0.1F;
    public static final float RESEARCH_CONNECTION_STATIC_BLUE = 0.1F;
    public static final float RESEARCH_CONNECTION_SIBLING_BLUE = 0.2F;
    /*
     * Category-tab tuning controls. These deliberately remain public and
     * grouped so the complete switch presentation can be calibrated without
     * hunting through render, hover, and click code.
     */
    public static final int CATEGORY_TAB_SIZE = 24;
    public static final int CATEGORY_ICON_SIZE = 12;
    public static final int CATEGORY_TABS_PER_SIDE = 9;
    public static final int CATEGORY_TAB_LEFT_X = -24;
    public static final int CATEGORY_TAB_RIGHT_X = TREE_WIDTH;
    public static final int CATEGORY_TAB_START_Y = 8;
    public static final int CATEGORY_TAB_Y_STEP = 24;
    public static final int CATEGORY_ICON_X_OFFSET = 15;
    public static final int CATEGORY_ICON_Y_OFFSET = 6;
    /**
     * Additional horizontal offset for the selected category icon. The value
     * is expressed for left-side tabs and mirrored for right-side tabs.
     */
    public static final int ACTIVE_CATEGORY_ICON_X_OFFSET = -8;
    public static final int CATEGORY_ICON_SOURCE_X = 0;
    public static final int CATEGORY_ICON_SOURCE_Y = 0;
    public static final int CATEGORY_ICON_SOURCE_SIZE = 32;
    public static final int CATEGORY_TAB_SELECTED_SOURCE_X = 152;
    public static final int CATEGORY_TAB_INACTIVE_SOURCE_X = 176;
    public static final int CATEGORY_TAB_INACTIVE_OVERLAY_SOURCE_X = 200;
    public static final int CATEGORY_TAB_SOURCE_Y = 232;
    public static final int CATEGORY_TAB_ATLAS_SIZE = 256;
    /**
     * Horizontal resting offset for unselected category tabs. Positive values
     * tuck left-side tabs farther right under the book frame; right-side tabs
     * mirror the value toward the frame.
     */
    public static final int INACTIVE_CATEGORY_TAB_X_OFFSET = 0;
    public static final int ACTIVE_CATEGORY_TAB_X_OFFSET = 2;
    public static final long CATEGORY_TAB_SWITCH_ANIMATION_MS = 160L;
    public static final double CATEGORY_TAB_SWITCH_EASE_POWER = 3.0D;
    public static final float CATEGORY_TAB_SWITCH_SOUND_PITCH = 1.0F;
    public static final float CATEGORY_TAB_SWITCH_SOUND_VOLUME = 0.4F;
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
    /** Five GUI units are ten physical pixels at the tested GUI scale. */
    private static final int INFUSION_SECTION_GAP = 5;
    private static final int INFUSION_INSTABILITY_Y = 193;
    private static String lastSelectedCategoryId = "";

    private static final ButtonRegion BACK_BUTTON = new ButtonRegion(118, 189, 20, 12);
    private static final ButtonRegion PREVIOUS_BUTTON = new ButtonRegion(-16, 190, 12, 8);
    private static final ButtonRegion NEXT_BUTTON = new ButtonRegion(262, 190, 12, 8);

    private ResearchDefinition openResearch;
    private final ThaumonomiconNavigationHistory researchHistory =
            new ThaumonomiconNavigationHistory();
    private final List<ItemLinkRegion> itemLinkRegions = new ArrayList<>();
    private String selectedCategoryId;
    private String previousCategoryId = "";
    private long categorySwitchStartedAt;
    private int pagePair;
    private int left;
    private int top;
    private float treeScale = 1.0F;
    private float treePanX;
    private float treePanY;

    public ThaumonomiconScreen() {
        super(Component.translatable("screen.thaumcraftmodern.thaumonomicon.title"));
    }

    @Override
    protected void init() {
        ensureSelectedCategory();
        centerSelectedCategory();
        updateOrigin();
        ResearchDiagnostics.log(
                "CLIENT_THAUMONOMICON_INIT",
                "selectedCategory={} visibleCategories={} allCategories={} researchStates={}",
                selectedCategoryId,
                visibleCategories().stream()
                        .map(ResearchCategoryDefinition::id)
                        .toList(),
                ResearchCategoryRegistry.all().stream()
                        .map(ResearchCategoryDefinition::id)
                        .toList(),
                ResearchRegistry.all().stream()
                        .map(research -> research.id()
                                + ":category=" + research.categoryId()
                                + ":inactive=" + research.inactive()
                                + ":visible=" + isVisible(research)
                                + ":unlocked=" + isUnlocked(research)
                                + ":completed=" + isCompleted(research.id()))
                        .toList()
        );
    }

    @Override
    public void onClose() {
        ResearchDiagnostics.log(
                "CLIENT_THAUMONOMICON_CLOSE",
                "selectedCategory={} openResearch={} pagePair={}",
                selectedCategoryId,
                openResearch == null ? "<tree>" : openResearch.id(),
                pagePair
        );
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        updateOrigin();
        if (openResearch == null) {
            renderResearchTree(graphics, mouseX, mouseY, partialTick);
        } else {
            renderBook(graphics, mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void updateOrigin() {
        if (openResearch == null) {
            int categoryCount = visibleCategories().size();
            int leftTabsWidth = categoryCount > 0 ? CATEGORY_TAB_SIZE : 0;
            int rightTabsWidth = categoryCount > CATEGORY_TABS_PER_SIDE
                    ? CATEGORY_TAB_SIZE
                    : 0;
            int fullWidth = TREE_WIDTH + leftTabsWidth + rightTabsWidth;
            treeScale = Math.min(
                    1.0F,
                    Math.min(
                            Math.max(
                                    0.1F,
                                    (width - TREE_SCREEN_PADDING * 2.0F) / fullWidth
                            ),
                            Math.max(
                                    0.1F,
                                    (height - TREE_SCREEN_PADDING * 2.0F) / TREE_HEIGHT
                            )
                    )
            );
            left = Math.round(
                    (width - fullWidth * treeScale) / 2.0F
                            + leftTabsWidth * treeScale
            );
            top = Math.round((height - TREE_HEIGHT * treeScale) / 2.0F);
            return;
        }

        treeScale = 1.0F;
        left = (width - BOOK_PANE_WIDTH) / 2;
        top = (height - BOOK_PANE_HEIGHT) / 2;
    }

    private void renderResearchTree(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        double localMouseX = (mouseX - left) / treeScale;
        double localMouseY = (mouseY - top) / treeScale;
        graphics.pose().pushPose();
        graphics.pose().translate(left, top, 0.0F);
        graphics.pose().scale(treeScale, treeScale, 1.0F);

        ResearchCategoryDefinition category = selectedCategory().orElse(null);
        ResourceLocation background = category == null
                ? RESEARCH_BACKGROUND
                : new ResourceLocation(category.backgroundTexture());

        /*
         * TC4 tabs live behind the Thaumonomicon frame. Drawing the complete
         * tab pass first lets the wooden atlas mask every tucked-under part of
         * both the tab and its icon.
         */
        renderCategoryTabs(graphics);
        graphics.blit(
                RESEARCH_FRAME,
                0,
                0,
                0,
                0,
                TREE_WIDTH,
                TREE_HEIGHT,
                256,
                256
        );
        graphics.blit(
                background,
                TREE_INNER_X,
                TREE_INNER_Y,
                BACKGROUND_SOURCE_X,
                BACKGROUND_SOURCE_Y,
                TREE_INNER_WIDTH,
                TREE_INNER_HEIGHT,
                512,
                512
        );
        Component categoryTitle = category == null
                ? title
                : Component.translatable(category.titleKey());
        graphics.drawCenteredString(
                font,
                categoryTitle,
                TREE_WIDTH / 2,
                7,
                0xE7D4B2
        );

        List<ResearchDefinition> visible = visibleResearch();
        graphics.enableScissor(
                left + Math.round(TREE_INNER_X * treeScale),
                top + Math.round(TREE_INNER_Y * treeScale),
                left + Math.round((TREE_INNER_X + TREE_INNER_WIDTH) * treeScale),
                top + Math.round((TREE_INNER_Y + TREE_INNER_HEIGHT) * treeScale)
        );
        drawResearchConnections(graphics, visible, partialTick);
        for (ResearchDefinition research : visible) {
            int x = researchX(research);
            int y = researchY(research);
            boolean unlocked = isUnlocked(research);
            boolean completed = isCompleted(research.id());
            int frameTint = completed
                    ? 0xFFFFFFFF
                    : unlocked
                    ? availableResearchTint(Util.getMillis())
                    : RESEARCH_LOCKED_FRAME_TINT;
            int iconTint = completed || unlocked
                    ? frameTint
                    : research.iconResource().isBlank()
                    ? RESEARCH_LOCKED_ITEM_TINT
                    : RESEARCH_LOCKED_RESOURCE_TINT;
            if (research.completionWarp() > 0) {
                renderResearchWarpAura(graphics, x, y);
            }
            int frameSourceX = researchFrameSourceX(research);
            ClassicUiRender.drawTintedScaledTexture(
                    graphics,
                    RESEARCH_FRAME,
                    x,
                    y,
                    RESEARCH_NODE_SIZE,
                    RESEARCH_NODE_SIZE,
                    frameSourceX,
                    RESEARCH_NODE_SOURCE_Y,
                    RESEARCH_NODE_SOURCE_SIZE,
                    RESEARCH_NODE_SOURCE_SIZE,
                    256,
                    256,
                    frameTint
            );
            if (research.specialFrame()) {
                ClassicUiRender.drawTintedScaledTexture(
                        graphics,
                        RESEARCH_FRAME,
                        x,
                        y,
                        RESEARCH_NODE_SIZE,
                        RESEARCH_NODE_SIZE,
                        RESEARCH_NODE_SPECIAL_SOURCE_X,
                        RESEARCH_NODE_SOURCE_Y,
                        RESEARCH_NODE_SOURCE_SIZE,
                        RESEARCH_NODE_SOURCE_SIZE,
                        256,
                        256,
                        frameTint
                );
            }
            /*
             * TC4 never replaced a visible research icon with a question
             * mark. Unknown hidden/lost/concealed entries were omitted from
             * the tree; a visible but unavailable entry kept its real icon
             * and was rendered with the same dark tint as its frame.
             */
            renderResearchIcon(graphics, research, x, y, iconTint);
        }
        /*
         * Item-backed research icons are submitted to GuiGraphics' shared
         * buffer and may otherwise be drawn only after the scissor is gone.
         * Flush while the tree viewport is still active so dragged nodes can
         * never leak through the wooden frame into the world behind the book.
         */
        graphics.flush();
        graphics.disableScissor();
        graphics.pose().popPose();
        renderTreeTooltip(
                graphics,
                localMouseX,
                localMouseY,
                mouseX,
                mouseY
        );
    }

    private void renderResearchWarpAura(
            GuiGraphics graphics,
            int nodeX,
            int nodeY
    ) {
        int tick = minecraft == null || minecraft.player == null
                ? 0
                : minecraft.player.tickCount;
        int animationFrame = Math.floorMod(tick, RESEARCH_WARP_AURA_FRAMES);
        int sourceFrame = RESEARCH_WARP_AURA_FRAMES - 1 - animationFrame;
        int auraOffset = (RESEARCH_WARP_AURA_SIZE - RESEARCH_NODE_SIZE) / 2;
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                RESEARCH_WARP_AURA,
                nodeX - auraOffset,
                nodeY - auraOffset,
                RESEARCH_WARP_AURA_SIZE,
                RESEARCH_WARP_AURA_SIZE,
                sourceFrame * RESEARCH_WARP_AURA_FRAME_SIZE,
                RESEARCH_WARP_AURA_STRIP * RESEARCH_WARP_AURA_FRAME_SIZE,
                RESEARCH_WARP_AURA_FRAME_SIZE,
                RESEARCH_WARP_AURA_FRAME_SIZE,
                RESEARCH_WARP_AURA_TEXTURE_SIZE,
                RESEARCH_WARP_AURA_TEXTURE_SIZE,
                RESEARCH_WARP_AURA_TINT
        );
    }

    private static int researchFrameSourceX(ResearchDefinition research) {
        return switch (research.nodeFrame()) {
            case PRIMARY -> RESEARCH_NODE_PRIMARY_SOURCE_X;
            case ROUND -> RESEARCH_NODE_ROUND_SOURCE_X;
            case SECONDARY -> RESEARCH_NODE_SECONDARY_SOURCE_X;
            case HIDDEN -> RESEARCH_NODE_HIDDEN_SOURCE_X;
        };
    }

    private void drawResearchConnections(
            GuiGraphics graphics,
            List<ResearchDefinition> visible,
            float partialTick
    ) {
        java.util.Map<String, ResearchDefinition> byId = visible.stream()
                .collect(java.util.stream.Collectors.toMap(
                        ResearchDefinition::id,
                        research -> research
                ));
        java.util.Set<String> drawn = new java.util.HashSet<>();
        for (ResearchDefinition research : visible) {
            for (String parentId : research.parents()) {
                ResearchDefinition parent = byId.get(parentId);
                if (parent == null) {
                    continue;
                }
                drawResearchConnection(
                        graphics, research, parent, drawn, partialTick, false
                );
            }
            for (String siblingId : research.siblings()) {
                ResearchDefinition sibling = byId.get(siblingId);
                if (sibling == null || sibling.parents().contains(research.id())) {
                    continue;
                }
                drawResearchConnection(
                        graphics, research, sibling, drawn, partialTick, true
                );
            }
        }
        /* Keep the complete link pass behind the node frames and icons. */
        graphics.flush();
    }

    private void drawResearchConnection(
            GuiGraphics graphics,
            ResearchDefinition first,
            ResearchDefinition second,
            java.util.Set<String> drawn,
            float partialTick,
            boolean sibling
    ) {
        String edge = first.id().compareTo(second.id()) < 0
                ? first.id() + "\u0000" + second.id()
                : second.id() + "\u0000" + first.id();
        if (!drawn.add(edge)) {
            return;
        }
        boolean complete = isCompleted(first.id());
        boolean targetComplete = isCompleted(second.id());
        float red = complete ? RESEARCH_CONNECTION_STATIC_RED : 0.0F;
        float green = complete
                ? RESEARCH_CONNECTION_STATIC_GREEN
                : targetComplete ? 1.0F : 0.0F;
        float blue = complete
                ? sibling
                        ? RESEARCH_CONNECTION_SIBLING_BLUE
                        : RESEARCH_CONNECTION_STATIC_BLUE
                : targetComplete ? 0.0F : 1.0F;
        drawConnection(
                graphics,
                researchX(first) + RESEARCH_NODE_SIZE / 2,
                researchY(first) + RESEARCH_NODE_SIZE / 2,
                researchX(second) + RESEARCH_NODE_SIZE / 2,
                researchY(second) + RESEARCH_NODE_SIZE / 2,
                red,
                green,
                blue,
                partialTick,
                !complete
        );
    }

    private void drawConnection(
            GuiGraphics graphics,
            int x1,
            int y1,
            int x2,
            int y2,
            float red,
            float green,
            float blue,
            float partialTick,
            boolean wiggle
    ) {
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        int increments = Math.max(
                1,
                (int) (distance / RESEARCH_CONNECTION_POINT_SPACING)
        );
        float stepX = (float) (deltaX / increments);
        float stepY = (float) (deltaY / increments);
        boolean horizontal = Math.abs(deltaX) > Math.abs(deltaY);
        if (horizontal) {
            stepX *= RESEARCH_CONNECTION_MAJOR_STEP_SCALE;
        } else {
            stepY *= RESEARCH_CONNECTION_MAJOR_STEP_SCALE;
        }
        float tick = (minecraft.player == null
                ? 0.0F
                : minecraft.player.tickCount) + partialTick;
        VertexConsumer buffer = graphics.bufferSource().getBuffer(
                RenderType.guiOverlay()
        );
        float renderedConnectionWidth = (float) (
                RESEARCH_CONNECTION_WIDTH
                        / Math.max(1.0D, minecraft.getWindow().getGuiScale())
                        / Math.max(0.0001F, treeScale)
        );
        float previousX = 0.0F;
        float previousY = 0.0F;
        float previousRed = 0.0F;
        float previousGreen = 0.0F;
        float previousBlue = 0.0F;
        float previousAlpha = 0.0F;
        for (int index = 0; index <= increments; index++) {
            float phase = index / (float) increments;
            float currentRed = red;
            float currentGreen = green;
            float currentBlue = blue;
            float alpha = RESEARCH_CONNECTION_ALPHA;
            float offsetX = 0.0F;
            float offsetY = 0.0F;
            if (wiggle) {
                offsetX = (float) Math.sin(
                        (tick + index) / RESEARCH_CONNECTION_WIGGLE_X_PERIOD
                ) * RESEARCH_CONNECTION_WIGGLE_AMPLITUDE * (1.0F - phase);
                offsetY = (float) Math.sin(
                        (tick + index) / RESEARCH_CONNECTION_WIGGLE_Y_PERIOD
                ) * RESEARCH_CONNECTION_WIGGLE_AMPLITUDE * (1.0F - phase);
                currentRed *= 1.0F - phase;
                currentGreen *= 1.0F - phase;
                currentBlue *= 1.0F - phase;
                alpha *= phase;
            }
            float currentX = x1 - stepX * index + offsetX;
            float currentY = y1 - stepY * index + offsetY;
            if (index > 0) {
                drawConnectionSegment(
                        graphics,
                        buffer,
                        previousX,
                        previousY,
                        currentX,
                        currentY,
                        previousRed,
                        previousGreen,
                        previousBlue,
                        previousAlpha,
                        currentRed,
                        currentGreen,
                        currentBlue,
                        alpha,
                        renderedConnectionWidth
                );
            }
            previousX = currentX;
            previousY = currentY;
            previousRed = currentRed;
            previousGreen = currentGreen;
            previousBlue = currentBlue;
            previousAlpha = alpha;
            float decay = 1.0F - 1.0F
                    / (increments * RESEARCH_CONNECTION_STEP_DECAY_SCALE);
            if (horizontal) {
                stepX *= decay;
            } else {
                stepY *= decay;
            }
        }
    }

    /**
     * Modern OpenGL implementations are allowed to clamp or discard wide
     * line primitives.  A GUI quad ribbon preserves TC4's three-pixel line
     * strip while staying in Minecraft's ordered, blended GUI buffer.  The
     * overlay type is intentional: the research background already occupies
     * the regular GUI depth plane, so links must not be depth-rejected there.
     */
    private static void drawConnectionSegment(
            GuiGraphics graphics,
            VertexConsumer buffer,
            float x1,
            float y1,
            float x2,
            float y2,
            float red1,
            float green1,
            float blue1,
            float alpha1,
            float red2,
            float green2,
            float blue2,
            float alpha2,
            float renderedWidth
    ) {
        float segmentX = x2 - x1;
        float segmentY = y2 - y1;
        float length = (float) Math.sqrt(
                segmentX * segmentX + segmentY * segmentY
        );
        if (length <= 0.0001F) {
            return;
        }
        float halfWidth = renderedWidth / 2.0F;
        float normalX = -segmentY / length * halfWidth;
        float normalY = segmentX / length * halfWidth;
        var pose = graphics.pose().last().pose();
        buffer.vertex(pose, x1 - normalX, y1 - normalY, 0.0F)
                .color(red1, green1, blue1, alpha1).endVertex();
        buffer.vertex(pose, x1 + normalX, y1 + normalY, 0.0F)
                .color(red1, green1, blue1, alpha1).endVertex();
        buffer.vertex(pose, x2 + normalX, y2 + normalY, 0.0F)
                .color(red2, green2, blue2, alpha2).endVertex();
        buffer.vertex(pose, x2 - normalX, y2 - normalY, 0.0F)
                .color(red2, green2, blue2, alpha2).endVertex();
    }

    private void renderCategoryTabs(GuiGraphics graphics) {
        List<ResearchCategoryDefinition> categories = visibleCategories();
        for (int index = 0; index < categories.size(); index++) {
            ResearchCategoryDefinition category = categories.get(index);
            ButtonRegion tab = categoryTab(index, category);
            boolean selected = category.id().equals(selectedCategoryId);
            boolean rightSide = index >= CATEGORY_TABS_PER_SIDE;
            int iconX = tab.x() + CATEGORY_ICON_X_OFFSET;
            if (selected) {
                iconX += rightSide
                        ? -ACTIVE_CATEGORY_ICON_X_OFFSET
                        : ACTIVE_CATEGORY_ICON_X_OFFSET;
            }
            drawCategoryTabLayer(
                    graphics,
                    tab,
                    rightSide,
                    selected
                            ? CATEGORY_TAB_SELECTED_SOURCE_X
                            : CATEGORY_TAB_INACTIVE_SOURCE_X
            );

            if (!category.iconResource().isBlank()) {
                ClassicUiRender.drawTintedScaledTexture(
                        graphics,
                        new ResourceLocation(category.iconResource()),
                        iconX,
                        tab.y() + CATEGORY_ICON_Y_OFFSET,
                        CATEGORY_ICON_SIZE,
                        CATEGORY_ICON_SIZE,
                        CATEGORY_ICON_SOURCE_X,
                        CATEGORY_ICON_SOURCE_Y,
                        CATEGORY_ICON_SOURCE_SIZE,
                        CATEGORY_ICON_SOURCE_SIZE,
                        CATEGORY_ICON_SOURCE_SIZE,
                        CATEGORY_ICON_SOURCE_SIZE,
                        0xFFFFFFFF
                );
            } else {
                ClassicUiRender.drawItemCentered(
                        graphics,
                        itemFromId(
                                category.iconItem(),
                                ModItems.THAUMONOMICON.get().getDefaultInstance()
                        ),
                        iconX + CATEGORY_ICON_SIZE / 2,
                        tab.y() + CATEGORY_ICON_Y_OFFSET
                                + CATEGORY_ICON_SIZE / 2,
                        CATEGORY_ICON_SIZE
                );
            }
            if (!selected) {
                drawCategoryTabLayer(
                        graphics,
                        tab,
                        rightSide,
                        CATEGORY_TAB_INACTIVE_OVERLAY_SOURCE_X
                );
            }
        }
    }

    private static void drawCategoryTabLayer(
            GuiGraphics graphics,
            ButtonRegion tab,
            boolean rightSide,
            int sourceX
    ) {
        if (rightSide) {
            ClassicUiRender.drawHorizontallyFlippedScaledTexture(
                    graphics,
                    RESEARCH_FRAME,
                    tab.x(),
                    tab.y(),
                    CATEGORY_TAB_SIZE,
                    CATEGORY_TAB_SIZE,
                    sourceX,
                    CATEGORY_TAB_SOURCE_Y,
                    CATEGORY_TAB_SIZE,
                    CATEGORY_TAB_SIZE,
                    CATEGORY_TAB_ATLAS_SIZE,
                    CATEGORY_TAB_ATLAS_SIZE
            );
            return;
        }
        ClassicUiRender.drawScaledTexture(
                graphics,
                RESEARCH_FRAME,
                tab.x(),
                tab.y(),
                CATEGORY_TAB_SIZE,
                CATEGORY_TAB_SIZE,
                sourceX,
                CATEGORY_TAB_SOURCE_Y,
                CATEGORY_TAB_SIZE,
                CATEGORY_TAB_SIZE,
                CATEGORY_TAB_ATLAS_SIZE,
                CATEGORY_TAB_ATLAS_SIZE
        );
    }

    private void renderTreeTooltip(
            GuiGraphics graphics,
            double localMouseX,
            double localMouseY,
            int mouseX,
            int mouseY
    ) {
        List<ResearchCategoryDefinition> categories = visibleCategories();
        for (int index = 0; index < categories.size(); index++) {
            if (categoryTab(index, categories.get(index))
                    .contains(0, 0, localMouseX, localMouseY)) {
                graphics.renderTooltip(
                        font,
                        Component.translatable(categories.get(index).titleKey()),
                        mouseX,
                        mouseY
                );
                return;
            }
        }

        if (!isWithinResearchViewport(localMouseX, localMouseY)) {
            return;
        }

        for (ResearchDefinition research : visibleResearch()) {
            int x = researchX(research);
            int y = researchY(research);
            if (!contains(
                    x,
                    y,
                    RESEARCH_NODE_SIZE,
                    RESEARCH_NODE_SIZE,
                    localMouseX,
                    localMouseY
            )) {
                continue;
            }
            boolean completed = isCompleted(research.id());
            boolean unlocked = isUnlocked(research);
            renderResearchTooltip(
                    graphics,
                    research,
                    completed,
                    unlocked,
                    mouseX,
                    mouseY
            );
            return;
        }
    }

    static boolean isWithinResearchViewport(double x, double y) {
        return x >= TREE_INNER_X
                && x < TREE_INNER_X + TREE_INNER_WIDTH
                && y >= TREE_INNER_Y
                && y < TREE_INNER_Y + TREE_INNER_HEIGHT;
    }

    private void renderResearchTooltip(
            GuiGraphics graphics,
            ResearchDefinition research,
            boolean completed,
            boolean unlocked,
            int mouseX,
            int mouseY
    ) {
        /*
         * ItemRenderer can defer node icons until the shared buffer is
         * flushed. Submit those icons first, then render the complete custom
         * tooltip on the same high layer vanilla tooltips use. This keeps the
         * background, text, and embedded aspect costs above every tree icon.
         */
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, RESEARCH_TOOLTIP_Z);
        try {
        String title = Component.translatable(research.titleKey()).getString();
        String subtitle = Component.translatable(
                research.subtitleKey()
        ).getString();
        int tooltipX = mouseX + RESEARCH_TOOLTIP_X_OFFSET;
        int tooltipY = mouseY + RESEARCH_TOOLTIP_Y_OFFSET;

        if (!unlocked) {
            renderLockedResearchTooltip(
                    graphics,
                    research,
                    title,
                    tooltipX,
                    tooltipY
            );
            return;
        }

        boolean secondary = !completed && research.purchasable();
        boolean primary = !secondary && !completed;
        ResearchTooltipLine state = primary
                ? primaryResearchTooltipLine(research)
                : secondary
                ? secondaryResearchTooltipLine(research)
                : null;
        String warp = research.completionWarp() > 0
                ? Component.translatable(
                        "tooltip.thaumcraftmodern.research_completion_warp",
                        research.completionWarp()
                ).getString()
                : "";

        int tooltipWidth = Math.max(
                font.width(title),
                halfScaleWidth(subtitle)
        );
        if (state != null) {
            tooltipWidth = Math.max(
                    tooltipWidth,
                    halfScaleWidth(state.text())
            );
        }
        if (!warp.isEmpty()) {
            tooltipWidth = Math.max(tooltipWidth, halfScaleWidth(warp));
        }
        int contentHeight = font.wordWrapHeight(title, tooltipWidth) + 5;
        int extraHeight = secondary ? 29 : state == null ? 0 : 9;
        if (!warp.isEmpty()) {
            extraHeight += 9;
        }

        graphics.fill(
                tooltipX - RESEARCH_TOOLTIP_PADDING,
                tooltipY - RESEARCH_TOOLTIP_PADDING,
                tooltipX + tooltipWidth + RESEARCH_TOOLTIP_PADDING,
                tooltipY + contentHeight + 6 + extraHeight,
                RESEARCH_TOOLTIP_BACKGROUND
        );
        drawHalfScaleResearchText(
                graphics,
                subtitle,
                tooltipX,
                tooltipY + contentHeight - 1,
                RESEARCH_TOOLTIP_SUBTITLE
        );
        if (!warp.isEmpty()) {
            drawHalfScaleResearchText(
                    graphics,
                    warp,
                    tooltipX,
                    tooltipY + contentHeight + 8,
                    RESEARCH_TOOLTIP_WARP
            );
            contentHeight += 9;
        }
        if (!secondary && state != null) {
            drawHalfScaleResearchText(
                    graphics,
                    state.text(),
                    tooltipX,
                    tooltipY + contentHeight + 8,
                    state.color()
            );
        } else if (secondary && state != null) {
            ThaumonomiconAspectCostRenderer.renderMaskedRow(
                    graphics,
                    font,
                    research.purchaseCost(),
                    aspectId -> currentKnowledge()
                            .map(knowledge -> knowledge.knowsAspect(aspectId))
                            .orElse(false),
                    aspectId -> currentKnowledge()
                            .map(knowledge -> knowledge.aspectAmount(aspectId))
                            .orElse(0),
                    tooltipX,
                    tooltipY + contentHeight + 8,
                    Util.getMillis()
            );
            drawHalfScaleResearchText(
                    graphics,
                    state.text(),
                    tooltipX,
                    tooltipY + contentHeight + 27,
                    state.color()
            );
        }
        graphics.drawString(
                font,
                title,
                tooltipX,
                tooltipY,
                researchTitleColor(research, unlocked),
                false
        );
        } finally {
            graphics.flush();
            graphics.pose().popPose();
        }
    }

    private void renderLockedResearchTooltip(
            GuiGraphics graphics,
            ResearchDefinition research,
            String title,
            int tooltipX,
            int tooltipY
    ) {
        String missing = Component.translatable("tc.researchmissing")
                .getString();
        int tooltipWidth = Math.max(
                font.width(title),
                (int) (font.width(missing) / 1.5F)
        );
        int missingHeight = font.wordWrapHeight(missing, tooltipWidth * 2);
        graphics.fill(
                tooltipX - RESEARCH_TOOLTIP_PADDING,
                tooltipY - RESEARCH_TOOLTIP_PADDING,
                tooltipX + tooltipWidth + RESEARCH_TOOLTIP_PADDING,
                tooltipY + missingHeight + 10,
                RESEARCH_TOOLTIP_BACKGROUND
        );
        drawHalfScaleResearchTextWrapped(
                graphics,
                missing,
                tooltipX,
                tooltipY + 12,
                tooltipWidth * 2,
                RESEARCH_TOOLTIP_MISSING
        );
        graphics.drawString(
                font,
                title,
                tooltipX,
                tooltipY,
                researchTitleColor(research, false),
                false
        );
    }

    private ResearchTooltipLine primaryResearchTooltipLine(
            ResearchDefinition research
    ) {
        if (research.inactive()) {
            return new ResearchTooltipLine(
                    Component.translatable(
                            "screen.thaumcraftmodern.thaumonomicon.content_inactive"
                    ).getString(),
                    RESEARCH_TOOLTIP_BLOCKED
            );
        }
        if (research.purchasable()) {
            return secondaryResearchTooltipLine(research);
        }
        if (hasResearchNotes(research.id())) {
            return new ResearchTooltipLine(
                    Component.translatable("tc.research.hasnote").getString(),
                    RESEARCH_TOOLTIP_HAS_NOTES
            );
        }
        if (hasScribingMaterials()) {
            return new ResearchTooltipLine(
                    Component.translatable("tc.research.getprim").getString(),
                    RESEARCH_TOOLTIP_READY
            );
        }
        return new ResearchTooltipLine(
                Component.translatable("tc.research.shortprim").getString(),
                RESEARCH_TOOLTIP_BLOCKED
        );
    }

    private ResearchTooltipLine secondaryResearchTooltipLine(
            ResearchDefinition research
    ) {
        boolean affordable = canAffordResearch(research);
        return new ResearchTooltipLine(
                Component.translatable(
                        affordable ? "tc.research.purchase" : "tc.research.short"
                ).getString(),
                affordable
                        ? RESEARCH_TOOLTIP_READY
                        : RESEARCH_TOOLTIP_BLOCKED
        );
    }

    private static int researchTitleColor(
            ResearchDefinition research,
            boolean unlocked
    ) {
        if (unlocked) {
            return research.specialFrame()
                    ? RESEARCH_TOOLTIP_SPECIAL_TITLE
                    : RESEARCH_TOOLTIP_TITLE;
        }
        return research.specialFrame()
                ? RESEARCH_TOOLTIP_LOCKED_SPECIAL_TITLE
                : RESEARCH_TOOLTIP_LOCKED_TITLE;
    }

    private int halfScaleWidth(String text) {
        return (int) Math.ceil(
                font.width(text) / RESEARCH_TOOLTIP_WIDTH_DIVISOR
        );
    }

    private void drawHalfScaleResearchText(
            GuiGraphics graphics,
            String text,
            int x,
            int y,
            int color
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 1.0F);
        graphics.pose().scale(
                RESEARCH_TOOLTIP_SMALL_SCALE,
                RESEARCH_TOOLTIP_SMALL_SCALE,
                1.0F
        );
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void drawHalfScaleResearchTextWrapped(
            GuiGraphics graphics,
            String text,
            int x,
            int y,
            int width,
            int color
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 1.0F);
        graphics.pose().scale(
                RESEARCH_TOOLTIP_SMALL_SCALE,
                RESEARCH_TOOLTIP_SMALL_SCALE,
                1.0F
        );
        graphics.drawWordWrap(font, Component.literal(text), 0, 0, width, color);
        graphics.pose().popPose();
    }

    private void renderBook(GuiGraphics graphics, int mouseX, int mouseY) {
        itemLinkRegions.clear();
        // gui_researchbook.png is a 512px atlas. Only its first 362px form the
        // open book; the rest contains navigation sprites and must be cropped.
        int bookLeft = (width - BOOK_RENDER_WIDTH) / 2;
        int bookTop = (height - BOOK_RENDER_HEIGHT) / 2;
        ClassicUiRender.drawScaledTexture(
                graphics,
                BOOK,
                bookLeft,
                bookTop,
                BOOK_RENDER_WIDTH,
                BOOK_RENDER_HEIGHT,
                0,
                0,
                512,
                362,
                512,
                512
        );

        renderPage(graphics, pagePair, left - 15, top - 6, PAGE_WIDTH, mouseX, mouseY);
        renderPage(graphics, pagePair + 1, left + 137, top - 6, PAGE_WIDTH, mouseX, mouseY);
        renderBookControls(graphics, mouseX, mouseY);
        renderItemLinkTooltip(graphics, mouseX, mouseY);
    }

    private void renderItemLinkTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        ItemLinkRegion hovered = itemLinkRegions.stream()
                .filter(region -> region.contains(mouseX, mouseY))
                .findFirst()
                .orElse(null);
        if (hovered == null || minecraft == null) {
            return;
        }
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(hovered.stack().getHoverName());
        if (RunicShieldService.finalCharge(hovered.stack()) > 0) {
            tooltip.add(RunicShieldService.chargeTooltip(hovered.stack()));
        }
        if (researchForItem(hovered.stack()).isPresent()) {
            tooltip.add(Component.translatable(
                    "screen.thaumcraftmodern.thaumonomicon.open_item_page"
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        graphics.renderTooltip(
                font,
                tooltip,
                Optional.empty(),
                mouseX,
                mouseY
        );
    }

    private void renderBookControls(GuiGraphics graphics, int mouseX, int mouseY) {
        // These are the original TC4 arrow/back sprites stored below the book.
        ClassicUiRender.drawScaledTexture(
                graphics,
                BOOK,
                left + BACK_BUTTON.x(),
                top + BACK_BUTTON.y(),
                BACK_BUTTON.width(),
                BACK_BUTTON.height(),
                76,
                404,
                40,
                24,
                512,
                512
        );
        if (pagePair > 0) {
            ClassicUiRender.drawScaledTexture(
                    graphics,
                    BOOK,
                    left + PREVIOUS_BUTTON.x(),
                    top + PREVIOUS_BUTTON.y(),
                    PREVIOUS_BUTTON.width(),
                    PREVIOUS_BUTTON.height(),
                    0,
                    368,
                    24,
                    16,
                    512,
                    512
            );
        }
        if (openResearch != null && pagePair + 2 < openResearch.pages().size()) {
            ClassicUiRender.drawScaledTexture(
                    graphics,
                    BOOK,
                    left + NEXT_BUTTON.x(),
                    top + NEXT_BUTTON.y(),
                    NEXT_BUTTON.width(),
                    NEXT_BUTTON.height(),
                    24,
                    368,
                    24,
                    16,
                    512,
                    512
            );
        }

        if (BACK_BUTTON.contains(left, top, mouseX, mouseY)) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("screen.thaumcraftmodern.thaumonomicon.back"),
                    mouseX,
                    mouseY
            );
        } else if (pagePair > 0 && PREVIOUS_BUTTON.contains(left, top, mouseX, mouseY)) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("screen.thaumcraftmodern.thaumonomicon.previous_page"),
                    mouseX,
                    mouseY
            );
        } else if (openResearch != null
                && pagePair + 2 < openResearch.pages().size()
                && NEXT_BUTTON.contains(left, top, mouseX, mouseY)) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("screen.thaumcraftmodern.thaumonomicon.next_page"),
                    mouseX,
                    mouseY
            );
        }
    }

    private void renderPage(
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
                            "screen.thaumcraftmodern.thaumonomicon.content_inactive"
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

        ItemStack central = infusionStack(display.centralItem(), 1);
        ItemStack output = infusionStack(display.outputItem(), 1);
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
        renderLinkedItem(graphics, central, centralX, centralY);

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
                "screen.thaumcraftmodern.thaumonomicon.instability",
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
                    Component.translatable("screen.thaumcraftmodern.thaumonomicon.recipe_missing"),
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
                            "screen.thaumcraftmodern.thaumonomicon.recipe_missing"
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
                                            "aspect.thaumcraftmodern."
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
        ItemStack displayed = durabilityPreview(stack);
        graphics.renderItem(displayed, x, y);
        graphics.renderItemDecorations(font, displayed, x, y);
        registerItemLink(displayed, x, y, 16, 16);
    }

    private static ItemStack durabilityPreview(ItemStack stack) {
        return ThaumonomiconDurabilityPreview.atTime(stack, Util.getMillis());
    }

    private void registerItemLink(ItemStack stack, int x, int y,
            int width, int height) {
        if (!stack.isEmpty()) {
            itemLinkRegions.add(new ItemLinkRegion(stack.copy(), x, y, width, height));
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (openResearch == null) {
            double localMouseX = (mouseX - left) / treeScale;
            double localMouseY = (mouseY - top) / treeScale;
            List<ResearchCategoryDefinition> categories = visibleCategories();
            for (int index = 0; index < categories.size(); index++) {
                ResearchCategoryDefinition category = categories.get(index);
                ButtonRegion tab = categoryTab(index, category);
                if (tab.contains(0, 0, localMouseX, localMouseY)) {
                    String nextCategoryId = category.id();
                    ResearchDiagnostics.log(
                            "CLIENT_THAUMONOMICON_CATEGORY",
                            "button={} from={} to={} index={}",
                            button,
                            selectedCategoryId,
                            nextCategoryId,
                            index
                    );
                    if (!nextCategoryId.equals(selectedCategoryId)) {
                        previousCategoryId = selectedCategoryId;
                        categorySwitchStartedAt = Util.getMillis();
                    }
                    selectedCategoryId = nextCategoryId;
                    lastSelectedCategoryId = selectedCategoryId;
                    centerSelectedCategory();
                    playCategorySound();
                    return true;
                }
            }
            for (ResearchDefinition research : visibleResearch()) {
                int x = researchX(research);
                int y = researchY(research);
                if (contains(
                        x,
                        y,
                        RESEARCH_NODE_SIZE,
                        RESEARCH_NODE_SIZE,
                        localMouseX,
                        localMouseY
                )) {
                    boolean completed = isCompleted(research.id());
                    boolean unlocked = isUnlocked(research);
                    if (research.inactive()) {
                        ResearchDiagnostics.log(
                                "CLIENT_THAUMONOMICON_RESEARCH_OPEN",
                                "button={} research={} category={} pages={} "
                                        + "inactive=true read_only=true",
                                button,
                                research.id(),
                                research.categoryId(),
                                research.pages().size()
                        );
                        openResearch = research;
                        researchHistory.clear();
                        pagePair = 0;
                        updateOrigin();
                        playPageSound();
                        return true;
                    }
                    if (!completed) {
                        if (!unlocked) {
                            ResearchDiagnostics.log(
                                    "CLIENT_THAUMONOMICON_RESEARCH_REJECTED",
                                    "button={} research={} reason=locked inactive={} unlocked={} completed={}",
                                    button,
                                    research.id(),
                                    research.inactive(),
                                    false,
                                    false
                            );
                            return true;
                        }
                        if (research.purchasable()) {
                            boolean affordable = canAffordResearch(research);
                            ResearchDiagnostics.log(
                                    "CLIENT_RESEARCH_PURCHASE_REQUEST",
                                    "button={} research={} cost={} affordable={}",
                                    button,
                                    research.id(),
                                    research.purchaseCost(),
                                    affordable
                            );
                            if (affordable) {
                                ModNetwork.sendToServer(
                                        new PurchaseResearchPacket(research.id())
                                );
                            }
                        } else {
                            ResearchDiagnostics.log(
                                    "CLIENT_RESEARCH_NOTES_REQUEST",
                                    "button={} research={} inactive={} unlocked={} completed={} materials={}",
                                    button,
                                    research.id(),
                                    research.inactive(),
                                    true,
                                    false,
                                    hasScribingMaterials()
                            );
                            ModNetwork.sendToServer(
                                    new RequestResearchNotesPacket(research.id())
                            );
                        }
                        return true;
                    }
                    ResearchDiagnostics.log(
                            "CLIENT_THAUMONOMICON_RESEARCH_OPEN",
                            "button={} research={} category={} pages={} inactive={} unlocked={} completed={}",
                            button,
                            research.id(),
                            research.categoryId(),
                            research.pages().size(),
                            research.inactive(),
                            unlocked,
                            completed
                    );
                    openResearch = research;
                    researchHistory.clear();
                    pagePair = 0;
                    updateOrigin();
                    playPageSound();
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 0) {
            Optional<ResearchDefinition> linked = itemLinkRegions.stream()
                    .filter(region -> region.contains(mouseX, mouseY))
                    .map(ItemLinkRegion::stack)
                    .map(this::researchForItem)
                    .flatMap(Optional::stream)
                    .findFirst();
            if (linked.isPresent()) {
                openLinkedResearch(linked.get());
                return true;
            }
        }

        if (BACK_BUTTON.contains(left, top, mouseX, mouseY)) {
            ResearchDiagnostics.log(
                    "CLIENT_THAUMONOMICON_BACK",
                    "research={} pagePair={}",
                    openResearch.id(),
                    pagePair
            );
            leaveResearchLevel("button");
            return true;
        }
        if (PREVIOUS_BUTTON.contains(left, top, mouseX, mouseY)) {
            if (pagePair > 0) {
                int previous = pagePair;
                pagePair = Math.max(0, pagePair - 2);
                ResearchDiagnostics.log(
                        "CLIENT_THAUMONOMICON_PAGE",
                        "research={} direction=previous from={} to={}",
                        openResearch.id(),
                        previous,
                        pagePair
                );
                playPageSound();
            }
            return true;
        }
        if (NEXT_BUTTON.contains(left, top, mouseX, mouseY)) {
            if (pagePair + 2 < openResearch.pages().size()) {
                int previous = pagePair;
                pagePair += 2;
                ResearchDiagnostics.log(
                        "CLIENT_THAUMONOMICON_PAGE",
                        "research={} direction=next from={} to={}",
                        openResearch.id(),
                        previous,
                        pagePair
                );
                playPageSound();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE && openResearch != null) {
            ResearchDiagnostics.log(
                    "CLIENT_THAUMONOMICON_BACK",
                    "source=escape research={} pagePair={}",
                    openResearch.id(),
                    pagePair
            );
            leaveResearchLevel("escape");
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private Optional<ResearchDefinition> researchForItem(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ResearchRegistry.all().stream()
                .filter(research -> openResearch == null
                        || !research.id().equals(openResearch.id()))
                .filter(research -> isCompleted(research.id()))
                .sorted(Comparator
                        .comparing((ResearchDefinition research) ->
                                !research.iconItem().equals(itemId.toString()))
                        .thenComparing(ResearchDefinition::id))
                .filter(research -> research.iconItem().equals(itemId.toString())
                        || researchProducesItem(research, itemId))
                .findFirst();
    }

    private boolean researchProducesItem(ResearchDefinition research,
            ResourceLocation itemId) {
        for (ResearchPageDefinition page : research.pages()) {
            InfusionDisplayDefinition infusion = page.infusionDisplay();
            if (infusion != null && itemId.toString().equals(infusion.outputItem())) {
                return true;
            }
            if (page.type() != ResearchPageDefinition.Type.RECIPE) continue;
            for (String rawRecipeId : page.recipeIds()) {
                ResourceLocation recipeId = ResourceLocation.tryParse(rawRecipeId);
                if (recipeId == null) continue;
                CrucibleRecipeDefinition crucible = CrucibleRecipeRegistry.all().stream()
                        .filter(candidate -> candidate.id().equals(recipeId))
                        .findFirst().orElse(null);
                if (crucible != null
                        && BuiltInRegistries.ITEM.getKey(crucible.output().getItem())
                                .equals(itemId)) {
                    return true;
                }
                if (minecraft != null && minecraft.level != null) {
                    Recipe<?> recipe = minecraft.level.getRecipeManager()
                            .byKey(recipeId).orElse(null);
                    if (recipe != null && BuiltInRegistries.ITEM.getKey(
                            recipe.getResultItem(minecraft.level.registryAccess()).getItem())
                            .equals(itemId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void openLinkedResearch(ResearchDefinition target) {
        if (openResearch == null) return;
        researchHistory.push(
                openResearch.id(),
                pagePair,
                selectedCategoryId
        );
        ResearchDiagnostics.log(
                "CLIENT_THAUMONOMICON_ITEM_LINK",
                "from={} pagePair={} to={} category={} depth={}",
                openResearch.id(), pagePair, target.id(), target.categoryId(),
                researchHistory.depth()
        );
        openResearch = target;
        selectedCategoryId = target.categoryId();
        lastSelectedCategoryId = selectedCategoryId;
        pagePair = 0;
        updateOrigin();
        playPageSound();
    }

    private void leaveResearchLevel(String source) {
        if (!researchHistory.isEmpty()) {
            ThaumonomiconNavigationHistory.Location previous =
                    researchHistory.pop().orElseThrow();
            ResearchDefinition previousResearch = ResearchRegistry
                    .find(previous.researchId()).orElse(null);
            if (previousResearch == null) {
                researchHistory.clear();
                openResearch = null;
                pagePair = 0;
                updateOrigin();
                playPageSound();
                return;
            }
            ResearchDiagnostics.log(
                    "CLIENT_THAUMONOMICON_ITEM_LINK_BACK",
                    "source={} from={} to={} pagePair={} depth={}",
                    source,
                    openResearch == null ? "<tree>" : openResearch.id(),
                    previousResearch.id(), previous.pagePair(),
                    researchHistory.depth()
            );
            openResearch = previousResearch;
            pagePair = previous.pagePair();
            selectedCategoryId = previous.categoryId();
            lastSelectedCategoryId = selectedCategoryId;
        } else {
            openResearch = null;
            pagePair = 0;
        }
        updateOrigin();
        playPageSound();
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (openResearch == null && button == 0) {
            treePanX += (float) (dragX / treeScale);
            treePanY += (float) (dragY / treeScale);
            clampTreePan();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private int researchX(ResearchDefinition research) {
        return TREE_WIDTH / 2
                + research.x()
                + Math.round(treePanX)
                - RESEARCH_NODE_SIZE / 2;
    }

    private int researchY(ResearchDefinition research) {
        return TREE_HEIGHT / 2
                + research.y()
                + Math.round(treePanY)
                - RESEARCH_NODE_SIZE / 2;
    }

    private void centerSelectedCategory() {
        List<ResearchDefinition> categoryResearch = visibleResearch();
        if (categoryResearch.isEmpty()) {
            treePanX = 0.0F;
            treePanY = 0.0F;
            return;
        }
        int minX = categoryResearch.stream().mapToInt(ResearchDefinition::x).min().orElse(0);
        int maxX = categoryResearch.stream().mapToInt(ResearchDefinition::x).max().orElse(0);
        int minY = categoryResearch.stream().mapToInt(ResearchDefinition::y).min().orElse(0);
        int maxY = categoryResearch.stream().mapToInt(ResearchDefinition::y).max().orElse(0);
        treePanX = -(minX + maxX) / 2.0F;
        treePanY = -(minY + maxY) / 2.0F;
        clampTreePan();
    }

    private void clampTreePan() {
        List<ResearchDefinition> categoryResearch = visibleResearch();
        if (categoryResearch.isEmpty()) {
            treePanX = 0.0F;
            treePanY = 0.0F;
            return;
        }
        int minX = categoryResearch.stream().mapToInt(ResearchDefinition::x).min().orElse(0);
        int maxX = categoryResearch.stream().mapToInt(ResearchDefinition::x).max().orElse(0);
        int minY = categoryResearch.stream().mapToInt(ResearchDefinition::y).min().orElse(0);
        int maxY = categoryResearch.stream().mapToInt(ResearchDefinition::y).max().orElse(0);
        treePanX = clampPan(
                treePanX,
                minX,
                maxX,
                TREE_INNER_X,
                TREE_INNER_X + TREE_INNER_WIDTH,
                TREE_WIDTH / 2
        );
        treePanY = clampPan(
                treePanY,
                minY,
                maxY,
                TREE_INNER_Y,
                TREE_INNER_Y + TREE_INNER_HEIGHT,
                TREE_HEIGHT / 2
        );
    }

    private static float clampPan(
            float value,
            int minimumContent,
            int maximumContent,
            int minimumViewport,
            int maximumViewport,
            int viewportCenter
    ) {
        float minimum = minimumViewport + RESEARCH_NODE_SIZE / 2.0F
                - viewportCenter
                - maximumContent;
        float maximum = maximumViewport - RESEARCH_NODE_SIZE / 2.0F
                - viewportCenter
                - minimumContent;
        if (minimum > maximum) {
            return (minimum + maximum) / 2.0F;
        }
        return Math.max(minimum, Math.min(maximum, value));
    }

    private List<ResearchDefinition> visibleResearch() {
        return ResearchRegistry.all().stream()
                .filter(research -> research.categoryId().equals(selectedCategoryId))
                .filter(research -> !research.virtual())
                .filter(this::isVisible)
                .toList();
    }

    private boolean isVisible(ResearchDefinition research) {
        if (ThaumcraftModernClientConfig.debugShowAllResearch()) {
            return true;
        }
        return currentKnowledge()
                .map(knowledge -> ResearchProgressService.isVisible(research, knowledge))
                .orElse(!research.concealed());
    }

    private boolean isUnlocked(ResearchDefinition research) {
        return currentKnowledge()
                .map(knowledge -> researchIsUnlocked(
                        knowledge.hasCompletedResearch(research.id()),
                        ResearchProgressService.isAvailable(research, knowledge)
                ))
                .orElse(false);
    }

    static boolean researchIsUnlocked(boolean completed, boolean available) {
        // A completed hidden research remains fully readable even when its
        // one-time reveal condition is no longer represented as available.
        return completed || available;
    }

    private Optional<ResearchCategoryDefinition> selectedCategory() {
        ensureSelectedCategory();
        return ResearchCategoryRegistry.find(selectedCategoryId);
    }

    private void ensureSelectedCategory() {
        List<ResearchCategoryDefinition> categories = visibleCategories();
        if (categories.isEmpty()) {
            selectedCategoryId = "";
            return;
        }
        if ((selectedCategoryId == null || selectedCategoryId.isBlank())
                && !lastSelectedCategoryId.isBlank()) {
            selectedCategoryId = lastSelectedCategoryId;
        }
        if (selectedCategoryId == null
                || categories.stream()
                .noneMatch(category -> category.id().equals(selectedCategoryId))) {
            selectedCategoryId = categories.get(0).id();
        }
        lastSelectedCategoryId = selectedCategoryId;
    }

    private List<ResearchCategoryDefinition> visibleCategories() {
        return ResearchCategoryRegistry.all().stream()
                .filter(category -> currentKnowledge()
                        .map(knowledge ->
                                ResearchProgressService.hasVisibleResearch(
                                        category.id(),
                                        knowledge
                                )
                        )
                        .orElseGet(() -> ResearchRegistry.all().stream()
                                .anyMatch(research ->
                                        research.categoryId().equals(category.id())
                                                && !research.virtual()
                                                && !research.concealed()
                                )
                        )
                )
                .toList();
    }

    private ButtonRegion categoryTab(
            int index,
            ResearchCategoryDefinition category
    ) {
        boolean rightSide = index >= CATEGORY_TABS_PER_SIDE;
        int row = index % CATEGORY_TABS_PER_SIDE;
        int x = rightSide
                ? CATEGORY_TAB_RIGHT_X
                : CATEGORY_TAB_LEFT_X;
        x += animatedCategoryTabOffset(category.id(), rightSide);
        return new ButtonRegion(
                x,
                CATEGORY_TAB_START_Y + row * CATEGORY_TAB_Y_STEP,
                CATEGORY_TAB_SIZE,
                CATEGORY_TAB_SIZE
        );
    }

    private int animatedCategoryTabOffset(
            String categoryId,
            boolean rightSide
    ) {
        int restingOffset = rightSide
                ? -INACTIVE_CATEGORY_TAB_X_OFFSET
                : INACTIVE_CATEGORY_TAB_X_OFFSET;
        if (categorySwitchStartedAt <= 0L) {
            return categoryId.equals(selectedCategoryId)
                    ? ACTIVE_CATEGORY_TAB_X_OFFSET
                    : restingOffset;
        }
        float progress = Math.min(
                1.0F,
                (Util.getMillis() - categorySwitchStartedAt)
                        / (float) CATEGORY_TAB_SWITCH_ANIMATION_MS
        );
        float eased = 1.0F - (float) Math.pow(
                1.0F - progress,
                CATEGORY_TAB_SWITCH_EASE_POWER
        );
        if (categoryId.equals(selectedCategoryId)) {
            return Math.round(
                    restingOffset
                            + (ACTIVE_CATEGORY_TAB_X_OFFSET - restingOffset)
                            * eased
            );
        }
        if (categoryId.equals(previousCategoryId)) {
            return Math.round(
                    ACTIVE_CATEGORY_TAB_X_OFFSET
                            + (restingOffset - ACTIVE_CATEGORY_TAB_X_OFFSET)
                            * eased
            );
        }
        return restingOffset;
    }

    private ItemStack researchIcon(ResearchDefinition research) {
        return itemFromId(
                research.iconItem(),
                ModItems.THAUMONOMICON.get().getDefaultInstance()
        );
    }

    private void renderResearchIcon(
            GuiGraphics graphics,
            ResearchDefinition research,
            int frameX,
            int frameY,
            int tint
    ) {
        if (!research.iconResource().isBlank()) {
            ClassicUiRender.drawTintedScaledTexture(
                    graphics,
                    new ResourceLocation(research.iconResource()),
                    frameX + RESEARCH_ICON_OFFSET,
                    frameY + RESEARCH_ICON_OFFSET,
                    RESEARCH_ICON_SIZE,
                    RESEARCH_ICON_SIZE,
                    0,
                    0,
                    32,
                    32,
                    32,
                    32,
                    tint
            );
            return;
        }
        ClassicUiRender.drawTintedItemCentered(
                graphics,
                researchIcon(research),
                frameX + RESEARCH_NODE_SIZE / 2,
                frameY + RESEARCH_NODE_SIZE / 2,
                RESEARCH_ICON_SIZE,
                tint
        );
    }

    private static ItemStack itemFromId(String itemId, ItemStack fallback) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return fallback;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item == null || item == Items.AIR
                ? fallback
                : item.getDefaultInstance();
    }

    private boolean isCompleted(String researchId) {
        return currentKnowledge()
                .map(knowledge -> knowledge.hasCompletedResearch(researchId))
                .orElse(false);
    }

    private Optional<PlayerThaumKnowledge> currentKnowledge() {
        if (minecraft == null || minecraft.player == null) {
            return Optional.empty();
        }
        return KnowledgeAccess.get(minecraft.player);
    }

    private boolean canCreateResearchNotes(ResearchDefinition research) {
        return currentKnowledge()
                .map(knowledge ->
                        ResearchProgressService.canCreateNotes(research, knowledge)
                )
                .orElse(false);
    }

    private boolean canAffordResearch(ResearchDefinition research) {
        return currentKnowledge()
                .map(knowledge -> research.purchaseCost().stream()
                        .allMatch(cost ->
                                knowledge.knowsAspect(cost.aspectId())
                                        && knowledge.aspectAmount(cost.aspectId())
                                        >= cost.amount()
                        ))
                .orElse(false);
    }

    private boolean hasResearchNotes(String researchId) {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        for (int slot = 0;
             slot < minecraft.player.getInventory().getContainerSize();
             slot++) {
            if (ResearchNotesItem.matchesResearch(
                    minecraft.player.getInventory().getItem(slot),
                    researchId
            )) {
                return true;
            }
        }
        return false;
    }

    private boolean hasScribingMaterials() {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        boolean hasPaper = false;
        boolean hasInk = false;
        for (int slot = 0;
             slot < minecraft.player.getInventory().getContainerSize();
             slot++) {
            ItemStack stack = minecraft.player.getInventory().getItem(slot);
            hasPaper |= stack.is(Items.PAPER);
            hasInk |= ScribingToolsItem.hasInk(stack);
            if (hasPaper && hasInk) {
                return true;
            }
        }
        return false;
    }

    static int availableResearchTint(long timeMillis) {
        float brightness = (float) Math.sin(
                (timeMillis % RESEARCH_AVAILABLE_PULSE_MILLIS)
                        / (double) RESEARCH_AVAILABLE_PULSE_MILLIS
                        * Math.PI
                        * 2.0D
        ) * RESEARCH_AVAILABLE_PULSE_AMPLITUDE
                + RESEARCH_AVAILABLE_PULSE_BASE;
        int channel = Math.round(brightness * 255.0F);
        return 0xFF000000 | channel << 16 | channel << 8 | channel;
    }

    private void playPageSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.PAGE.get(), 1.0F));
        }
    }

    private void playCategorySound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(
                    SimpleSoundInstance.forUI(
                            ModSounds.CAMERA_CLACK.get(),
                            CATEGORY_TAB_SWITCH_SOUND_PITCH,
                            CATEGORY_TAB_SWITCH_SOUND_VOLUME
                    )
            );
        }
    }

    private static boolean contains(
            int x,
            int y,
            double width,
            double height,
            double mouseX,
            double mouseY
    ) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record ButtonRegion(int x, int y, int width, int height) {
        private boolean contains(int originX, int originY, double mouseX, double mouseY) {
            return ThaumonomiconScreen.contains(
                    originX + x,
                    originY + y,
                    width,
                    height,
                    mouseX,
                    mouseY
            );
        }
    }

    private record ResearchTooltipLine(String text, int color) {
    }

    private record ItemLinkRegion(
            ItemStack stack,
            int x,
            int y,
            int width,
            int height
    ) {
        private boolean contains(double mouseX, double mouseY) {
            return ThaumonomiconScreen.contains(
                    x, y, width, height, mouseX, mouseY);
        }
    }

}
