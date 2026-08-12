package com.thaumcraftmodern.enchantment;

import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.crucible.ItemAspectRegistry;
import com.thaumcraftmodern.registry.ModEnchantments;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ThaumcraftEnchantmentEvents {
    private static final int REPAIR_INTERVAL = 40;

    private ThaumcraftEnchantmentEvents() {
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        applyHaste(event.player);
        if (event.player instanceof ServerPlayer player
                && !player.getAbilities().instabuild
                && player.tickCount % REPAIR_INTERVAL == 0) {
            repairInventory(player);
        }
    }

    static void applyHaste(Player player) {
        if (player.getAbilities().flying || player.zza <= 0.0F) {
            return;
        }
        int level = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.HASTE.get(),
                player.getInventory().armor.get(0)
        );
        if (level <= 0) {
            return;
        }
        float bonus = level * 0.015F;
        if (player.onGround()) {
            bonus /= 2.0F;
        }
        if (player.isInWater()) {
            bonus /= 2.0F;
        }
        player.moveRelative(bonus, new Vec3(0.0D, 0.0D, 1.0D));
    }

    private static void repairInventory(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            repair(stack, player);
        }
        for (ItemStack stack : player.getInventory().armor) {
            repair(stack, player);
        }
    }

    private static void repair(ItemStack stack, ServerPlayer player) {
        if (stack.isEmpty() || stack.getDamageValue() <= 0
                || !(stack.getItem() instanceof
                        com.thaumicreborn.api.equipment.ThaumicRepairable)) {
            return;
        }
        int level = Math.min(2, EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.REPAIR.get(), stack));
        if (level <= 0) {
            return;
        }
        Map<String, Integer> cost = repairCost(stack, level);
        if (cost.isEmpty() || !consumeFromInventory(player, cost)) {
            return;
        }
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - level));
    }

    static Map<String, Integer> repairCost(ItemStack stack, int level) {
        Map<String, Integer> aspects = ItemAspectRegistry.aspects(stack)
                .orElse(Map.of());
        return repairCost(aspects, level);
    }

    static Map<String, Integer> repairCost(Map<String, Integer> aspects,
            int level) {
        EnumMap<PrimalAspect, Integer> primals =
                new EnumMap<>(PrimalAspect.class);
        for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
            reduce(entry.getKey(), entry.getValue(), primals);
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (PrimalAspect primal : PrimalAspect.ordered()) {
            int amount = primals.getOrDefault(primal, 0);
            int cost = (int) Math.sqrt(amount * 2.0D) * Math.min(2, level);
            if (cost > 0) {
                result.put(primal.id(), cost);
            }
        }
        return Map.copyOf(result);
    }

    private static void reduce(String id, int amount,
            EnumMap<PrimalAspect, Integer> result) {
        if (amount <= 0) {
            return;
        }
        try {
            result.merge(PrimalAspect.fromId(id), amount, Math::addExact);
            return;
        } catch (IllegalArgumentException ignored) {
            // Compound aspect; recursively preserve its amount in each branch.
        }
        AspectDefinition definition = AspectRegistryRuntime.find(id).orElse(null);
        if (definition == null || definition.components().size() != 2) {
            return;
        }
        reduce(definition.components().get(0), amount, result);
        reduce(definition.components().get(1), amount, result);
    }

    private static boolean consumeFromInventory(ServerPlayer player,
            Map<String, Integer> cost) {
        for (int slot = player.getInventory().items.size() - 1;
                slot >= 0; slot--) {
            ItemStack candidate = player.getInventory().items.get(slot);
            if (WandVisService.isWand(candidate)
                    && WandVisService.consume(player, candidate, cost)) {
                return true;
            }
        }
        return false;
    }
}
