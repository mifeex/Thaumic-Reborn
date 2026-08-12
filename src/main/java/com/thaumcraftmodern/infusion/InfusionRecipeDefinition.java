package com.thaumcraftmodern.infusion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Server-side, datapack-owned description of a TC4 infusion recipe. */
public record InfusionRecipeDefinition(
        ResourceLocation id,
        String research,
        int instability,
        Ingredient central,
        List<Ingredient> components,
        ItemStack output,
        Map<String, Integer> essentia,
        ResultKind resultKind,
        String modifierKey,
        int modifierValue
) {
    public enum ResultKind {
        FIXED, TAG_INT, TAG_BOOLEAN, ITEM_REPLACEMENT, ENCHANTMENT, RUNIC_AUGMENT
    }

    public InfusionRecipeDefinition(ResourceLocation id, String research,
            int instability, Ingredient central, List<Ingredient> components,
            ItemStack output, Map<String, Integer> essentia) {
        this(id, research, instability, central, components, output, essentia,
                ResultKind.FIXED, "", 0);
    }

    public InfusionRecipeDefinition {
        Objects.requireNonNull(id, "id");
        research = Objects.requireNonNull(research, "research").trim();
        if (instability < 0) {
            throw new IllegalArgumentException("Infusion instability cannot be negative");
        }
        Objects.requireNonNull(central, "central");
        components = List.copyOf(Objects.requireNonNull(components, "components"));
        if (components.isEmpty()) {
            throw new IllegalArgumentException("Infusion recipe needs components");
        }
        output = Objects.requireNonNull(output, "output").copy();
        resultKind = Objects.requireNonNull(resultKind, "resultKind");
        modifierKey = Objects.requireNonNull(modifierKey, "modifierKey").trim();
        if (resultKind == ResultKind.FIXED && output.isEmpty()) {
            throw new IllegalArgumentException("Infusion output cannot be empty");
        }
        if (resultKind != ResultKind.FIXED && modifierKey.isEmpty()) {
            throw new IllegalArgumentException("Infusion modifier needs a key");
        }
        LinkedHashMap<String, Integer> costs = new LinkedHashMap<>();
        Objects.requireNonNull(essentia, "essentia").forEach((aspect, amount) -> {
            if (aspect == null || aspect.isBlank() || amount == null || amount <= 0) {
                throw new IllegalArgumentException("Invalid infusion essentia cost");
            }
            costs.put(aspect, amount);
        });
        if (costs.isEmpty()) {
            throw new IllegalArgumentException("Infusion recipe needs essentia");
        }
        essentia = Collections.unmodifiableMap(costs);
    }

    @Override
    public ItemStack output() {
        return output.copy();
    }

    public boolean matchesCentral(ItemStack stack) {
        if (resultKind == ResultKind.RUNIC_AUGMENT) {
            return central.test(stack)
                    && com.thaumcraftmodern.item.RunicShieldService.isAugmentable(stack);
        }
        if (resultKind != ResultKind.ENCHANTMENT) return central.test(stack);
        Enchantment enchantment = enchantment();
        // ItemStack#isEnchantable rejects every stack that already has an enchantment.
        // TC4 deliberately permits repeated infusion while the target enchantment can
        // still gain a level and remains compatible with all existing enchantments.
        if (enchantment == null || stack.isEmpty() || !enchantment.canEnchant(stack)
                || !stack.getItem().isEnchantable(stack)) return false;
        Map<Enchantment, Integer> existing = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> entry : existing.entrySet()) {
            if (entry.getKey() == enchantment) {
                if (entry.getValue() >= enchantment.getMaxLevel()) return false;
            } else if (!enchantment.isCompatibleWith(entry.getKey())
                    || !entry.getKey().isCompatibleWith(enchantment)) return false;
        }
        return true;
    }

    public ItemStack createResult(ItemStack centralStack) {
        if (resultKind == ResultKind.FIXED) {
            ItemStack result = output();
            copyCentralDamage(centralStack, result);
            return result;
        }
        ItemStack result = resultKind == ResultKind.ITEM_REPLACEMENT
                ? output() : centralStack.copy();
        if (resultKind == ResultKind.ITEM_REPLACEMENT) {
            if (centralStack.hasTag()) result.setTag(centralStack.getTag().copy());
            copyCentralDamage(centralStack, result);
        }
        result.setCount(1);
        switch (resultKind) {
            case TAG_INT -> result.getOrCreateTag().putInt(modifierKey, modifierValue);
            case TAG_BOOLEAN -> result.getOrCreateTag().putBoolean(modifierKey,
                    modifierValue != 0);
            case ITEM_REPLACEMENT -> result.getOrCreateTag().putInt(
                    modifierKey, modifierValue);
            case ENCHANTMENT -> {
                Enchantment enchantment = enchantment();
                if (enchantment != null) {
                    Map<Enchantment, Integer> enchantments =
                            EnchantmentHelper.getEnchantments(result);
                    enchantments.put(enchantment,
                            EnchantmentHelper.getItemEnchantmentLevel(
                                    enchantment, result) + 1);
                    EnchantmentHelper.setEnchantments(enchantments, result);
                }
            }
            case RUNIC_AUGMENT -> {
                return com.thaumcraftmodern.item.RunicShieldService.addHardening(
                        centralStack);
            }
            default -> { }
        }
        return result;
    }

    private static void copyCentralDamage(ItemStack centralStack, ItemStack result) {
        if (centralStack.isDamageableItem() && result.isDamageableItem()) {
            result.setDamageValue(transferredDamage(
                    centralStack.getDamageValue(), result.getMaxDamage()));
        }
    }

    public static int transferredDamage(int centralDamage, int resultMaxDamage) {
        if (resultMaxDamage <= 0) return 0;
        return Math.min(Math.max(0, centralDamage), resultMaxDamage - 1);
    }

    public int effectiveInstability(ItemStack centralStack) {
        if (resultKind == ResultKind.RUNIC_AUGMENT) return 5
                + com.thaumcraftmodern.item.RunicShieldService
                        .finalCharge(centralStack) / 2;
        if (resultKind != ResultKind.ENCHANTMENT) return instability;
        int levels = EnchantmentHelper.getEnchantments(centralStack).values()
                .stream().mapToInt(Integer::intValue).sum();
        return instability + levels / 2;
    }

    public Map<String, Integer> effectiveEssentia(ItemStack centralStack) {
        if (resultKind == ResultKind.RUNIC_AUGMENT) {
            int charge = com.thaumcraftmodern.item.RunicShieldService
                    .finalCharge(centralStack);
            // Match TC4's double-to-int narrowing exactly: values beyond the
            // int range saturate at Integer.MAX_VALUE instead of wrapping.
            int amount = (int) (32.0D * Math.pow(2.0D, charge));
            return Map.of("tutamen", amount / 2,
                    "praecantatio", amount / 2, "potentia", amount);
        }
        if (resultKind != ResultKind.ENCHANTMENT) return essentia;
        Enchantment target = enchantment();
        if (target == null) return essentia;
        float modifier = EnchantmentHelper.getItemEnchantmentLevel(target, centralStack);
        for (Map.Entry<Enchantment, Integer> entry
                : EnchantmentHelper.getEnchantments(centralStack).entrySet()) {
            if (entry.getKey() != target) modifier += entry.getValue() * 0.1F;
        }
        LinkedHashMap<String, Integer> scaled = new LinkedHashMap<>();
        float finalModifier = modifier;
        essentia.forEach((aspect, amount) -> scaled.put(aspect,
                amount + (int) (amount * finalModifier)));
        return Collections.unmodifiableMap(scaled);
    }

    public int experienceLevels(ItemStack centralStack) {
        Enchantment enchantment = enchantment();
        return enchantment == null ? 0
                : Math.max(1, enchantment.getMinCost(1) / 3)
                * (1 + EnchantmentHelper.getItemEnchantmentLevel(
                        enchantment, centralStack));
    }

    public List<Ingredient> effectiveComponents(ItemStack centralStack) {
        if (resultKind != ResultKind.RUNIC_AUGMENT) return components;
        java.util.ArrayList<Ingredient> result = new java.util.ArrayList<>(components);
        if (components.size() > 1) {
            int charge = com.thaumcraftmodern.item.RunicShieldService
                    .finalCharge(centralStack);
            for (int index = 0; index < charge; index++) {
                result.add(components.get(1));
            }
        }
        return List.copyOf(result);
    }

    private Enchantment enchantment() {
        if (resultKind != ResultKind.ENCHANTMENT) return null;
        ResourceLocation id = ResourceLocation.tryParse(modifierKey);
        return id == null ? null : BuiltInRegistries.ENCHANTMENT.get(id);
    }
}
