package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import com.thaumcraftmodern.client.render.FortressArmorClientExtensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/** Thaumium Fortress armor with infusion-added goggles and one exclusive mask. */
public final class FortressArmorItem extends ArmorItem
        implements ThaumcraftRepairable, RevealingGear {
    public static final String MASK_TAG = "mask";
    public static final String GOGGLES_TAG = "goggles";

    private final @Nullable Integer builtInMask;

    public FortressArmorItem(Type type, Properties properties) {
        this(type, null, properties);
    }

    public FortressArmorItem(Type type, @Nullable Integer builtInMask,
            Properties properties) {
        super(FortressArmorMaterial.INSTANCE, type, properties);
        if (builtInMask != null && (builtInMask < 0 || builtInMask > 2)) {
            throw new IllegalArgumentException("Fortress mask must be 0..2");
        }
        this.builtInMask = builtInMask;
    }

    public static @Nullable Integer mask(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(MASK_TAG)) {
            int mask = stack.getTag().getInt(MASK_TAG);
            if (mask >= 0 && mask < 3) return mask;
        }
        return stack.getItem() instanceof FortressArmorItem armor
                ? armor.builtInMask : null;
    }

    public static boolean hasGoggles(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(GOGGLES_TAG);
    }

    public static ItemStack withMask(ItemStack stack, int mask) {
        ItemStack result = stack.copy();
        result.getOrCreateTag().putInt(MASK_TAG, mask);
        return result;
    }

    public static ItemStack withGoggles(ItemStack stack) {
        ItemStack result = stack.copy();
        result.getOrCreateTag().putBoolean(GOGGLES_TAG, true);
        return result;
    }

    @Override public boolean reveals(ItemStack stack) { return hasGoggles(stack); }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return builtInMask == null ? super.getDescriptionId(stack)
                : "item.thaumcraftmodern.fortress_helmet";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (hasGoggles(stack)) tooltip.add(Component.translatable(
                "item.thaumcraftmodern.goggles_of_revealing")
                .withStyle(ChatFormatting.DARK_PURPLE));
        Integer mask = mask(stack);
        if (mask != null) tooltip.add(Component.translatable(
                "item.HelmetFortress.mask." + mask)
                .withStyle(ChatFormatting.GOLD));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity,
            EquipmentSlot slot, String type) {
        return ThaumcraftModern.MOD_ID
                + ":textures/entity/models/fortress_armor.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(FortressArmorClientExtensions.create());
    }
}
