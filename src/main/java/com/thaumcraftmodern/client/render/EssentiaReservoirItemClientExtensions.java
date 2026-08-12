package com.thaumcraftmodern.client.render;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Lazily supplies TC4's full reservoir model in inventories and research. */
public final class EssentiaReservoirItemClientExtensions {
    private EssentiaReservoirItemClientExtensions() { }

    public static IClientItemExtensions create() {
        return new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new EssentiaReservoirItemRenderer();
                }
                return renderer;
            }
        };
    }
}
