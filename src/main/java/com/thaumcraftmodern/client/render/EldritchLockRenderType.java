package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Colour-only TC4 lock passes: solid cubes, translucent tunnel and additive stars. */
final class EldritchLockRenderType extends RenderStateShard {
    static final ResourceLocation CUBE = texture("textures/models/eldritch_cube.png");
    static final ResourceLocation TUNNEL = texture("textures/misc/tunnel.png");
    static final ResourceLocation PARTICLES = texture("textures/misc/particlefield.png");

    private static final TexturingStateShard REPEAT_TEXTURE =
            new TexturingStateShard(
                    "thaumic_reborn_eldritch_lock_repeat",
                    () -> {
                        RenderSystem.texParameter(GL11.GL_TEXTURE_2D,
                                GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                        RenderSystem.texParameter(GL11.GL_TEXTURE_2D,
                                GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
                    },
                    () -> { }
            );
    private static final RenderType CUBES = type("eldritch_lock_cubes", CUBE, false, true, false);
    private static final RenderType BACKGROUND = type("eldritch_lock_tunnel", TUNNEL, false, false, true);
    private static final RenderType STARS = type("eldritch_lock_stars", PARTICLES, true, false, true);

    private EldritchLockRenderType() {
        super("thaumic_reborn_eldritch_lock_render_type", () -> {}, () -> {});
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(ThaumcraftModern.MOD_ID, path);
    }

    private static RenderType type(String name, ResourceLocation texture,
            boolean additive, boolean depthWrite, boolean repeat) {
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":" + name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1024,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new TextureStateShard(texture, true, false))
                        .setTransparencyState(additive
                                ? ADDITIVE_TRANSPARENCY
                                : TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setTexturingState(repeat ? REPEAT_TEXTURE : DEFAULT_TEXTURING)
                        .setWriteMaskState(depthWrite ? COLOR_DEPTH_WRITE : COLOR_WRITE)
                        .createCompositeState(false)
        );
    }

    static RenderType cubes() { return CUBES; }
    static RenderType background() { return BACKGROUND; }
    static RenderType stars() { return STARS; }
}
