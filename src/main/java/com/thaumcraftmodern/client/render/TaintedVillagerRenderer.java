package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** TC4 tainted villager geometry, scale and byte-exact TC4 skin. */
public final class TaintedVillagerRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        TaintedVillagerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/villager.png"
    );

    public TaintedVillagerRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new TaintedVillagerModel(
                        context.bakeLayer(TaintedVillagerModel.LAYER)
                ),
                0.5F
        );
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        pose.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }
}
