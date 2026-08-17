package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.PrimalAspectColors;
import com.thaumcraftmodern.visnet.VisRelayBlockEntity;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;

import java.text.DecimalFormat;
import java.util.List;

/** Server-authoritative six-primal vis storage used by TC4's vis amulets. */
public final class VisStorageItem extends CurioAccessoryItem {
    private static final String STORAGE_TAG = "VisStorage";
    private static final int TICK_INTERVAL = 5;
    private static final int TRANSFER_CENTIVIS = 5;
    private static final int RELAY_RANGE_SQUARED = 26;
    private static final DecimalFormat VIS_FORMAT = new DecimalFormat("#######.##");

    private final int capacityCentivis;

    public VisStorageItem(int capacityVis, Properties properties) {
        super(properties);
        this.capacityCentivis = VisStorageState.capacityCentivis(capacityVis);
    }

    public int capacityCentivis() {
        return capacityCentivis;
    }

    public int visCentivis(ItemStack stack, PrimalAspect aspect) {
        return VisStorageState.visCentivis(storage(stack), aspect);
    }

    public int addCentivis(ItemStack stack, PrimalAspect aspect, int amount) {
        return VisStorageState.addCentivis(
                storage(stack), aspect, amount, capacityCentivis);
    }

    public int removeCentivis(ItemStack stack, PrimalAspect aspect, int amount) {
        return VisStorageState.removeCentivis(storage(stack), aspect, amount);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)
                || player.tickCount % TICK_INTERVAL != 0) {
            return;
        }
        rechargeHeldWand(player, stack);
        rechargeFromNearestRelay(player, stack);
    }

    @Override
    public boolean canSync(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public CompoundTag writeSyncData(SlotContext slotContext, ItemStack stack) {
        return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
    }

    @Override
    public void readSyncData(SlotContext slotContext, CompoundTag data,
            ItemStack stack) {
        stack.setTag(data.copy());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.thaumic_reborn.vis_storage_recharge")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(
                "tooltip.thaumic_reborn.vis_storage_capacity",
                capacityCentivis / WandVisService.CENTIVIS_PER_VIS)
                .withStyle(ChatFormatting.GOLD));
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int amount = visCentivis(stack, aspect);
            if (amount <= 0) continue;
            tooltip.add(Component.literal(" " + VIS_FORMAT.format(
                    amount / (double) WandVisService.CENTIVIS_PER_VIS))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(
                            PrimalAspectColors.color(aspect))))
                    .append(Component.literal(" "))
                    .append(Component.translatable("tc.aspect." + aspect.id())));
        }
    }

    private void rechargeHeldWand(ServerPlayer player, ItemStack amulet) {
        ItemStack wand = player.getMainHandItem();
        if (!WandVisService.isWand(wand)) return;
        boolean changed = false;
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int available = visCentivis(amulet, aspect);
            int room = WandVisService.capacityCentivis(wand)
                    - WandVisService.visCentivis(wand, aspect.id());
            int requested = Math.min(TRANSFER_CENTIVIS,
                    Math.min(available, room));
            if (requested <= 0) continue;
            int accepted = WandVisService.addCentivisUnchecked(
                    wand, aspect.id(), requested);
            if (accepted > 0) {
                removeCentivis(amulet, aspect, accepted);
                changed = true;
            }
        }
        if (changed) player.getInventory().setChanged();
    }

    private void rechargeFromNearestRelay(ServerPlayer player, ItemStack amulet) {
        VisRelayBlockEntity relay = nearestRelay(player);
        if (relay == null) return;
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int room = capacityCentivis - visCentivis(amulet, aspect);
            int requested = Math.min(TRANSFER_CENTIVIS, room);
            if (requested <= 0) continue;
            int consumed = relay.consumeVis(aspect, requested);
            if (consumed > 0) addCentivis(amulet, aspect, consumed);
        }
    }

    private static @Nullable VisRelayBlockEntity nearestRelay(
            ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        VisRelayBlockEntity nearest = null;
        double nearestDistance = RELAY_RANGE_SQUARED;
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos position = origin.offset(x, y, z);
                    double distance = player.distanceToSqr(
                            position.getX() + 0.5D,
                            position.getY() + 0.5D,
                            position.getZ() + 0.5D);
                    if (distance >= nearestDistance
                            || !player.serverLevel().isLoaded(position)
                            || !(player.serverLevel().getBlockEntity(position)
                            instanceof VisRelayBlockEntity candidate)) continue;
                    nearest = candidate;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    private static CompoundTag storage(ItemStack stack) {
        return VisStorageState.initialize(
                stack.getOrCreateTagElement(STORAGE_TAG));
    }
}
