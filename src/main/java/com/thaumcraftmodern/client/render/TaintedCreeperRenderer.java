package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** TC4 tainted creeper: classic creeper geometry and byte-exact TC4 skin. */
public final class TaintedCreeperRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        CreeperModel<LegacyThaumcraftMob>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/creeper.png"
    );

    public TaintedCreeperRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)),
                0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }
}
