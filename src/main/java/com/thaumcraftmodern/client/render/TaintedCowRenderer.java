package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** TC4 tainted cow: classic cow geometry with the byte-exact TC4 skin. */
public final class TaintedCowRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        CowModel<LegacyThaumcraftMob>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/cow.png"
    );

    public TaintedCowRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new CowModel<>(context.bakeLayer(ModelLayers.COW)),
                0.7F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }
}
