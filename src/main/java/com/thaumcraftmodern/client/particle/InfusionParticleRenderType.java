package com.thaumcraftmodern.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

/** TC4 ParticleEngine layer one, backed by the untouched 16x16 sprite sheet. */
final class InfusionParticleRenderType implements ParticleRenderType {
    static final InfusionParticleRenderType INSTANCE =
            new InfusionParticleRenderType();
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ThaumcraftModern.MOD_ID,
                    "textures/misc/particles.png");

    private InfusionParticleRenderType() {
    }

    @Override
    public void begin(BufferBuilder buffer, TextureManager textures) {
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE
        );
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public void end(Tesselator tesselator) {
        tesselator.end();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    @Override
    public String toString() {
        return "THAUMCRAFTMODERN_INFUSION_TC4_SHEET";
    }
}
