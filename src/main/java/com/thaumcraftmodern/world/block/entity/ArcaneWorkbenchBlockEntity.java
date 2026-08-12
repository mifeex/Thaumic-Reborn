package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.menu.ArcaneWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ArcaneWorkbenchBlockEntity extends BlockEntity implements MenuProvider {
    private final ArcaneCraftingInventory crafting = new ArcaneCraftingInventory();
    private final SimpleContainer wand = new SimpleContainer(1);

    public ArcaneWorkbenchBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ARCANE_WORKBENCH.get(), position, state);
        crafting.addListener(ignored -> setChanged());
        wand.addListener(ignored -> syncWand());
    }

    public ArcaneCraftingInventory crafting() {
        return crafting;
    }

    public SimpleContainer wand() {
        return wand;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Crafting", crafting.createTag());
        tag.put("Wand", wand.createTag());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        crafting.fromTag(tag.getList("Crafting", Tag.TAG_COMPOUND));
        wand.fromTag(tag.getList("Wand", Tag.TAG_COMPOUND));
        // The menu may be opened immediately after the block entity is loaded.
        // Mark both containers dirty so the restored slot state is propagated
        // through the normal menu/container synchronization path.
        crafting.setChanged();
        wand.setChanged();
        setChanged();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("Wand", wand.createTag());
        return tag;
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
            handleUpdateTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        // Crafting slots are synchronized by ArcaneWorkbenchMenu. Reloading
        // them from a block-entity packet races the menu slot packets whenever
        // the wand is inserted or removed and can restore an older grid layout.
        // The block-entity renderer only needs the wand, so keep this update
        // deliberately isolated from the persistent crafting inventory.
        wand.fromTag(tag.getList("Wand", Tag.TAG_COMPOUND));
    }

    private void syncWand() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        Containers.dropContents(level, worldPosition, crafting);
        Containers.dropContents(level, worldPosition, wand);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.thaumcraftmodern.arcane_workbench");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(
            int containerId,
            Inventory inventory,
            Player player
    ) {
        return new ArcaneWorkbenchMenu(containerId, inventory, this);
    }
}
