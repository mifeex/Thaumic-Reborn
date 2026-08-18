package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.item.BathSaltsItem;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.world.block.entity.ArcaneSpaBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.material.Fluid;

/** Original one-slot spa menu, including the server-authoritative mix toggle. */
public final class ArcaneSpaMenu extends AbstractContainerMenu {
    public static final int TOGGLE_MIX_BUTTON = 1;
    private final Container spa;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public static ArcaneSpaMenu fromNetwork(int id, Inventory inventory,
            FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof ArcaneSpaBlockEntity spa) {
            return new ArcaneSpaMenu(id, inventory, spa, spa.data());
        }
        return new ArcaneSpaMenu(id, inventory, new SimpleContainer(1),
                new SimpleContainerData(ArcaneSpaBlockEntity.DATA_COUNT));
    }

    public ArcaneSpaMenu(int id, Inventory inventory, Container spa,
            ContainerData data) {
        super(ModMenus.ARCANE_SPA.get(), id);
        checkContainerSize(spa, 1);
        checkContainerDataCount(data, ArcaneSpaBlockEntity.DATA_COUNT);
        this.spa = spa;
        this.data = data;
        this.access = spa instanceof ArcaneSpaBlockEntity entity
                ? ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos())
                : ContainerLevelAccess.NULL;

        addSlot(new Slot(spa, 0, 65, 31) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BathSaltsItem;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    public boolean mixing() { return data.get(0) != 0; }
    public int fluidAmount() { return data.get(1); }
    public Fluid fluid() { return BuiltInRegistries.FLUID.byId(data.get(2)); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != TOGGLE_MIX_BUTTON
                || !(player instanceof ServerPlayer)
                || !(spa instanceof ArcaneSpaBlockEntity entity)) {
            return false;
        }
        entity.toggleMix();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack live = slot.getItem();
        ItemStack original = live.copy();
        if (index == 0) {
            if (!moveItemStackTo(live, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (live.getItem() instanceof BathSaltsItem) {
            if (!moveItemStackTo(live, 0, 1, false)) return ItemStack.EMPTY;
        } else if (index < 28) {
            if (!moveItemStackTo(live, 28, 37, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(live, 1, 28, false)) {
            return ItemStack.EMPTY;
        }
        if (live.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (live.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, live);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ARCANE_SPA.get());
    }
}
