package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Exact TC4 VOID armor material: durability 10, defense 3/7/6/3, enchantability 10. */
public enum VoidArmorMaterial implements ArmorMaterial {
    INSTANCE;
    @Override public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) { case BOOTS -> 130; case LEGGINGS -> 150; case CHESTPLATE -> 160; case HELMET -> 110; };
    }
    @Override public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) { case BOOTS, HELMET -> 3; case LEGGINGS -> 6; case CHESTPLATE -> 7; };
    }
    @Override public int getEnchantmentValue() { return 10; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemTags.create(new ResourceLocation("forge", "ingots/void")));
    }
    @Override public String getName() { return ThaumcraftModern.MOD_ID + ":void"; }
    @Override public float getToughness() { return 0; }
    @Override public float getKnockbackResistance() { return 0; }
}
