package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.entity.TravelingTrunkEntity;
import com.thaumcraftmodern.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Original three-row trunk inventory; Terra exposes the fourth row. */
public final class TravelingTrunkMenu extends AbstractContainerMenu {
    public static final int STAY_BUTTON = 0;
    private final TravelingTrunkEntity trunk;
    private final int rows;
    private final DataSlot staying;

    public TravelingTrunkMenu(int id, Inventory playerInventory, TravelingTrunkEntity trunk) {
        super(ModMenus.TRAVELING_TRUNK.get(), id);
        this.trunk = trunk;
        this.rows = trunk == null ? 3 : trunk.rows();
        this.staying = new DataSlot() {
            @Override public int get() { return trunk != null && trunk.isStaying() ? 1 : 0; }
            @Override public void set(int value) { if (trunk != null) trunk.setStaying(value != 0); }
        };
        if (trunk != null) {
            for (int row = 0; row < rows; row++) for (int column = 0; column < 9; column++) {
                addSlot(new Slot(trunk.inventory(), column + row * 9,
                        8 + column * 18, 15 + row * 23));
            }
            trunk.inventory().startOpen(playerInventory.player);
            trunk.setOpen(true);
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column + row * 9 + 9,
                    8 + column * 18, 118 + row * 18));
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 176));
        }
        addDataSlot(staying);
    }

    public static TravelingTrunkMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buffer) {
        Entity entity = inventory.player.level().getEntity(buffer.readVarInt());
        return new TravelingTrunkMenu(id, inventory,
                entity instanceof TravelingTrunkEntity trunk ? trunk : null);
    }

    public int rows() { return rows; }
    public TravelingTrunkEntity trunk() { return trunk; }
    public boolean isStaying() { return staying.get() != 0; }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (id != STAY_BUTTON || trunk == null) return false;
        trunk.setStaying(!trunk.isStaying());
        return true;
    }

    @Override public boolean stillValid(Player player) {
        return trunk != null && trunk.isAlive() && player.distanceToSqr(trunk) <= 64D;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int trunkSlots = rows * 9;
            if (index < trunkSlots) {
                if (!moveItemStackTo(stack, trunkSlots, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, trunkSlots, false)) return ItemStack.EMPTY;
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return result;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        if (trunk != null) {
            trunk.inventory().stopOpen(player);
            trunk.setOpen(false);
        }
    }
}
