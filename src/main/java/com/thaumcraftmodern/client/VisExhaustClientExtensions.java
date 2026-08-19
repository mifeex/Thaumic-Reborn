package com.thaumcraftmodern.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * Client presentation shared by Flux Flu and Flux Phage.
 */
public final class VisExhaustClientExtensions
        implements IClientMobEffectExtensions {
    public static final VisExhaustClientExtensions INSTANCE =
            new VisExhaustClientExtensions();

    /**
     * Manual vertical tuning for the inventory effect icon, in GUI pixels.
     * Positive values move the icon down; negative values move it up.
     */
    public static final int INVENTORY_ICON_Y_OFFSET = 4;

    private VisExhaustClientExtensions() {
    }

    @Override
    public boolean renderInventoryIcon(
            MobEffectInstance instance,
            EffectRenderingInventoryScreen<?> screen,
            GuiGraphics guiGraphics,
            int x,
            int y,
            int blitOffset
    ) {
        TextureAtlasSprite icon = Minecraft.getInstance()
                .getMobEffectTextures()
                .get(instance.getEffect());
        guiGraphics.blit(
                x,
                y + INVENTORY_ICON_Y_OFFSET,
                blitOffset,
                18,
                18,
                icon
        );
        return true;
    }
}
