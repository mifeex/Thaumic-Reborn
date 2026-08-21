package com.thaumcraftmodern.client;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.ElementalSwordItem;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.ToggleZephyrDefensePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class ZephyrSwordKeyEvents {
    private static boolean holdingUseForDefense;

    private ZephyrSwordKeyEvents() {}

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft minecraft = Minecraft.getInstance();
        boolean defending = minecraft.player != null
                && minecraft.player.getMainHandItem().getItem() instanceof ElementalSwordItem
                && ElementalSwordItem.isDefending(minecraft.player.getMainHandItem());
        if (holdingUseForDefense && !defending) {
            minecraft.options.keyUse.setDown(false);
            holdingUseForDefense = false;
        }
        if (minecraft.player == null || minecraft.gameMode == null || minecraft.screen != null
                || !(minecraft.player.getMainHandItem().getItem() instanceof ElementalSwordItem)) {
            return;
        }

        if (minecraft.player.isShiftKeyDown() && minecraft.options.keyUse.consumeClick()) {
            toggleClient(minecraft);
        }

        if (!ElementalSwordItem.isDefending(minecraft.player.getMainHandItem())) return;
        minecraft.options.keyUse.setDown(true);
        holdingUseForDefense = true;
        while (minecraft.options.keyAttack.consumeClick()) {
            InputEvent.InteractionKeyMappingTriggered click = ForgeHooksClient.onClickInput(
                    0, minecraft.options.keyAttack, InteractionHand.MAIN_HAND);
            if (!click.isCanceled() && minecraft.hitResult instanceof EntityHitResult entityHit) {
                minecraft.gameMode.attack(minecraft.player, entityHit.getEntity());
            }
            if (click.shouldSwingHand()) minecraft.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static void toggleClient(Minecraft minecraft) {
        boolean defending = ElementalSwordItem.toggleDefending(
                minecraft.player.getMainHandItem());
        if (defending) {
            minecraft.player.startUsingItem(InteractionHand.MAIN_HAND);
            minecraft.options.keyUse.setDown(true);
            holdingUseForDefense = true;
        } else {
            minecraft.player.stopUsingItem();
            minecraft.options.keyUse.setDown(false);
            holdingUseForDefense = false;
        }
        ModNetwork.sendToServer(new ToggleZephyrDefensePacket(
                minecraft.player.getInventory().selected));
    }
}
