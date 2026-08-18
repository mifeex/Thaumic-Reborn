package com.thaumcraftmodern.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

/** TC4 Bone Bow: 512 durability, fast draw, 2.5 velocity and +0.5 arrow damage. */
public final class BoneBowItem extends BowItem {
    public BoneBowItem(Properties properties) {
        super(properties.durability(512));
    }

    public static float getBoneBowPowerForTime(int charge) {
        return BoneBowMechanics.powerForTime(charge);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (getUseDuration(stack) - remaining >= BoneBowMechanics.FORCED_RELEASE_TICKS) {
            living.releaseUsingItem();
        }
    }

    @Override
    public int getEnchantmentValue() {
        return 3;
    }

    @Override
    public void releaseUsing(ItemStack bow, Level level, LivingEntity living, int timeLeft) {
        if (!(living instanceof Player player)) return;

        boolean infiniteBow = player.getAbilities().instabuild
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
        ItemStack ammunition = player.getProjectile(bow);
        int charge = getUseDuration(bow) - timeLeft;
        charge = ForgeEventFactory.onArrowLoose(bow, level, player, charge,
                !ammunition.isEmpty() || infiniteBow);
        if (charge < 0 || (ammunition.isEmpty() && !infiniteBow)) return;
        if (ammunition.isEmpty()) ammunition = new ItemStack(Items.ARROW);

        float power = getBoneBowPowerForTime(charge);
        if (power < 0.1F) return;
        boolean infiniteAmmo = player.getAbilities().instabuild
                || ammunition.getItem() instanceof ArrowItem arrowItem
                && arrowItem.isInfinite(ammunition, bow, player);

        if (!level.isClientSide) {
            ArrowItem arrowItem = ammunition.getItem() instanceof ArrowItem item
                    ? item : (ArrowItem) Items.ARROW;
            AbstractArrow arrow = arrowItem.createArrow(level, ammunition, player);
            arrow = customArrow(arrow);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.0F, power * BoneBowMechanics.ARROW_VELOCITY, 1.0F);
            arrow.setBaseDamage(arrow.getBaseDamage() + 0.5D);

            int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
            if (powerLevel > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5D + 0.5D);
            }
            int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
            if (punchLevel > 0) arrow.setKnockback(punchLevel);
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) {
                arrow.setSecondsOnFire(100);
            }

            bow.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(player.getUsedItemHand()));
            if (infiniteAmmo) arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(arrow);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        if (!infiniteAmmo && !player.getAbilities().instabuild) {
            ammunition.shrink(1);
            if (ammunition.isEmpty()) player.getInventory().removeItem(ammunition);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }
}
