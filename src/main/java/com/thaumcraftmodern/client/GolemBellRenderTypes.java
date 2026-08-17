package com.thaumcraftmodern.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Fixed-function equivalents of TC4's additive bell marker and script passes. */
final class GolemBellRenderTypes extends RenderStateShard {
    private static final TexturingStateShard REPEATING_SCRIPT =
            new TexturingStateShard(
                    "thaumic_reborn_repeating_golem_script",
                    () -> RenderSystem.texParameter(
                            GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_WRAP_S,
                            GL11.GL_REPEAT
                    ),
                    () -> RenderSystem.texParameter(
                            GL11.GL_TEXTURE_2D,
                            GL11.GL_TEXTURE_WRAP_S,
                            GL12.GL_CLAMP_TO_EDGE
                    )
            );

    private GolemBellRenderTypes() {
        super("thaumic_reborn_golem_bell_render_types", () -> { }, () -> { });
    }

    static RenderType overlay(String name, ResourceLocation texture) {
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":golem_bell_" + name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                512,
                false,
                true,
                state(texture)
        );
    }

    static RenderType link(ResourceLocation texture) {
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":golem_bell_link",
                // Modern POSITION_COLOR_TEX_SHADER consumes attributes in
                // this order. The fields are the same ones TC4 supplied;
                // only their packed order differs from 1.12's buffer format.
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.TRIANGLE_STRIP,
                4096,
                false,
                true,
                linkState(texture)
        );
    }

    private static RenderType.CompositeState linkState(
            ResourceLocation texture
    ) {
        return RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(texture, true, false))
                // TC4 used glBlendFunc(SRC_ALPHA, ONE). Minecraft's generic
                // ADDITIVE_TRANSPARENCY is ONE, ONE, which ignores the soft
                // alpha mask in mark.png/home.png and turns each glyph into
                // the washed-out solid discs seen in the broken render.
                .setTransparencyState(LIGHTNING_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE)
                // TC4 addressed script.png outside normalized U=0..1 and
                // relied on GL_REPEAT while subtracting time from U. Modern
                // direct textures default to edge clamping, which exposes the
                // full horizontal rune strip as the white comb artifact.
                .setTexturingState(REPEATING_SCRIPT)
                .createCompositeState(false);
    }

    private static RenderType.CompositeState state(ResourceLocation texture) {
        return stateBuilder(texture).createCompositeState(false);
    }

    private static RenderType.CompositeState.CompositeStateBuilder stateBuilder(
            ResourceLocation texture
    ) {
        return RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                // The original mark.png/home.png.mcmeta explicitly enables
                // blur. Preserve that linear filtering instead of forcing
                // nearest sampling while the 128/256 px glyphs are minified.
                .setTextureState(new TextureStateShard(texture, true, false))
                // LIGHTNING_TRANSPARENCY is Minecraft's SRC_ALPHA, ONE pass,
                // exactly matching RenderEventHandler#drawFaceOverlay in TC4.
                .setTransparencyState(LIGHTNING_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE);
    }
}
