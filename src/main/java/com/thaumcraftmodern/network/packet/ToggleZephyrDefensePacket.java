package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.item.ElementalSwordItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToggleZephyrDefensePacket(int selectedSlot) {
    public static void encode(ToggleZephyrDefensePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.selectedSlot);
    }

    public static ToggleZephyrDefensePacket decode(FriendlyByteBuf buffer) {
        return new ToggleZephyrDefensePacket(buffer.readVarInt());
    }

    public static void handle(ToggleZephyrDefensePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.getInventory().selected != packet.selectedSlot) return;
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof ElementalSwordItem)) return;
            if (ElementalSwordItem.toggleDefending(stack)) {
                player.startUsingItem(InteractionHand.MAIN_HAND);
            } else {
                player.stopUsingItem();
            }
            player.getInventory().setChanged();
        });
        context.setPacketHandled(true);
    }
}
