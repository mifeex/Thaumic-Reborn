package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

/** Original lifter glow pass: SRC_ALPHA/ONE, full-bright and depth-tested. */
final class ArcaneLevitatorRenderType extends RenderStateShard {
    private static final RenderType TYPE = RenderType.create(
            ThaumcraftModern.MOD_ID + ":arcane_levitator_glow",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new TextureStateShard(
                            TextureAtlas.LOCATION_BLOCKS, false, false))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    private ArcaneLevitatorRenderType() {
        super("thaumic_reborn_arcane_levitator_render_type", () -> {}, () -> {});
    }

    static RenderType get() {
        return TYPE;
    }
}
