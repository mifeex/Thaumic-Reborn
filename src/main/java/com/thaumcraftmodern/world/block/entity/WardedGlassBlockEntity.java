package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Owner data from TC4 TileOwned used by warded glass. */
public final class WardedGlassBlockEntity extends BlockEntity {
    private String owner = "";

    public WardedGlassBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARDED_GLASS.get(), pos, state);
    }

    public String owner() { return owner; }

    public void setOwner(String owner) {
        this.owner = owner == null ? "" : owner;
        setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("owner", owner);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.getString("owner");
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection,
            ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
