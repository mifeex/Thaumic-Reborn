package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Fixed-function equivalents of TC4 FXLightningBolt's two texture passes. */
final class ClassicBoltRenderTypes extends RenderStateShard {
    private static final Map<Key, RenderType> TYPES =
            new ConcurrentHashMap<>();

    private ClassicBoltRenderTypes() {
        super("thaumic_reborn_classic_bolt_render_types", () -> {}, () -> {});
    }

    static RenderType bolt(ResourceLocation texture, boolean additive) {
        return TYPES.computeIfAbsent(
                new Key(texture, additive),
                ClassicBoltRenderTypes::create
        );
    }

    private static RenderType create(Key key) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new TextureStateShard(
                        key.texture(), false, false))
                .setTransparencyState(key.additive()
                        ? ADDITIVE_TRANSPARENCY
                        : TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":classic_bolt_"
                        + (key.additive() ? "small" : "large"),
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                512,
                false,
                true,
                state
        );
    }

    private record Key(ResourceLocation texture, boolean additive) {
    }
}
