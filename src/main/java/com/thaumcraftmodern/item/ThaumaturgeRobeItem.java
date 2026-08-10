package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import com.thaumcraftmodern.api.wand.VisDiscountGear;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.client.render.ThaumaturgeRobeClientExtensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Faithful enchanted-fabric robe piece from TC4. */
public final class ThaumaturgeRobeItem extends ArmorItem
        implements DyeableLeatherItem, VisDiscountGear, ThaumcraftRepairable {
    public static final int DEFAULT_COLOR = 0x6A3880;

    private static final ResourceLocation ENCHANTED_FABRIC =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "enchanted_fabric"
            );

    public ThaumaturgeRobeItem(Type type, Properties properties) {
        super(ThaumaturgeRobeArmorMaterial.INSTANCE, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ThaumaturgeRobeClientExtensions.create());
    }

    @Override
    public int visDiscountPercent(
            ItemStack stack,
            Player player,
            PrimalAspect aspect
    ) {
        return getType() == Type.BOOTS ? 1 : 2;
    }

    @Override
    public int getColor(ItemStack stack) {
        return hasCustomColor(stack)
                ? DyeableLeatherItem.super.getColor(stack)
                : DEFAULT_COLOR;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return ENCHANTED_FABRIC.equals(
                ForgeRegistries.ITEMS.getKey(repair.getItem())
        ) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        int discount = getType() == Type.BOOTS ? 1 : 2;
        tooltip.add(
                Component.translatable("tc.visdiscount")
                        .append(": " + discount + "%")
                        .withStyle(ChatFormatting.DARK_PURPLE)
        );
    }

    @Override
    public String getArmorTexture(
            ItemStack stack,
            Entity entity,
            EquipmentSlot slot,
            String type
    ) {
        String layer = slot == EquipmentSlot.LEGS ? "robes_2" : "robes_1";
        String suffix = type == null ? "" : "_overlay";
        return ThaumcraftModern.MOD_ID
                + ":textures/models/" + layer + suffix + ".png";
    }
}
