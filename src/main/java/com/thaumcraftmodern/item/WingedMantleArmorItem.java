package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.render.WingedMantleClientExtensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Battle robes whose chest piece doubles as a durable elytra. */
public final class WingedMantleArmorItem extends ArmorItem {
    public WingedMantleArmorItem(Type type, Properties properties) {
        super(WingedMantleArmorMaterial.INSTANCE, type, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity,
                                  EquipmentSlot slot, String type) {
        if (slot == EquipmentSlot.LEGS) {
            return ThaumcraftModern.MOD_ID
                    + ":textures/entity/models/winged_mantle_leggings.png";
        }
        return ThaumcraftModern.MOD_ID
                + ":textures/entity/models/winged_mantle_armor.png";
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return getType() == Type.CHESTPLATE
                && stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity,
                                    int flightTicks) {
        if (!entity.level().isClientSide && (flightTicks + 1) % 20 == 0) {
            stack.hurtAndBreak(1, entity,
                    broken -> broken.broadcastBreakEvent(EquipmentSlot.CHEST));
        }
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(WingedMantleClientExtensions.create());
    }
}
