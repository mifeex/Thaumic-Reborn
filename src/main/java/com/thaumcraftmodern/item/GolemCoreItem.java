package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.GolemCoreType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class GolemCoreItem extends Item {
    private final GolemCoreType type;

    public GolemCoreItem(GolemCoreType type) {
        super(new Item.Properties().rarity(Rarity.UNCOMMON));
        this.type = type;
    }

    public GolemCoreType type() { return type; }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.thaumic_reborn.golem_core." + type.id())
                .withStyle(ChatFormatting.GOLD));
    }
}
