package com.thaumcraftmodern.client;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.network.packet.ScanFeedbackPacket;
import com.thaumcraftmodern.scan.AspectReward;
import net.minecraft.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.List;

public final class ClientScanOverlay {
    private static final int MAX_ASPECT_COLUMNS = 6;
    private static final long FAILURE_DISPLAY_MILLIS = 4_500L;
    private static final long CLASSIC_NOTIFICATION_DELAY_MILLIS = 5_000L;
    private static final long CLASSIC_FIRST_NOTIFICATION_BONUS_MILLIS = 2_500L;
    private static final long CLASSIC_ENTRANCE_MILLIS =
            CLASSIC_NOTIFICATION_DELAY_MILLIS / 4L;
    private static final long FADE_IN_MILLIS = 250L;
    private static final long FADE_OUT_MILLIS = 650L;
    private static final long ASPECT_FLIGHT_MILLIS = 1_500L;
    private static final long ASPECT_FLIGHT_RANDOM_DELAY_MILLIS = 1_000L;
    private static final int TEXT_COLOR = 0xF4E8C9;
    private static final float FAILURE_TEXT_SCALE = 0.45F;
    private static final float SUCCESS_TEXT_SCALE = 0.5F;
    private static final int SUCCESS_ASPECT_ICON_SIZE = 8;
    private static final int FLYING_ASPECT_ICON_SIZE = 19;
    private static final int MAX_FLYING_ASPECTS = 64;
    private static final float NODE_COUNT_TEXT_SCALE = 0.65F;
    private static final ResourceLocation CLASSIC_PARTICLES =
            new ResourceLocation(
                    "thaumic_reborn",
                    "textures/misc/particles.png"
            );
    private static final ResourceLocation CLASSIC_THAUMONOMICON =
            new ResourceLocation(
                    "thaumic_reborn",
                    "textures/item/thaumonomicon.png"
            );
    private static final ResourceLocation UNKNOWN_ASPECT =
            new ResourceLocation(
                    "thaumic_reborn",
                    "textures/aspects/_unknown.png"
            );
    private static ScanFeedbackPacket current;
    private static long shownAt;
    private static long expiresAt;

    private ClientScanOverlay() {
    }

    public static void show(ScanFeedbackPacket packet) {
        current = packet;
        shownAt = Util.getMillis();
        expiresAt = shownAt + (packet.success()
                ? CLASSIC_NOTIFICATION_DELAY_MILLIS
                + CLASSIC_FIRST_NOTIFICATION_BONUS_MILLIS
                : FAILURE_DISPLAY_MILLIS);
    }

    /** Uses the same bottom-right notification lane as Thaumometer feedback. */
    public static void showWarp(String messageKey) {
        show(new ScanFeedbackPacket(false, messageKey, "", List.of()));
    }

    /**
     * TC4-style scan feedback has no panel behind it. Text notifications sit
     * at the bottom-right and are revealed by the classic travelling glow,
     * while awarded aspects follow the original curved flight from the center
     * of the screen towards the Thaumonomicon icon at the top-right.
     */
    public static void renderNotification(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ScanFeedbackPacket notification = visibleNotification();
        if (minecraft.player == null
                || minecraft.screen != null
                || minecraft.options.hideGui
                || notification == null) {
            return;
        }
        if (notification.success()) {
            renderSuccessNotification(
                    graphics,
                    minecraft,
                    notification,
                    screenWidth,
                    screenHeight
            );
        } else if (isStudyFailure(notification.messageKey())
                || isWarpMessage(notification.messageKey())) {
            renderFailureNotification(
                    graphics,
                    minecraft,
                    notification,
                    screenWidth,
                    screenHeight
            );
        }
    }

