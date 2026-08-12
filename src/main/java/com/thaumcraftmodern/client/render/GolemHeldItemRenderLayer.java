package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.entity.ClassicGolemEntity;
import com.thaumcraftmodern.entity.GolemCoreType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Original TC4 carried-stack layer: cargo between both hands, tools in the right hand. */
public final class GolemHeldItemRenderLayer<T extends ClassicGolemEntity>
        extends RenderLayer<T, StrawGolemModel<T>> {
    private static final float HELD_ITEM_SCALE = 1.3F;
    private final ItemRenderer items;

    public GolemHeldItemRenderLayer(RenderLayerParent<T, StrawGolemModel<T>> parent, ItemRenderer items) {
        super(parent);
        this.items = items;
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffers, int light, T golem,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (golem.isDeadOrDying()) return;
        if (golem.core() == GolemCoreType.FISHING) {
            renderRightHand(poses, buffers, light, golem, new ItemStack(Items.FISHING_ROD));
            return;
        }
        if (golem.core() == GolemCoreType.LIQUID) {
            ItemStack bucket = golem.carriedForDisplay();
            if (bucket.isEmpty()) bucket = new ItemStack(Items.BUCKET);
            renderRightHand(poses, buffers, light, golem, bucket);
            return;
        }
        ItemStack carried = golem.carriedForDisplay();
        if (!carried.isEmpty()) renderBetweenHands(poses, buffers, light, golem, carried);
    }

    private void renderBetweenHands(PoseStack poses, MultiBufferSource buffers, int light,
            T golem, ItemStack stack) {
        poses.pushPose();
        poses.scale(.4F, .4F, .4F);
        // 1.7.10 icons occupied 0..1 and needed an X=-0.5 centering correction.
        // Modern FIXED item models are already centered, so applying it again puts cargo in
        // the left hand. Keep every item type on the body's centre line between both hands.
        poses.translate(0F, 2.5F, -1.25F);
        poses.scale(.72F, .72F, .72F);
        poses.mulPose(Axis.XP.rotationDegrees(180F));
        poses.mulPose(Axis.YP.rotationDegrees(180F));
        poses.scale(HELD_ITEM_SCALE, HELD_ITEM_SCALE, HELD_ITEM_SCALE);
        items.renderStatic(golem, stack, ItemDisplayContext.FIXED, false,
                poses, buffers, golem.level(), light, OverlayTexture.NO_OVERLAY, golem.getId());
        poses.popPose();
    }

    private void renderRightHand(PoseStack poses, MultiBufferSource buffers, int light,
            T golem, ItemStack stack) {
        poses.pushPose();
        poses.scale(.4F, .4F, .4F);
        getParentModel().translateToRightArm(poses);
        poses.translate(-10F / 16F, 20F / 16F, 0F);
        poses.mulPose(Axis.XP.rotationDegrees(-90F));
        poses.mulPose(Axis.YP.rotationDegrees(180F));
        items.renderStatic(golem, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false,
                poses, buffers, golem.level(), light, OverlayTexture.NO_OVERLAY, golem.getId());
        poses.popPose();
    }
}
