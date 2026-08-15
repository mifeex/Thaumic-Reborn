package com.thaumcraftmodern.item;

import com.thaumcraftmodern.world.block.EldritchAltarPartBlock;
import com.thaumcraftmodern.world.block.entity.EldritchAltarPartBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

/** Inserts the four original eyes into an eldritch altar and opens the gate. */
public final class EldritchEyeItem extends Item {
    public EldritchEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(
                context.getClickedPos()
        );
        if (!(state.getBlock() instanceof EldritchAltarPartBlock)
                || state.getValue(EldritchAltarPartBlock.PART) != 0) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)
                || !(level.getBlockEntity(context.getClickedPos())
                        instanceof EldritchAltarPartBlockEntity altar)
                || !altar.insertEye(level)) {
            return InteractionResult.FAIL;
        }
        if (context.getPlayer() == null
                || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
