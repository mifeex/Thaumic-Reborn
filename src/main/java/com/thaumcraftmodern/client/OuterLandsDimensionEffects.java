package com.thaumcraftmodern.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/** The fixed, oppressive violet-black TC4 Outer Lands atmosphere. */
public final class OuterLandsDimensionEffects extends DimensionSpecialEffects {
    public OuterLandsDimensionEffects() {
        super(1.0F, false, SkyType.NONE, true, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
        // Original WorldProviderOuter uses 0xA080A0 at 15% brightness.
        return new Vec3(
                160.0D / 255.0D * 0.15D,
                128.0D / 255.0D * 0.15D,
                160.0D / 255.0D * 0.15D
        );
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return true;
    }
}
