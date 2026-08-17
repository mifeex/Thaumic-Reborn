package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * TC4 bloom crystal pass: SRC_ALPHA/ONE, culling and depth writes enabled.
 */
final class EtherealBloomRenderType extends RenderStateShard {
    private static final ResourceLocation CRYSTAL = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/crystalcapacitor.png"
    );
    private static final RenderType CRYSTAL_TYPE = RenderType.create(
            ThaumcraftModern.MOD_ID + ":ethereal_bloom_crystal",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(
                            CRYSTAL,
                            false,
                            false
                    ))
                    /*
                     * Minecraft's LIGHTNING_TRANSPARENCY is the exact
                     * SRC_ALPHA/ONE blend used by the TC4 renderer.
                     * ADDITIVE_TRANSPARENCY is ONE/ONE and overexposes the
                     * already bright crystal texture.
                     */
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    private EtherealBloomRenderType() {
        super("thaumic_reborn_ethereal_bloom_render_type", () -> {
        }, () -> {
        });
    }

    static RenderType crystal() {
        return CRYSTAL_TYPE;
    }
}
