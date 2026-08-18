package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.GolemDecorationType;
import net.minecraft.world.item.Item;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Installable TC4 golem accessory. Installation itself is owned by the golem entity. */
public final class GolemDecorationItem extends Item {
    private final GolemDecorationType type;

    public GolemDecorationItem(GolemDecorationType type) {
        super(new Item.Properties());
        this.type = type;
    }

    public GolemDecorationType type() { return type; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".description")
                .withStyle(ChatFormatting.GRAY));
    }
}
