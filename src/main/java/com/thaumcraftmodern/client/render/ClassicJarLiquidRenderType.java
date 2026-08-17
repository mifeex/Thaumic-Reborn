package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

/**
 * Direct-colour rendering pass matching TC4's jar liquid tessellator.
 *
 * <p>TileJarRenderer used POSITION_TEX_COLOR with lighting and culling
 * disabled. In particular, the aspect RGB was not passed through the modern
 * entity lightmap/diffuse-lighting pipeline.</p>
 */
final class ClassicJarLiquidRenderType extends RenderStateShard {
    private static final RenderType TYPE = RenderType.create(
            ThaumcraftModern.MOD_ID + ":classic_jar_liquid",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_TEX_SHADER)
                    .setTextureState(new TextureStateShard(
                            TextureAtlas.LOCATION_BLOCKS,
                            false,
                            false
                    ))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    private ClassicJarLiquidRenderType() {
        super("thaumic_reborn_classic_jar_liquid", () -> {
        }, () -> {
        });
    }

    static RenderType get() {
        return TYPE;
    }
}
