package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.research.ResearchDefinition;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

/** Pure hit-testing for the research tree; actions remain screen-owned. */
final class ThaumonomiconResearchInteraction {
    private static final int VIEWPORT_X = 16;
    private static final int VIEWPORT_Y = 16;
    private static final int VIEWPORT_WIDTH = 224;
    private static final int VIEWPORT_HEIGHT = 197;

    private ThaumonomiconResearchInteraction() {
    }

    static boolean isWithinViewport(double x, double y) {
        return x >= VIEWPORT_X
                && x < VIEWPORT_X + VIEWPORT_WIDTH
                && y >= VIEWPORT_Y
                && y < VIEWPORT_Y + VIEWPORT_HEIGHT;
    }

    static Optional<ResearchDefinition> researchAt(
            List<ResearchDefinition> research,
            ToIntFunction<ResearchDefinition> xPosition,
            ToIntFunction<ResearchDefinition> yPosition,
            int nodeSize,
            double mouseX,
            double mouseY
    ) {
        for (ResearchDefinition definition : research) {
            if (contains(
                        xPosition.applyAsInt(definition),
                        yPosition.applyAsInt(definition),
                        nodeSize,
                        nodeSize,
                        mouseX,
                        mouseY
                )) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    private static boolean contains(
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }
}
