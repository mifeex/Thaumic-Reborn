package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import com.thaumcraftmodern.client.render.VoidArmorClientExtensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class VoidArmorItem extends ArmorItem implements ThaumcraftRepairable {
    public VoidArmorItem(Type type, Properties properties) { super(VoidArmorMaterial.INSTANCE, type, properties); }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(VoidArmorClientExtensions.create());
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        VoidItemMechanics.repairOnePerSecond(stack, level, entity);
    }
    @Override public void onArmorTick(ItemStack stack, Level level, Player player) {
        VoidItemMechanics.repairOnePerSecond(stack, level, player);
    }
    @Override public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return ThaumcraftModern.MOD_ID + ":textures/models/void_" + (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png";
    }
}
