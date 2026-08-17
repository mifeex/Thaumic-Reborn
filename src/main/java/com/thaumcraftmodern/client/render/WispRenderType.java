package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Additive, colour-only pass used by TC4's full-bright Wisp billboard.
 */
final class WispRenderType extends RenderStateShard {
    static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/misc/wisp.png"
    );

    private static final RenderType TYPE = RenderType.create(
            ThaumcraftModern.MOD_ID + ":wisp",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(
                            TEXTURE,
                            true,
                            false
                    ))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    private WispRenderType() {
        super("thaumic_reborn_wisp_render_type", () -> {
        }, () -> {
        });
    }

    static RenderType get() {
        return TYPE;
    }
}
