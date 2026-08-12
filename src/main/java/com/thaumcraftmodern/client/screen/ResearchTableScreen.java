package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.item.ResearchNotesItem;
import com.thaumcraftmodern.item.DiscoveryItem;
import com.thaumcraftmodern.item.ScribingToolsItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.research.HexResearchPuzzle;
import com.thaumcraftmodern.research.ResearchDiagnostics;
import com.thaumcraftmodern.research.ResearchExpertiseService;
import com.thaumcraftmodern.research.ResearchDuplicationService;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.world.menu.ResearchTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public final class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/guiresearchtable2.png"
            );
    private static final ResourceLocation PARCHMENT =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/misc/parchment3.png"
            );
    private static final ResourceLocation HEX =
            new ResourceLocation(ThaumcraftModern.MOD_ID, "textures/gui/hex1.png");
    private static final ResourceLocation HOVERED_FREE_HEX =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/gui/hex_hover_white.png"
            );
    private static final ResourceLocation SCRIPT =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/misc/script.png"
            );
    private static final ResourceLocation ASPECT_RECIPE_BACKGROUND =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/aspects/_back.png"
            );
    private static final ResourceLocation UNKNOWN_ASPECT =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/aspects/_unknown.png"
            );
    private static final int PANEL_HEIGHT = 167;
    private static final int INVENTORY_X = 40;
    private static final int INVENTORY_Y = 167;
    private static final int INVENTORY_WIDTH = 184;
    private static final int INVENTORY_HEIGHT = 88;
    private static final int PUZZLE_CENTER_X = 169;
    private static final int PUZZLE_CENTER_Y = 83;
    private static final int PUZZLE_HEX_SIZE = 16;
    private static final int PALETTE_X = 10;
    private static final int PALETTE_Y = 40;
    private static final int PALETTE_ROWS = ResearchTablePaletteLayout.ROWS;
    private static final int PALETTE_SIZE = 80;
    private static final int PREVIOUS_PAGE_X = 27;
    private static final int NEXT_PAGE_X = 51;
    private static final int PAGE_ARROW_Y = 121;
    private static final int PAGE_ARROW_WIDTH = 24;
    private static final int PAGE_ARROW_HEIGHT = 8;
    private static final int PREVIOUS_PAGE_SOURCE_X = 184;
    private static final int NEXT_PAGE_SOURCE_X = 208;
    private static final int PAGE_ARROW_SOURCE_Y = 208;
    private static final int FIRST_COMBINE_X = 11;
    private static final int SECOND_COMBINE_X = 71;
    private static final int COMBINE_SLOT_Y = 137;
    private static final int FIRST_COMBINE_ICON_X = 13;
    private static final int SECOND_COMBINE_ICON_X = 71;
    private static final int COMBINE_ICON_Y = 139;
    private static final int COMBINE_BUTTON_X = 35;
    private static final int COMBINE_BUTTON_Y = 139;
    private static final int COMBINE_BUTTON_WIDTH = 32;
    private static final int COMBINE_BUTTON_HEIGHT = 16;
    private static final int COMBINE_ACTIVE_SOURCE_X = 184;
    private static final int COMBINE_ACTIVE_SOURCE_Y = 184;
    private static final int DUPLICATE_X = 37;
    private static final int DUPLICATE_Y = 5;
    private static final int DUPLICATE_SIZE = 24;
    private static final int EXPERTISE_RECIPE_ICON_Y_OFFSET = 16;
    private static final int EXPERTISE_RECIPE_ICON_SIZE = 16;
    private static final int EXPERTISE_RECIPE_BACKGROUND_SIZE = 20;
    private static final int EXPERTISE_RECIPE_COMPONENT_STEP = 18;
    private static final float DRAGGED_ASPECT_Z = 20.0F;
    private static final RuneMark[] RUNES = {
            new RuneMark(110, 23, 1, false),
            new RuneMark(133, 18, 4, true),
            new RuneMark(205, 20, 7, false),
            new RuneMark(226, 34, 12, true),
            new RuneMark(106, 69, 15, false),
            new RuneMark(226, 77, 2, true),
            new RuneMark(108, 121, 10, true),
            new RuneMark(134, 140, 6, false),
            new RuneMark(201, 140, 14, true),
            new RuneMark(225, 119, 3, false)
    };
    private int placementPaletteIndex = -1;
    private int firstCombinationIndex = -1;
    private int secondCombinationIndex = -1;
    private int draggedPaletteIndex = -1;
    private int palettePage;

    public ResearchTableScreen(
            ResearchTableMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = 255;
        imageHeight = 255;
    }

    @Override
    protected void init() {
        super.init();
        ResearchDiagnostics.log(
                "CLIENT_TABLE_OPEN",
                "player={} container={} paletteSize={} amounts={} notes={} tools={}",
                minecraft == null || minecraft.player == null
                        ? "<no-player>"
                        : minecraft.player.getGameProfile().getName(),
                menu.containerId,
                ResearchTableMenu.palette().size(),
                minecraft == null || minecraft.player == null
                        ? "{}"
                        : KnowledgeAccess.get(minecraft.player)
                                .map(knowledge -> knowledge.aspectAmounts().toString())
                                .orElse("<no-knowledge>"),
                menu.notes().getTag(),
                menu.scribingTools()
        );
    }

    @Override
    public void onClose() {
        ResearchDiagnostics.log(
                "CLIENT_TABLE_CLOSE",
                "player={} container={} amounts={} notes={}",
                minecraft == null || minecraft.player == null
                        ? "<no-player>"
                        : minecraft.player.getGameProfile().getName(),
                menu.containerId,
                minecraft == null || minecraft.player == null
                        ? "{}"
                        : KnowledgeAccess.get(minecraft.player)
                                .map(knowledge -> knowledge.aspectAmounts().toString())
                                .orElse("<no-knowledge>"),
                menu.notes().getTag()
        );
        super.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, PANEL_HEIGHT, 256, 256);
        graphics.blit(
                BACKGROUND,
                leftPos + INVENTORY_X,
                topPos + INVENTORY_Y,
                0,
                166,
                INVENTORY_WIDTH,
                INVENTORY_HEIGHT,
                256,
                256
        );
        if (hasResearchWorkspace()) {
            graphics.blit(PARCHMENT, leftPos + 94, topPos + 8, 0, 0, 150, 150, 256, 256);
            renderRunes(graphics);
            renderHexGrid(graphics);
        }
        renderPuzzle(graphics, mouseX, mouseY);
        renderHoveredEmptyCellOutline(graphics, mouseX, mouseY);
        renderPaletteArrows(graphics);
        renderPalette(graphics, mouseX, mouseY);
        renderCombinationControls(graphics, mouseX, mouseY);
        renderDropTargetHighlights(graphics, mouseX, mouseY);
        if (canDuplicate()) {
            graphics.blit(
                    BACKGROUND,
                    leftPos + DUPLICATE_X,
                    topPos + DUPLICATE_Y,
                    232,
                    200,
                    DUPLICATE_SIZE,
                    DUPLICATE_SIZE,
                    256,
                    256
            );
        }
    }

    private void renderHexGrid(GuiGraphics graphics) {
        if (minecraft == null || minecraft.player == null) return;
        for (HexResearchPuzzle.Cell cell : menu.puzzle(minecraft.player).cells()) {
            ClassicUiRender.drawTintedScaledTexture(
                    graphics,
                    HEX,
                    cellX(cell),
                    cellY(cell),
                    PUZZLE_HEX_SIZE,
                    PUZZLE_HEX_SIZE,
                    0,
                    0,
                    32,
                    32,
                    32,
                    32,
                    0x40FFFFFF
            );
        }
    }

    private void renderRunes(GuiGraphics graphics) {
        for (RuneMark rune : RUNES) {
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + rune.x() + 5, topPos + rune.y() + 5, 0.0F);
            if (rune.rotated()) {
                graphics.pose().mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            ClassicUiRender.drawTintedScaledTexture(
                    graphics,
                    SCRIPT,
                    -5,
                    -5,
                    10,
                    10,
                    rune.index() * 16,
                    0,
                    16,
                    16,
                    256,
                    16,
                    0x421C1108
            );
            graphics.pose().popPose();
        }
    }

    private void renderPuzzle(GuiGraphics graphics, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null || !hasResearchWorkspace()) {
            int lineY = topPos + 72;
            for (var line : font.split(
                    Component.translatable(
                            hasResearchNotes()
                                    ? "screen.thaumcraftmodern.research_table.no_ink"
                                    : "screen.thaumcraftmodern.research_table.no_notes"
                    ),
                    126
            )) {
                graphics.drawCenteredString(font, line, leftPos + PUZZLE_CENTER_X, lineY, 0x5A351D);
                lineY += 10;
            }
            return;
        }

        HexResearchPuzzle puzzle = menu.puzzle(minecraft.player);
        HexResearchPuzzle.Cell hovered = hoveredPuzzleCellByCenter(
                puzzle,
                mouseX,
                mouseY
        );
        PlayerThaumKnowledge knowledge = KnowledgeAccess.get(minecraft.player)
                .orElse(null);
        renderConnections(graphics, puzzle, knowledge);
        for (HexResearchPuzzle.Cell cell : puzzle.cells()) {
            int x = cellX(cell);
            int y = cellY(cell);
            String aspectId = puzzle.aspectAt(cell).orElse(null);
            if (aspectId != null) {
                if (knowledge == null || !knowledge.knowsAspect(aspectId)) {
                    int unknownColor = cell.equals(hovered)
                            ? lightenColor(0x000000)
                            : 0x000000;
                    ClassicUiRender.drawAspect(
                            graphics,
                            UNKNOWN_ASPECT,
                            x,
                            y,
                            PUZZLE_HEX_SIZE,
                            unknownColor,
                            0.5F
                    );
                    continue;
                }
                AspectDefinition definition = AspectRegistryRuntime.find(aspectId).orElse(null);
                if (definition != null) {
                    int color = puzzle.isAnchor(cell) || puzzle.hasRelatedNeighbor(cell)
                            ? definition.color()
                            : 0x8A8A8A;
                    if (cell.equals(hovered)) {
                        color = lightenColor(color);
                    }
                    ClassicUiRender.drawAspect(
                            graphics,
                            new ResourceLocation(definition.icon()),
                            x,
                            y,
                            16,
                            color
                    );
                }
            }
        }
    }

    private void renderConnections(
            GuiGraphics graphics,
            HexResearchPuzzle puzzle,
            PlayerThaumKnowledge knowledge
    ) {
        for (HexResearchPuzzle.Cell cell : puzzle.cells()) {
            for (HexResearchPuzzle.Cell neighbor : HexResearchPuzzle.neighbors(cell)) {
                if (cell.compareTo(neighbor) >= 0
                        || puzzle.aspectAt(cell).isEmpty()
                        || puzzle.aspectAt(neighbor).isEmpty()
                        || knowledge == null
                        || !knowledge.knowsAspect(
                                puzzle.aspectAt(cell).orElseThrow()
                        )
                        || !knowledge.knowsAspect(
                                puzzle.aspectAt(neighbor).orElseThrow()
                        )
                        || !AspectRegistryRuntime.catalog().related(
                                puzzle.aspectAt(cell).orElseThrow(),
                                puzzle.aspectAt(neighbor).orElseThrow())) continue;
                int startX = cellX(cell) + PUZZLE_HEX_SIZE / 2;
                int startY = cellY(cell) + PUZZLE_HEX_SIZE / 2;
                int endX = cellX(neighbor) + PUZZLE_HEX_SIZE / 2;
                int endY = cellY(neighbor) + PUZZLE_HEX_SIZE / 2;
                drawClassicConnection(
                        graphics,
                        startX,
                        startY,
                        endX,
                        endY
                );
            }
        }
    }

    /** TC4-style additive GPU connection, slightly widened for readability. */
    private void drawClassicConnection(
            GuiGraphics graphics,
            float startX,
            float startY,
            float endX,
            float endY
    ) {
        int ticks = minecraft == null || minecraft.player == null
                ? 0
                : minecraft.player.tickCount;
        float alpha = 0.6F + (float) Math.sin(ticks + startX) * 0.3F;

        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE
        );
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(4.0F);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(
                VertexFormat.Mode.DEBUG_LINE_STRIP,
                DefaultVertexFormat.POSITION_COLOR
        );
        buffer.vertex(graphics.pose().last().pose(), startX, startY, 0.0F)
                .color(0, 153, 204, Math.round(alpha * 255.0F))
                .endVertex();
        buffer.vertex(graphics.pose().last().pose(), endX, endY, 0.0F)
                .color(0, 153, 204, Math.round(alpha * 255.0F))
                .endVertex();
        Tesselator.getInstance().end();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.defaultBlendFunc();
    }

    private void renderHoveredEmptyCellOutline(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        if (minecraft == null || minecraft.player == null
                || !hasResearchWorkspace()) {
            return;
        }
        HexResearchPuzzle puzzle = menu.puzzle(minecraft.player);
        HexResearchPuzzle.Cell hovered = hoveredPuzzleCellByCenter(
                puzzle,
                mouseX,
                mouseY
        );
        if (hovered != null && puzzle.aspectAt(hovered).isEmpty()) {
            ClassicUiRender.drawTintedScaledTexture(
                    graphics,
                    HOVERED_FREE_HEX,
                    cellX(hovered),
                    cellY(hovered),
                    PUZZLE_HEX_SIZE,
                    PUZZLE_HEX_SIZE,
                    0,
                    0,
                    32,
                    32,
                    32,
                    32,
                    0xA0FFFFFF
            );
        }
    }

    private HexResearchPuzzle.Cell hoveredPuzzleCellByCenter(
            HexResearchPuzzle puzzle,
            double mouseX,
            double mouseY
    ) {
        HexResearchPuzzle.Cell hovered = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (HexResearchPuzzle.Cell cell : puzzle.cells()) {
            double deltaX = mouseX - (cellX(cell) + PUZZLE_HEX_SIZE / 2.0D);
            double deltaY = mouseY - (cellY(cell) + PUZZLE_HEX_SIZE / 2.0D);
            double distanceSquared = deltaX * deltaX + deltaY * deltaY;
            if (distanceSquared <= 64.0D
                    && distanceSquared < nearestDistanceSquared) {
                hovered = cell;
                nearestDistanceSquared = distanceSquared;
            }
        }
        return hovered;
    }

    private static int lightenColor(int rgbColor) {
        int red = (rgbColor >> 16) & 0xFF;
        int green = (rgbColor >> 8) & 0xFF;
        int blue = rgbColor & 0xFF;
        red += Math.round((255 - red) * 0.15F);
        green += Math.round((255 - green) * 0.15F);
        blue += Math.round((255 - blue) * 0.15F);
        return (red << 16) | (green << 8) | blue;
    }

    private void renderPalette(GuiGraphics graphics, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        var knowledge = KnowledgeAccess.get(minecraft.player).orElse(null);
        List<String> palette = ResearchTableMenu.palette();
        List<Integer> visibleIndices = visiblePaletteIndices();
        AspectDefinition hoveredAspect = null;
        graphics.enableScissor(
                leftPos + PALETTE_X,
                topPos + PALETTE_Y,
                leftPos + PALETTE_X + PALETTE_SIZE,
                topPos + PALETTE_Y + PALETTE_SIZE
        );
        for (int visibleIndex = 0; visibleIndex < visibleIndices.size(); visibleIndex++) {
            int paletteIndex = visibleIndices.get(visibleIndex);
            String aspectId = palette.get(paletteIndex);
            AspectDefinition definition = AspectRegistryRuntime.find(aspectId).orElse(null);
            if (definition == null) {
                continue;
            }
            int x = paletteX(visibleIndex);
            int y = paletteY(visibleIndex);
            int amount = knowledge.aspectAmount(aspectId);
            ClassicUiRender.drawAspectTag(
                    graphics,
                    font,
                    new ResourceLocation(definition.icon()),
                    x,
                    y,
                    16,
                    amount > 0 ? definition.color() : 0x353535,
                    amount
            );
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                hoveredAspect = definition;
            }
        }
        graphics.disableScissor();
        if (hoveredAspect != null) {
            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            Component.translatable(
                                            "aspect.thaumcraftmodern."
                                                    + hoveredAspect.id()
                                    )
                                    .withStyle(ChatFormatting.AQUA),
                            Component.translatable(
                                            "tc.aspect."
                                                    + hoveredAspect.id()
                                    )
                                    .withStyle(ChatFormatting.GRAY)
                    ),
                    mouseX,
                    mouseY
            );
            renderExpertiseComponents(
                    graphics,
                    knowledge,
                    hoveredAspect,
                    mouseX,
                    mouseY
            );
        }
    }

    private void renderPaletteArrows(GuiGraphics graphics) {
        int maxPage = paletteMaxPage();
        palettePage = Math.min(palettePage, maxPage);
        if (palettePage > 0) {
            graphics.blit(
                    BACKGROUND,
                    leftPos + PREVIOUS_PAGE_X,
                    topPos + PAGE_ARROW_Y,
                    PREVIOUS_PAGE_SOURCE_X,
                    PAGE_ARROW_SOURCE_Y,
                    PAGE_ARROW_WIDTH,
                    PAGE_ARROW_HEIGHT,
                    256,
                    256
            );
        }
        if (palettePage < maxPage) {
            graphics.blit(
                    BACKGROUND,
                    leftPos + NEXT_PAGE_X,
                    topPos + PAGE_ARROW_Y,
                    NEXT_PAGE_SOURCE_X,
                    PAGE_ARROW_SOURCE_Y,
                    PAGE_ARROW_WIDTH,
                    PAGE_ARROW_HEIGHT,
                    256,
                    256
            );
        }
    }

    private void renderExpertiseComponents(
            GuiGraphics graphics,
            com.thaumcraftmodern.knowledge.PlayerThaumKnowledge knowledge,
            AspectDefinition hovered,
            int mouseX,
            int mouseY
    ) {
        if (!ResearchExpertiseService.canInspectComponents(knowledge)
                || !hovered.isCompound()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        for (int index = 0; index < hovered.components().size(); index++) {
            if (AspectRegistryRuntime.find(
                    hovered.components().get(index)
            ).isEmpty()) {
                continue;
            }
            ClassicUiRender.drawTintedScaledTexture(
                    graphics,
                    ASPECT_RECIPE_BACKGROUND,
                    mouseX + 8
                            - (EXPERTISE_RECIPE_BACKGROUND_SIZE
                            - EXPERTISE_RECIPE_ICON_SIZE) / 2
                            + index * EXPERTISE_RECIPE_COMPONENT_STEP,
                    mouseY + EXPERTISE_RECIPE_ICON_Y_OFFSET
                            - (EXPERTISE_RECIPE_BACKGROUND_SIZE
                            - EXPERTISE_RECIPE_ICON_SIZE) / 2,
                    EXPERTISE_RECIPE_BACKGROUND_SIZE,
                    EXPERTISE_RECIPE_BACKGROUND_SIZE,
                    0,
                    0,
                    64,
                    64,
                    64,
                    64,
                    0xFFFFFFFF
            );
        }
        for (int index = 0; index < hovered.components().size(); index++) {
            AspectDefinition component = AspectRegistryRuntime.find(
                    hovered.components().get(index)
            ).orElse(null);
            if (component == null) {
                continue;
            }
            int x = mouseX + 8
                    + index * EXPERTISE_RECIPE_COMPONENT_STEP;
            int y = mouseY + EXPERTISE_RECIPE_ICON_Y_OFFSET;
            ClassicUiRender.drawAspect(
                    graphics,
                    new ResourceLocation(component.icon()),
                    x,
                    y,
                    EXPERTISE_RECIPE_ICON_SIZE,
                    component.color()
            );
        }
        graphics.pose().popPose();
    }

    private void renderCombinationControls(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean active = firstCombinationIndex >= 0 && secondCombinationIndex >= 0;
        if (active) {
            graphics.blit(
                    BACKGROUND,
                    leftPos + COMBINE_BUTTON_X,
                    topPos + COMBINE_BUTTON_Y,
                    COMBINE_ACTIVE_SOURCE_X,
                    COMBINE_ACTIVE_SOURCE_Y,
                    COMBINE_BUTTON_WIDTH,
                    COMBINE_BUTTON_HEIGHT,
                    256,
                    256
            );
            int dotAlpha = ((System.currentTimeMillis() / 320L) & 1L) == 0L ? 0xFF : 0x66;
            int dotX = leftPos + COMBINE_BUTTON_X + COMBINE_BUTTON_WIDTH / 2 - 1;
            int dotY = topPos + COMBINE_BUTTON_Y + COMBINE_BUTTON_HEIGHT / 2 - 1;
            renderCombineButtonGlow(graphics, dotX, dotY, dotAlpha);
        }

        renderSelectedAspect(
                graphics,
                firstCombinationIndex,
                FIRST_COMBINE_ICON_X,
                COMBINE_ICON_Y
        );
        renderSelectedAspect(
                graphics,
                secondCombinationIndex,
                SECOND_COMBINE_ICON_X,
                COMBINE_ICON_Y
        );
        boolean hovered = contains(
                leftPos + COMBINE_BUTTON_X,
                topPos + COMBINE_BUTTON_Y,
                COMBINE_BUTTON_WIDTH,
                COMBINE_BUTTON_HEIGHT,
                mouseX,
                mouseY
        );
        if (active && hovered) {
            graphics.fill(
                    leftPos + COMBINE_BUTTON_X,
                    topPos + COMBINE_BUTTON_Y,
                    leftPos + COMBINE_BUTTON_X + COMBINE_BUTTON_WIDTH,
                    topPos + COMBINE_BUTTON_Y + COMBINE_BUTTON_HEIGHT,
                    0x38FFF4A8
            );
        }
        if (hovered) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("screen.thaumcraftmodern.research_table.combine"),
                    mouseX,
                    mouseY
            );
        }
    }

    private void renderSelectedAspect(GuiGraphics graphics, int paletteIndex, int x, int y) {
        List<String> palette = ResearchTableMenu.palette();
        if (paletteIndex < 0 || paletteIndex >= palette.size()) {
            return;
        }
        String aspectId = palette.get(paletteIndex);
        AspectRegistryRuntime.find(aspectId).ifPresent(definition -> {
            int amount = minecraft == null || minecraft.player == null
                    ? 0
                    : KnowledgeAccess.get(minecraft.player)
                            .map(knowledge -> knowledge.aspectAmount(aspectId))
                            .orElse(0);
            if (amount > 0) {
                ClassicUiRender.drawAspect(
                        graphics,
                        new ResourceLocation(definition.icon()),
                        leftPos + x,
                        topPos + y,
                        16,
                        definition.color()
                );
            } else {
                ClassicUiRender.drawTintedScaledTexture(
                        graphics,
                        new ResourceLocation(definition.icon()),
                        leftPos + x,
                        topPos + y,
                        16,
                        16,
                        0,
                        0,
                        32,
                        32,
                        32,
                        32,
                        0xFF353535
                );
            }
        });
    }

    private static void renderCombineButtonGlow(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int alpha
    ) {
        int outerAlpha = Math.max(8, alpha * 18 / 255);
        int middleAlpha = Math.max(16, alpha * 42 / 255);
        int innerAlpha = Math.max(28, alpha * 92 / 255);
        fillCircle(graphics, centerX, centerY, 4, (outerAlpha << 24) | 0x00FFFBE8);
        fillCircle(graphics, centerX, centerY, 3, (middleAlpha << 24) | 0x00FFFBE8);
        fillCircle(graphics, centerX, centerY, 2, (innerAlpha << 24) | 0x00FFFBE8);
        fillCircle(graphics, centerX, centerY, 1, (alpha << 24) | 0x00FFFDF2);
    }

    private static void fillCircle(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {
        for (int offsetY = -radius; offsetY <= radius; offsetY++) {
            int halfWidth = (int) Math.floor(Math.sqrt(radius * radius - offsetY * offsetY));
            graphics.fill(
                    centerX - halfWidth,
                    centerY + offsetY,
                    centerX + halfWidth + 1,
                    centerY + offsetY + 1,
                    color
            );
        }
    }

    private void renderDropTargetHighlights(GuiGraphics graphics, int mouseX, int mouseY) {
        if (draggedPaletteIndex < 0) {
            return;
        }
        if (contains(
                leftPos + FIRST_COMBINE_X,
                topPos + COMBINE_SLOT_Y,
                16,
                16,
                mouseX,
                mouseY
        )) {
            graphics.fill(
                    leftPos + FIRST_COMBINE_X,
                    topPos + COMBINE_SLOT_Y,
                    leftPos + FIRST_COMBINE_X + 16,
                    topPos + COMBINE_SLOT_Y + 16,
                    0xB0FFFFFF
            );
        } else if (contains(
                leftPos + SECOND_COMBINE_X,
                topPos + COMBINE_SLOT_Y,
                16,
                16,
                mouseX,
                mouseY
        )) {
            graphics.fill(
                    leftPos + SECOND_COMBINE_X,
                    topPos + COMBINE_SLOT_Y,
                    leftPos + SECOND_COMBINE_X + 16,
                    topPos + COMBINE_SLOT_Y + 16,
                    0xB0FFFFFF
            );
        }

        // TC4-style puzzle placement needs no coloured target frame: the
        // dragged aspect itself is the preview, and valid relationships are
        // communicated by the connection line after placement.
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Labels and slot captions are already part of the original TC4 artwork.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (minecraft != null && minecraft.player != null) {
            if (isInteractionButton(button)
                    && canDuplicate()
                    && contains(
                            leftPos + DUPLICATE_X,
                            topPos + DUPLICATE_Y,
                            DUPLICATE_SIZE,
                            DUPLICATE_SIZE,
                            mouseX,
                            mouseY
                    )
                    && minecraft.gameMode != null) {
                playButtonClickSound();
                minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId,
                        ResearchTableMenu.DUPLICATE_BUTTON
                );
                return true;
            }
            int maxPage = paletteMaxPage();
            if (isInteractionButton(button) && palettePage > 0 && contains(
                    leftPos + PREVIOUS_PAGE_X,
                    topPos + PAGE_ARROW_Y,
                    PAGE_ARROW_WIDTH,
                    PAGE_ARROW_HEIGHT,
                    mouseX,
                    mouseY
            )) {
                palettePage--;
                playPaletteScrollSound();
                return true;
            }
            if (isInteractionButton(button) && palettePage < maxPage && contains(
                    leftPos + NEXT_PAGE_X,
                    topPos + PAGE_ARROW_Y,
                    PAGE_ARROW_WIDTH,
                    PAGE_ARROW_HEIGHT,
                    mouseX,
                    mouseY
            )) {
                palettePage++;
                playPaletteScrollSound();
                return true;
            }
            if (isInteractionButton(button) && contains(
                    leftPos + FIRST_COMBINE_X,
                    topPos + COMBINE_SLOT_Y,
                    16,
                    16,
                    mouseX,
                    mouseY
            )) {
                firstCombinationIndex = -1;
                playAspectSound();
                return true;
            }
            if (isInteractionButton(button) && contains(
                    leftPos + SECOND_COMBINE_X,
                    topPos + COMBINE_SLOT_Y,
                    16,
                    16,
                    mouseX,
                    mouseY
            )) {
                secondCombinationIndex = -1;
                playAspectSound();
                return true;
            }
            if (isInteractionButton(button) && contains(
                    leftPos + COMBINE_BUTTON_X,
                    topPos + COMBINE_BUTTON_Y,
                    COMBINE_BUTTON_WIDTH,
                    COMBINE_BUTTON_HEIGHT,
                    mouseX,
                    mouseY
            )) {
                if (minecraft.gameMode != null
                        && firstCombinationIndex >= 0
                        && secondCombinationIndex >= 0) {
                    String firstAspect =
                            ResearchTableMenu.palette().get(firstCombinationIndex);
                    String secondAspect =
                            ResearchTableMenu.palette().get(secondCombinationIndex);
                    ResearchDiagnostics.log(
                            "CLIENT_COMBINE_SEND",
                            "player={} container={} first={} second={} encoded={}",
                            minecraft.player.getGameProfile().getName(),
                            menu.containerId,
                            firstAspect,
                            secondAspect,
                            ResearchTableMenu.encodeCombination(
                                    firstCombinationIndex,
                                    secondCombinationIndex
                            )
                    );
                    minecraft.gameMode.handleInventoryButtonClick(
                            menu.containerId,
                            ResearchTableMenu.encodeCombination(
                                    firstCombinationIndex,
                                    secondCombinationIndex
                            )
                    );
                }
                return true;
            }

            var knowledge = KnowledgeAccess.get(minecraft.player).orElse(null);
            List<String> palette = ResearchTableMenu.palette();
            List<Integer> visibleIndices = visiblePaletteIndices();
            for (int visibleIndex = 0; visibleIndex < visibleIndices.size(); visibleIndex++) {
                int paletteIndex = visibleIndices.get(visibleIndex);
                int x = paletteX(visibleIndex);
                int y = paletteY(visibleIndex);
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    String aspectId = palette.get(paletteIndex);
                    if (knowledge != null && knowledge.knowsAspect(aspectId)) {
                        AspectDefinition definition =
                                AspectRegistryRuntime.find(aspectId).orElse(null);
                        if (isInteractionButton(button)
                                && hasShiftDown()
                                && minecraft.gameMode != null
                                && ResearchExpertiseService
                                        .canCombineFromPalette(knowledge)
                                && definition != null
                                && definition.isCompound()
                                && definition.components().stream().allMatch(
                                        component ->
                                                knowledge.aspectAmount(
                                                        component
                                                ) > 0
                                )) {
                            ResearchDiagnostics.log(
                                    "CLIENT_MASTERY_COMBINE_SEND",
                                    "player={} container={} result={} paletteIndex={} encoded={}",
                                    minecraft.player.getGameProfile().getName(),
                                    menu.containerId,
                                    aspectId,
                                    paletteIndex,
                                    ResearchTableMenu.encodeMasteryCombination(
                                            paletteIndex
                                    )
                            );
                            minecraft.gameMode.handleInventoryButtonClick(
                                    menu.containerId,
                                    ResearchTableMenu.encodeMasteryCombination(
                                            paletteIndex
                                    )
                            );
                            return true;
                        }
                        if (knowledge.aspectAmount(aspectId) > 0
                                && isInteractionButton(button)) {
                            draggedPaletteIndex = paletteIndex;
                            ResearchDiagnostics.log(
                                    "CLIENT_DRAG_BEGIN",
                                    "player={} container={} aspect={} amount={} paletteIndex={} mouse=({}, {})",
                                    minecraft.player.getGameProfile().getName(),
                                    menu.containerId,
                                    aspectId,
                                    knowledge.aspectAmount(aspectId),
                                    paletteIndex,
                                    mouseX,
                                    mouseY
                            );
                            playAspectSound();
                        } else if (isInteractionButton(button)) {
                            ResearchDiagnostics.log(
                                    "CLIENT_DRAG_REJECTED",
                                    "player={} aspect={} amount={} reason=empty_pool",
                                    minecraft.player.getGameProfile().getName(),
                                    aspectId,
                                    knowledge.aspectAmount(aspectId)
                            );
                        }
                        return true;
                    }
                }
            }

            if (hasResearchWorkspace()) {
                for (HexResearchPuzzle.Cell cell : menu.puzzle(minecraft.player).cells()) {
                    int x = cellX(cell);
                    int y = cellY(cell);
                    if (mouseX >= x && mouseX < x + PUZZLE_HEX_SIZE
                            && mouseY >= y && mouseY < y + PUZZLE_HEX_SIZE) {
                        if (minecraft.gameMode == null) {
                            return false;
                        }
                        if (button == 1) {
                            ResearchDiagnostics.log(
                                    "CLIENT_ERASE_SEND",
                                    "player={} container={} cell={} encoded={}",
                                    minecraft.player.getGameProfile().getName(),
                                    menu.containerId,
                                    cell,
                                    ResearchTableMenu.encodeErase(cell)
                            );
                            HexResearchPuzzle puzzle = menu.puzzle(minecraft.player);
                            if (!puzzle.isComplete()
                                    && !puzzle.isAnchor(cell)
                                    && puzzle.aspectAt(cell).isPresent()) {
                                playEraseSound();
                            }
                            minecraft.gameMode.handleInventoryButtonClick(
                                    menu.containerId,
                                    ResearchTableMenu.encodeErase(cell)
                            );
                            return true;
                        }
                        if (button == 0 && placementPaletteIndex >= 0) {
                            String aspectId =
                                    ResearchTableMenu.palette().get(placementPaletteIndex);
                            ResearchDiagnostics.log(
                                    "CLIENT_PLACE_SEND",
                                    "player={} container={} source=selected cell={} aspect={} amount={} encoded={}",
                                    minecraft.player.getGameProfile().getName(),
                                    menu.containerId,
                                    cell,
                                    aspectId,
                                    knowledge == null ? -1 : knowledge.aspectAmount(aspectId),
                                    ResearchTableMenu.encodePlacement(cell, placementPaletteIndex)
                            );
                            if (knowledge != null
                                    && menu.puzzle(minecraft.player).validatePlacement(
                                            cell,
                                            aspectId,
                                            knowledge
                                    ) == HexResearchPuzzle.PlacementResult.PLACED) {
                                playPlacementSound();
                            }
                            minecraft.gameMode.handleInventoryButtonClick(
                                    menu.containerId,
                                    ResearchTableMenu.encodePlacement(cell, placementPaletteIndex)
                            );
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (isInteractionButton(button) && draggedPaletteIndex >= 0) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isInteractionButton(button) && draggedPaletteIndex >= 0) {
            int paletteIndex = draggedPaletteIndex;
            draggedPaletteIndex = -1;
            handleAspectDrop(paletteIndex, mouseX, mouseY);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void handleAspectDrop(int paletteIndex, double mouseX, double mouseY) {
        if (contains(
                leftPos + FIRST_COMBINE_X,
                topPos + COMBINE_SLOT_Y,
                16,
                16,
                mouseX,
                mouseY
        )) {
            firstCombinationIndex = paletteIndex;
            ResearchDiagnostics.log(
                    "CLIENT_DROP",
                    "target=combine_first aspect={} paletteIndex={}",
                    ResearchTableMenu.palette().get(paletteIndex),
                    paletteIndex
            );
            return;
        }
        if (contains(
                leftPos + SECOND_COMBINE_X,
                topPos + COMBINE_SLOT_Y,
                16,
                16,
                mouseX,
                mouseY
        )) {
            secondCombinationIndex = paletteIndex;
            ResearchDiagnostics.log(
                    "CLIENT_DROP",
                    "target=combine_second aspect={} paletteIndex={}",
                    ResearchTableMenu.palette().get(paletteIndex),
                    paletteIndex
            );
            return;
        }

        HexResearchPuzzle.Cell cell = hoveredPuzzleCell(mouseX, mouseY);
        if (hasResearchWorkspace()
                && cell != null
                && minecraft != null
                && minecraft.gameMode != null) {
            placementPaletteIndex = paletteIndex;
            String aspectId = ResearchTableMenu.palette().get(paletteIndex);
            int currentAmount = minecraft.player == null
                    ? -1
                    : KnowledgeAccess.get(minecraft.player)
                            .map(knowledge -> knowledge.aspectAmount(aspectId))
                            .orElse(-1);
            ResearchDiagnostics.log(
                    "CLIENT_PLACE_SEND",
                    "player={} container={} source=drag cell={} aspect={} amount={} encoded={}",
                    minecraft.player == null
                            ? "<no-player>"
                            : minecraft.player.getGameProfile().getName(),
                    menu.containerId,
                    cell,
                    aspectId,
                    currentAmount,
                    ResearchTableMenu.encodePlacement(cell, paletteIndex)
            );
            PlayerThaumKnowledge knowledge = minecraft.player == null
                    ? null
                    : KnowledgeAccess.get(minecraft.player).orElse(null);
            if (knowledge != null
                    && menu.puzzle(minecraft.player).validatePlacement(
                            cell,
                            aspectId,
                            knowledge
                    ) == HexResearchPuzzle.PlacementResult.PLACED) {
                playPlacementSound();
            }
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    ResearchTableMenu.encodePlacement(cell, paletteIndex)
            );
            return;
        }

        int paletteUnderMouse = paletteIndexAt(mouseX, mouseY);
        if (paletteUnderMouse == paletteIndex) {
            ResearchDiagnostics.log(
                    "CLIENT_DROP",
                    "target=palette_reselect aspect={} paletteIndex={}",
                    ResearchTableMenu.palette().get(paletteIndex),
                    paletteIndex
            );
            selectAspectForCombination(paletteIndex);
        } else {
            ResearchDiagnostics.log(
                    "CLIENT_DROP",
                    "target=none aspect={} paletteIndex={} mouse=({}, {})",
                    ResearchTableMenu.palette().get(paletteIndex),
                    paletteIndex,
                    mouseX,
                    mouseY
            );
        }
    }

    private void selectAspectForCombination(int paletteIndex) {
        placementPaletteIndex = paletteIndex;
        if (firstCombinationIndex < 0) {
            firstCombinationIndex = paletteIndex;
        } else if (secondCombinationIndex < 0) {
            secondCombinationIndex = paletteIndex;
        } else {
            secondCombinationIndex = paletteIndex;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderDuplicationCost(graphics, mouseX, mouseY);
        renderDraggedAspect(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean canDuplicate() {
        if (minecraft == null || minecraft.player == null
                || !(menu.notes().getItem() instanceof DiscoveryItem)
                || !DiscoveryItem.hasValidPayload(menu.notes())) {
            return false;
        }
        return KnowledgeAccess.get(minecraft.player)
                .filter(knowledge -> knowledge.hasCompletedResearch(
                        ResearchDuplicationService.UNLOCK_RESEARCH
                ))
                .isPresent()
                && ResearchRegistry.find(DiscoveryItem.researchId(menu.notes()))
                .filter(research -> !research.researchCost().isEmpty())
                .isPresent();
    }

    private void renderDuplicationCost(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        if (!canDuplicate() || !contains(
                leftPos + DUPLICATE_X,
                topPos + DUPLICATE_Y,
                DUPLICATE_SIZE,
                DUPLICATE_SIZE,
                mouseX,
                mouseY
        )) {
            return;
        }
        var research = ResearchRegistry.find(
                DiscoveryItem.researchId(menu.notes())
        ).orElseThrow();
        var costs = ResearchDuplicationService.cost(
                research,
                DiscoveryItem.copies(menu.notes())
        ).stream().sorted(java.util.Comparator.comparing(
                com.thaumcraftmodern.aspect.AspectCost::aspectId
        )).toList();

        graphics.blit(
                BACKGROUND,
                leftPos + 100,
                topPos + 21,
                184,
                224,
                48,
                16,
                256,
                256
        );
        graphics.drawString(
                font,
                Component.translatable("tc.research.copy"),
                leftPos + 100,
                topPos + 12,
                0xFFFFFFFF,
                true
        );
        for (int index = 0; index < costs.size(); index++) {
            var cost = costs.get(index);
            AspectDefinition aspect = AspectRegistryRuntime.find(
                    cost.aspectId()
            ).orElse(null);
            if (aspect == null) {
                continue;
            }
            int x = leftPos + 148 + index * 16;
            int y = topPos + 21;
            ClassicUiRender.drawAspectTag(
                    graphics,
                    font,
                    new ResourceLocation(aspect.icon()),
                    x,
                    y,
                    16,
                    aspect.color(),
                    cost.amount()
            );
        }
    }

    private void renderDraggedAspect(GuiGraphics graphics, int mouseX, int mouseY) {
        if (draggedPaletteIndex < 0
                || draggedPaletteIndex >= ResearchTableMenu.palette().size()) {
            return;
        }
        String aspectId = ResearchTableMenu.palette().get(draggedPaletteIndex);
        AspectRegistryRuntime.find(aspectId).ifPresent(definition -> {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, DRAGGED_ASPECT_Z);
            ClassicUiRender.drawAspect(
                    graphics,
                    new ResourceLocation(definition.icon()),
                    mouseX - 8,
                    mouseY - 8,
                    16,
                    definition.color()
            );
            graphics.pose().popPose();
        });
    }

    private int cellX(HexResearchPuzzle.Cell cell) {
        return hexX(cell.q()) - PUZZLE_HEX_SIZE / 2;
    }

    private int cellY(HexResearchPuzzle.Cell cell) {
        return hexY(cell.q(), cell.r()) - PUZZLE_HEX_SIZE / 2;
    }

    private int hexX(int q) {
        return leftPos + PUZZLE_CENTER_X + Math.round(13.5F * q);
    }

    private int hexY(int q, int r) {
        return topPos + PUZZLE_CENTER_Y + Math.round(
                9.0F * (float) Math.sqrt(3.0D) * (r + q / 2.0F)
        );
    }

    private int paletteX(int index) {
        return leftPos + PALETTE_X + (index / PALETTE_ROWS) * 16;
    }

    private int paletteY(int index) {
        return topPos + PALETTE_Y + (index % PALETTE_ROWS) * 16;
    }

    private int paletteIndexAt(double mouseX, double mouseY) {
        List<Integer> visibleIndices = visiblePaletteIndices();
        for (int visibleIndex = 0; visibleIndex < visibleIndices.size(); visibleIndex++) {
            if (contains(
                    paletteX(visibleIndex),
                    paletteY(visibleIndex),
                    16,
                    16,
                    mouseX,
                    mouseY
            )) {
                return visibleIndices.get(visibleIndex);
            }
        }
        return -1;
    }

    private List<Integer> visiblePaletteIndices() {
        if (minecraft == null || minecraft.player == null) {
            return List.of();
        }
        var knowledge = KnowledgeAccess.get(minecraft.player).orElse(null);
        if (knowledge == null) {
            return List.of();
        }
        return ResearchTablePaletteLayout.visibleIndices(
                ResearchTableMenu.palette(),
                knowledge.knownAspects(),
                palettePage
        );
    }

    private int paletteMaxPage() {
        if (minecraft == null || minecraft.player == null) {
            return 0;
        }
        var knowledge = KnowledgeAccess.get(minecraft.player).orElse(null);
        if (knowledge == null) {
            return 0;
        }
        int knownCount = ResearchTablePaletteLayout.visibleIndices(
                ResearchTableMenu.palette(),
                knowledge.knownAspects()
        ).size();
        return ResearchTablePaletteLayout.maxPage(knownCount);
    }

    private HexResearchPuzzle.Cell hoveredPuzzleCell(double mouseX, double mouseY) {
        if (minecraft == null || minecraft.player == null) return null;
        for (HexResearchPuzzle.Cell cell : menu.puzzle(minecraft.player).cells()) {
            if (contains(
                    cellX(cell), cellY(cell), PUZZLE_HEX_SIZE, PUZZLE_HEX_SIZE,
                    mouseX, mouseY)) {
                return cell;
            }
        }
        return null;
    }

    private boolean hasResearchNotes() {
        return menu.notes().getItem() instanceof ResearchNotesItem;
    }

    private boolean hasResearchWorkspace() {
        return hasResearchNotes()
                && menu.scribingTools().getItem() instanceof ScribingToolsItem
                && menu.scribingTools().getDamageValue() < menu.scribingTools().getMaxDamage();
    }

    private void playAspectSound() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(
                    ModSounds.HH_OFF.get(),
                    1.0F + minecraft.player.getRandom().nextFloat() * 0.1F,
                    0.2F
            ));
        }
    }

    private void playPaletteScrollSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(
                    ModSounds.KEY.get(),
                    1.0F,
                    0.3F
            ));
        }
    }

    private void playButtonClickSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(
                    ModSounds.CAMERA_CLACK.get(),
                    1.0F,
                    0.4F
            ));
        }
    }

    private void playCombineSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(
                    ModSounds.HH_ON.get(),
                    1.0F,
                    0.3F
            ));
        }
    }

    private void playPlacementSound() {
        playCombineSound();
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(
                    ModSounds.WRITE.get(),
                    1.0F,
                    0.2F
            ));
        }
    }

    private void playEraseSound() {
        playCombineSound();
        if (minecraft != null && minecraft.player != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(
                    ModSounds.ERASE.get(),
                    1.0F + minecraft.player.getRandom().nextFloat() * 0.1F,
                    0.2F
            ));
        }
    }

    private static boolean contains(
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static boolean isInteractionButton(int button) {
        return button == 0 || button == 1;
    }

    private record RuneMark(int x, int y, int index, boolean rotated) {
    }
}
