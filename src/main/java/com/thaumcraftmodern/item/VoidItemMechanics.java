package com.thaumcraftmodern.item;

import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Shared server-authoritative behavior of TC4 void tools and armor. */
public final class VoidItemMechanics {
    private VoidItemMechanics() { }

    public static void repairOnePerSecond(ItemStack stack, Level level, Entity owner) {
        if (!level.isClientSide && stack.isDamaged() && owner != null
                && owner.tickCount % 20 == 0) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
        }
    }

    public static void applySapless(LivingEntity target, LivingEntity attacker,
            int durationTicks) {
        if (target.level().isClientSide) return;
        if (target instanceof ServerPlayer && attacker instanceof ServerPlayer player
                && player.server != null && !player.server.isPvpAllowed()) return;
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, durationTicks));
    }

    /** TC4 accepts both the material repair item and a Primal Charm. */
    public static boolean isPrimalCharm(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.ARCANE_RECIPE_COMPONENTS
                .get("primal_charm").get());
    }
}
