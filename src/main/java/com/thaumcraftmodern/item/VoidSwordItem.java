package com.thaumcraftmodern.item;

import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

public final class VoidSwordItem extends SwordItem implements ThaumcraftRepairable {
    public VoidSwordItem(Properties properties) { super(VoidTier.INSTANCE, 3, -2.4F, properties); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        VoidItemMechanics.applySapless(target, attacker, 60);
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
