package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Exact TC4 VOIDFORTRESS material: durability 18, defense 4/8/7/4, enchantability 10. */
public enum VoidRobeArmorMaterial implements ArmorMaterial {
    INSTANCE;
    @Override public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) { case BOOTS -> 234; case LEGGINGS -> 270; case CHESTPLATE -> 288; case HELMET -> 198; };
    }
    @Override public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) { case BOOTS, HELMET -> 4; case LEGGINGS -> 7; case CHESTPLATE -> 8; };
    }
    @Override public int getEnchantmentValue() { return 10; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemTags.create(new ResourceLocation("forge", "ingots/void")));
    }
    @Override public String getName() { return ThaumcraftModern.MOD_ID + ":void_robe"; }
    @Override public float getToughness() { return 0; }
    @Override public float getKnockbackResistance() { return 0; }
}
