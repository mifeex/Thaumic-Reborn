package com.thaumcraftmodern.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class GolemBellVertexFormatTest {
    @Test
    void scriptRibbonPassesEveryShaderAttribute() {
        BufferBuilder builder = new BufferBuilder(128);
        builder.begin(
                VertexFormat.Mode.TRIANGLE_STRIP,
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        GolemBellVertexWriter.write(
                builder,
                new Matrix4f(),
                new Vec3(1.0D, 2.0D, 3.0D),
                0.25F,
                0.75F,
                0x8A41D6,
                192
        );
        BufferBuilder.RenderedBuffer rendered = builder.end();
        try {
            assertFalse(rendered.isEmpty());
        } finally {
            rendered.release();
        }
    }
}
