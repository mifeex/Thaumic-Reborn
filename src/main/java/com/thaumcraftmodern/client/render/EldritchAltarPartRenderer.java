package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.EldritchAltarPartBlock;
import com.thaumcraftmodern.world.block.entity.EldritchAltarPartBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Random;

/**
 * Modern port of TC4's TileEldritchCapRenderer and
 * TileEldritchObeliskRenderer.
 */
public final class EldritchAltarPartRenderer
        implements BlockEntityRenderer<EldritchAltarPartBlockEntity> {
    private static final ResourceLocation CAP =
            texture("textures/models/obelisk_cap.png");
    private static final ResourceLocation ALTAR_CAP =
            texture("textures/models/obelisk_cap_altar.png");
    private static final ResourceLocation SIDE =
            texture("textures/models/obelisk_side.png");
    private static final ResourceLocation FIELD_BACKING =
            texture("textures/misc/particlefield32.png");
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final float EYE_VERTICAL_OFFSET = 0.2F + 5.0F / 16.0F;
    private static final LegacyEldritchCapModel CAP_MODEL =
            new LegacyEldritchCapModel();
    private final ItemRenderer items;

    public EldritchAltarPartRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        items = context.getItemRenderer();
    }

    @Override
    public void render(
            EldritchAltarPartBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        int part = blockEntity.getBlockState().getValue(
                EldritchAltarPartBlock.PART
        );
        if (part == 0) {
            renderCap(poseStack, buffers, ALTAR_CAP, packedLight, -90.0F);
            renderAltarEyes(
                    blockEntity,
                    poseStack,
                    buffers,
                    packedLight,
                    packedOverlay
            );
        } else if (part == 1) {
            renderObelisk(
                    blockEntity,
                    partialTick,
                    poseStack,
                    buffers,
                    packedLight
            );
        } else if (part == 4) {
            renderCap(poseStack, buffers, CAP, packedLight, -90.0F);
        }
    }

    /** Exact four-side placement from TC4's TileEldritchCapRenderer. */
    private void renderAltarEyes(
            EldritchAltarPartBlockEntity altar,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        int eyeCount = Mth.clamp(altar.insertedEyes(), 0, 4);
        if (eyeCount == 0) {
            return;
        }
        ItemStack eye = new ItemStack(ModItems.ELDRITCH_EYE.get());
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        for (int side = 0; side < eyeCount; side++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(side * 90.0F));
            poseStack.translate(0.46D, EYE_VERTICAL_OFFSET, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.XN.rotationDegrees(18.0F));
            items.renderStatic(
                    eye,
                    ItemDisplayContext.GROUND,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffers,
                    altar.getLevel(),
                    side
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderCap(
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation texture,
            int packedLight,
            float xRotation
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(xRotation));
        CAP_MODEL.renderClosedShell(
                poseStack,
                buffers.getBuffer(
                        EldritchRenderTypes.capTriangles(texture)
                ),
                packedLight
        );
        poseStack.popPose();
    }

    private static void renderObelisk(
            EldritchAltarPartBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        long gameTime = blockEntity.getLevel() == null
                ? 0L
                : blockEntity.getLevel().getGameTime();
        float ticks = gameTime + partialTick;
        float bob = Mth.sin(ticks / 10.0F) * 0.1F + 0.1F;

        poseStack.pushPose();
        poseStack.translate(0.5D, bob + 1.0D, 0.5D);
        renderVoidBacking(poseStack, buffers);
        renderSideField(ticks, poseStack, buffers);
        renderSides(
                poseStack,
                buffers.getBuffer(RenderType.entityTranslucent(SIDE)),
                packedLight
        );

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        CAP_MODEL.renderClosedShell(
                poseStack,
                buffers.getBuffer(
                        EldritchRenderTypes.capTriangles(CAP)
                ),
                packedLight
        );
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 3.0D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        CAP_MODEL.renderClosedShell(
                poseStack,
                buffers.getBuffer(
                        EldritchRenderTypes.capTriangles(CAP)
                ),
                packedLight
        );
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderSides(
            PoseStack poseStack,
            VertexConsumer vertices,
            int packedLight
    ) {
        PoseStack.Pose pose = poseStack.last();
        quad(pose, vertices, -.5F, 3, -.5F, .5F, 3, -.5F,
                .5F, 0, -.5F, -.5F, 0, -.5F, packedLight);
        quad(pose, vertices, .5F, 3, .5F, -.5F, 3, .5F,
                -.5F, 0, .5F, .5F, 0, .5F, packedLight);
        quad(pose, vertices, -.5F, 3, .5F, -.5F, 3, -.5F,
                -.5F, 0, -.5F, -.5F, 0, .5F, packedLight);
        quad(pose, vertices, .5F, 3, -.5F, .5F, 3, .5F,
                .5F, 0, .5F, .5F, 0, -.5F, packedLight);
    }

    private static void renderSideField(
            float ticks,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        Random random = new Random(31100L);
        for (int layer = 0; layer < 16; layer++) {
            float depth = 16.0F - layer;
            float shade = layer == 0 ? 0.1F : 1.0F / (depth + 1.0F);
            int red = layer == 0 ? (int) (shade * 255.0F) : (int) (
                    (random.nextFloat() * 0.5F + 0.1F) * shade * 255.0F
            );
            int green = layer == 0 ? (int) (shade * 255.0F) : (int) (
                    (random.nextFloat() * 0.5F + 0.4F) * shade * 255.0F
            );
            int blue = layer == 0 ? (int) (shade * 255.0F) : (int) (
                    (random.nextFloat() * 0.5F + 0.5F) * shade * 255.0F
            );
            float scale = layer == 0 ? 0.125F
                    : layer == 1 ? 0.5F : 0.0625F;
            float scroll = ticks / 2500.0F;
            float angle = (layer * layer * 4321 + layer * 9) * 2.0F;
            VertexConsumer vertices = buffers.getBuffer(
                    layer == 0
                            ? EldritchRenderTypes.tunnel()
                            : EldritchRenderTypes.field()
            );
            fieldBox(
                    poseStack.last(),
                    vertices,
                    red,
                    green,
                    blue,
                    scale,
                    scroll,
                    angle,
                    layer * 0.0002F
            );
        }
    }

    private static void renderVoidBacking(
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entitySolid(FIELD_BACKING)
        );
        PoseStack.Pose pose = poseStack.last();
        float half = 0.497F;
        darkQuad(pose, vertices, -.5F, 0, -half, .5F, 0, -half,
                .5F, 3, -half, -.5F, 3, -half);
        darkQuad(pose, vertices, .5F, 0, half, -.5F, 0, half,
                -.5F, 3, half, .5F, 3, half);
        darkQuad(pose, vertices, -half, 0, .5F, -half, 0, -.5F,
                -half, 3, -.5F, -half, 3, .5F);
        darkQuad(pose, vertices, half, 0, -.5F, half, 0, .5F,
                half, 3, .5F, half, 3, -.5F);
    }

    private static void fieldBox(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            int red,
            int green,
            int blue,
            float scale,
            float scroll,
            float angleDegrees,
            float offset
    ) {
        float angle = angleDegrees * Mth.DEG_TO_RAD;
        float cosine = Mth.cos(angle) * scale;
        float sine = Mth.sin(angle) * scale;
        float[] uv0 = uv(-.5F, 0, cosine, sine, scroll);
        float[] uv1 = uv(.5F, 0, cosine, sine, scroll);
        float[] uv2 = uv(.5F, 3, cosine, sine, scroll);
        float[] uv3 = uv(-.5F, 3, cosine, sine, scroll);
        /*
         * TC4 renders the field at +/-0.499, immediately inside the
         * translucent obelisk_side shell at +/-0.5.
         */
        float half = .499F - offset;
        fieldQuad(pose, vertices, -.5F, 0, -half, .5F, 0, -half,
                .5F, 3, -half, -.5F, 3, -half,
                uv0, uv1, uv2, uv3, red, green, blue);
        fieldQuad(pose, vertices, .5F, 0, half, -.5F, 0, half,
                -.5F, 3, half, .5F, 3, half,
                uv0, uv1, uv2, uv3, red, green, blue);
        fieldQuad(pose, vertices, -half, 0, .5F, -half, 0, -.5F,
                -half, 3, -.5F, -half, 3, .5F,
                uv0, uv1, uv2, uv3, red, green, blue);
        fieldQuad(pose, vertices, half, 0, -.5F, half, 0, .5F,
                half, 3, .5F, half, 3, -.5F,
                uv0, uv1, uv2, uv3, red, green, blue);
    }

    private static float[] uv(
            float x,
            float y,
            float cosine,
            float sine,
            float scroll
    ) {
        return new float[]{
                0.5F + x * cosine - y * sine,
                0.5F + x * sine + y * cosine + scroll
        };
    }

    private static void quad(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            int packedLight
    ) {
        regularVertex(pose, vertices, x0, y0, z0, 0, 0, packedLight);
        regularVertex(pose, vertices, x1, y1, z1, 1, 0, packedLight);
        regularVertex(pose, vertices, x2, y2, z2, 1, 1, packedLight);
        regularVertex(pose, vertices, x3, y3, z3, 0, 1, packedLight);
    }

    private static void regularVertex(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x, float y, float z,
            float u, float v,
            int packedLight
    ) {
        vertices.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
    }

    private static void darkQuad(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3
    ) {
        darkVertex(pose, vertices, x0, y0, z0, 0, 0);
        darkVertex(pose, vertices, x1, y1, z1, 1, 0);
        darkVertex(pose, vertices, x2, y2, z2, 1, 1);
        darkVertex(pose, vertices, x3, y3, z3, 0, 1);
    }

    private static void darkVertex(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x, float y, float z,
            float u, float v
    ) {
        vertices.vertex(pose.pose(), x, y, z)
                .color(48, 48, 56, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
    }

    private static void fieldQuad(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float[] uv0, float[] uv1, float[] uv2, float[] uv3,
            int red, int green, int blue
    ) {
        fieldVertex(pose, vertices, x0, y0, z0, uv0, red, green, blue);
        fieldVertex(pose, vertices, x1, y1, z1, uv1, red, green, blue);
        fieldVertex(pose, vertices, x2, y2, z2, uv2, red, green, blue);
        fieldVertex(pose, vertices, x3, y3, z3, uv3, red, green, blue);
    }

    private static void fieldVertex(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x, float y, float z,
            float[] uv,
            int red, int green, int blue
    ) {
        vertices.vertex(pose.pose(), x, y, z)
                .color(red, green, blue, 255)
                .uv(uv[0], uv[1])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                path
        );
    }

    @Override
    public boolean shouldRenderOffScreen(
            EldritchAltarPartBlockEntity blockEntity
    ) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
