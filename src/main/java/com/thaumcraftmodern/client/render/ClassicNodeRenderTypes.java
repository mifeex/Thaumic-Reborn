package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Render states matching the fixed-function passes used by TC4 nodes.
 *
 * <p>The original renderer writes colour but not depth, uses either
 * SRC_ALPHA/ONE or SRC_ALPHA/ONE_MINUS_SRC_ALPHA, and disables the depth test
 * only while revealing an ordinary node through a Thaumometer or Goggles.</p>
 */
public final class ClassicNodeRenderTypes extends RenderStateShard {
    static final ResourceLocation NODE_TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/nodes.png"
    );
    static final ResourceLocation WISPY_TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/misc/wispy.png"
            );
    static final ResourceLocation ENERGIZED_RING_TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/item/lightningringv.png"
            );
    static final ResourceLocation VIS_BEAM_TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/beam1.png"
    );
    static final ResourceLocation PARTICLE_TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/particles.png"
    );

    private static final RenderType DEPTH_ADDITIVE =
            createNodeType("classic_node_additive", true, false);
    private static final RenderType DEPTH_TRANSLUCENT =
            createNodeType("classic_node_translucent", false, false);
    private static final RenderType SEE_THROUGH_ADDITIVE =
            createNodeType("classic_node_additive_see_through", true, true);
    private static final RenderType SEE_THROUGH_TRANSLUCENT =
            createNodeType("classic_node_translucent_see_through", false, true);
    private static final RenderType DRAIN_STREAM = createDrainStreamType();
    private static final RenderType ENERGIZED_RING =
            createEnergizedRingType();
    private static final RenderType VIS_BEAM = createAdditiveEffectType(
            "vis_power_beam", VIS_BEAM_TEXTURE, 1024);
    private static final RenderType VIS_IMPACT = createAdditiveEffectType(
            "vis_power_impact", PARTICLE_TEXTURE, 256);

    private ClassicNodeRenderTypes() {
        super("thaumic_reborn_classic_node_render_types", () -> {
        }, () -> {
        });
    }

    public static RenderType node(boolean additive, boolean seeThrough) {
        if (additive) {
            return seeThrough ? SEE_THROUGH_ADDITIVE : DEPTH_ADDITIVE;
        }
        return seeThrough ? SEE_THROUGH_TRANSLUCENT : DEPTH_TRANSLUCENT;
    }

    static RenderType drainStream() {
        return DRAIN_STREAM;
    }

    static RenderType energizedRing() {
        return ENERGIZED_RING;
    }

    static RenderType visBeam() {
        return VIS_BEAM;
    }

    static RenderType visImpact() {
        return VIS_IMPACT;
    }

    private static RenderType createAdditiveEffectType(
            String name,
            ResourceLocation texture,
            int bufferSize
    ) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new TextureStateShard(texture, true, false))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
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
                bufferSize,
                false,
                true,
                state
        );
    }

    private static RenderType createNodeType(
            String name,
            boolean additive,
            boolean seeThrough
    ) {
        RenderType.CompositeState.CompositeStateBuilder state =
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        /*
                         * nodes.png.mcmeta requests blur=true in the original
                         * JAR, so the render state must not force nearest
                         * sampling for this standalone texture.
                         */
                        .setTextureState(new TextureStateShard(
                                NODE_TEXTURE,
                                true,
                                false
                        ))
                        .setTransparencyState(
                                additive
                                        ? ADDITIVE_TRANSPARENCY
                                        : TRANSLUCENT_TRANSPARENCY
                        )
                        .setDepthTestState(
                                seeThrough ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST
                        )
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_WRITE);

        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":" + name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                state.createCompositeState(false)
        );
    }

    private static RenderType createDrainStreamType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(
                        WISPY_TEXTURE,
                        true,
                        false
                ))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);

        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":classic_node_drain_stream",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.TRIANGLES,
                1024,
                false,
                true,
                state
        );
    }

    /** TC4 TileNodeEnergizedRenderer uses SRC_ALPHA/ONE for this atlas. */
    private static RenderType createEnergizedRingType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new TextureStateShard(
                        ENERGIZED_RING_TEXTURE,
                        false,
                        false
                ))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":energized_node_lightning_ring",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                state
        );
    }
}
