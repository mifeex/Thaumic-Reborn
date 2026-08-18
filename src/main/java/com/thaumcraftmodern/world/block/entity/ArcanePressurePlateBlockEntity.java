package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** TC4 TileOwned data used by the arcane pressure plate. */
public final class ArcanePressurePlateBlockEntity extends BlockEntity {
    private String owner = "";
    private final Set<String> access = new HashSet<>();

    public ArcanePressurePlateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_PRESSURE_PLATE.get(), pos, state);
    }

    public String owner() { return owner; }
    public Set<String> access() { return Collections.unmodifiableSet(access); }
    public void setOwner(String owner) { this.owner = owner == null ? "" : owner; sync(); }
    public boolean canEdit(String name) { return owner.equals(name) || access.contains("1" + name); }
    public boolean isKnown(String name) {
        return owner.equals(name) || access.contains("0" + name) || access.contains("1" + name);
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("owner", owner);
        tag.putString("access", String.join("\n", access));
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.getString("owner");
        access.clear();
        String raw = tag.getString("access");
        if (!raw.isEmpty()) Collections.addAll(access, raw.split("\n"));
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
