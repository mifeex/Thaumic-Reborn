package com.thaumcraftmodern.research;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Restores the TC4 item-triggered research clue for the Primordial Pearl.
 */
@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class WorldContentResearchEvents {
    private WorldContentResearchEvents() {
    }

    @SubscribeEvent
    public static void itemPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getItem().getItem().is(ModItems.PRIMORDIAL_PEARL.get())) {
            ResearchProgressService.recordCriterion(
                    player,
                    "thaumic_reborn:legacy_clue/primpearl",
                    "primordial_pearl_pickup"
            );
        }
    }
}
