package com.thaumcraftmodern.item;

import com.thaumcraftmodern.world.menu.FocusPouchMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

/** TC4's portable 18-slot inventory restricted to wand foci. */
public final class FocusPouchItem extends Item {
    public static final int SLOT_COUNT = 18;
    public static final String INVENTORY_TAG = "Inventory";

    public FocusPouchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);
        if (player instanceof ServerPlayer server) {
            int slot = hand == InteractionHand.OFF_HAND ? -1 : server.getInventory().selected;
            NetworkHooks.openScreen(server, new SimpleMenuProvider(
                    (id, inventory, ignored) -> new FocusPouchMenu(id, inventory, hand, slot),
                    Component.translatable("container.thaumic_reborn.focus_pouch")), buffer -> {
                buffer.writeEnum(hand);
                buffer.writeVarInt(slot);
            });
        }
        return InteractionResultHolder.sidedSuccess(pouch, level.isClientSide);
    }

    public static List<ItemStack> loadInventory(ItemStack pouch) {
        List<ItemStack> result = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) result.add(ItemStack.EMPTY);
        if (!pouch.hasTag()) return result;
        ListTag list = pouch.getTag().getList(INVENTORY_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < SLOT_COUNT) result.set(slot, ItemStack.of(entry));
        }
        return result;
    }

    public static void saveInventory(ItemStack pouch, List<ItemStack> inventory) {
        ListTag list = new ListTag();
        for (int i = 0; i < Math.min(SLOT_COUNT, inventory.size()); i++) {
            ItemStack stack = inventory.get(i);
            if (stack.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putByte("Slot", (byte) i);
            stack.save(entry);
            list.add(entry);
        }
        pouch.getOrCreateTag().put(INVENTORY_TAG, list);
    }
}
