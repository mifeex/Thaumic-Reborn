package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Blend modes used by TC4's layered portable-hole field. */
final class TemporaryHoleRenderTypes extends RenderStateShard {
    private static final ResourceLocation TUNNEL_TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/tunnel.png"
    );
    private static final ResourceLocation STAR_TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/particlefield.png"
    );
    private static final TexturingStateShard REPEAT_TEXTURE =
            new TexturingStateShard(
                    "thaumic_reborn_portable_hole_repeat",
                    () -> {
                        RenderSystem.texParameter(
                                GL11.GL_TEXTURE_2D,
                                GL11.GL_TEXTURE_WRAP_S,
                                GL11.GL_REPEAT
                        );
                        RenderSystem.texParameter(
                                GL11.GL_TEXTURE_2D,
                                GL11.GL_TEXTURE_WRAP_T,
                                GL11.GL_REPEAT
                        );
                    },
                    () -> { }
            );
    private static final RenderType TUNNEL = create(
            "tunnel",
            TUNNEL_TEXTURE,
            false
    );
    private static final RenderType STARS = create(
            "stars",
            STAR_TEXTURE,
            true
    );

    private TemporaryHoleRenderTypes() {
        super("thaumic_reborn_portable_hole", () -> { }, () -> { });
    }

    static RenderType tunnel() {
        return TUNNEL;
    }

    static RenderType stars() {
        return STARS;
    }

    private static RenderType create(
            String name,
            ResourceLocation texture,
            boolean additive
    ) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(
                        additive
                                ? ADDITIVE_TRANSPARENCY
                                : TRANSLUCENT_TRANSPARENCY
                )
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .setTexturingState(REPEAT_TEXTURE)
                .createCompositeState(false);
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":portable_hole_" + name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                2048,
                false,
                true,
                state
        );
    }
}
