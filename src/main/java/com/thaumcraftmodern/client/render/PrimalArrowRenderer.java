package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.entity.PrimalArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** TC4 used the vanilla arrow texture beneath its aspect-coloured glow. */
public final class PrimalArrowRenderer extends ArrowRenderer<PrimalArrowEntity> {
    private static final ResourceLocation ARROW_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/projectiles/arrow.png");

    public PrimalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimalArrowEntity arrow) {
        return ARROW_TEXTURE;
    }
}
