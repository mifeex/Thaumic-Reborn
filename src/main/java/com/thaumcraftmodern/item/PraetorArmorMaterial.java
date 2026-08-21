package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Praetor armor: iron durability plus 20, with defense 3/7/6. */
public enum PraetorArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return baseDurability(type) * 15 + 20;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS, HELMET -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 7;
        };
    }

    @Override public int getEnchantmentValue() { return 25; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
    @Override public Ingredient getRepairIngredient() {
        return ThaumiumArmorMaterial.INSTANCE.getRepairIngredient();
    }
    @Override public String getName() { return ThaumcraftModern.MOD_ID + ":praetor"; }
    @Override public float getToughness() { return 0.0F; }
    @Override public float getKnockbackResistance() { return 0.0F; }

    private static int baseDurability(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 13;
            case LEGGINGS -> 15;
            case CHESTPLATE -> 16;
            case HELMET -> 11;
        };
    }
}
