package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import com.thaumcraftmodern.api.wand.VisDiscountGear;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.client.render.VoidRobeClientExtensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Consumer;

public final class VoidRobeArmorItem extends ArmorItem implements DyeableLeatherItem,
        VisDiscountGear, ThaumcraftRepairable, RevealingGear {
    /** TC4 ItemRobeArmor.DEFAULT_ROBE_COLOR (6961280). */
    public static final int DEFAULT_COLOR = 0x6A3880;
    public VoidRobeArmorItem(Type type, Properties properties) { super(VoidRobeArmorMaterial.INSTANCE, type, properties); }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(VoidRobeClientExtensions.create());
    }
    @Override public int visDiscountPercent(ItemStack stack, Player player, PrimalAspect aspect) { return 5; }
    @Override public boolean reveals(ItemStack stack) { return getType() == Type.HELMET; }
    @Override public int getColor(ItemStack stack) {
        return hasCustomColor(stack) ? DyeableLeatherItem.super.getColor(stack) : DEFAULT_COLOR;
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        VoidItemMechanics.repairOnePerSecond(stack, level, entity);
    }
    @Override public void onArmorTick(ItemStack stack, Level level, Player player) {
        VoidItemMechanics.repairOnePerSecond(stack, level, player);
    }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tc.visdiscount").append(": 5%").withStyle(ChatFormatting.DARK_PURPLE));
    }
    @Override public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return ThaumcraftModern.MOD_ID + ":textures/models/void_robe_armor"
                + (type == null ? "_overlay" : "") + ".png";
    }
}
