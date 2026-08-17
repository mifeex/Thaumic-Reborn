package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** TC4 Runic Matrix overlay pass: SRC_ALPHA/ONE, depth-tested, color-only. */
final class RunicMatrixRenderType extends RenderStateShard {
    private RunicMatrixRenderType() {
        super("thaumic_reborn_runic_matrix_render_type", () -> { }, () -> { });
    }

    static RenderType additive(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":runic_matrix_additive",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                512,
                false,
                true,
                state
        );
    }
}
