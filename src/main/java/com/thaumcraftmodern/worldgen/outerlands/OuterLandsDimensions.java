package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class OuterLandsDimensions {
    public static final ResourceKey<Level> OUTER_LANDS = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(ThaumcraftModern.MOD_ID, "outer_lands")
    );

    private OuterLandsDimensions() {
    }
}
