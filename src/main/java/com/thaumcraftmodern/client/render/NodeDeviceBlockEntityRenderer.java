package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.visnet.NodeStabilizerBlockEntity;
import com.thaumcraftmodern.visnet.NodeTransducerBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Random;

public final class NodeDeviceBlockEntityRenderer<T extends BlockEntity>
        implements BlockEntityRenderer<T> {
    private static final ResourceLocation MESH = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/node_stabilizer.obj"
    );
    private static final ResourceLocation STABILIZER = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/node_stabilizer.png"
    );
    private static final ResourceLocation STABILIZER_OVER = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/node_stabilizer_over.png"
    );
    private static final ResourceLocation CONVERTER = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/node_converter.png"
    );
    private static final ResourceLocation CONVERTER_OVER = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/node_converter_over.png"
    );
    private static final ResourceLocation NODE_BUBBLE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/node_bubble.png"
    );
    private static final ResourceLocation BOLT_LARGE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/p_large.png"
    );
    private static final ResourceLocation BOLT_SMALL = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/p_small.png"
    );
    public NodeDeviceBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            T tile,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (tile instanceof NodeStabilizerBlockEntity stabilizer) {
            renderStabilizer(stabilizer, partialTick, pose, buffers, packedLight);
        } else if (tile instanceof NodeTransducerBlockEntity transducer) {
            renderTransducer(transducer, partialTick, pose, buffers, packedLight);
        }
    }

    private void renderStabilizer(
            NodeStabilizerBlockEntity tile,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        float ticks = Minecraft.getInstance().player == null
                ? partialTick
                : Minecraft.getInstance().player.tickCount + partialTick;
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.XN.rotationDegrees(90.0F));
        render("lock", STABILIZER, pose, buffers, packedLight, 1, 1, 1);
        for (int index = 0; index < 4; index++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(index * 90.0F));
            pose.mulPose(Axis.YP.rotationDegrees(45.0F));
            pose.translate(0.0D, 0.0D, tile.count() / 100.0D);
            render("piston", STABILIZER, pose, buffers, packedLight, 1, 1, 1);
            float red = 1.0F;
            float green = tile.advanced() ? 0.2F : 1.0F;
            float blue = tile.advanced() ? 0.2F : 1.0F;
            float pulse = Mth.sin((ticks + index * 5.0F) / 3.0F)
                    * 0.1F + 0.9F;
            int glow = 50 + (int) (170.0F
                    * (tile.count() / 37.0F * pulse));
            render("piston", STABILIZER_OVER, pose, buffers,
                    legacyLight(glow), red, green, blue);
            pose.popPose();
        }
        pose.popPose();
        if (tile.count() > 0) {
            renderBubble(tile, partialTick, pose, buffers);
        }
    }

    private static void renderBubble(
            NodeStabilizerBlockEntity tile,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers
    ) {
        float ticks = Minecraft.getInstance().player == null
                ? partialTick
                : Minecraft.getInstance().player.tickCount + partialTick;
        float alpha = Mth.clamp(tile.count() / 37.0F
                * (Mth.sin(ticks / 8.0F) * 0.1F + 0.5F), 0, 1);
        float green = tile.advanced() ? 0.267F : 1.0F;
        float blue = tile.advanced() ? 0.267F : 1.0F;
        pose.pushPose();
        pose.translate(0.5D, 1.5D, 0.5D);
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera()
                .rotation());
        VertexConsumer out = buffers.getBuffer(
                RenderType.entityTranslucentEmissive(NODE_BUBBLE));
        var matrix = pose.last();
        bubbleVertex(out, matrix, -0.9F, -0.9F, 0, 1,
                green, blue, alpha);
        bubbleVertex(out, matrix, 0.9F, -0.9F, 1, 1,
                green, blue, alpha);
        bubbleVertex(out, matrix, 0.9F, 0.9F, 1, 0,
                green, blue, alpha);
        bubbleVertex(out, matrix, -0.9F, 0.9F, 0, 0,
                green, blue, alpha);
        pose.popPose();
    }

    /**
     * TC4 TileNodeConverter.spawnConverterBolts: one converter-to-node bolt
     * each client tick and, half the time, one stabilizer-to-node bolt. Four
     * retained tick layers reproduce the original FXLightningBolt lifetime.
     */
    private static void renderConverterBolts(
            NodeTransducerBlockEntity tile,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers
    ) {
        long now = tile.getLevel().getGameTime();
        var look = Minecraft.getInstance().gameRenderer.getMainCamera()
                .getLookVector();
        Vec3 cameraLook = new Vec3(look.x(), look.y(), look.z());
        for (int age = 0; age < 4; age++) {
            long created = now - age;
            Random random = new Random(tile.getBlockPos().asLong()
                    ^ created * 0x9E3779B97F4A7C15L);
            Vec3 target = new Vec3(0.5D, -0.5D, 0.5D);
            Vec3 converter = new Vec3(
                    0.25D + random.nextFloat() * 0.5D,
                    0.5D,
                    0.25D + random.nextFloat() * 0.5D
            );
            drawBolt(converter, target, random.nextLong(), age,
                    partialTick, cameraLook, pose, buffers);
            if (random.nextBoolean()
                    && tile.getLevel().getBlockEntity(
                    tile.getBlockPos().below(2))
                    instanceof NodeStabilizerBlockEntity) {
                Vec3 stabilizer = new Vec3(
                        0.25D + random.nextFloat() * 0.5D,
                        -1.5D,
                        0.25D + random.nextFloat() * 0.5D
                );
                drawBolt(stabilizer, target, random.nextLong(), age,
                        partialTick, cameraLook, pose, buffers);
            }
        }
    }

    private static void drawBolt(
            Vec3 start,
            Vec3 end,
            long seed,
            int age,
            float partialTick,
            Vec3 cameraLook,
            PoseStack pose,
            MultiBufferSource buffers
    ) {
        // TC4 ClientProxy.bolt uses an eight-segment, four-tick
        // FXLightningBolt. Its path is re-jittered as the particle ages.
        Vec3[] points = new Vec3[9];
        Vec3 delta = end.subtract(start);
        Random random = new Random(seed + age * 31L);
        points[0] = start;
        points[8] = end;
        for (int index = 1; index < 8; index++) {
            double progress = index / 8.0D;
            double envelope = 1.0D
                    - Math.abs(0.5D - progress) * 2.0D;
            points[index] = start.add(delta.scale(progress)).add(
                    new Vec3(
                            random.nextDouble() - 0.5D,
                            random.nextDouble() - 0.5D,
                            random.nextDouble() - 0.5D
                    ).scale(0.24D * envelope)
            );
        }
        float ageFraction = (age + partialTick) / 4.0F;
        float fade = Math.max(0.05F, (1.0F - ageFraction) * 0.5F);
        drawBoltPass(buffers.getBuffer(
                        ClassicBoltRenderTypes.bolt(BOLT_LARGE, false)),
                pose.last(), points, cameraLook, 0.0375F, fade,
                1.0F, 1.0F, 1.0F);
        drawBoltPass(buffers.getBuffer(
                        ClassicBoltRenderTypes.bolt(BOLT_SMALL, true)),
                pose.last(), points, cameraLook, 0.03F, fade,
                0.4F, 0.8F, 1.0F);
    }

    private static void drawBoltPass(
            VertexConsumer out,
            PoseStack.Pose pose,
            Vec3[] points,
            Vec3 cameraLook,
            float width,
            float alpha,
            float red,
            float green,
            float blue
    ) {
        Vec3[] offsets = new Vec3[points.length];
        for (int index = 0; index < points.length; index++) {
            Vec3 tangent;
            if (index == 0) {
                tangent = points[1].subtract(points[0]);
            } else if (index == points.length - 1) {
                tangent = points[index].subtract(points[index - 1]);
            } else {
                tangent = points[index + 1].subtract(points[index - 1]);
            }
            Vec3 side = tangent.cross(cameraLook);
            if (side.lengthSqr() < 1.0E-8D) {
                side = tangent.cross(new Vec3(0, 1, 0));
            }
            if (side.lengthSqr() < 1.0E-8D) {
                side = new Vec3(1, 0, 0);
            }
            offsets[index] = side.normalize().scale(width);
        }

        /*
         * TC4 samples the solid centre column of p_large/p_small along every
         * internal segment. Mapping U=0..1 separately per segment samples the
         * transparent rounded ends eight times and turns the bolt into a row
         * of disconnected shards. Reuse one offset at each shared point and
         * keep U=0.5 through the body, reserving U=0 for the two end caps.
         */
        for (int index = 0; index < points.length - 1; index++) {
            Vec3 start = points[index];
            Vec3 end = points[index + 1];
            Vec3 startOffset = offsets[index];
            Vec3 endOffset = offsets[index + 1];
            boltVertex(out, pose.pose(), pose.normal(),
                    end.subtract(endOffset), 0.5F, 0,
                    alpha, red, green, blue);
            boltVertex(out, pose.pose(), pose.normal(),
                    start.subtract(startOffset), 0.5F, 0,
                    alpha, red, green, blue);
            boltVertex(out, pose.pose(), pose.normal(),
                    start.add(startOffset), 0.5F, 1,
                    alpha, red, green, blue);
            boltVertex(out, pose.pose(), pose.normal(),
                    end.add(endOffset), 0.5F, 1,
                    alpha, red, green, blue);
        }

        Vec3 startDirection = points[1].subtract(points[0]).normalize();
        Vec3 startTip = points[0].subtract(startDirection.scale(width));
        boltVertex(out, pose.pose(), pose.normal(),
                points[0].subtract(offsets[0]), 0.5F, 0,
                alpha, red, green, blue);
        boltVertex(out, pose.pose(), pose.normal(),
                startTip.subtract(offsets[0]), 0, 0,
                alpha, red, green, blue);
        boltVertex(out, pose.pose(), pose.normal(),
                startTip.add(offsets[0]), 0, 1,
                alpha, red, green, blue);
        boltVertex(out, pose.pose(), pose.normal(),
                points[0].add(offsets[0]), 0.5F, 1,
                alpha, red, green, blue);

        int last = points.length - 1;
        Vec3 endDirection = points[last].subtract(points[last - 1]).normalize();
        Vec3 endTip = points[last].add(endDirection.scale(width));
        boltVertex(out, pose.pose(), pose.normal(),
                endTip.subtract(offsets[last]), 0, 0,
                alpha, red, green, blue);
        boltVertex(out, pose.pose(), pose.normal(),
                points[last].subtract(offsets[last]), 0.5F, 0,
                alpha, red, green, blue);
        boltVertex(out, pose.pose(), pose.normal(),
                points[last].add(offsets[last]), 0.5F, 1,
                alpha, red, green, blue);
        boltVertex(out, pose.pose(), pose.normal(),
                endTip.add(offsets[last]), 0, 1,
                alpha, red, green, blue);
    }

    private static void boltVertex(
            VertexConsumer out,
            Matrix4f pose,
            Matrix3f normal,
            Vec3 point,
            float u,
            float v,
            float alpha,
            float red,
            float green,
            float blue
    ) {
        out.vertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 1, 0)
                .endVertex();
    }

    private static void bubbleVertex(
            VertexConsumer out,
            PoseStack.Pose pose,
            float x,
            float y,
            float u,
            float v,
            float green,
            float blue,
            float alpha
    ) {
        out.vertex(pose.pose(), x, y, 0)
                .color(1, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 0, 1)
                .endVertex();
    }

    private void renderTransducer(
            NodeTransducerBlockEntity tile,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        float progress = Math.min(50.0F, tile.count()) / 137.0F;
        float ticks = Minecraft.getInstance().player == null
                ? partialTick
                : Minecraft.getInstance().player.tickCount + partialTick;
        float[] color = switch (tile.status()) {
            case 2 -> new float[]{1.0F, 0.0F, 0.3F};
            case 1 -> new float[]{1.0F, 0.6F, 0.1F};
            default -> new float[]{0.5F, 1.0F, 0.5F};
        };
        pose.pushPose();
        pose.translate(0.5D, 1.0D, 0.5D);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        render("lock", CONVERTER, pose, buffers, packedLight, 1, 1, 1);
        render("lock", CONVERTER_OVER, pose, buffers,
                glow(progress, ticks), color[0], color[1], color[2]);
        for (int index = 0; index < 4; index++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(index * 90.0F));
            pose.mulPose(Axis.YP.rotationDegrees(45.0F));
            pose.translate(0.0D, 0.0D, progress);
            render("piston", CONVERTER, pose, buffers, packedLight, 1, 1, 1);
            render("piston", CONVERTER_OVER, pose, buffers,
                    glow(progress, ticks + index * 5.0F),
                    color[0], color[1], color[2]);
            pose.popPose();
        }
        pose.popPose();
        if (tile.count() > 50 && tile.getLevel() != null) {
            renderConverterBolts(tile, partialTick, pose, buffers);
        }
    }

    private static int glow(float progress, float ticks) {
        float pulse = Mth.sin(ticks / 3.0F) * 0.1F + 0.9F;
        int light = 50 + (int) (170.0F * progress * 2.5F * pulse);
        return legacyLight(light);
    }

    private static int legacyLight(int rawLightmapCoordinate) {
        return LightTexture.pack(
                Mth.clamp(rawLightmapCoordinate / 16, 0, 15),
                0
        );
    }

    private void render(
            String group,
            ResourceLocation texture,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            float red,
            float green,
            float blue
    ) {
        VertexConsumer consumer = buffers.getBuffer(
                RenderType.entityCutoutNoCull(texture));
        LegacyObjMesh.get(MESH).render(
                group,
                pose,
                consumer,
                light,
                red,
                green,
                blue,
                1.0F
        );
    }

    @Override
    public boolean shouldRenderOffScreen(T tile) {
        // Bubble and converter bolts extend outside the one-block BE bounds.
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
