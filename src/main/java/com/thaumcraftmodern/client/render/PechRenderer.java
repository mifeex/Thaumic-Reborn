package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class PechRenderer
        extends MobRenderer<LegacyThaumcraftMob, PechModel> {
    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(context.bakeLayer(PechModel.LAYER)), 0.5F);
        addLayer(new PechHeldItemLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        String texture = switch (entity.pechType()) {
            case 1 -> "pech_thaum.png";
            case 2 -> "pech_stalker.png";
            default -> "pech_forage.png";
        };
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                "textures/entity/models/" + texture
        );
    }

    /** Exact modern equivalent of TC4 RenderPech.PechHeldItemLayer. */
    private static final class PechHeldItemLayer
            extends RenderLayer<LegacyThaumcraftMob, PechModel> {
        private final net.minecraft.client.renderer.ItemInHandRenderer items;

        private PechHeldItemLayer(
                PechRenderer renderer,
                EntityRendererProvider.Context context
        ) {
            super(renderer);
            items = context.getItemInHandRenderer();
        }

        @Override
        public void render(
                PoseStack pose,
                MultiBufferSource buffers,
                int packedLight,
                LegacyThaumcraftMob pech,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch
        ) {
            ItemStack held = pech.getMainHandItem();
            if (held.isEmpty()) return;
            pose.pushPose();
            getParentModel().translateToHand(
                    net.minecraft.world.entity.HumanoidArm.RIGHT,
                    pose
            );
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            items.renderItem(
                    pech,
                    held,
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    false,
                    pose,
                    buffers,
                    packedLight
            );
            pose.popPose();
        }
    }
}
