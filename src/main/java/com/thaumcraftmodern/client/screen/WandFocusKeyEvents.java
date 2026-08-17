package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.ChangeWandFocusPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class WandFocusKeyEvents {
    private static final KeyMapping CHANGE_FOCUS = new KeyMapping(
            "key.thaumic_reborn.change_focus", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F, "key.categories.thaumic_reborn");
    private static boolean previousDown;

    private WandFocusKeyEvents() {}

    public static boolean matchesFocusKey(int keyCode, int scanCode) {
        return CHANGE_FOCUS.matches(keyCode, scanCode);
    }

    @SubscribeEvent public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        boolean down = CHANGE_FOCUS.isDown();
        if (down && !previousDown && minecraft.player != null
                && minecraft.screen == null
                && minecraft.player.getMainHandItem().getItem() instanceof WandItem wand
                && wand.form().acceptsFocus()) {
            if (minecraft.player.isShiftKeyDown())
                ModNetwork.sendToServer(new ChangeWandFocusPacket("remove"));
            else minecraft.setScreen(new WandFocusRadialScreen());
        }
        previousDown = down;
    }

    @Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) {
            event.register(CHANGE_FOCUS);
        }
    }
}
