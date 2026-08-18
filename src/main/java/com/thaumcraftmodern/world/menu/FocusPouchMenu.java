package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.item.FocusPouchItem;
import com.thaumcraftmodern.item.WandFocusItem;
import com.thaumcraftmodern.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Exact TC4 3x6 focus pouch layout plus protected source slot. */
public final class FocusPouchMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final InteractionHand hand;
    private final int pouchSlot;
    private boolean loading;
    private final SimpleContainer pouchInventory = new SimpleContainer(FocusPouchItem.SLOT_COUNT) {
        @Override public int getMaxStackSize() { return 1; }
        @Override public void setChanged() {
            super.setChanged();
            if (!loading && !playerInventory.player.level().isClientSide) save();
        }
    };

    public static FocusPouchMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buffer) {
        return new FocusPouchMenu(id, inventory, buffer.readEnum(InteractionHand.class),
                buffer.readVarInt());
    }

    public FocusPouchMenu(int id, Inventory inventory, InteractionHand hand, int pouchSlot) {
        super(ModMenus.FOCUS_POUCH.get(), id);
        this.playerInventory = inventory;
        this.hand = hand;
        this.pouchSlot = pouchSlot;
        loading = true;
        var stored = FocusPouchItem.loadInventory(pouch());
        for (int i = 0; i < stored.size(); i++) pouchInventory.setItem(i, stored.get(i));
        loading = false;

        for (int row = 0; row < 3; row++) for (int column = 0; column < 6; column++) {
            addSlot(new Slot(pouchInventory, column + row * 6,
                    37 + column * 18, 51 + row * 18) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof WandFocusItem;
                }
                @Override public int getMaxStackSize() { return 1; }
            });
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addPlayerSlot(column + row * 9 + 9, 8 + column * 18, 151 + row * 18);
        }
        for (int column = 0; column < 9; column++) {
            addPlayerSlot(column, 8 + column * 18, 209);
        }
    }

    private void addPlayerSlot(int index, int x, int y) {
        addSlot(new Slot(playerInventory, index, x, y) {
            @Override public boolean mayPickup(Player player) { return index != pouchSlot; }
            @Override public boolean mayPlace(ItemStack stack) { return index != pouchSlot; }
        });
    }

    private ItemStack pouch() {
        if (hand == InteractionHand.OFF_HAND) return playerInventory.player.getOffhandItem();
        return pouchSlot >= 0 && pouchSlot < playerInventory.items.size()
                ? playerInventory.items.get(pouchSlot) : ItemStack.EMPTY;
    }

    private void save() {
        ItemStack pouch = pouch();
        if (!(pouch.getItem() instanceof FocusPouchItem)) return;
        List<ItemStack> stacks = new ArrayList<>(FocusPouchItem.SLOT_COUNT);
        for (int i = 0; i < FocusPouchItem.SLOT_COUNT; i++) stacks.add(pouchInventory.getItem(i));
        FocusPouchItem.saveInventory(pouch, stacks);
        playerInventory.setChanged();
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack live = slot.getItem();
        ItemStack copy = live.copy();
        if (index < FocusPouchItem.SLOT_COUNT) {
            if (!moveItemStackTo(live, FocusPouchItem.SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else if (live.getItem() instanceof WandFocusItem) {
            if (!moveItemStackTo(live, 0, FocusPouchItem.SLOT_COUNT, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (live.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && hand == InteractionHand.MAIN_HAND && button == pouchSlot) return;
        if (slotId >= 0 && slotId < slots.size()
                && slots.get(slotId).container == playerInventory
                && slots.get(slotId).getSlotIndex() == pouchSlot) return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override public void removed(Player player) { save(); super.removed(player); }
    @Override public boolean stillValid(Player player) {
        return player == playerInventory.player && player.isAlive()
                && pouch().getItem() instanceof FocusPouchItem;
    }
}
