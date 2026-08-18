package com.thaumcraftmodern.entity;

import net.minecraft.util.Mth;

/**
 * TC4 {@code EntityTaintChicken.onLivingUpdate} wing state, separated from
 * the shared mob so the renderer never has to invent a perpetual idle flap.
 */
final class TaintedChickenFlapAnimation {
    private float rotation;
    private float previousRotation;
    private float spread;
    private float previousSpread;
    private float speed = 1.0F;

    void tick(boolean onGround) {
        previousRotation = rotation;
        previousSpread = spread;
        spread = Mth.clamp(
                spread + (onGround ? -1.0F : 4.0F) * 0.3F,
                0.0F,
                1.0F
        );
        if (!onGround && speed < 1.0F) {
            speed = 1.0F;
        }
        speed *= 0.9F;
        rotation += speed * 2.0F;
    }

    float sample(float partialTick) {
        float wingRotation = Mth.lerp(
                partialTick,
                previousRotation,
                rotation
        );
        float wingSpread = Mth.lerp(
                partialTick,
                previousSpread,
                spread
        );
        return (Mth.sin(wingRotation) + 1.0F) * wingSpread;
    }
}
