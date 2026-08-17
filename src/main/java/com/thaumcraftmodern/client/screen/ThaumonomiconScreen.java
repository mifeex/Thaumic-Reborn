package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.config.ThaumcraftModernClientConfig;
import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import com.thaumcraftmodern.crucible.CrucibleRecipeRegistry;
import com.thaumcraftmodern.item.ResearchNotesItem;
import com.thaumcraftmodern.item.ScribingToolsItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.RequestResearchNotesPacket;
import com.thaumcraftmodern.network.packet.PurchaseResearchPacket;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.crafting.Recipe;

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
    private static String lastSelectedCategoryId = "";

    private final ThaumonomiconNavigationController navigation =
            new ThaumonomiconNavigationController();
    private final List<ThaumonomiconItemLinkRegion> itemLinkRegions =
            new ArrayList<>();
    private final ThaumonomiconOpenBookRenderer openBookRenderer =
            new ThaumonomiconOpenBookRenderer();
    private ThaumonomiconResearchTooltipRenderer researchTooltipRenderer;
    private String selectedCategoryId;
    private String previousCategoryId = "";
    private long categorySwitchStartedAt;
    private int left;
    private int top;
    private float treeScale = 1.0F;
    private float treePanX;
    private float treePanY;
    private ThaumonomiconBrowserModel browserModel =
            ThaumonomiconBrowserModel.create();
    private List<ResearchCategoryDefinition> visibleCategorySnapshot =
            List.of();
    private ThaumonomiconBrowserModel.CategoryView categoryView =
            browserModel.categoryView("", research -> false);

    public ThaumonomiconScreen() {
        super(Component.translatable("screen.thaumic_reborn.thaumonomicon.title"));
    }

    @Override
    protected void init() {
        researchTooltipRenderer = new ThaumonomiconResearchTooltipRenderer(
                font,
                this::canAffordResearch,
                this::hasResearchNotes,
                this::hasScribingMaterials,
                aspectId -> currentKnowledge()
                        .map(knowledge -> knowledge.knowsAspect(aspectId))
                        .orElse(false),
                aspectId -> currentKnowledge()
                        .map(knowledge -> knowledge.aspectAmount(aspectId))
                        .orElse(0)
        );
        rebuildBrowserModel();
        ensureSelectedCategory();
        refreshSelectedCategoryView();
        centerSelectedCategory();
        updateOrigin();
        ResearchDiagnostics.log(
                "CLIENT_THAUMONOMICON_INIT",
                "selectedCategory={} visibleCategories={} allCategories={} researchStates={}",
                selectedCategoryId,
                visibleCategories().stream()
                        .map(ResearchCategoryDefinition::id)
                        .toList(),
                browserModel.categories().stream()
                        .map(ResearchCategoryDefinition::id)
                        .toList(),
                browserModel.research().stream()
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
                navigation.research() == null
                        ? "<tree>"
                        : navigation.research().id(),
                navigation.pagePair()
        );
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!browserModel.isCurrent()) {
            refreshResearchData();
        }
        renderBackground(graphics);
        if (navigation.research() == null) {
            renderResearchTree(graphics, mouseX, mouseY, partialTick);
        } else {
            openBookRenderer.render(
                    graphics,
                    minecraft,
                    font,
                    width,
                    height,
                    left,
                    top,
                    navigation.research(),
                    navigation.pagePair(),
                    itemLinkRegions,
                    stack -> getTooltipFromItem(minecraft, stack),
                    stack -> researchForItem(stack).isPresent(),
                    mouseX,
                    mouseY
            );
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void updateOrigin() {
        if (navigation.research() == null) {
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

        List<ResearchDefinition> visible = categoryView.research();
        graphics.enableScissor(
                left + Math.round(TREE_INNER_X * treeScale),
                top + Math.round(TREE_INNER_Y * treeScale),
                left + Math.round((TREE_INNER_X + TREE_INNER_WIDTH) * treeScale),
                top + Math.round((TREE_INNER_Y + TREE_INNER_HEIGHT) * treeScale)
        );
        drawResearchConnections(graphics, categoryView, partialTick);
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
            ThaumonomiconBrowserModel.CategoryView view,
            float partialTick
    ) {
        for (ThaumonomiconBrowserModel.Connection connection
                : view.connections()) {
            drawResearchConnection(
                    graphics,
                    connection.first(),
                    connection.second(),
                    partialTick,
                    connection.sibling()
            );
        }
        /* Keep the complete link pass behind the node frames and icons. */
        graphics.flush();
    }

    private void drawResearchConnection(
            GuiGraphics graphics,
            ResearchDefinition first,
            ResearchDefinition second,
            float partialTick,
            boolean sibling
    ) {
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

        if (!ThaumonomiconResearchInteraction.isWithinViewport(
                localMouseX,
                localMouseY
        )) {
            return;
        }
        ResearchDefinition research = ThaumonomiconResearchInteraction
                .researchAt(
                        visibleResearch(),
                        this::researchX,
                        this::researchY,
                        RESEARCH_NODE_SIZE,
                        localMouseX,
                        localMouseY
                )
                .orElse(null);
        if (research == null) {
            return;
        }
        boolean completed = isCompleted(research.id());
        boolean unlocked = isUnlocked(research);
        researchTooltipRenderer.render(
                graphics,
                research,
                completed,
                unlocked,
                mouseX,
                mouseY
        );
    }

    static boolean isWithinResearchViewport(double x, double y) {
        return ThaumonomiconResearchInteraction.isWithinViewport(x, y);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (navigation.research() == null) {
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
                    refreshSelectedCategoryView();
                    centerSelectedCategory();
                    playCategorySound();
                    return true;
                }
            }
            ResearchDefinition research = ThaumonomiconResearchInteraction
                    .researchAt(
                            visibleResearch(),
                            this::researchX,
                            this::researchY,
                            RESEARCH_NODE_SIZE,
                            localMouseX,
                            localMouseY
                    )
                    .orElse(null);
            if (research != null) {
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
                        navigation.openRoot(research);
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
                navigation.openRoot(research);
                updateOrigin();
                playPageSound();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 0) {
            Optional<ResearchDefinition> linked = itemLinkRegions.stream()
                    .filter(region -> region.contains(mouseX, mouseY))
                    .map(ThaumonomiconItemLinkRegion::stack)
                    .map(this::researchForItem)
                    .flatMap(Optional::stream)
                    .findFirst();
            if (linked.isPresent()) {
                openLinkedResearch(linked.get());
                return true;
            }
        }

        if (ThaumonomiconBookLayout.BACK.contains(
                left, top, mouseX, mouseY
        )) {
            ResearchDiagnostics.log(
                    "CLIENT_THAUMONOMICON_BACK",
                    "research={} pagePair={}",
                    navigation.research().id(),
                    navigation.pagePair()
            );
            leaveResearchLevel("button");
            return true;
        }
        if (ThaumonomiconBookLayout.PREVIOUS.contains(
                left, top, mouseX, mouseY
        )) {
            Optional<ThaumonomiconNavigationController.PageChange> change =
                    navigation.previousPage();
            if (change.isPresent()) {
                ResearchDiagnostics.log(
                        "CLIENT_THAUMONOMICON_PAGE",
                        "research={} direction=previous from={} to={}",
                        navigation.research().id(),
                        change.get().previousPagePair(),
                        change.get().pagePair()
                );
                playPageSound();
            }
            return true;
        }
        if (ThaumonomiconBookLayout.NEXT.contains(
                left, top, mouseX, mouseY
        )) {
            Optional<ThaumonomiconNavigationController.PageChange> change =
                    navigation.nextPage();
            if (change.isPresent()) {
                ResearchDiagnostics.log(
                        "CLIENT_THAUMONOMICON_PAGE",
                        "research={} direction=next from={} to={}",
                        navigation.research().id(),
                        change.get().previousPagePair(),
                        change.get().pagePair()
                );
                playPageSound();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE
                && navigation.research() != null) {
            ResearchDiagnostics.log(
                    "CLIENT_THAUMONOMICON_BACK",
                    "source=escape research={} pagePair={}",
                    navigation.research().id(),
                    navigation.pagePair()
            );
            leaveResearchLevel("escape");
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private Optional<ResearchDefinition> researchForItem(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return browserModel.research().stream()
                .filter(research -> navigation.research() == null
                        || !research.id().equals(navigation.research().id()))
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
        ResearchDefinition source = navigation.research();
        if (source == null) return;
        int sourcePagePair = navigation.pagePair();
        navigation.openLinked(target, selectedCategoryId);
        ResearchDiagnostics.log(
                "CLIENT_THAUMONOMICON_ITEM_LINK",
                "from={} pagePair={} to={} category={} depth={}",
                source.id(), sourcePagePair, target.id(), target.categoryId(),
                navigation.depth()
        );
        selectedCategoryId = target.categoryId();
        lastSelectedCategoryId = selectedCategoryId;
        refreshSelectedCategoryView();
        updateOrigin();
        playPageSound();
    }

    private void leaveResearchLevel(String source) {
        ThaumonomiconNavigationController.BackResult result =
                navigation.back(ResearchRegistry::find);
        if (result.research() != null && result.usedHistory()) {
            ResearchDiagnostics.log(
                    "CLIENT_THAUMONOMICON_ITEM_LINK_BACK",
                    "source={} from={} to={} pagePair={} depth={}",
                    source,
                    result.fromResearchId(),
                    result.research().id(), result.pagePair(),
                    result.remainingDepth()
            );
            selectedCategoryId = result.categoryId();
            lastSelectedCategoryId = selectedCategoryId;
            refreshSelectedCategoryView();
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
        if (navigation.research() == null && button == 0) {
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
        if (categoryView.research().isEmpty()) {
            treePanX = 0.0F;
            treePanY = 0.0F;
            return;
        }
        ThaumonomiconBrowserModel.Bounds bounds = categoryView.bounds();
        int minX = bounds.minX();
        int maxX = bounds.maxX();
        int minY = bounds.minY();
        int maxY = bounds.maxY();
        treePanX = -(minX + maxX) / 2.0F;
        treePanY = -(minY + maxY) / 2.0F;
        clampTreePan();
    }

    private void clampTreePan() {
        if (categoryView.research().isEmpty()) {
            treePanX = 0.0F;
            treePanY = 0.0F;
            return;
        }
        ThaumonomiconBrowserModel.Bounds bounds = categoryView.bounds();
        int minX = bounds.minX();
        int maxX = bounds.maxX();
        int minY = bounds.minY();
        int maxY = bounds.maxY();
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
        return categoryView.research();
    }

    /** Rebuilds registry-derived browser data after sync/reload. */
    public void refreshResearchData() {
        rebuildBrowserModel();
        ensureSelectedCategory();
        refreshSelectedCategoryView();
        navigation.refresh(ResearchRegistry::find);
        if (navigation.research() == null) {
            /*
             * Knowledge syncs arrive while this screen is open after actions
             * such as purchasing secondary research. Keep the player's
             * current viewport instead of snapping the refreshed tree back to
             * its center; only constrain it if the visible bounds changed.
             */
            clampTreePan();
        }
        updateOrigin();
    }

    private void rebuildBrowserModel() {
        if (!browserModel.isCurrent()) {
            browserModel = ThaumonomiconBrowserModel.create();
        }
        visibleCategorySnapshot = browserModel.visibleCategories(
                this::isVisible
        );
    }

    private void refreshSelectedCategoryView() {
        categoryView = browserModel.categoryView(
                selectedCategoryId == null ? "" : selectedCategoryId,
                this::isVisible
        );
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
        return visibleCategorySnapshot;
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

}
