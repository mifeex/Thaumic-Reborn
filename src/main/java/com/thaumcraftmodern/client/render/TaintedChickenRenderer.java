package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * TC4 tainted chicken: the classic chicken geometry and the original 64x32
 * Thaumcraft skin.  This must not share the humanoid fallback renderer: that
 * interprets the chicken UV atlas as a Steve skin and leaves only scattered
 * cubes visible.
 */
public final class TaintedChickenRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        ChickenModel<LegacyThaumcraftMob>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/chicken.png"
    );

    public TaintedChickenRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)),
                0.3F
        );
    }

    @Override
    protected float getBob(LegacyThaumcraftMob entity, float partialTick) {
        return entity.taintedChickenWingBob(partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }
}
