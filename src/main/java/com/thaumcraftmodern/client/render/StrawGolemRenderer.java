package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.ClassicGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class StrawGolemRenderer<T extends ClassicGolemEntity>
        extends MobRenderer<T, StrawGolemModel<T>> {
    public StrawGolemRenderer(EntityRendererProvider.Context context) {
        super(context,new StrawGolemModel<>(context.bakeLayer(StrawGolemModel.LAYER)),.25F);
        addLayer(new GolemCoreRenderLayer<>(this, context.getItemRenderer()));
        addLayer(new GolemHeldItemRenderLayer<>(this, context.getItemRenderer()));
        addLayer(new AdvancedGolemLayer<>(this, context.bakeLayer(AdvancedGolemLayer.LAYER)));
    }
    @Override protected void setupRotations(T golem, PoseStack poses, float age, float yaw, float partial) {
        super.setupRotations(golem,poses,age,yaw,partial);
        float amount = golem.walkAnimation.speed(partial);
        if (amount >= .01F) {
            float swing = golem.walkAnimation.position(partial) + 6F;
            float wave = (Math.abs(swing%13F-6.5F)-3.25F)/3.25F;
            poses.mulPose(Axis.ZP.rotationDegrees(6.5F*wave));
        }
    }
    @Override public ResourceLocation getTextureLocation(T entity) {
        return new ResourceLocation(ThaumcraftModern.MOD_ID,
                "textures/entity/models/golem_" + entity.material().id() + ".png");
    }
}
