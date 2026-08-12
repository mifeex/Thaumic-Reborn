package com.thaumcraftmodern.world.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ArcaneCraftingInventory extends SimpleContainer implements CraftingContainer {
    public static final int WIDTH = 3;
    public static final int HEIGHT = 3;

    public ArcaneCraftingInventory() {
        super(WIDTH * HEIGHT);
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public List<ItemStack> getItems() {
        return java.util.stream.IntStream.range(0, getContainerSize())
                .mapToObj(this::getItem)
                .toList();
    }

    /** Saves the sparse 3x3 layout instead of compacting non-empty stacks. */
    public ListTag createSlotTag() {
        ListTag items = new ListTag();
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) continue;
            CompoundTag item = stack.save(new CompoundTag());
            item.putByte("Slot", (byte) slot);
            items.add(item);
        }
        return items;
    }

    public void fromSlotTag(ListTag items) {
        clearContent();
        int legacySlot = 0;
        for (int index = 0; index < items.size(); index++) {
            CompoundTag item = items.getCompound(index);
            int slot = item.contains("Slot")
                    ? Byte.toUnsignedInt(item.getByte("Slot"))
                    : legacySlot++;
            if (slot >= 0 && slot < getContainerSize()) {
                setItem(slot, ItemStack.of(item));
            }
        }
    }
}