    /** Draws the same scan result above an open inventory/container GUI. */
    public static void renderScreenNotification(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ScanFeedbackPacket notification = visibleNotification();
        if (minecraft.player == null || minecraft.options.hideGui
                || notification == null) {
            return;
        }
        if (notification.success()) {
            renderSuccessNotification(graphics, minecraft, notification,
                    screenWidth, screenHeight);
        } else if (isStudyFailure(notification.messageKey())
                || isWarpMessage(notification.messageKey())) {
            renderFailureNotification(graphics, minecraft, notification,
                    screenWidth, screenHeight);
        }
    }

    /**
     * Compatibility entrypoint retained for callers which registered the old
     * failure-only overlay.
     */
    public static void renderFailureNotification(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        renderNotification(
                gui,
                graphics,
                partialTick,
                screenWidth,
                screenHeight
        );
    }

    private static void renderFailureNotification(
            GuiGraphics graphics,
            Minecraft minecraft,
            ScanFeedbackPacket notification,
            int screenWidth,
            int screenHeight
    ) {

        long now = Util.getMillis();
        float fadeIn = fadeIn(now);
        float visibility = visibility(now);
        float easedSlide = 1.0F - (1.0F - fadeIn) * (1.0F - fadeIn);
        int alpha = Mth.clamp(Math.round(255.0F * visibility), 4, 255);
        boolean warpMessage = isWarpMessage(notification.messageKey());
        int color = (alpha << 24) | (warpMessage ? 0xAA00AA : TEXT_COLOR);

        Component message = notification.displayKey().isBlank()
                ? Component.translatable(notification.messageKey())
                : Component.translatable(
                        notification.messageKey(),
                        Component.translatable(notification.displayKey())
                );
        if (warpMessage) {
            message = message.copy().withStyle(
                    ChatFormatting.DARK_PURPLE,
                    ChatFormatting.ITALIC
            );
        }
        int maxRenderedWidth = Math.max(100, Math.min(220, screenWidth / 2));
        int maxTextWidth = Math.round(maxRenderedWidth / FAILURE_TEXT_SCALE);
        List<FormattedCharSequence> lines = minecraft.font.split(message, maxTextWidth);
        int textWidth = lines.stream()
                .mapToInt(minecraft.font::width)
                .max()
                .orElse(0);
        int renderedTextWidth = Math.round(textWidth * FAILURE_TEXT_SCALE);
        int targetX = screenWidth - renderedTextWidth - 12;
        int x = Mth.lerpInt(easedSlide, screenWidth + 6, targetX);
        int lineHeight = minecraft.font.lineHeight + 2;
        int renderedHeight = Math.round(
                lines.size() * lineHeight * FAILURE_TEXT_SCALE
        );
        int y = screenHeight - 18 - renderedHeight;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 200.0F);
        graphics.pose().scale(
                FAILURE_TEXT_SCALE,
                FAILURE_TEXT_SCALE,
                1.0F
        );
        int localY = 0;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(minecraft.font, line, 0, localY, color, true);
            localY += lineHeight;
        }
        graphics.pose().popPose();
    }

    private static void renderSuccessNotification(
            GuiGraphics graphics,
            Minecraft minecraft,
            ScanFeedbackPacket notification,
            int screenWidth,
            int screenHeight
    ) {
        long now = Util.getMillis();
        float fadeIn = successFadeIn(now);
        float visibility = successVisibility(now);
        if (visibility <= 0.0F) {
            return;
        }
        int alpha = Mth.clamp(Math.round(255.0F * visibility), 4, 255);
        int color = (successTextAlpha(alpha) << 24) | TEXT_COLOR;
        List<SuccessAspectRow> aspectRows = successAspectRows(notification);

        Component message = "tc.addaspectpool".equals(notification.messageKey())
                ? Component.empty()
                : notification.displayKey().isBlank()
                ? Component.translatable(
                        "message.thaumic_reborn.scan.success_generic"
                )
                : Component.translatable(
                        notification.messageKey(),
                        Component.translatable(notification.displayKey())
                );
        int maxRenderedWidth = Math.max(110, Math.min(240, screenWidth / 2));
        int maxTextWidth = Math.round(maxRenderedWidth / SUCCESS_TEXT_SCALE);
        List<FormattedCharSequence> messageLines =
                minecraft.font.split(message, maxTextWidth);
        int messageWidth = messageLines.stream()
                .mapToInt(minecraft.font::width)
                .max()
                .orElse(0);
        int renderedMessageWidth = Math.round(
                messageWidth * SUCCESS_TEXT_SCALE
        );
        int messageHeight = Math.round(
                messageLines.size()
                        * (minecraft.font.lineHeight + 2)
                        * SUCCESS_TEXT_SCALE
        );
        int aspectRowHeight = SUCCESS_ASPECT_ICON_SIZE;
        int contentHeight = messageHeight
                + (aspectRows.isEmpty() ? 0 : 3 + aspectRows.size() * aspectRowHeight);
        int bottom = screenHeight - 10;
        int top = bottom - contentHeight;
        int messageTargetX = screenWidth - renderedMessageWidth - 12;
        int messageX = messageTargetX;

        graphics.pose().pushPose();
        graphics.pose().translate(messageX, top, 220.0F);
        graphics.pose().scale(
                SUCCESS_TEXT_SCALE,
                SUCCESS_TEXT_SCALE,
                1.0F
        );
        int localY = 0;
        for (FormattedCharSequence line : messageLines) {
            graphics.drawString(
                    minecraft.font,
                    line,
                    0,
                    localY,
                    color,
                    true
            );
            localY += minecraft.font.lineHeight + 2;
        }
        graphics.pose().popPose();
        drawClassicNotificationGlow(
                graphics,
                minecraft,
                fadeIn,
                messageWidth / 2,
                screenWidth,
                top + messageHeight
        );

        int rowsTop = top + messageHeight + 3;
        for (int index = 0; index < aspectRows.size(); index++) {
            SuccessAspectRow row = aspectRows.get(index);
            int textWidth = minecraft.font.width(row.text());
            int renderedTextWidth = Math.round(
                    textWidth * SUCCESS_TEXT_SCALE
            );
            int rowWidth = SUCCESS_ASPECT_ICON_SIZE + 3 + renderedTextWidth;
            int targetX = screenWidth - rowWidth - 12;
            int x = targetX;
            int y = rowsTop + index * aspectRowHeight;
            drawSuccessAspectRow(
                    graphics,
                    minecraft,
                    row,
                    x,
                    y,
                    alpha
            );
        }
        renderFlyingAspects(
                graphics,
                notification,
                now,
                screenWidth,
                screenHeight
        );
    }

    private static void drawClassicNotificationGlow(
            GuiGraphics graphics,
            Minecraft minecraft,
            float fadeIn,
            int halfUnscaledTextWidth,
            int screenWidth,
            int notificationBaselineY
    ) {
        float scale = 1.0F - fadeIn;
        if (scale <= 0.0F) {
            return;
        }
        int size = Math.max(1, Math.round(16.0F * scale));
        int x = Math.round(notificationGlowX(
                screenWidth,
                halfUnscaledTextWidth,
                scale
        ));
        int y = Math.round(notificationBaselineY - 2.0F - 8.0F * scale);
        int frame = (minecraft.player.tickCount & 15) * 16;
        int alpha = Mth.clamp(
                Math.round(notificationGlowAlpha(scale) * 255.0F),
                0,
                255
        );
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                CLASSIC_PARTICLES,
                x,
                y,
                size,
                size,
                frame,
                80,
                16,
                16,
                256,
                256,
                (alpha << 24) | 0xFFFFFF
        );
    }

    static float notificationGlowX(
            int screenWidth,
            int halfUnscaledTextWidth,
            float scale
    ) {
        float inverse = 1.0F - scale;
        return (screenWidth - 5.0F)
                - 8.0F * scale
                - inverse * inverse * inverse
                * halfUnscaledTextWidth * 3.0F;
    }

    static float notificationGlowAlpha(float scale) {
        float referenceAlpha = 255.0F - scale * 240.0F;
        return Mth.clamp(
                0.5F - referenceAlpha / 511.0F,
                0.0F,
                1.0F
        );
    }

    private static void drawSuccessAspectRow(
            GuiGraphics graphics,
            Minecraft minecraft,
            SuccessAspectRow row,
            int x,
            int y,
            int alpha
    ) {
        AspectDefinition definition =
                AspectRegistryRuntime.find(row.aspectId()).orElse(null);
        if (definition == null) {
            return;
        }
        float opacity = alpha / 511.0F;
        ClassicUiRender.drawAspect(
                graphics,
                new ResourceLocation(definition.icon()),
                x,
                y,
                SUCCESS_ASPECT_ICON_SIZE,
                definition.color(),
                opacity
        );
        graphics.pose().pushPose();
        graphics.pose().translate(
                x + SUCCESS_ASPECT_ICON_SIZE + 3,
                y + 2,
                221.0F
        );
        graphics.pose().scale(
                SUCCESS_TEXT_SCALE,
                SUCCESS_TEXT_SCALE,
                1.0F
        );
        graphics.drawString(
                minecraft.font,
                row.text(),
                0,
                0,
                (successTextAlpha(alpha) << 24) | TEXT_COLOR,
                true
        );
        graphics.pose().popPose();
    }

    /**
     * Minecraft treats font colors with alpha values below {@code 4} as
     * legacy RGB colors and promotes them to fully opaque. Keep the classic
     * half-opacity text while ensuring the final fade-out frames remain
     * explicitly translucent instead of flashing at full opacity.
     */
    static int successTextAlpha(int notificationAlpha) {
        int halfAlpha = Mth.clamp(notificationAlpha, 0, 255) / 2;
        return halfAlpha == 0 ? 0 : Math.max(4, halfAlpha);
    }

    private static List<SuccessAspectRow> successAspectRows(
            ScanFeedbackPacket notification
    ) {
        boolean classicAspectPool = "tc.addaspectpool".equals(
                notification.messageKey()
        );
        return notification.aspects().stream()
                .filter(gain -> gain.amount() > 0)
                .filter(gain -> AspectRegistryRuntime.find(
                        gain.aspectId()
                ).isPresent())
                .map(gain -> {
                    Component aspectName = Component.translatable(
                            "aspect.thaumic_reborn." + gain.aspectId()
                    );
                    Component text = classicAspectPool
                            ? Component.translatable(
                                    "message.thaumic_reborn.knowledge_fragment.aspect_pool",
                                    gain.amount(),
                                    aspectName
                            )
                            : gain.newlyDiscovered()
                            ? Component.translatable(
                                    "message.thaumic_reborn.scan.aspect_discovered_amount",
                                    aspectName,
                                    gain.amount()
                            )
                            : Component.translatable(
                                    "message.thaumic_reborn.scan.aspect_added",
                                    aspectName,
                                    gain.amount(),
                                    gain.total()
                            );
                    return new SuccessAspectRow(gain.aspectId(), text);
                })
                .toList();
    }

    private static void renderFlyingAspects(
            GuiGraphics graphics,
            ScanFeedbackPacket notification,
            long now,
            int screenWidth,
            int screenHeight
    ) {
        int ordinal = 0;
        float mainAlpha = 0.0F;
        for (ScanFeedbackPacket.AspectGain gain : notification.aspects()) {
            AspectDefinition definition =
                    AspectRegistryRuntime.find(gain.aspectId()).orElse(null);
            if (definition == null) {
                continue;
            }
            int copies = Math.min(
                    Math.max(0, gain.amount()),
                    MAX_FLYING_ASPECTS - ordinal
            );
            for (int copy = 0; copy < copies; copy++) {
                long created = shownAt
                        + Math.round(
                                deterministicUnit(
                                        gain.aspectId().hashCode() * 61
                                                + copy * 37
                                                + ordinal * 19
                                )
                                        * ASPECT_FLIGHT_RANDOM_DELAY_MILLIS
                        );
                double progress = (now - created)
                        / (double) ASPECT_FLIGHT_MILLIS;
                if (progress >= 0.0D && progress <= 1.0D) {
                    float opacity = flightAlpha(progress);
                    drawFlyingAspect(
                            graphics,
                            definition,
                            gain.aspectId(),
                            copy,
                            progress,
                            screenWidth,
                            screenHeight,
                            opacity
                    );
                    mainAlpha = Math.max(mainAlpha, opacity);
                }
                ordinal++;
                if (ordinal >= MAX_FLYING_ASPECTS) {
                    drawThaumonomiconAnchor(
                            graphics,
                            screenWidth,
                            mainAlpha
                    );
                    return;
                }
            }
        }
        drawThaumonomiconAnchor(graphics, screenWidth, mainAlpha);
    }

    private static void drawFlyingAspect(
            GuiGraphics graphics,
            AspectDefinition definition,
            String aspectId,
            int copy,
            double progress,
            int screenWidth,
            int screenHeight,
            float opacity
    ) {
        float startFractionX = 0.4F + deterministicUnit(
                aspectId.hashCode() * 31 + copy * 17
        ) * 0.2F;
        float startFractionY = 0.4F + deterministicUnit(
                aspectId.hashCode() * 43 + copy * 29
        ) * 0.2F;
        double startX = screenWidth * startFractionX;
        double startY = screenHeight * startFractionY;
        double controlX = screenWidth * (0.25F + startFractionX);
        double controlY = screenHeight * startFractionY;
        int x = (int) Math.round(quadraticBezier(
                startX,
                controlX,
                screenWidth,
                progress
        ));
        int y = (int) Math.round(quadraticBezier(
                startY,
                controlY,
                -8.0D,
                progress
        ));
        int size = Math.max(
                1,
                Math.round(FLYING_ASPECT_ICON_SIZE * opacity)
        );
        ClassicUiRender.drawAspect(
                graphics,
                new ResourceLocation(definition.icon()),
                x,
                y,
                size,
                definition.color(),
                opacity * 0.66F
        );
    }

    private static void drawThaumonomiconAnchor(
            GuiGraphics graphics,
            int screenWidth,
            float alpha
    ) {
        if (alpha <= 0.0F) {
            return;
        }
        int alphaByte = Mth.clamp(
                Math.round(alpha * 255.0F),
                0,
                255
        );
        ClassicUiRender.drawTintedScaledTexture(
                graphics,
                CLASSIC_THAUMONOMICON,
                screenWidth - 16,
                0,
                16,
                16,
                0,
                0,
                32,
                32,
                32,
                32,
                (alphaByte << 24) | 0xFFFFFF
        );
    }

    static double quadraticBezier(
            double start,
            double control,
            double end,
            double progress
    ) {
        double inverse = 1.0D - progress;
        return inverse * inverse * start
                + 2.0D * inverse * progress * control
                + progress * progress * end;
    }

    static float flightAlpha(double progress) {
        if (progress < 0.0D || progress > 1.0D) {
            return 0.0F;
        }
        if (progress < 0.3D) {
            return (float) (progress / 0.3D);
        }
        if (progress > 0.66D) {
            return (float) (1.0D - (progress - 0.66D) / 0.34D);
        }
        return 1.0F;
    }

    private static float deterministicUnit(int seed) {
        int mixed = seed ^ seed >>> 16;
        return (mixed & 0x7FFF) / 32767.0F;
    }

    private static float fadeIn(long now) {
        return Mth.clamp(
                (now - shownAt) / (float) FADE_IN_MILLIS,
                0.0F,
                1.0F
        );
    }

    private static float successFadeIn(long now) {
        return Mth.clamp(
                (now - shownAt) / (float) CLASSIC_ENTRANCE_MILLIS,
                0.0F,
                1.0F
        );
    }

    private static float successVisibility(long now) {
        float entrance = (15.0F + 240.0F * successFadeIn(now)) / 255.0F;
        float fadeOut = Mth.clamp(
                (expiresAt - now)
                        / (float) CLASSIC_NOTIFICATION_DELAY_MILLIS,
                0.0F,
                1.0F
        );
        return Math.min(entrance, fadeOut);
    }

    private static float visibility(long now) {
        float fadeOut = Mth.clamp(
                (expiresAt - now) / (float) FADE_OUT_MILLIS,
                0.0F,
                1.0F
        );
        return Math.min(fadeIn(now), fadeOut);
    }

    private record SuccessAspectRow(String aspectId, Component text) {
    }

    public static void renderThaumometerReadout(
            GuiGraphics graphics,
            Minecraft minecraft,
            int left,
            int top,
            int width,
            int height
    ) {
        ScanFeedbackPacket notification = visibleNotification();
        if (notification == null || !notification.success() || notification.aspects().isEmpty()) {
            return;
        }

        Component target = notification.displayKey().isBlank()
                ? Component.empty()
                : Component.translatable(notification.displayKey());
        if (!target.getString().isBlank()) {
            graphics.drawCenteredString(
                    minecraft.font,
                    target,
                    left + width / 2,
                    top + Math.max(3, height / 8),
                    0xFFF1E6BA
            );
        }

        renderAspectValues(
                graphics,
                minecraft,
                notification.aspects().stream()
                        .map(gain -> new AspectValue(
                                gain.aspectId(),
                                gain.amount(),
                                true
                        ))
                        .toList(),
                left,
                top + Math.max(15, height / 7),
                width,
                height - Math.max(15, height / 7)
        );
    }

    public static void renderThaumometerTarget(
            GuiGraphics graphics,
            Minecraft minecraft,
            Component target,
            Component nodeDescription,
            List<AspectReward> aspects,
            int left,
            int top,
            int width,
            int height
    ) {
        boolean hasTitle = !target.getString().isBlank();
        if (hasTitle) {
            graphics.drawCenteredString(
                    minecraft.font,
                    target,
                    left + width / 2,
                    top + Math.max(3, height / 8),
                    0xFFF1E6BA
            );
        }
        boolean hasNodeDescription = !nodeDescription.getString().isBlank();
        if (hasNodeDescription) {
            graphics.drawCenteredString(
                    minecraft.font,
                    nodeDescription,
                    left + width / 2,
                    top + Math.max(3, height / 8)
                            + minecraft.font.lineHeight + 1,
                    0xFFF1E6BA
            );
        }
        if (aspects.isEmpty()) {
            return;
        }

        int aspectTopOffset = hasNodeDescription
                ? Math.max(26, height / 5)
                : hasTitle ? Math.max(15, height / 7) : 0;
        renderAspectValues(
                graphics,
                minecraft,
                aspects.stream()
                        .map(reward -> new AspectValue(
                                reward.aspectId(),
                                reward.amount(),
                                true
                        ))
                        .toList(),
                left,
                top + aspectTopOffset,
                width,
                height - aspectTopOffset
        );
    }

    public static void renderNodeAspects(
            GuiGraphics graphics,
            Minecraft minecraft,
            List<AspectReward> aspects,
            int centerX,
            int bottomY,
            float alpha,
            float scale
    ) {
        if (aspects.isEmpty() || alpha <= 0.0F || scale <= 0.0F) {
            return;
        }

        int columns = Math.min(5, aspects.size());
        int rows = (aspects.size() + columns - 1) / columns;
        int iconSize = 18;
        int horizontalSpacing = 24;
        int verticalSpacing = 22;
        int width = columns * horizontalSpacing - 6;
        int height = rows * verticalSpacing - 4;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, bottomY, 200.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        java.util.function.Predicate<String> knowsAspect =
                KnowledgeAccess.get(minecraft.player)
                        .<java.util.function.Predicate<String>>map(
                                knowledge -> knowledge::knowsAspect
                        )
                        .orElse(aspect -> false);
        renderAspectValues(
                graphics,
                minecraft,
                ClientNodeAspectMask.apply(aspects, knowsAspect).stream()
                        .map(aspect -> new AspectValue(
                                aspect.aspectId(),
                                aspect.amount(),
                                aspect.known()
                        ))
                        .toList(),
                -width / 2,
                -height,
                width,
                height,
                alpha,
                NODE_COUNT_TEXT_SCALE
        );
        graphics.pose().popPose();
    }

    private static void renderAspectValues(
            GuiGraphics graphics,
            Minecraft minecraft,
            List<AspectValue> aspects,
            int left,
            int top,
            int width,
            int height
    ) {
        renderAspectValues(
                graphics,
                minecraft,
                aspects,
                left,
                top,
                width,
                height,
                1.0F,
                1.0F
        );
    }

    private static void renderAspectValues(
            GuiGraphics graphics,
            Minecraft minecraft,
            List<AspectValue> aspects,
            int left,
            int top,
            int width,
            int height,
            float alpha,
            float textScale
    ) {
        if (aspects.isEmpty() || width <= 0 || height <= 0) {
            return;
        }

        int columns = Math.min(MAX_ASPECT_COLUMNS, aspects.size());
        int rows = (aspects.size() + columns - 1) / columns;
        int iconSize = Math.max(
                12,
                Math.min(
                        24,
                        Math.min(
                                width / Math.max(1, columns + 1),
                                height / Math.max(1, rows) - 3
                        )
                )
        );
        int horizontalSpacing = iconSize + 7;
        int verticalSpacing = iconSize + 3;
        int contentHeight = rows * verticalSpacing - 3;
        int startY = top + Math.max(0, (height - contentHeight) / 2);

        for (int index = 0; index < aspects.size(); index++) {
            AspectValue value = aspects.get(index);
            AspectDefinition definition =
                    AspectRegistryRuntime.find(value.aspectId()).orElse(null);
            if (definition == null) {
                continue;
            }
            int row = index / columns;
            int column = index % columns;
            int entriesInRow = Math.min(
                    columns,
                    aspects.size() - row * columns
            );
            int rowWidth = entriesInRow * horizontalSpacing - 7;
            int iconX = left + (width - rowWidth) / 2
                    + column * horizontalSpacing;
            int iconY = startY + row * verticalSpacing;
            ClassicUiRender.drawAspectTag(
                    graphics,
                    minecraft.font,
                    value.known()
                            ? new ResourceLocation(definition.icon())
                            : UNKNOWN_ASPECT,
                    iconX,
                    iconY,
                    iconSize,
                    definition.color(),
                    Integer.toString(value.amount()),
                    alpha,
                    textScale
            );
        }
    }

    private record AspectValue(
            String aspectId,
            int amount,
            boolean known
    ) {
    }

    private static ScanFeedbackPacket visibleNotification() {
        if (current == null || Util.getMillis() >= expiresAt) {
            current = null;
            return null;
        }
        return current;
    }

    private static boolean isStudyFailure(String messageKey) {
        return messageKey.equals("message.thaumic_reborn.scan.unknown")
                || messageKey.equals(
                        "message.thaumic_reborn.scan.error.target_not_registered"
                )
                || messageKey.equals(
                        "message.thaumic_reborn.scan.error.missing_parent"
                );
    }

    private static boolean isWarpMessage(String messageKey) {
        return messageKey.startsWith("warp.text.")
                || messageKey.startsWith("tc.addwarp")
                || messageKey.startsWith("tc.removewarp");
    }
}
