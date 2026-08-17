package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.item.ResearchNotesItem;
import com.thaumcraftmodern.item.ScribingToolsItem;
import com.thaumcraftmodern.item.DiscoveryItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.menu.ResearchTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResearchTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SCRIBING_TOOLS_SLOT = 0;
    public static final int NOTES_SLOT = 1;

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SCRIBING_TOOLS_SLOT -> stack.getItem() instanceof ScribingToolsItem;
                case NOTES_SLOT -> stack.getItem() instanceof ResearchNotesItem
                        || stack.getItem() instanceof DiscoveryItem;
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (slot == NOTES_SLOT) {
                ResearchNotesItem.ensureInitialized(getStackInSlot(slot));
            }
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(
                        worldPosition,
                        getBlockState(),
                        getBlockState(),
                        3
                );
            }
        }
    };
    private LazyOptional<ItemStackHandler> itemCapability = createItemCapability();

    public ResearchTableBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.RESEARCH_TABLE.get(), position, state);
    }

    public ItemStackHandler items() {
        return items;
    }

    @Override
    public AABB getRenderBoundingBox() {
        // The classic research table BER draws a two-block tabletop plus the
        // parchment and quill outside the owning block's default unit AABB.
        // LevelRenderer still frustum-tests this box even though the renderer
        // itself opts into off-screen rendering.
        return new AABB(worldPosition).inflate(1.5D, 1.0D, 1.5D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
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
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = createItemCapability();
    }

    private LazyOptional<ItemStackHandler> createItemCapability() {
        return LazyOptional.of(() -> items);
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(
                        level,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D,
                        stack
                );
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.thaumic_reborn.research_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ResearchTableMenu(containerId, inventory, this);
    }
}
