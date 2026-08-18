package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.item.ArcaneBoreItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class ArcaneBoreItemClientExtensions {
    private ArcaneBoreItemClientExtensions() { }
    public static IClientItemExtensions create(ArcaneBoreItem.Kind kind) {
        return new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new ArcaneBoreItemRenderer(kind);
                return renderer;
            }
        };
    }
}
