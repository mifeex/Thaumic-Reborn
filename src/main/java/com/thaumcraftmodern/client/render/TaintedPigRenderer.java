package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** TC4 tainted pig: classic pig geometry and byte-exact TC4 skin. */
public final class TaintedPigRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        PigModel<LegacyThaumcraftMob>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/pig.png"
    );

    public TaintedPigRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new PigModel<>(context.bakeLayer(ModelLayers.PIG)),
                0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }
}
