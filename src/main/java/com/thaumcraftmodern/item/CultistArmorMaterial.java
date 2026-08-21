package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Preserves the existing non-Praetor cultist armor values. */
public enum CultistArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 13 * 25;
            case LEGGINGS -> 15 * 25;
            case CHESTPLATE -> 16 * 25;
            case HELMET -> 11 * 25;
        };
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS, HELMET -> 2;
            case LEGGINGS -> 5;
            case CHESTPLATE -> 6;
        };
    }

    @Override public int getEnchantmentValue() { return 25; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
    @Override public Ingredient getRepairIngredient() {
        return ThaumiumArmorMaterial.INSTANCE.getRepairIngredient();
    }
    @Override public String getName() { return ThaumcraftModern.MOD_ID + ":cultist"; }
    @Override public float getToughness() { return 0.0F; }
    @Override public float getKnockbackResistance() { return 0.0F; }
}
