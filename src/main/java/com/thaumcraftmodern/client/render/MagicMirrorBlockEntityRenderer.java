package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.particle.EssentiaMirrorVisualEffects;
import com.thaumcraftmodern.mirror.LinkedMirrorBlockEntity;
import com.thaumcraftmodern.world.block.MagicMirrorBlock;
import com.thaumcraftmodern.world.block.entity.EssentiaMirrorBlockEntity;
import com.thaumcraftmodern.world.block.entity.MagicMirrorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Original pane/trans-pane shell plus the linked tunnel and particle field. */
public final class MagicMirrorBlockEntityRenderer<T extends LinkedMirrorBlockEntity>
        implements BlockEntityRenderer<T> {
    private static final ResourceLocation PANE = texture("block/mirrorpane.png");
    private static final ResourceLocation PANE_OPEN =
            texture("block/mirrorpanetrans.png");
    private static final ResourceLocation FRAME = texture("block/mirrorframe.png");
    private static final ResourceLocation FRAME_ESSENTIA =
            texture("block/mirrorframe2.png");
    private static final ResourceLocation TUNNEL = texture("misc/tunnel.png");
    private static final ResourceLocation PARTICLES = texture("misc/particlefield.png");
    public MagicMirrorBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(T mirror, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (mirror instanceof EssentiaMirrorBlockEntity essentiaMirror) {
            EssentiaMirrorVisualEffects.tick(essentiaMirror);
        }
        Direction facing = mirror.getBlockState().getValue(MagicMirrorBlock.FACING);
        float time = mirror.getLevel() == null ? partialTick
                : mirror.getLevel().getGameTime() + partialTick;
        boolean open = mirror instanceof MagicMirrorBlockEntity itemMirror
                ? itemMirror.visuallyOpen() : mirror.linked();
        boolean rotateInner = !(mirror instanceof EssentiaMirrorBlockEntity);
        if (open) {
            drawOpenSurface(buffers, pose, facing, TUNNEL, 0.001F,
                    -time * 0.01F, time * 0.008F, 2.0F, 0.45F, 0xF000F0,
                    rotateInner);
            drawOpenSurface(buffers, pose, facing, PARTICLES, 0.002F,
                    time * 0.006F, -time * 0.012F, 1.0F, 0.48F, 0xF000F0,
                    rotateInner);
            // TC4's transparent open pane leaves the centre free for the
            // animated tunnel and particle field.
            if (rotateInner) {
                drawRotated90(buffers, pose, facing, PANE_OPEN, 0.003F,
                        packedLight);
            } else {
                draw(buffers, pose, facing, PANE_OPEN, 0.003F,
                        0.0F, 0.0F, 1.0F, 1.0F, packedLight);
            }
        } else {
            if (rotateInner) {
                drawRotated90(buffers, pose, facing, PANE, 0.003F,
                        packedLight);
            } else {
                draw(buffers, pose, facing, PANE, 0.003F,
                        0.0F, 0.0F, 1.0F, 1.0F, packedLight);
            }
        }
        // The legacy frame is one complete transparent 16x16 sprite. It must be
        // drawn across the whole face; splitting it over four bars destroys the
        // gold ornament and was the reason only the blue pane was visible.
        drawFrame(buffers, pose, facing,
                mirror instanceof EssentiaMirrorBlockEntity
                        ? FRAME_ESSENTIA : FRAME,
                packedLight, !(mirror instanceof EssentiaMirrorBlockEntity));
    }

    private static void draw(MultiBufferSource buffers, PoseStack pose,
            Direction facing, ResourceLocation texture, float outwardOffset,
            float uScroll, float vScroll, float uvScale, float alpha, int light) {
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(texture));
        // Legacy mirrorpane is already transparent around its oval. It is
        // rendered over the full block face; applying INSET here shrinks it a
        // second time and makes the white mirror much too small.
        Plane plane = Plane.forFacing(facing, outwardOffset, 0.0F);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        vertex(vertices, matrix, normal, plane.a, uScroll, vScroll, facing, alpha, light);
        vertex(vertices, matrix, normal, plane.b, uScroll + uvScale, vScroll, facing, alpha, light);
        vertex(vertices, matrix, normal, plane.c, uScroll + uvScale, vScroll + uvScale, facing, alpha, light);
        vertex(vertices, matrix, normal, plane.d, uScroll, vScroll + uvScale, facing, alpha, light);
    }

    private static void vertex(VertexConsumer out, Matrix4f pose, Matrix3f normal,
            Point point, float u, float v, Direction facing, float alpha, int light) {
        out.vertex(pose, point.x, point.y, point.z)
                .color(1, 1, 1, alpha).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, facing.getStepX(), facing.getStepY(), facing.getStepZ())
                .endVertex();
    }

    private static void drawRotated90(MultiBufferSource buffers, PoseStack pose,
            Direction facing, ResourceLocation texture, float outwardOffset,
            int light) {
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityTranslucent(texture));
        Plane plane = Plane.forFacing(facing, outwardOffset, 0.0F);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        vertex(vertices, matrix, normal, plane.a, 0, 1, facing, 1.0F, light);
        vertex(vertices, matrix, normal, plane.b, 0, 0, facing, 1.0F, light);
        vertex(vertices, matrix, normal, plane.c, 1, 0, facing, 1.0F, light);
        vertex(vertices, matrix, normal, plane.d, 1, 1, facing, 1.0F, light);
    }

    /**
     * Clips the moving portal field to the opaque footprint of the legacy
     * mirror pane. The old renderer achieved the same silhouette through its
     * generated layered face; rendering a single textured quad exposed an
     * obviously wrong purple square around the oval mirror.
     */
    private static void drawOpenSurface(MultiBufferSource buffers, PoseStack pose,
            Direction facing, ResourceLocation texture, float outwardOffset,
            float uScroll, float vScroll, float uvScale, float alpha, int light,
            boolean rotate90) {
        // Transparent inner opening of mirrorpanetrans, expressed as half-open
        // 16px spans. The surrounding larger oval remains the blue mirror pane.
        int[][] spans = {
                {7, 9}, {6, 10}, {5, 11}, {5, 11},
                {5, 11}, {5, 11}, {6, 10}, {7, 9}
        };
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        for (int row = 0; row < spans.length; row++) {
            float minA = spans[row][0] / 16.0F;
            float maxA = spans[row][1] / 16.0F;
            float minB = (4 + row) / 16.0F;
            float maxB = (5 + row) / 16.0F;
            if (rotate90) {
                float oldMinA = minA;
                float oldMaxA = maxA;
                minA = 1.0F - maxB;
                maxA = 1.0F - minB;
                minB = oldMinA;
                maxB = oldMaxA;
            }
            Plane plane = Plane.forFacingRect(facing, outwardOffset,
                    minA, maxA, minB, maxB);
            float u0 = uScroll + minA * uvScale;
            float u1 = uScroll + maxA * uvScale;
            float v0 = vScroll + minB * uvScale;
            float v1 = vScroll + maxB * uvScale;
            vertex(vertices, matrix, normal, plane.a, u0, v0, facing, alpha, light);
            vertex(vertices, matrix, normal, plane.b, u1, v0, facing, alpha, light);
            vertex(vertices, matrix, normal, plane.c, u1, v1, facing, alpha, light);
            vertex(vertices, matrix, normal, plane.d, u0, v1, facing, alpha, light);
        }
    }

    private static void drawFrame(MultiBufferSource buffers, PoseStack pose,
            Direction facing, ResourceLocation texture, int light,
            boolean rotate90) {
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityCutoutNoCull(texture));
        Plane plane = Plane.forFacing(facing, 0.006F, 0.0F);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        if (rotate90) {
            vertex(vertices, matrix, normal, plane.a, 0, 1, facing, 1.0F, light);
            vertex(vertices, matrix, normal, plane.b, 0, 0, facing, 1.0F, light);
            vertex(vertices, matrix, normal, plane.c, 1, 0, facing, 1.0F, light);
            vertex(vertices, matrix, normal, plane.d, 1, 1, facing, 1.0F, light);
        } else {
            // The original essentia frame is already vertical in its PNG.
            vertex(vertices, matrix, normal, plane.a, 0, 0, facing, 1.0F, light);
            vertex(vertices, matrix, normal, plane.b, 1, 0, facing, 1.0F, light);
            vertex(vertices, matrix, normal, plane.c, 1, 1, facing, 1.0F, light);
            vertex(vertices, matrix, normal, plane.d, 0, 1, facing, 1.0F, light);
        }
    }

    private record Point(float x, float y, float z) { }
    private record Plane(Point a, Point b, Point c, Point d) {
        static Plane forFacing(Direction face, float offset, float inset) {
            float lo = inset, hi = 1 - inset;
            return forFacingRect(face, offset, lo, hi, lo, hi);
        }
        static Plane forFacingRect(Direction face, float offset,
                float minA, float maxA, float minB, float maxB) {
            return switch (face) {
                case SOUTH -> xyPositive(minA, maxA, minB, maxB, 0 + offset);
                case NORTH -> xyNegative(minA, maxA, minB, maxB, 1 - offset);
                case EAST -> yz(minA, maxA, minB, maxB, 0 + offset);
                case WEST -> yzReverse(minA, maxA, minB, maxB, 1 - offset);
                case UP -> xzPositive(minA, maxA, minB, maxB, 0 + offset);
                case DOWN -> xzNegative(minA, maxA, minB, maxB, 1 - offset);
            };
        }
        private static Plane xyNegative(float minX, float maxX,
                float minY, float maxY, float z) {
            return new Plane(new Point(minX, maxY, z), new Point(maxX, maxY, z),
                    new Point(maxX, minY, z), new Point(minX, minY, z));
        }
        private static Plane xyPositive(float minX, float maxX,
                float minY, float maxY, float z) {
            return new Plane(new Point(minX, minY, z), new Point(maxX, minY, z),
                    new Point(maxX, maxY, z), new Point(minX, maxY, z));
        }
        private static Plane yz(float minZ, float maxZ,
                float minY, float maxY, float x) {
            return new Plane(new Point(x, maxY, minZ), new Point(x, maxY, maxZ),
                    new Point(x, minY, maxZ), new Point(x, minY, minZ));
        }
        private static Plane yzReverse(float minZ, float maxZ,
                float minY, float maxY, float x) {
            return new Plane(new Point(x, maxY, maxZ), new Point(x, maxY, minZ),
                    new Point(x, minY, minZ), new Point(x, minY, maxZ));
        }
        private static Plane xzNegative(float minX, float maxX,
                float minZ, float maxZ, float y) {
            return new Plane(new Point(minX, y, minZ), new Point(maxX, y, minZ),
                    new Point(maxX, y, maxZ), new Point(minX, y, maxZ));
        }
        private static Plane xzPositive(float minX, float maxX,
                float minZ, float maxZ, float y) {
            return new Plane(new Point(minX, y, minZ), new Point(minX, y, maxZ),
                    new Point(maxX, y, maxZ), new Point(maxX, y, minZ));
        }
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(ThaumcraftModern.MOD_ID,
                "textures/" + path);
    }
}
