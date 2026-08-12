package com.thaumcraftmodern.item;

import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;

public final class VoidPickaxeItem extends PickaxeItem implements ThaumcraftRepairable {
    public VoidPickaxeItem(Properties properties) { super(VoidTier.INSTANCE, 1, -2.8F, properties); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        VoidItemMechanics.applySapless(target, attacker, 80);
        return super.hurtEnemy(stack, target, attacker);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        VoidItemMechanics.repairOnePerSecond(stack, level, entity);
    }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return VoidItemMechanics.isPrimalCharm(repair) || super.isValidRepairItem(stack, repair);
    }
}
