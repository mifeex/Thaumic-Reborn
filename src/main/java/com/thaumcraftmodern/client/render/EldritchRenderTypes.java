package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class EldritchRenderTypes extends RenderStateShard {
    static final ResourceLocation TUNNEL =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/misc/tunnel.png"
            );
    static final ResourceLocation FIELD =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/misc/particlefield.png"
            );
    private static final RenderType TUNNEL_TYPE = fieldType(
            "eldritch_tunnel",
            TUNNEL,
            false
    );
    private static final RenderType FIELD_TYPE = fieldType(
            "eldritch_field",
            FIELD,
            true
    );
    private static final Map<ResourceLocation, RenderType> CAP_TRIANGLES =
            new ConcurrentHashMap<>();

    private EldritchRenderTypes() {
        super("thaumic_reborn_eldritch_render_types", () -> {}, () -> {});
    }

    static RenderType tunnel() {
        return TUNNEL_TYPE;
    }

    static RenderType field() {
        return FIELD_TYPE;
    }

    static RenderType capTriangles(ResourceLocation texture) {
        return CAP_TRIANGLES.computeIfAbsent(
                texture,
                EldritchRenderTypes::createCapTriangles
        );
    }

    private static RenderType createCapTriangles(
            ResourceLocation texture
    ) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                /*
                 * The legacy cap OBJ is two-sided, but its texture is not a
                 * translucent surface.  Sending its triangles through the
                 * sorted translucent buffer lets the rear face of the small
                 * top prism win the sort at close range, which appears as a
                 * black rectangle on the front of the obelisk.
                 *
                 * TC4 rendered this model with culling disabled and ordinary
                 * depth writes.  Keep that combination here: it preserves
                 * both sides of the OBJ while making the nearest triangle
                 * deterministically occlude the rear one.
                 */
                .setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false);
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":eldritch_cap_"
                        + texture.getPath().replace('/', '_'),
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                1024,
                false,
                false,
                state
        );
    }

    private static RenderType fieldType(
            String name,
            ResourceLocation texture,
            boolean additive
    ) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(texture, true, false))
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
                .createCompositeState(false);
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":" + name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                2048,
                false,
                true,
                state
        );
    }
}
