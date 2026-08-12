package com.thaumcraftmodern.warp;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Restores TC4 ClientProxy's global tooltip for held or worn warp gear. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class WarpGearTooltipEvents {
    private WarpGearTooltipEvents() {
    }

    @SubscribeEvent
    public static void appendWarp(ItemTooltipEvent event) {
        int warp = WarpGearService.warp(event.getItemStack());
        if (warp > 0) {
            event.getToolTip().add(
                    Component.translatable("item.warping")
                            .append(" " + warp)
                            .withStyle(ChatFormatting.DARK_PURPLE)
            );
        }
    }
}
