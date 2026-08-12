package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.AdvancedAlchemicalFurnaceBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Renders the unchanged TC4 Base/Tank OBJ and its working gauges. */
public final class AdvancedAlchemicalFurnaceBlockEntityRenderer
        implements BlockEntityRenderer<AdvancedAlchemicalFurnaceBlockEntity> {
    private static final ResourceLocation MODEL = texture("models/adv_alch_furnace.obj");
    private static final ResourceLocation BASE = texture("models/alch_furnace.png");
    private static final ResourceLocation BASE_ON = texture("models/alch_furnace_on.png");
    private static final ResourceLocation TANK = texture("models/alch_furnace_tank.png");
    private static final ResourceLocation TANK_ON = texture("models/alch_furnace_tank_on.png");
    private static final ResourceLocation FLUX = sprite("block/flux_goo");
    private static final ResourceLocation METAL = texture("block/metalbase.png");
    private static final ResourceLocation FIRE = sprite("block/fire_0");
    private LegacyObjMesh model;

    public AdvancedAlchemicalFurnaceBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }

    @Override public void render(AdvancedAlchemicalFurnaceBlockEntity furnace, float partialTick,
            PoseStack poses, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (furnace.isNozzle()) return;
        if (model == null) model = LegacyObjMesh.load(MODEL);
        poses.pushPose();
        poses.translate(0.5, 0, 0.5);
        poses.mulPose(Axis.XP.rotationDegrees(-90));
        model.render("Base", poses,
                buffers.getBuffer(RenderType.entityCutoutNoCull(
                        furnace.isProcessing() ? BASE_ON : BASE)),
                packedLight, 1, 1, 1, 1);
        VertexConsumer tank = buffers.getBuffer(RenderType.entityCutoutNoCull(
                        furnace.isProcessing() ? TANK_ON : TANK));
        for (int side = 0; side < 4; side++) {
            poses.pushPose();
            poses.mulPose(Axis.ZP.rotationDegrees(90 * side));
            model.render("Tank", poses, tank, packedLight, 1, 1, 1, 1);
            poses.popPose();
        }
        if (furnace.isProcessing())
            renderVis(poses, buffers, furnace.essentiaAmount());
        if (furnace.isProcessing())
            renderHeat(poses, buffers, furnace.heat());
        poses.popPose();
    }

    private static void renderVis(PoseStack poses, MultiBufferSource buffers, int amount) {
        TextureAtlasSprite flux = atlasSprite(FLUX);
        VertexConsumer animated = buffers.getBuffer(
                RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        poses.pushPose();
        poses.translate(0.5, -0.5, 1.1);
        poses.mulPose(Axis.YP.rotationDegrees(180));
        quad(poses, animated, flux, 0, LightTexture.pack(12, 0));
        poses.popPose();

        float fill = 1.0F - amount / (float) AdvancedAlchemicalFurnaceBlockEntity.MAX_ESSENTIA;
        for (int side = 0; side < 4; side++) {
            poses.pushPose();
            poses.mulPose(Axis.ZP.rotationDegrees(90 * side));
            poses.mulPose(Axis.XP.rotationDegrees(-90));
            poses.translate(0.85, -1.8, -1.4);
            poses.scale(0.3F, 0.6F, 1);
            quad(poses, buffers.getBuffer(RenderType.entityTranslucent(METAL)), 0,
                    LightTexture.pack(9, 0));
            poses.translate(0, 0, -0.01);
            quad(poses, animated, flux, fill, LightTexture.pack(12, 0));
            poses.popPose();

            poses.pushPose();
            poses.mulPose(Axis.ZP.rotationDegrees(-90 * side));
            poses.mulPose(Axis.XP.rotationDegrees(90));
            poses.translate(1.15, 1.8, -1.4);
            poses.scale(-0.3F, -0.6F, -1);
            quad(poses, buffers.getBuffer(RenderType.entityTranslucent(METAL)), 0,
                    LightTexture.pack(9, 0));
            poses.translate(0, 0, 0.01);
            quad(poses, animated, flux, fill, LightTexture.pack(12, 0));
            poses.popPose();
        }
    }

    private static void renderHeat(PoseStack poses, MultiBufferSource buffers, int heat) {
        TextureAtlasSprite fire = atlasSprite(FIRE);
        VertexConsumer animated = buffers.getBuffer(
                RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        float fill = 1.0F - Math.min(1.0F,
                heat / (float) AdvancedAlchemicalFurnaceBlockEntity.MAX_POWER);
        poses.pushPose();
        poses.translate(0, 0, 1);
        for (int side = 0; side < 4; side++) {
            poses.pushPose();
            poses.mulPose(Axis.ZP.rotationDegrees(90 * side));
            poses.mulPose(Axis.XP.rotationDegrees(135));
            poses.mulPose(Axis.ZP.rotationDegrees(180));
            poses.translate(-0.5, 0, -1);
            quad(poses, animated, fire, fill, LightTexture.pack(14, 0));
            poses.translate(0, 0, 0.05);
            quad(poses, buffers.getBuffer(RenderType.entityTranslucent(METAL)), 0,
                    LightTexture.pack(9, 0));
            poses.popPose();
        }
        poses.popPose();
    }

    /** TC4 stretches the complete icon between y=fill and y=1. */
    private static void quad(PoseStack poses, VertexConsumer out, float fill, int light) {
        quad(poses, out, 0, 1, 0, 1, fill, light);
    }

    /** Uses the sprite's stitched atlas region so its current animated frame is sampled. */
    private static void quad(PoseStack poses, VertexConsumer out,
            TextureAtlasSprite sprite, float fill, int light) {
        quad(poses, out, sprite.getU0(), sprite.getU1(),
                sprite.getV0(), sprite.getV1(), fill, light);
    }

    private static void quad(PoseStack poses, VertexConsumer out,
            float u0, float u1, float v0, float v1, float fill, int light) {
        Matrix4f matrix = poses.last().pose();
        Matrix3f normal = poses.last().normal();
        out.vertex(matrix, 0, 1, 0).color(255,255,255,255).uv(u0,v1).overlayCoords(0).uv2(light).normal(normal,0,0,-1).endVertex();
        out.vertex(matrix, 1, 1, 0).color(255,255,255,255).uv(u1,v1).overlayCoords(0).uv2(light).normal(normal,0,0,-1).endVertex();
        out.vertex(matrix, 1, fill, 0).color(255,255,255,255).uv(u1,v0).overlayCoords(0).uv2(light).normal(normal,0,0,-1).endVertex();
        out.vertex(matrix, 0, fill, 0).color(255,255,255,255).uv(u0,v0).overlayCoords(0).uv2(light).normal(normal,0,0,-1).endVertex();
    }

    private static TextureAtlasSprite atlasSprite(ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(
                TextureAtlas.LOCATION_BLOCKS).apply(location);
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(ThaumcraftModern.MOD_ID, "textures/" + path);
    }

    private static ResourceLocation sprite(String path) {
        return new ResourceLocation(ThaumcraftModern.MOD_ID, path);
    }
}
