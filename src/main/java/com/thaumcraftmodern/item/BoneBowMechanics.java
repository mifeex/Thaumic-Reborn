package com.thaumcraftmodern.item;

/** Bootstrap-free constants and draw curve copied from TC4's ItemBowBone. */
public final class BoneBowMechanics {
    public static final int FULL_DRAW_TICKS = 10;
    public static final int FORCED_RELEASE_TICKS = 19;
    public static final float ARROW_VELOCITY = 2.5F;

    private BoneBowMechanics() {
    }

    public static float powerForTime(int charge) {
        float power = charge / (float) FULL_DRAW_TICKS;
        power = (power * power + power * 2.0F) / 3.0F;
        return Math.min(power, 1.0F);
    }
}
