package com.thaumcraftmodern.item;

import com.thaumcraftmodern.world.block.ArcaneDoorBlock;
import com.thaumcraftmodern.world.block.entity.ArcaneDoorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class ArcaneDoorKeyItem extends Item {
    private final boolean gold;
    public ArcaneDoorKeyItem(boolean gold,Properties properties){super(properties);this.gold=gold;}
    @Override public boolean isFoil(ItemStack stack){return stack.hasTag()&&stack.getTag().contains("location");}
    @Override public InteractionResult useOn(UseOnContext context){
        var level=context.getLevel();BlockPos clicked=context.getClickedPos();var state=level.getBlockState(clicked);
        if(!(state.getBlock() instanceof ArcaneDoorBlock))return InteractionResult.PASS;
        BlockPos base=state.getValue(ArcaneDoorBlock.HALF)==DoubleBlockHalf.UPPER?clicked.below():clicked;
        if(!(level.getBlockEntity(base) instanceof ArcaneDoorBlockEntity door))return InteractionResult.PASS;
        ItemStack stack=context.getItemInHand();String name=context.getPlayer()==null?"":context.getPlayer().getGameProfile().getName();String location=base.getX()+","+base.getY()+","+base.getZ();CompoundTag tag=stack.getTag();
        if(tag==null||!tag.contains("location")){
            if(level.isClientSide)return InteractionResult.SUCCESS;
            if(name.equals(door.owner())||(!gold&&door.canMintIron(name))){ItemStack linked=new ItemStack(this);linked.getOrCreateTag().putString("location",location);if(!context.getPlayer().getInventory().add(linked))context.getPlayer().drop(linked,false);if(!context.getPlayer().getAbilities().instabuild)stack.shrink(1);context.getPlayer().displayClientMessage(Component.translatable("message.thaumic_reborn.key_linked"),true);}
            return InteractionResult.CONSUME;
        }
        if(!location.equals(tag.getString("location"))){if(!level.isClientSide&&context.getPlayer()!=null)context.getPlayer().displayClientMessage(Component.translatable("message.thaumic_reborn.key_wrong"),true);return InteractionResult.sidedSuccess(level.isClientSide);}
        if(!level.isClientSide&&!door.canOpen(name)){door.grant(name,gold);if(level.getBlockEntity(base.above()) instanceof ArcaneDoorBlockEntity upper)upper.grant(name,gold);door.setChanged();if(!context.getPlayer().getAbilities().instabuild)stack.shrink(1);context.getPlayer().displayClientMessage(Component.translatable("message.thaumic_reborn.key_granted"),true);}
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
