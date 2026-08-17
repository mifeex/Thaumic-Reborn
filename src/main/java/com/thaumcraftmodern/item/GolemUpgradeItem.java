package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.GolemUpgradeType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class GolemUpgradeItem extends Item {
    private final GolemUpgradeType type;

    public GolemUpgradeItem(GolemUpgradeType type) {
        super(new Item.Properties().rarity(Rarity.UNCOMMON));
        this.type = type;
    }

    public GolemUpgradeType type() { return type; }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.thaumic_reborn.golem_upgrade." + type.id())
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
