package com.thaumcraftmodern.wand;

import com.thaumcraftmodern.api.wand.VisDiscountEffect;
import com.thaumcraftmodern.api.wand.VisDiscountEvent;
import com.thaumcraftmodern.api.wand.VisDiscountGear;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;

/**
 * Aggregates every player-owned source of vis-cost modification.
 *
 * <p>Positive percentage points reduce cost; negative points increase it.
 * Equipment and active effects are read live for every preview, validation,
 * and payment. The final event lets warp phenomena and integrations
 * contribute without bypassing {@link WandVisService}.</p>
 */
public final class VisDiscountService {
    private VisDiscountService() {
    }

    public static int totalPercent(
            Player player,
            PrimalAspect aspect
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(aspect, "aspect");

        int total = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            total = Math.addExact(
                    total,
                    percentFromGear(stack, player, aspect)
            );
        }
        var curios = CuriosApi.getCuriosInventory(player).resolve();
        if (curios.isPresent()) {
            var equipped = curios.get().getEquippedCurios();
            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                total = Math.addExact(
                        total,
                        percentFromGear(equipped.getStackInSlot(slot),
                                player, aspect)
                );
            }
        }
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect() instanceof VisDiscountEffect effect) {
                total = Math.addExact(
                        total,
                        effect.visDiscountPercent(instance, player, aspect)
                );
            }
        }

        VisDiscountEvent event =
                new VisDiscountEvent(player, aspect, total);
        MinecraftForge.EVENT_BUS.post(event);
        return event.discountPercent();
    }

    static int percentFromGear(
            ItemStack stack,
            Player player,
            PrimalAspect aspect
    ) {
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty()) return 0;
        if (stack.getItem() instanceof VisDiscountGear gear) {
            return gear.visDiscountPercent(stack, player, aspect);
        }
        if (stack.getItem() instanceof
                com.thaumicreborn.api.equipment.VisDiscountGear gear) {
            return gear.visDiscountPercent(stack, player, aspect.id());
        }
        return 0;
    }
}
