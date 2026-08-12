package com.thaumcraftmodern.scan;

import com.thaumcraftmodern.item.ThaumometerItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server-authoritative stable-hover scanning for container slots. */
public final class InventoryScanService {
    private static final Map<UUID, HoverSession> SESSIONS =
            new ConcurrentHashMap<>();

    private InventoryScanService() {}

    public static void hover(ServerPlayer player, int containerId, int slotIndex) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu.containerId != containerId
                || !(menu.getCarried().getItem() instanceof ThaumometerItem)
                || slotIndex < 0 || slotIndex >= menu.slots.size()) {
            SESSIONS.remove(player.getUUID());
            return;
        }
        ItemStack target = menu.getSlot(slotIndex).getItem();
        if (target.isEmpty() || target.getItem() instanceof ThaumometerItem) {
            SESSIONS.remove(player.getUUID());
            return;
        }
        ScanRegistry.ItemScanIdentity identity = ScanRegistry.identityForItem(target);
        String scanKey = identity.knowledgeKey();
        if (KnowledgeAccess.get(player).map(knowledge -> knowledge.hasScan(scanKey))
                .orElse(false)) {
            SESSIONS.remove(player.getUUID());
            return;
        }

        long gameTick = player.serverLevel().getGameTime();
        HoverSession previous = SESSIONS.get(player.getUUID());
        boolean same = previous != null
                && previous.containerId == containerId
                && previous.slotIndex == slotIndex
                && previous.scanKey.equals(scanKey)
                && gameTick - previous.lastGameTick <= 2;
        int elapsed = same ? previous.elapsedTicks : 0;
        if (previous == null || gameTick > previous.lastGameTick) elapsed++;
        HoverSession next = new HoverSession(containerId, slotIndex, scanKey,
                gameTick, elapsed);
        if (elapsed < ScanSessionManager.REQUIRED_TICKS) {
            SESSIONS.put(player.getUUID(), next);
            return;
        }

        SESSIONS.remove(player.getUUID());
        ScanService.complete(player, new ScanSessionManager.InventoryItemTarget(
                player.level().dimension(), identity.type(), identity.targetId(),
                identity.knowledgeKey()));
    }

    private record HoverSession(int containerId, int slotIndex, String scanKey,
                                long lastGameTick, int elapsedTicks) { }
}
