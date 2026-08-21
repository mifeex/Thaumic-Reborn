package com.thaumcraftmodern.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

public final class VoidSwordItem extends SwordItem implements ThaumcraftRepairable {
    private static final double ATTACK_DAMAGE_MODIFIER = 6.5D;

    public VoidSwordItem(Properties properties) { super(VoidTier.INSTANCE, 3, -2.4F, properties); }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(
            EquipmentSlot slot
    ) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        return ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                        BASE_ATTACK_DAMAGE_UUID,
                        "Weapon modifier",
                        ATTACK_DAMAGE_MODIFIER,
                        AttributeModifier.Operation.ADDITION
                ))
                .put(Attributes.ATTACK_SPEED, new AttributeModifier(
                        BASE_ATTACK_SPEED_UUID,
                        "Weapon modifier",
                        -2.4D,
                        AttributeModifier.Operation.ADDITION
                ))
                .build();
    }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        VoidItemMechanics.applySapless(target, attacker, 60);
        return super.hurtEnemy(stack, target, attacker);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        VoidItemMechanics.repairOnePerSecond(stack, level, entity);
    }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return VoidItemMechanics.isPrimalCharm(repair) || super.isValidRepairItem(stack, repair);
    }
}
