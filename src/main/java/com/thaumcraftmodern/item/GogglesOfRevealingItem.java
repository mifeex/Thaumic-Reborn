package com.thaumcraftmodern.item;

import com.thaumcraftmodern.api.wand.VisDiscountGear;
import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class GogglesOfRevealingItem extends ArmorItem
        implements VisDiscountGear, ThaumcraftRepairable, RevealingGear {
    public static final int VIS_DISCOUNT_PERCENT = 5;

    public GogglesOfRevealingItem(Properties properties) {
        super(RevealingArmorMaterial.INSTANCE, Type.HELMET, properties);
    }

    @Override public boolean reveals(ItemStack stack) { return true; }

    @Override
    public int visDiscountPercent(
            ItemStack stack,
            Player player,
            PrimalAspect aspect
    ) {
        return VIS_DISCOUNT_PERCENT;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(
                Component.translatable("tc.visdiscount")
                        .append(": " + VIS_DISCOUNT_PERCENT + "%")
                        .withStyle(ChatFormatting.DARK_PURPLE)
        );
    }

    @Override
    public String getArmorTexture(
            ItemStack stack,
            Entity entity,
            EquipmentSlot slot,
            String type
    ) {
        return "thaumic_reborn:textures/models/goggles.png";
    }
}
