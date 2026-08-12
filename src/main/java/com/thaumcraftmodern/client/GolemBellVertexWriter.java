package com.thaumcraftmodern.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Writes the TC4 script-ribbon fields in the order required by the modern shader. */
final class GolemBellVertexWriter {
    private GolemBellVertexWriter() {
    }

    static void write(VertexConsumer out, Matrix4f matrix, Vec3 point,
            float u, float v, int rgb, int alpha) {
        out.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, alpha)
                .uv(u, v)
                .endVertex();
    }
}
