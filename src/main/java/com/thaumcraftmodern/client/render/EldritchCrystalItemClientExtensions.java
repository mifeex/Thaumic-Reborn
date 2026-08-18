package com.thaumcraftmodern.client.render;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class EldritchCrystalItemClientExtensions {
    private EldritchCrystalItemClientExtensions() {
    }

    public static IClientItemExtensions create() {
        return new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new EldritchCrystalItemRenderer();
                }
                return renderer;
            }
        };
    }
}
