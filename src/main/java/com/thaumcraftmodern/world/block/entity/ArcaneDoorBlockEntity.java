package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashSet;
import java.util.Set;

public final class ArcaneDoorBlockEntity extends BlockEntity {
    private String owner=""; private final Set<String> access=new HashSet<>();
    public ArcaneDoorBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.ARCANE_DOOR.get(),pos,state);}
    public String owner(){return owner;} public void setOwner(String value){owner=value==null?"":value;sync();}
    public boolean canOpen(String name){return name.equals(owner)||access.contains("0"+name)||access.contains("1"+name);}
    public boolean canMintIron(String name){return name.equals(owner)||access.contains("1"+name);}
    public boolean hasAccess(String name,boolean gold){return access.contains((gold?"1":"0")+name)||access.contains("1"+name);}
    public boolean grant(String name,boolean gold){boolean changed=access.add((gold?"1":"0")+name);if(changed)sync();return changed;}
    private void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putString("owner",owner);tag.putString("access",String.join("\n",access));}
    @Override public void load(CompoundTag tag){super.load(tag);owner=tag.getString("owner");access.clear();String raw=tag.getString("access");if(!raw.isEmpty())java.util.Collections.addAll(access,raw.split("\\n"));}
    @Override public CompoundTag getUpdateTag(){return saveWithoutMetadata();}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public void onDataPacket(Connection net,ClientboundBlockEntityDataPacket packet){CompoundTag tag=packet.getTag();if(tag!=null)load(tag);}
}
