package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * TC4's warden reuses the guardian mesh at 1.5 scale and adds the pulsating,
 * additive hood-eye pass.
 */
public final class EldritchWardenRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        EldritchGuardianModel> {
    private static final int CLASSIC_EYE_LIGHT_BASE = 210;
    private static final int CLASSIC_EYE_LIGHT_AMPLITUDE = 15;
    private static final RenderType CLASSIC_EYE_RENDER_TYPE =
            ClassicEyeRenderType.create(
                    LegacyMobKind.ELDRITCH_WARDEN.texture()
            );

    public EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new EldritchGuardianModel(
                        context.bakeLayer(EldritchGuardianModel.LAYER)
                ),
                0.8F
        );
        addLayer(new EyeLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return entity.kind().texture();
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        pose.scale(1.5F, 1.5F, 1.5F);
    }

    private static final class EyeLayer extends RenderLayer<
            LegacyThaumcraftMob,
            EldritchGuardianModel> {
        private EyeLayer(RenderLayerParent<
                LegacyThaumcraftMob,
                EldritchGuardianModel> parent) {
            super(parent);
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
            pose.pushPose();
            pose.scale(1.01F, 1.01F, 1.01F);
            VertexConsumer vertices = buffers.getBuffer(
                    CLASSIC_EYE_RENDER_TYPE
            );
            getParentModel().renderWardenEyes(
                    pose,
                    vertices,
                    classicEyeLight(entity.tickCount),
                    OverlayTexture.NO_OVERLAY
            );
            pose.popPose();
        }
    }

    static int classicEyeLight(int tickCount) {
        return (int) (
                CLASSIC_EYE_LIGHT_BASE
                        + Mth.sin(tickCount / 3.0F)
                        * CLASSIC_EYE_LIGHT_AMPLITUDE
        );
    }

    /**
     * The vanilla eyes render type is permanently full-bright. TC4 instead
     * used additive blending with a pulsing lightmap value of 195..225, so it
     * needs the regular entity shader with the lightmap kept enabled.
     */
    private abstract static class ClassicEyeRenderType
            extends RenderStateShard {
        private ClassicEyeRenderType() {
            super("tc4_warden_eye_access", () -> { }, () -> { });
        }

        private static RenderType create(ResourceLocation texture) {
            RenderType.CompositeState state =
                    RenderType.CompositeState.builder()
                            .setShaderState(
                                    RENDERTYPE_ENTITY_TRANSLUCENT_SHADER
                            )
                            .setTextureState(
                                    new TextureStateShard(texture, false, false)
                            )
                            .setTransparencyState(ADDITIVE_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .setWriteMaskState(COLOR_WRITE)
                            .createCompositeState(false);
            return RenderType.create(
                    "thaumic_reborn_warden_eyes",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state
            );
        }
    }
}
