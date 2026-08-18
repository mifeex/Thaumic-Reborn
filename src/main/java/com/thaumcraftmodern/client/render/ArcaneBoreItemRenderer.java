package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.item.ArcaneBoreItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** TC4 inventory render delegates to the same cuboids and Bore.png atlas. */
final class ArcaneBoreItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final ArcaneBoreModel model;
    private final ArcaneBoreItem.Kind kind;
    ArcaneBoreItemRenderer(ArcaneBoreItem.Kind kind) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        this.kind = kind;
        model = new ArcaneBoreModel(Minecraft.getInstance().getEntityModels()
                .bakeLayer(ArcaneBoreModel.LAYER));
    }
    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(
                ArcaneBoreBaseBlockEntityRenderer.TEXTURE));
        pose.pushPose(); pose.translate(0.5D, 0, 0.5D);
        if (kind == ArcaneBoreItem.Kind.BASE) {
            model.renderSupport(pose, vertices, light, overlay);
            model.renderSupportNozzle(pose, vertices, light, overlay);
        } else {
            pose.translate(0, 0.5D, 0);
            pose.pushPose(); pose.translate(0, -0.5D, 0);
            model.renderBoreBase(pose, vertices, light, overlay); pose.popPose();
            pose.pushPose(); pose.mulPose(Axis.ZP.rotationDegrees(90)); pose.translate(0, -0.5D, 0);
            model.renderBoreNozzle(pose, vertices, light, overlay); pose.popPose();
            pose.translate(0, 0.5D, 0);
            model.renderEmitter(pose, vertices, light, overlay, true);
        }
        pose.popPose();
    }
}
