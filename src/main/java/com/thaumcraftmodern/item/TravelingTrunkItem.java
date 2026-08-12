package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.TravelingTrunkEntity;
import com.thaumcraftmodern.registry.ModEntities;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import java.util.List;
import net.minecraft.world.item.context.UseOnContext;

public final class TravelingTrunkItem extends Item {
    public TravelingTrunkItem(Properties properties) { super(properties); }

    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) return InteractionResult.PASS;
        TravelingTrunkEntity trunk = ModEntities.TRAVELING_TRUNK.get().create(level);
        if (trunk == null) return InteractionResult.FAIL;
        var pos = context.getClickedPos().relative(context.getClickedFace());
        trunk.moveTo(pos.getX() + .5D, pos.getY(), pos.getZ() + .5D,
                context.getHorizontalDirection().toYRot() + 180F, 0F);
        trunk.setOwner(context.getPlayer().getUUID());
        if (context.getItemInHand().hasTag() && context.getItemInHand().getTag()
                .contains("TrunkData", Tag.TAG_COMPOUND)) {
            trunk.loadPortableData(context.getItemInHand().getTag().getCompound("TrunkData"));
        }
        if (context.getItemInHand().hasCustomHoverName()) trunk.setCustomName(context.getItemInHand().getHoverName());
        if (!level.noCollision(trunk) || !level.addFreshEntity(trunk)) return InteractionResult.FAIL;
        if (!context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag flag) {
        if (!stack.hasTag() || !stack.getTag().contains("TrunkData", Tag.TAG_COMPOUND)) return;
        var data = stack.getTag().getCompound("TrunkData");
        int upgrade = data.contains("Upgrade") ? data.getInt("Upgrade") : -1;
        if (upgrade >= 0) lines.add(Component.translatable(
                "item.ItemGolemUpgrade." + upgrade + ".name").withStyle(ChatFormatting.BLUE));
        if (data.contains("Items", Tag.TAG_LIST)) lines.add(Component.translatable(
                "item.TrunkSpawner.text.1").withStyle(ChatFormatting.GRAY));
    }
}
