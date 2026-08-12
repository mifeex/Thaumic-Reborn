package com.thaumcraftmodern.client;

import com.thaumcraftmodern.network.packet.ResearchTableFeedbackPacket;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Bottom-right notification queue shared by every Research Table response. */
public final class ClientResearchTableOverlay {
    private static final long DISPLAY_MILLIS = 4_500L;
    private static final int MAX_TEXT_WIDTH = 220;
    private static final int SUCCESS_COLOR = 0xF4E8C9;
    private static final int FAILURE_COLOR = 0xEE6D6E;
    private static final Deque<Entry> QUEUE = new ArrayDeque<>();
    private static Entry current;
    private static long shownAt;

    private ClientResearchTableOverlay() {
    }

    public static void show(ResearchTableFeedbackPacket packet) {
        QUEUE.addLast(new Entry(packet.message(), packet.success()));
        advance(Util.getMillis());
    }

    public static void render(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null) {
            render(graphics, screenWidth, screenHeight);
        }
    }

    public static void renderScreen(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {
        render(graphics, screenWidth, screenHeight);
    }

    private static void render(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = Util.getMillis();
        advance(now);
        if (current == null || minecraft.player == null
                || minecraft.options.hideGui) {
            return;
        }
        List<FormattedCharSequence> lines = minecraft.font.split(
                current.message(),
                MAX_TEXT_WIDTH
        );
        int lineHeight = minecraft.font.lineHeight + 2;
        int top = screenHeight - 18 - lines.size() * lineHeight;
        int color = current.success() ? SUCCESS_COLOR : FAILURE_COLOR;
        for (int index = 0; index < lines.size(); index++) {
            FormattedCharSequence line = lines.get(index);
            int x = screenWidth - minecraft.font.width(line) - 12;
            graphics.drawString(
                    minecraft.font,
                    line,
                    x,
                    top + index * lineHeight,
                    color,
                    true
            );
        }
    }

    private static void advance(long now) {
        if (current != null && now - shownAt < DISPLAY_MILLIS) {
            return;
        }
        current = QUEUE.pollFirst();
        shownAt = now;
    }

    private record Entry(Component message, boolean success) {
    }
}
