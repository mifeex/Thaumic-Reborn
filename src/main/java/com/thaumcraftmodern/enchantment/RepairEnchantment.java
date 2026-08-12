package com.thaumcraftmodern.enchantment;

import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

/** TC4 4.2.3.5 EnchantmentRepair. */
public final class RepairEnchantment extends Enchantment {
    private static final EnchantmentCategory REPAIRABLE =
            EnchantmentCategory.create(
                    "thaumcraft_repairable",
                    item -> item instanceof ThaumcraftRepairable
                            || item instanceof
                            com.thaumicreborn.api.equipment.ThaumicRepairable
            );

    public RepairEnchantment() {
        super(Rarity.RARE, REPAIRABLE, EquipmentSlot.values());
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return 51;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.isDamageableItem()
                && (stack.getItem() instanceof ThaumcraftRepairable
                || stack.getItem() instanceof
                        com.thaumicreborn.api.equipment.ThaumicRepairable
                || stack.getItem() instanceof BookItem);
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != Enchantments.MENDING;
    }
}
