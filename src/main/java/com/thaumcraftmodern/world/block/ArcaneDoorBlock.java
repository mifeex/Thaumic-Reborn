package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.world.block.entity.ArcaneDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class ArcaneDoorBlock extends DoorBlock implements EntityBlock {
    public ArcaneDoorBlock(Properties properties){super(properties,BlockSetType.IRON);}
    private static BlockPos base(BlockState state,BlockPos pos){return state.getValue(HALF)==DoubleBlockHalf.UPPER?pos.below():pos;}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        BlockPos base=base(state,pos);if(!(level.getBlockEntity(base) instanceof ArcaneDoorBlockEntity door))return InteractionResult.CONSUME;
        if(!door.canOpen(player.getGameProfile().getName())){if(!level.isClientSide)player.displayClientMessage(Component.translatable("message.thaumic_reborn.door_refuses"),true);return InteractionResult.sidedSuccess(level.isClientSide);}
        if(!level.isClientSide)setOpen(player,level,level.getBlockState(base),base,!level.getBlockState(base).getValue(OPEN));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,@Nullable LivingEntity placer,ItemStack stack){super.setPlacedBy(level,pos,state,placer,stack);String owner=placer instanceof Player player?player.getGameProfile().getName():"";if(level.getBlockEntity(pos) instanceof ArcaneDoorBlockEntity lower)lower.setOwner(owner);if(level.getBlockEntity(pos.above()) instanceof ArcaneDoorBlockEntity upper)upper.setOwner(owner);}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ArcaneDoorBlockEntity(pos,state);}
}
