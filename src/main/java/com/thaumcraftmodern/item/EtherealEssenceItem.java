package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.PrimalAspectColors;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * TC4 ItemWispEssence equivalent. One stack carries exactly one aspect.
 */
public final class EtherealEssenceItem extends Item {
    private static final String ASPECT_KEY = "Aspect";
    private static final String AMOUNT_KEY = "Amount";

    public EtherealEssenceItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(
            Item item,
            PrimalAspect aspect,
            int amount
    ) {
        return create(item, aspect.id(), amount);
    }

    public static ItemStack create(
            Item item,
            String aspectId,
            int amount
    ) {
        if (!(item instanceof EtherealEssenceItem)) {
            throw new IllegalArgumentException("item is not ethereal essence");
        }
        if (aspectId == null
                || aspectId.isBlank()
                || !aspectId.equals(aspectId.trim())
                || !aspectId.equals(aspectId.toLowerCase(
                        java.util.Locale.ROOT
                ))) {
            throw new IllegalArgumentException("invalid aspect id");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(ASPECT_KEY, aspectId);
        tag.putInt(AMOUNT_KEY, amount);
        return stack;
    }

    public static Optional<PrimalAspect> aspect(ItemStack stack) {
        if (!(stack.getItem() instanceof EtherealEssenceItem)
                || !stack.hasTag()
                || !stack.getTag().contains(ASPECT_KEY, CompoundTag.TAG_STRING)) {
            return Optional.empty();
        }
        try {
            return Optional.of(PrimalAspect.fromId(
                    stack.getTag().getString(ASPECT_KEY)
            ));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public static Optional<String> aspectId(ItemStack stack) {
        if (!(stack.getItem() instanceof EtherealEssenceItem)
                || !stack.hasTag()
                || stack.getTag() == null
                || !stack.getTag().contains(
                        ASPECT_KEY,
                        CompoundTag.TAG_STRING
                )) {
            return Optional.empty();
        }
        String id = stack.getTag().getString(ASPECT_KEY);
        if (id.isBlank()
                || !id.equals(id.trim())
                || !id.equals(id.toLowerCase(java.util.Locale.ROOT))) {
            return Optional.empty();
        }
        return Optional.of(id);
    }

    public static int amount(ItemStack stack) {
        if (aspectId(stack).isEmpty() || stack.getTag() == null) {
            return 0;
        }
        return Math.max(0, stack.getTag().getInt(AMOUNT_KEY));
    }

    public static int color(ItemStack stack) {
        return aspectId(stack)
                .flatMap(AspectRegistryRuntime::find)
                .map(definition -> definition.color())
                .or(() -> aspect(stack).map(PrimalAspectColors::color))
                .orElse(0xFFFFFF);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        aspectId(stack).ifPresent(aspect -> tooltip.add(
                Component.translatable(
                                "tooltip.thaumic_reborn.ethereal_essence",
                                Component.translatable(
                                        "aspect.thaumic_reborn." + aspect
                                ),
                                amount(stack)
                        )
                        .withStyle(ChatFormatting.GRAY)
        ));
    }
}
