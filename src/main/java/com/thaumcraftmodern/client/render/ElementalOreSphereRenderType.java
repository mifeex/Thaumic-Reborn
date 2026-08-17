package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** See-through variant of TC4's original animated Wisp billboard. */
public final class ElementalOreSphereRenderType extends RenderStateShard {
    private static final int FRAME_COUNT = 16;
    private static final RenderType[] MARKERS = createMarkers();

    private ElementalOreSphereRenderType() {
        super("thaumic_reborn_elemental_ore_spheres", () -> {}, () -> {});
    }

    public static RenderType markers() {
        return markers(0);
    }

    public static RenderType markers(int frame) {
        return MARKERS[Math.floorMod(frame, FRAME_COUNT)];
    }

    private static RenderType[] createMarkers() {
        RenderType[] frames = new RenderType[FRAME_COUNT];
        for (int frame = 0; frame < FRAME_COUNT; frame++) {
            frames[frame] = createFrame(frame);
        }
        return frames;
    }

    private static RenderType createFrame(int frame) {
        ResourceLocation texture = new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                "textures/entity/misc/wisp_frames/wisp_%02d.png"
                        .formatted(frame)
        );
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(
                        texture,
                        true,
                        false
                ))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setDepthTestState(NO_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return RenderType.create(
                ThaumcraftModern.MOD_ID
                        + ":elemental_ore_wisp_frame_"
                        + frame,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                state
        );
    }
}
