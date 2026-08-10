package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Original TC4 paper jar label and its aspect glyph. */
public final class ClassicJarLabelRenderer {
    private static final ResourceLocation PAPER = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/label.png");
    private static final float CENTER_Y = 0.41F;
    private static final float SURFACE_OFFSET = 0.315F;
    private static final float ASPECT_HALF_SIZE = 8.0F * 0.021F;
    private static final int ASPECT_GRAY = 0x808080;

    private ClassicJarLabelRenderer() { }

    public static void render(String aspectId, Direction facing,
            PoseStack poses, MultiBufferSource buffers, int packedLight,
            int packedOverlay) {
        AspectDefinition aspect = AspectRegistryRuntime.find(aspectId).orElse(null);
        if (aspect == null || !facing.getAxis().isHorizontal()) return;
        ResourceLocation icon = ResourceLocation.tryParse(aspect.icon());
        if (icon == null) return;
        icon = labelIcon(icon);
        draw(facing, 0.25F, SURFACE_OFFSET, PAPER, 0xFFFFFF,
                poses, buffers, packedLight, packedOverlay);
        draw(facing, ASPECT_HALF_SIZE, SURFACE_OFFSET + 0.002F, icon,
                ASPECT_GRAY, poses, buffers, packedLight, packedOverlay);
    }

    private static ResourceLocation labelIcon(ResourceLocation icon) {
        String path = icon.getPath();
        if (ThaumcraftModern.MOD_ID.equals(icon.getNamespace())
                && path.startsWith("textures/aspects/")) {
            return new ResourceLocation(icon.getNamespace(),
                    "textures/aspects_label/"
                            + path.substring("textures/aspects/".length()));
        }
        return icon;
    }

    private static void draw(Direction facing, float halfSize, float offset,
            ResourceLocation texture, int color, PoseStack poses,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        float cx = 0.5F + facing.getStepX() * offset;
        float cz = 0.5F + facing.getStepZ() * offset;
        float minY = CENTER_Y - halfSize;
        float maxY = CENTER_Y + halfSize;
        float rx = -facing.getStepZ() * halfSize;
        float rz = facing.getStepX() * halfSize;
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        PoseStack.Pose pose = poses.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        vertex(out, matrix, normal, cx - rx, minY, cz - rz, 0, 1,
                red, green, blue, facing, packedLight, packedOverlay);
        vertex(out, matrix, normal, cx + rx, minY, cz + rz, 1, 1,
                red, green, blue, facing, packedLight, packedOverlay);
        vertex(out, matrix, normal, cx + rx, maxY, cz + rz, 1, 0,
                red, green, blue, facing, packedLight, packedOverlay);
        vertex(out, matrix, normal, cx - rx, maxY, cz - rz, 0, 0,
                red, green, blue, facing, packedLight, packedOverlay);
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix,
            Matrix3f normal, float x, float y, float z, float u, float v,
            int red, int green, int blue, Direction facing, int packedLight,
            int packedOverlay) {
        out.vertex(matrix, x, y, z).color(red, green, blue, 255).uv(u, v)
                .overlayCoords(packedOverlay).uv2(packedLight)
                .normal(normal, facing.getStepX(), 0, facing.getStepZ())
                .endVertex();
    }
}
