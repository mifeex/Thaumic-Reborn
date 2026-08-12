package com.thaumcraftmodern.client.screen;

/** Shared pixel coordinates for rendering and hit-testing book controls. */
final class ThaumonomiconBookLayout {
    static final Region BACK = new Region(118, 189, 20, 12);
    static final Region PREVIOUS = new Region(-16, 190, 12, 8);
    static final Region NEXT = new Region(262, 190, 12, 8);

    private ThaumonomiconBookLayout() {
    }

    record Region(int x, int y, int width, int height) {
        boolean contains(
                int originX,
                int originY,
                double mouseX,
                double mouseY
        ) {
            return mouseX >= originX + x
                    && mouseX < originX + x + width
                    && mouseY >= originY + y
                    && mouseY < originY + y + height;
        }
    }
}
