package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/** Exact TC4 VOID material: level 4, 600 uses, speed 8, damage 3, enchantability 20. */
public enum VoidTier implements Tier {
    INSTANCE;

    @Override public int getUses() { return 600; }
    @Override public float getSpeed() { return 8.0F; }
    @Override public float getAttackDamageBonus() { return 3.0F; }
    @Override public int getLevel() { return 4; }
    @Override public int getEnchantmentValue() { return 20; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemTags.create(new ResourceLocation("forge", "ingots/void")));
    }
    @Override public TagKey<Block> getTag() { return BlockTags.NEEDS_DIAMOND_TOOL; }
}
