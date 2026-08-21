package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.TallowCandleBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** One registry item whose BlockStateTag carries the candle's dye color. */
public final class TallowCandleItem extends BlockItem {
    public TallowCandleItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        DyeColor color = TallowCandleBlock.color(stack);
        String id = color == DyeColor.WHITE
                ? "tallow_candle"
                : color.getName() + "_tallow_candle";
        return Component.translatable("block." + ThaumcraftModern.MOD_ID + "." + id);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity,
            int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        lockCurrentColor(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        lockCurrentColor(context.getItemInHand());
        return super.useOn(context);
    }

    private static void lockCurrentColor(ItemStack stack) {
        if (!TallowCandleBlock.hasStoredColor(stack)) {
            TallowCandleBlock.storeColor(stack, TallowCandleBlock.color(stack));
        }
    }
}
