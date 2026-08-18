package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.focus.WandFocusType;
import com.thaumcraftmodern.item.WandFocusItem;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.world.block.entity.ArcaneBoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;

/** Original two-slot Arcane Bore container and exact slot coordinates. */
public final class ArcaneBoreMenu extends AbstractContainerMenu {
    private final Container bore;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public static ArcaneBoreMenu fromNetwork(int id, Inventory inventory,
            FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            return new ArcaneBoreMenu(id, inventory, bore, bore.data());
        }
        return new ArcaneBoreMenu(id, inventory, new SimpleContainer(2),
                new SimpleContainerData(ArcaneBoreBlockEntity.DATA_COUNT));
    }

    public ArcaneBoreMenu(int id, Inventory inventory, Container bore, ContainerData data) {
        super(ModMenus.ARCANE_BORE.get(), id);
        checkContainerSize(bore, 2);
        checkContainerDataCount(data, ArcaneBoreBlockEntity.DATA_COUNT);
        this.bore = bore;
        this.data = data;
        access = bore instanceof ArcaneBoreBlockEntity entity
                ? ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos())
                : ContainerLevelAccess.NULL;
        addSlot(new Slot(bore, 0, 26, 18) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandFocusItem focus
                        && focus.type() == WandFocusType.EXCAVATION;
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(bore, 1, 74, 18) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof PickaxeItem;
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9,
                    8 + column * 18, 59 + row * 18));
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column, 8 + column * 18, 117));
        addDataSlots(data);
    }

    public int area() { return data.get(0); }
    public int speed() { return data.get(1); }
    public int fortune() { return data.get(2); }
    public float speedyTime() { return data.get(3) / 10.0F; }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack live = slot.getItem(); ItemStack original = live.copy();
        if (index <= 1) {
            if (!moveItemStackTo(live, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else if (live.getItem() instanceof WandFocusItem focus
                && focus.type() == WandFocusType.EXCAVATION) {
            if (!moveItemStackTo(live, 0, 1, false)) return ItemStack.EMPTY;
        } else if (live.getItem() instanceof PickaxeItem) {
            if (!moveItemStackTo(live, 1, 2, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (live.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (live.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, live); return original;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ARCANE_BORE.get());
    }
}
