package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ElementalEquipmentEvents {
    private ElementalEquipmentEvents() {
    }

    @SubscribeEvent
    public static void blockZephyrSwordAttacks(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.getMainHandItem().getItem() instanceof ElementalSwordItem)
                || !ElementalSwordItem.isDefending(player.getMainHandItem())) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void attractStreamAxeDrops(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        for (ItemEntity item : level.getEntitiesOfClass(
                ItemEntity.class,
                player.getBoundingBox().inflate(64.0D),
                candidate -> candidate.getPersistentData().hasUUID(ElementalAxeItem.ATTRACTED_TO)
                        && candidate.getPersistentData().getUUID(ElementalAxeItem.ATTRACTED_TO)
                        .equals(player.getUUID())
        )) {
            if (now > item.getPersistentData().getLong(ElementalAxeItem.ATTRACTED_UNTIL)) {
                item.getPersistentData().remove(ElementalAxeItem.ATTRACTED_TO);
                item.getPersistentData().remove(ElementalAxeItem.ATTRACTED_UNTIL);
                item.setNoGravity(false);
                continue;
            }
            ElementalAxeItem.pullDrop(item, player);
        }
    }
}
