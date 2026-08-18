package com.thaumcraftmodern.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Applies the TC4 warp-mist event to world fog instead of the HUD. */
@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class ClientWarpFogEvents {
    private static final float MIST_NEAR_DISTANCE = 0.25F;
    private static final float MIST_FAR_DISTANCE = 24.0F;
    private static final float MIST_RED = 0.24F;
    private static final float MIST_GREEN = 0.20F;
    private static final float MIST_BLUE = 0.29F;

    private ClientWarpFogEvents() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientWarpOverlay.tickMist();
        }
    }

    @SubscribeEvent
    public static void renderFog(ViewportEvent.RenderFog event) {
        if (event.getType() != FogType.NONE) {
            return;
        }
        float strength = ClientWarpOverlay.mistStrength(
                (float) event.getPartialTick()
        );
        if (strength <= 0.0F) {
            return;
        }

        event.setNearPlaneDistance(lerp(
                strength,
                event.getNearPlaneDistance(),
                Math.min(event.getNearPlaneDistance(), MIST_NEAR_DISTANCE)
        ));
        event.setFarPlaneDistance(lerp(
                strength,
                event.getFarPlaneDistance(),
                Math.min(event.getFarPlaneDistance(), MIST_FAR_DISTANCE)
        ));
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void fogColor(ViewportEvent.ComputeFogColor event) {
        if (event.getCamera().getFluidInCamera() != FogType.NONE) {
            return;
        }
        float strength = ClientWarpOverlay.mistStrength(
                (float) event.getPartialTick()
        );
        if (strength <= 0.0F) {
            return;
        }
        event.setRed(lerp(strength, event.getRed(), MIST_RED));
        event.setGreen(lerp(strength, event.getGreen(), MIST_GREEN));
        event.setBlue(lerp(strength, event.getBlue(), MIST_BLUE));
    }

    private static float lerp(float amount, float start, float end) {
        return start + amount * (end - start);
    }
}
