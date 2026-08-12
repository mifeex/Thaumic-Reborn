package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.focus.FocusUpgradeType;
import com.thaumcraftmodern.item.WandFocusItem;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.entity.FocalManipulatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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

/** Original 192x233 focal-manipulator slot layout and server button contract. */
public final class FocalManipulatorMenu extends AbstractContainerMenu {
    private final Container table;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public static FocalManipulatorMenu fromNetwork(int id, Inventory inventory,
                                                    FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof FocalManipulatorBlockEntity table)
            return new FocalManipulatorMenu(id, inventory, table, table.data());
        return new FocalManipulatorMenu(id, inventory, new SimpleContainer(1),
                new SimpleContainerData(FocalManipulatorBlockEntity.DATA_COUNT));
    }

    public FocalManipulatorMenu(int id, Inventory inventory, Container table, ContainerData data) {
        super(ModMenus.FOCAL_MANIPULATOR.get(), id);
        checkContainerSize(table, 1);
        checkContainerDataCount(data, FocalManipulatorBlockEntity.DATA_COUNT);
        this.table = table;
        this.data = data;
        this.access = table instanceof FocalManipulatorBlockEntity entity
                ? ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos())
                : ContainerLevelAccess.NULL;
        addSlot(new Slot(table, 0, 88, 60) {
            @Override public boolean mayPlace(ItemStack stack) { return table.canPlaceItem(0, stack); }
        });
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9,
                    16 + column * 18, 151 + row * 18));
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column, 16 + column * 18, 209));
        addDataSlots(data);
    }

    public ItemStack focus() { return table.getItem(0); }
    public int totalCost() { return data.get(FocalManipulatorBlockEntity.DATA_TOTAL); }
    public int remainingCost() { return data.get(FocalManipulatorBlockEntity.DATA_REMAINING); }
    public int activeUpgrade() { return data.get(FocalManipulatorBlockEntity.DATA_UPGRADE); }
    public int activeRank() { return data.get(FocalManipulatorBlockEntity.DATA_RANK); }
    public int remainingPrimal(int index) { return data.get(FocalManipulatorBlockEntity.DATA_COST_START + index); }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer server)
                || !(table instanceof FocalManipulatorBlockEntity entity)) return false;
        boolean started;
        try { started = entity.begin(server, FocusUpgradeType.byId(id)); }
        catch (IllegalArgumentException ignored) { started = false; }
        if (!started) server.level().playSound(null, entity.getBlockPos(),
                ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
        return started;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack live = slot.getItem(); ItemStack original = live.copy();
        if (index == 0) {
            if (!moveItemStackTo(live, 1, slots.size(), false)) return ItemStack.EMPTY;
        } else if (live.getItem() instanceof WandFocusItem) {
            if (!moveItemStackTo(live, 0, 1, false)) return ItemStack.EMPTY;
        } else if (index < 28) {
            if (!moveItemStackTo(live, 28, 37, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(live, 1, 28, false)) return ItemStack.EMPTY;
        if (live.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, live); return original;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.FOCAL_MANIPULATOR.get());
    }
}
