package com.thaumcraftmodern.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/** TC4 PRIMALVOID material: level 5, 500 uses, speed 8, damage 4, enchantability 20. */
public enum PrimalCrusherTier implements Tier {
    INSTANCE;

    @Override public int getUses() { return 500; }
    @Override public float getSpeed() { return 8.0F; }
    @Override public float getAttackDamageBonus() { return 4.0F; }
    @Override public int getLevel() { return 5; }
    @Override public int getEnchantmentValue() { return 20; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemTags.create(
                new ResourceLocation("thaumic_reborn", "primal_charms")
        ));
    }
    @Override public TagKey<Block> getTag() {
        return BlockTags.create(new ResourceLocation(
                "thaumic_reborn", "mineable/primal_crusher"
        ));
    }
}
