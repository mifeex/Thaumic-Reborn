package com.thaumcraftmodern.item;

/** Bootstrap-independent ink durability arithmetic for classic scribing tools. */
final class ScribingToolsInk {
    private ScribingToolsInk() {
    }

    static int nextDamage(int currentDamage, int maxDamage) {
        if (maxDamage <= 0) {
            return 0;
        }
        return Math.min(maxDamage, Math.max(0, currentDamage) + 1);
    }
}
