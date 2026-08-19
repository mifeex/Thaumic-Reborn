package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.ArcanePressurePlateBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    private byte setting;

    public ArcanePressurePlateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_PRESSURE_PLATE.get(), pos, state);
    }

    public String owner() { return owner; }
    public Set<String> access() { return Collections.unmodifiableSet(access); }
    public int setting() { return setting; }
    public void setOwner(String owner) { this.owner = owner == null ? "" : owner; sync(); }
    public void setSetting(int setting) {
        this.setting = (byte) Math.max(0, Math.min(2, setting));
        sync();
    }
    public boolean canEdit(String name) { return owner.equals(name) || access.contains("1" + name); }
    public boolean isKnown(String name) {
        return owner.equals(name) || access.contains("0" + name) || access.contains("1" + name);
    }
    public boolean hasAccess(String name, boolean gold) {
        return access.contains((gold ? "1" : "0") + name) || access.contains("1" + name);
    }
    public boolean grant(String name, boolean gold) {
        boolean changed = access.add((gold ? "1" : "0") + name);
        if (changed) sync();
        return changed;
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("owner", owner);
        tag.putByte("setting", setting);
        ListTag entries = new ListTag();
        for (String name : access) {
            CompoundTag entry = new CompoundTag();
            entry.putString("name", name);
            entries.add(entry);
        }
        tag.put("access", entries);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.getString("owner");
        setting = tag.contains("setting", Tag.TAG_BYTE)
                ? (byte) Math.max(0, Math.min(2, tag.getByte("setting")))
                : getBlockState().hasProperty(ArcanePressurePlateBlock.MODE)
                        ? getBlockState().getValue(ArcanePressurePlateBlock.MODE).byteValue() : 0;
        access.clear();
        if (tag.contains("access", Tag.TAG_LIST)) {
            ListTag entries = tag.getList("access", Tag.TAG_COMPOUND);
            for (int i = 0; i < entries.size(); i++) {
                String name = entries.getCompound(i).getString("name");
                if (!name.isEmpty()) access.add(name);
            }
        } else {
            // Migration from the first port implementation.
            String raw = tag.getString("access");
            if (!raw.isEmpty()) Collections.addAll(access, raw.split("\n"));
        }
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
