package com.thaumcraftmodern.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.ElementalShovelItem;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.CycleShovelOrientationPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class ElementalToolKeyEvents {
    private static final KeyMapping CYCLE_SHOVEL = new KeyMapping(
            "key.thaumic_reborn.cycle_shovel_orientation",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.thaumic_reborn");

    private ElementalToolKeyEvents() {}

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        while (CYCLE_SHOVEL.consumeClick()) {
            if (minecraft.player == null || !(minecraft.player.getMainHandItem().getItem()
                    instanceof ElementalShovelItem)) continue;
            int orientation = ElementalShovelItem.cycleOrientation(minecraft.player.getMainHandItem());
            ModNetwork.sendToServer(new CycleShovelOrientationPacket());
            minecraft.player.displayClientMessage(Component.translatable(
                    "message.thaumic_reborn.shovel_orientation." + orientation), true);
        }
    }

    @Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) {
            event.register(CYCLE_SHOVEL);
        }
    }
}
