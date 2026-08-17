package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.crucible.ItemAspectRegistry;
import com.thaumcraftmodern.deconstruction.DeconstructionTableLogic;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.menu.DeconstructionTableMenu;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Server-authoritative port of TC4 TileDeconstructionTable. */
public final class DeconstructionTableBlockEntity extends BlockEntity
        implements WorldlyContainer, MenuProvider {
    public static final int INPUT_SLOT = 0;
    private static final int[] NO_SLOTS = {};
    private static final int[] INPUT_SLOTS = {INPUT_SLOT};

    private final NonNullList<ItemStack> items =
            NonNullList.withSize(1, ItemStack.EMPTY);
    private String aspectId;
    private int breakTime;
    private LazyOptional<IItemHandlerModifiable>[] sidedHandlers =
            createSidedHandlers();

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? breakTime : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                breakTime = Math.max(0, Math.min(
                        DeconstructionTableLogic.BREAK_TICKS,
                        value
                ));
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public DeconstructionTableBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(ModBlockEntities.DECONSTRUCTION_TABLE.get(), position, state);
    }

    public static void serverTick(
            Level rawLevel,
            BlockPos position,
            BlockState state,
            DeconstructionTableBlockEntity table
    ) {
        if (!(rawLevel instanceof ServerLevel level)) {
            return;
        }
        boolean changed = false;
        if (table.breakTime == 0 && table.canBreak()) {
            table.breakTime = DeconstructionTableLogic.BREAK_TICKS;
            changed = true;
        }
        if (table.breakTime > 0 && table.canBreak()) {
            table.breakTime--;
            if (table.breakTime == 0) {
                table.breakOne(level);
                changed = true;
            }
        } else {
            table.breakTime = 0;
        }
        if (changed) {
            table.sync();
        }
    }

    public ContainerData data() {
        return data;
    }

    public @Nullable String aspectId() {
        return aspectId;
    }

    public int breakTime() {
        return breakTime;
    }

    public boolean clearAspect(String expectedAspect) {
        if (aspectId == null || !aspectId.equals(expectedAspect)) {
            return false;
        }
        aspectId = null;
        sync();
        return true;
    }

    private boolean canBreak() {
        return aspectId == null
                && ItemAspectRegistry.aspects(items.get(INPUT_SLOT))
                .filter(aspects -> !aspects.isEmpty())
                .isPresent();
    }

    private void breakOne(ServerLevel level) {
        if (!canBreak()) {
            return;
        }
        ItemStack input = items.get(INPUT_SLOT);
        Map<String, Integer> aspects = ItemAspectRegistry.aspects(input)
                .orElse(Map.of());
        aspectId = DeconstructionTableLogic.rollDiscovery(
                aspects,
                AspectRegistryRuntime.catalog(),
                level.random
        ).orElse(null);
        input.shrink(1);
        if (input.isEmpty()) {
            items.set(INPUT_SLOT, ItemStack.EMPTY);
        }
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            Containers.dropContents(level, worldPosition, this);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        if (aspectId != null) {
            tag.putString("Aspect", aspectId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        aspectId = tag.contains("Aspect", CompoundTag.TAG_STRING)
                ? tag.getString("Aspect")
                : null;
        if (aspectId != null && aspectId.isBlank()) {
            aspectId = null;
        }
        // TC4 deliberately did not persist partial break progress.
        breakTime = 0;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet
    ) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? NO_SLOTS : INPUT_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(
            int slot,
            ItemStack stack,
            @Nullable Direction direction
    ) {
        return direction != Direction.UP && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(
            int slot,
            ItemStack stack,
            Direction direction
    ) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(INPUT_SLOT).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == INPUT_SLOT ? items.get(INPUT_SLOT) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            sync();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != INPUT_SLOT) {
            return;
        }
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        sync();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D
                ) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT
                && !stack.isEmpty()
                && ItemAspectRegistry.aspects(stack)
                .filter(aspects -> !aspects.isEmpty())
                .isPresent();
    }

    @Override
    public void clearContent() {
        items.clear();
        sync();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(
                "container.thaumic_reborn.deconstruction_table"
        );
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(
            int containerId,
            Inventory inventory,
            Player player
    ) {
        return new DeconstructionTableMenu(
                containerId,
                inventory,
                this,
                data
        );
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && side != null) {
            return sidedHandlers[side.ordinal()].cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<IItemHandlerModifiable> handler : sidedHandlers) {
            handler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        sidedHandlers = createSidedHandlers();
    }

    private LazyOptional<IItemHandlerModifiable>[] createSidedHandlers() {
        return SidedInvWrapper.create(this, Direction.values());
    }
}
