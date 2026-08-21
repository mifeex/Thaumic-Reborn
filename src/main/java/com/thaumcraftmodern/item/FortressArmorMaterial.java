package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Fortress armor: netherite durability, defense 3/8/7/3 and 3 toughness. */
public enum FortressArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override public int getDurabilityForType(ArmorItem.Type type) {
        return baseDurability(type) * 37;
    }
    @Override public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS, HELMET -> 3;
            case LEGGINGS -> 7;
            case CHESTPLATE -> 8;
        };
    }
    @Override public int getEnchantmentValue() { return 25; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemTags.create(new ResourceLocation("forge", "ingots/thaumium")));
    }
    @Override public String getName() { return ThaumcraftModern.MOD_ID + ":fortress"; }
    @Override public float getToughness() { return 3.0F; }
    @Override public float getKnockbackResistance() { return 0; }

    private static int baseDurability(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 13;
            case LEGGINGS -> 15;
            case CHESTPLATE -> 16;
            case HELMET -> 11;
        };
    }
}
