package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Original special-armor ratios plus the Angry Ghost and Sipping Fiend procs. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class FortressArmorEvents {
    static final float MAX_SPECIAL_PROTECTION = 0.80F;

    private FortressArmorEvents() { }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        float original = event.getAmount();
        if (event.getSource().getEntity() instanceof Player attacker) {
            ItemStack helmet = attacker.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getItem() instanceof FortressArmorItem
                    && Integer.valueOf(2).equals(FortressArmorItem.mask(helmet))
                    && attacker.getRandom().nextFloat() < original / 12.0F) {
                attacker.heal(1.0F);
            }
        }
        LivingEntity victim = event.getEntity();
        ItemStack helmet = victim.getItemBySlot(EquipmentSlot.HEAD);
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && helmet.getItem() instanceof FortressArmorItem
                && Integer.valueOf(1).equals(FortressArmorItem.mask(helmet))
                && victim.getRandom().nextFloat() < original / 10.0F) {
            attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
        }
        float ratio = protectionRatio(victim, event);
        if (ratio <= 0.0F) return;
        float wanted = original * Math.max(0.0F, 1.0F - ratio);
        if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            event.setAmount(wanted);
            return;
        }
        float armor = victim.getArmorValue();
        float toughness = (float) victim.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        event.setAmount(beforeVanillaArmor(wanted, armor, toughness));
    }

    static float protectionRatio(LivingEntity entity, LivingHurtEvent event) {
        int defense = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.getItem() instanceof FortressArmorItem armor) {
                defense += armor.getMaterial().getDefenseForType(armor.getType());
            }
        }
        if (defense == 0 || event.getSource().is(DamageTypeTags.IS_FIRE)) return 0;
        float set = 0.875F;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.LEGS,
                EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.getItem() instanceof FortressArmorItem) {
                set += 0.125F;
                if (FortressArmorItem.mask(stack) != null) set += 0.05F;
            }
        }
        float denominator = event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)
                ? 35.0F
                : event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)
                        || event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                        ? 20.0F : 25.0F;
        return Math.min(MAX_SPECIAL_PROTECTION, defense / denominator * set);
    }

    private static float beforeVanillaArmor(float wanted, float armor,
            float toughness) {
        if (wanted <= 0 || armor <= 0) return wanted;
        float low = wanted;
        float high = Math.max(wanted, 1.0F);
        while (CombatRules.getDamageAfterAbsorb(high, armor, toughness) < wanted
                && high < 100000.0F) high *= 2.0F;
        for (int iteration = 0; iteration < 24; iteration++) {
            float middle = (low + high) * 0.5F;
            if (CombatRules.getDamageAfterAbsorb(middle, armor, toughness) < wanted) {
                low = middle;
            } else high = middle;
        }
        return high;
    }
}
