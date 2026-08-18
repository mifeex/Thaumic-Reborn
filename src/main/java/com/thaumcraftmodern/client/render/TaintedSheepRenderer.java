package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** TC4 tainted sheep base and wool passes with their original textures. */
public final class TaintedSheepRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        TaintedSheepModel> {
    private static final ResourceLocation TEXTURE = texture("sheep.png");
    private static final ResourceLocation FUR_TEXTURE = texture("sheep_fur.png");

    public TaintedSheepRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new TaintedSheepModel(
                        context.bakeLayer(TaintedSheepModel.BASE_LAYER)
                ),
                0.7F
        );
        addLayer(new FurLayer(
                this,
                new TaintedSheepModel(
                        context.bakeLayer(TaintedSheepModel.FUR_LAYER)
                )
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                "textures/entity/models/" + name
        );
    }

    private static final class FurLayer extends RenderLayer<
            LegacyThaumcraftMob,
            TaintedSheepModel> {
        private final TaintedSheepModel furModel;

        private FurLayer(
                RenderLayerParent<LegacyThaumcraftMob, TaintedSheepModel> parent,
                TaintedSheepModel furModel
        ) {
            super(parent);
            this.furModel = furModel;
        }

        @Override
        public void render(
                PoseStack pose,
                MultiBufferSource buffers,
                int packedLight,
                LegacyThaumcraftMob entity,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch
        ) {
            furModel.prepareMobModel(
                    entity, limbSwing, limbSwingAmount, partialTick
            );
            furModel.setupAnim(
                    entity, limbSwing, limbSwingAmount,
                    ageInTicks, netHeadYaw, headPitch
            );
            VertexConsumer vertices = buffers.getBuffer(
                    RenderType.entityCutoutNoCull(FUR_TEXTURE)
            );
            furModel.renderToBuffer(
                    pose,
                    vertices,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F
            );
        }
    }
}
