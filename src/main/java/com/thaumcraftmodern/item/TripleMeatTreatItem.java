package com.thaumcraftmodern.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** TC4's compact meat food, including its two post-meal effects. */
public final class TripleMeatTreatItem extends Item {
    public static final int REGENERATION_TICKS = 200;
    public static final int STRENGTH_TICKS = 600;

    public TripleMeatTreatItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level,
            LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION, REGENERATION_TICKS, 1));
            entity.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST, STRENGTH_TICKS, 0));
        }
        return result;
    }
}
