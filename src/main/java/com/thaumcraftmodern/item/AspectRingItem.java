package com.thaumcraftmodern.item;

import com.thaumcraftmodern.api.wand.VisDiscountGear;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.PrimalAspectColors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** TC4's apprentice ring: a one-percent discount for exactly one primal. */
public final class AspectRingItem extends CurioAccessoryItem
        implements VisDiscountGear {
    private final PrimalAspect aspect;

    public AspectRingItem(PrimalAspect aspect, Properties properties) {
        super(properties);
        this.aspect = aspect;
    }

    public PrimalAspect aspect() {
        return aspect;
    }

    public int color() {
        return PrimalAspectColors.color(aspect);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(
                "item.thaumic_reborn.apprentice_ring",
                Component.translatable("tc.aspect." + aspect.id())
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.thaumic_reborn.aspect_ring_discount",
                Component.translatable("tc.aspect." + aspect.id())
        ).withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public int visDiscountPercent(ItemStack stack, Player player,
            PrimalAspect requestedAspect) {
        return requestedAspect == aspect ? 1 : 0;
    }
}
