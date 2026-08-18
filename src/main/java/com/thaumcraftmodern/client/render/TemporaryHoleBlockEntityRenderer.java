package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.TemporaryHoleBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Random;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** TC4 portable-hole tunnel skin rendered on the solid walls of the passage. */
public final class TemporaryHoleBlockEntityRenderer
        implements BlockEntityRenderer<TemporaryHoleBlockEntity> {
    private static final long FIELD_COLOR_SEED = 31100L;
    public TemporaryHoleBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            TemporaryHoleBlockEntity hole,
            float partialTick,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (hole.getLevel() == null) return;
        float time = hole.getLevel().getGameTime() + partialTick;
        for (Direction towardWall : Direction.values()) {
            BlockPos adjacentPos = hole.getBlockPos().relative(towardWall);
            BlockState adjacent = hole.getLevel().getBlockState(adjacentPos);
            if (adjacent.is(ModBlocks.TEMPORARY_HOLE.get())
                    || !adjacent.isCollisionShapeFullBlock(
                            hole.getLevel(),
                            adjacentPos
                    )) {
                continue;
            }
            drawFieldFace(
                    poses,
                    buffers,
                    towardWall,
                    time,
                    hole.getBlockPos()
            );
        }
    }

    static void drawFieldFace(
            PoseStack poses,
            MultiBufferSource buffers,
            Direction wall,
            float time,
            BlockPos position
    ) {
        VertexConsumer tunnel = buffers.getBuffer(
                TemporaryHoleRenderTypes.tunnel()
        );
        Matrix4f matrix = poses.last().pose();
        Matrix3f normal = poses.last().normal();
        Point[] face = face(wall);
        TextureOrigin origin = textureOrigin(position, wall);
        emitDoubleSided(tunnel, matrix, normal, face, wall.getOpposite(),
                origin.u * 0.5F, origin.v * 0.5F, 0.5F,
                0.10F, 0.10F, 0.10F, 1.0F);
        VertexConsumer field = buffers.getBuffer(
                TemporaryHoleRenderTypes.stars()
        );
        Random colors = new Random(FIELD_COLOR_SEED);
        float scroll = time * 0.0002F;
        for (int layer = 1; layer < 16; layer++) {
            float depth = 16.0F - layer;
            float scale = layer == 1 ? 2.0F : 0.25F;
            float shade = 1.0F / (depth + 1.0F);
            float red = (colors.nextFloat() * 0.5F + 0.1F) * shade;
            float green = (colors.nextFloat() * 0.5F + 0.4F) * shade;
            float blue = (colors.nextFloat() * 0.5F + 0.5F) * shade;
            float offset = layer * layer * 0.071F;
            emitDoubleSided(
                    field,
                    matrix,
                    normal,
                    face,
                    wall.getOpposite(),
                    origin.u * scale + offset,
                    origin.v * scale + scroll - offset * 0.37F,
                    scale,
                    red,
                    green,
                    blue,
                    1.0F
            );
        }
    }

    private static void emitDoubleSided(
            VertexConsumer out,
            Matrix4f matrix,
            Matrix3f normal,
            Point[] points,
            Direction facing,
            float u,
            float v,
            float scale,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        vertex(out, matrix, normal, points[0], u, v, facing,
                red, green, blue, alpha);
        vertex(out, matrix, normal, points[1], u + scale, v, facing,
                red, green, blue, alpha);
        vertex(out, matrix, normal, points[2], u + scale, v + scale, facing,
                red, green, blue, alpha);
        vertex(out, matrix, normal, points[3], u, v + scale, facing,
                red, green, blue, alpha);
        Direction reverse = facing.getOpposite();
        vertex(out, matrix, normal, points[3], u, v + scale, reverse,
                red, green, blue, alpha);
        vertex(out, matrix, normal, points[2], u + scale, v + scale, reverse,
                red, green, blue, alpha);
        vertex(out, matrix, normal, points[1], u + scale, v, reverse,
                red, green, blue, alpha);
        vertex(out, matrix, normal, points[0], u, v, reverse,
                red, green, blue, alpha);
    }

    private static void vertex(
            VertexConsumer out,
            Matrix4f matrix,
            Matrix3f normal,
            Point point,
            float u,
            float v,
            Direction facing,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        out.vertex(matrix, point.x, point.y, point.z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, facing.getStepX(), facing.getStepY(),
                        facing.getStepZ())
                .endVertex();
    }

    private static Point[] face(Direction direction) {
        float near = 0.001F;
        float far = 0.999F;
        return switch (direction) {
            case DOWN -> new Point[]{p(0, near, 1), p(1, near, 1),
                    p(1, near, 0), p(0, near, 0)};
            case UP -> new Point[]{p(0, far, 0), p(1, far, 0),
                    p(1, far, 1), p(0, far, 1)};
            case NORTH -> new Point[]{p(1, 0, near), p(0, 0, near),
                    p(0, 1, near), p(1, 1, near)};
            case SOUTH -> new Point[]{p(0, 0, far), p(1, 0, far),
                    p(1, 1, far), p(0, 1, far)};
            case WEST -> new Point[]{p(near, 0, 0), p(near, 0, 1),
                    p(near, 1, 1), p(near, 1, 0)};
            case EAST -> new Point[]{p(far, 0, 1), p(far, 0, 0),
                    p(far, 1, 0), p(far, 1, 1)};
        };
    }

    private static Point p(float x, float y, float z) {
        return new Point(x, y, z);
    }

    private static TextureOrigin textureOrigin(
            BlockPos position,
            Direction face
    ) {
        return switch (face.getAxis()) {
            case Y -> new TextureOrigin(position.getX(), position.getZ());
            case Z -> new TextureOrigin(position.getX(), position.getY());
            case X -> new TextureOrigin(position.getZ(), position.getY());
        };
    }

    private record Point(float x, float y, float z) {
    }

    private record TextureOrigin(float u, float v) {
    }
}
