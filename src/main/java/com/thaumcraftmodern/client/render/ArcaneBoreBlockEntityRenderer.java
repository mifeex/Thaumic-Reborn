package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.ArcaneBoreBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Original articulated bore plus synchronized two-color excavation beam. */
public final class ArcaneBoreBlockEntityRenderer
        implements BlockEntityRenderer<ArcaneBoreBlockEntity> {
    public static final ResourceLocation VORTEX = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/misc/vortex.png");
    public static final ResourceLocation JAR = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/jar.png");
    private final ArcaneBoreModel model;
    private final ArcaneBoreJarCoreModel jar;
    public ArcaneBoreBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        model = new ArcaneBoreModel(context.bakeLayer(ArcaneBoreModel.LAYER));
        jar = new ArcaneBoreJarCoreModel(context.bakeLayer(ArcaneBoreJarCoreModel.LAYER));
    }
    @Override public void render(ArcaneBoreBlockEntity bore, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var modelVertices = buffers.getBuffer(RenderType.entityCutoutNoCull(
                ArcaneBoreBaseBlockEntityRenderer.TEXTURE));
        pose.pushPose(); pose.translate(0.5D, 0.5D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(bore.rotX() - bore.aimX()));
        pose.pushPose();
        if (bore.baseOrientation() == net.minecraft.core.Direction.DOWN)
            pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.translate(0, -0.5D, 0);
        model.renderBoreBase(pose, modelVertices, light, overlay); pose.popPose();
        pose.mulPose(Axis.ZP.rotationDegrees(bore.rotZ() - bore.aimZ()));
        pose.pushPose(); pose.mulPose(Axis.ZP.rotationDegrees(90)); pose.translate(0, -0.5D, 0);
        model.renderBoreNozzle(pose, modelVertices, light, overlay); pose.popPose();
        pose.pushPose(); pose.mulPose(Axis.YP.rotationDegrees(bore.topRotation()));
        pose.translate(0, 0.5D, 0);
        model.renderEmitter(pose, modelVertices, light, overlay, bore.hasFocus()); pose.popPose();
        float ticks = (bore.getLevel() == null ? 0 : bore.getLevel().getGameTime()) + partialTick;
        float rotation = ticks % 45.0F;
        renderVortex(pose, buffers, light, -0.17F, -rotation * 8.0F, 10.0F, 0.40F, 1.0F);
        renderVortex(pose, buffers, light, -0.21F, rotation * 8.0F, 10.0F, 0.30F, 0.8F);
        renderVortex(pose, buffers, light, -0.25F, -rotation * 8.0F, -10.0F, 0.20F, 0.8F);
        pose.pushPose(); pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.translate(0, 0.3D, 0); pose.scale(0.6F, 0.6F, 0.6F);
        jar.render(pose, buffers.getBuffer(RenderType.entityTranslucent(JAR)), light, overlay);
        pose.popPose();
        pose.popPose();
        if (bore.beamTicks() > 0 && bore.digTarget() != null) renderBeam(bore, pose, buffers);
    }

    private static void renderVortex(PoseStack pose, MultiBufferSource buffers, int light,
            float y, float rotation, float yaw, float size, float alpha) {
        pose.pushPose(); pose.translate(0, y, 0);
        pose.mulPose(Axis.XP.rotationDegrees(-90));
        pose.mulPose(Axis.ZP.rotationDegrees(rotation));
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(VORTEX));
        var matrix = pose.last().pose(); var normal = pose.last().normal();
        textured(vertices, matrix, normal, -size, -size, 0, 0, light, alpha);
        textured(vertices, matrix, normal, size, -size, 1, 0, light, alpha);
        textured(vertices, matrix, normal, size, size, 1, 1, light, alpha);
        textured(vertices, matrix, normal, -size, size, 0, 1, light, alpha);
        pose.popPose();
    }

    private static void textured(VertexConsumer vertices, org.joml.Matrix4f matrix,
            org.joml.Matrix3f normal, float x, float y, float u, float v,
            int light, float alpha) {
        vertices.vertex(matrix, x, y, 0).color(1, 1, 1, alpha).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 0, 1).endVertex();
    }

    private static void renderBeam(ArcaneBoreBlockEntity bore, PoseStack pose,
            MultiBufferSource buffers) {
        Vec3 end = Vec3.atCenterOf(bore.digTarget()).subtract(Vec3.atCenterOf(bore.getBlockPos()));
        Vec3 direction = end.normalize();
        Vec3 side = direction.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1.0E-4D) side = direction.cross(new Vec3(1, 0, 0));
        side = side.normalize().scale(0.035D);
        Vec3 second = direction.cross(side).normalize().scale(0.035D);
        pose.pushPose(); pose.translate(0.5D, 0.5D, 0.5D);
        VertexConsumer vertices = buffers.getBuffer(RenderType.lightning());
        quad(vertices, pose.last().pose(), Vec3.ZERO, end, side, 0, 255, 102, 220);
        quad(vertices, pose.last().pose(), Vec3.ZERO, end, second, 255, 136, 85, 210);
        pose.popPose();
    }

    private static void quad(VertexConsumer vertices, org.joml.Matrix4f matrix,
            Vec3 start, Vec3 end, Vec3 side, int red, int green, int blue, int alpha) {
        vertex(vertices, matrix, start.add(side), red, green, blue, alpha);
        vertex(vertices, matrix, end.add(side), red, green, blue, alpha);
        vertex(vertices, matrix, end.subtract(side), red, green, blue, alpha);
        vertex(vertices, matrix, start.subtract(side), red, green, blue, alpha);
    }
    private static void vertex(VertexConsumer vertices, org.joml.Matrix4f matrix, Vec3 pos,
            int red, int green, int blue, int alpha) {
        vertices.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(red, green, blue, alpha).endVertex();
    }
    @Override public boolean shouldRenderOffScreen(ArcaneBoreBlockEntity bore) { return true; }
    @Override public int getViewDistance() { return 96; }
}
