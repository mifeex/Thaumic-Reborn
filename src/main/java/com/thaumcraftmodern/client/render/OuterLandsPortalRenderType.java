package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Translucent, colour-only pass used by TC4's Eldritch portal billboard. */
final class OuterLandsPortalRenderType extends RenderStateShard {
    static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/block/outer_lands_portal.png"
    );

    private static final RenderType TYPE = RenderType.create(
            ThaumcraftModern.MOD_ID + ":outer_lands_portal",
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
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    private OuterLandsPortalRenderType() {
        super("thaumcraftmodern_outer_lands_portal_render_type", () -> {
        }, () -> {
        });
    }

    static RenderType get() {
        return TYPE;
    }
}
