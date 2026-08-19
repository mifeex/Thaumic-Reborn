package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server-only TC4 Pech dice transaction. Unsupported TC4 equipment is not
 * substituted with semantically different items.
 */
public final class PechTradeService {
    private PechTradeService() {
    }

    public static TradeResult roll(
            int pechType,
            ItemStack payment,
            ItemStackHandler pack,
            RandomSource random,
            ServerPlayer player
    ) {
        int baseValue = PechBehavior.value(payment);
        if (baseValue <= 0) {
            return new TradeResult(List.of(), false);
        }
        boolean losesTrust = random.nextInt(100) <= baseValue / 2;
        int value = baseValue;
        if (random.nextInt(5) == 0) {
            value += random.nextInt(3);
        } else if (random.nextBoolean()) {
            value -= random.nextInt(3);
        }

        List<TradeEntry> table = table(pechType);
        List<ItemStack> output = new ArrayList<>(4);
        while (value > 0 && output.size() < 4) {
            int amount = Math.min(
                    5,
                    Math.max((value + 1) / 2, random.nextInt(value) + 1)
            );
            value -= amount;
            if (amount == 1 && random.nextBoolean()) {
                ItemStack packed = takeRandomPackItem(pack, random);
                if (!packed.isEmpty()) {
                    output.add(packed);
                    continue;
                }
            }
            if (amount >= 4 && random.nextBoolean()) {
                ItemStack dungeonLoot = dungeonLoot(player, random);
                if (!dungeonLoot.isEmpty()) {
                    output.add(dungeonLoot);
                    continue;
                }
            }
            List<TradeEntry> candidates = table.stream()
                    .filter(entry -> entry.value() == amount)
                    .toList();
            if (!candidates.isEmpty()) {
                ItemStack chosen = candidates.get(
                        random.nextInt(candidates.size())
                ).stack().get();
                if (!chosen.isEmpty()) {
                    output.add(chosen);
                }
            }
        }
        return new TradeResult(List.copyOf(output), losesTrust);
    }

    private static ItemStack dungeonLoot(
            ServerPlayer player,
            RandomSource random
    ) {
        LootTable table = player.serverLevel()
                .getServer()
                .getLootData()
                .getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        LootParams params = new LootParams.Builder(player.serverLevel())
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.CHEST);
        List<ItemStack> generated = table.getRandomItems(
                params,
                random.nextLong()
        );
        return generated.isEmpty()
                ? ItemStack.EMPTY
                : generated.get(random.nextInt(generated.size())).copy();
    }

    private static ItemStack takeRandomPackItem(
            ItemStackHandler pack,
            RandomSource random
    ) {
        List<Integer> occupied = new ArrayList<>();
        for (int slot = 0; slot < pack.getSlots(); slot++) {
            if (!pack.getStackInSlot(slot).isEmpty()) {
                occupied.add(slot);
            }
        }
        if (occupied.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return pack.extractItem(
                occupied.get(random.nextInt(occupied.size())),
                1,
                false
        );
    }

    private static List<TradeEntry> table(int type) {
        if (type == PechBehavior.MAGE) {
            List<TradeEntry> entries = new ArrayList<>();
            entries.add(entry(1, () -> new ItemStack(ModItems.MANA_BEAN.get())));
            entries.add(entry(1, () -> new ItemStack(ModItems.AIR_SHARD.get())));
            entries.add(entry(1, () -> new ItemStack(ModItems.FIRE_SHARD.get())));
            entries.add(entry(1, () -> new ItemStack(ModItems.WATER_SHARD.get())));
            entries.add(entry(1, () -> new ItemStack(ModItems.EARTH_SHARD.get())));
            entries.add(entry(1, () -> new ItemStack(ModItems.ORDER_SHARD.get())));
            entries.add(entry(1, () -> new ItemStack(ModItems.ENTROPY_SHARD.get())));
            entries.add(entry(1, () -> new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get())));
            entries.add(entry(2, () -> new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get())));
            entries.add(entry(3, () -> book(ModEnchantments.HASTE.get())));
            entries.add(entry(3, () -> new ItemStack(Items.GOLDEN_APPLE)));
            entries.add(entry(5, () -> new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
            entries.add(entry(5, () -> book(ModEnchantments.REPAIR.get())));
            entries.add(entry(
                    5,
                    () -> new ItemStack(
                            ModItems.ARCANE_RECIPE_COMPONENTS
                                    .get("focus_pouch")
                                    .get()
                    )
            ));
            return List.copyOf(entries);
        }
        if (type == PechBehavior.STALKER) {
            return List.of(
                    entry(1, () -> new ItemStack(ModItems.MANA_BEAN.get())),
                    entry(1, () -> new ItemStack(ModBlocks.VISHROOM.get())),
                    entry(2, () -> new ItemStack(Items.GHAST_TEAR)),
                    entry(2, () -> book(Enchantments.POWER_ARROWS)),
                    entry(3, () -> new ItemStack(Items.EXPERIENCE_BOTTLE)),
                    entry(3, () -> new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get())),
                    entry(3, () -> new ItemStack(Items.GOLDEN_APPLE)),
                    entry(5, () -> new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)),
                    entry(5, () -> book(Enchantments.ALL_DAMAGE_PROTECTION)),
                    entry(5, () -> book(Enchantments.PROJECTILE_PROTECTION))
            );
        }
        return List.of(
                entry(1, () -> new ItemStack(ModItems.MANA_BEAN.get())),
                entry(1, () -> new ItemStack(ModItems.NATIVE_IRON_CLUSTER.get())),
                entry(1, () -> new ItemStack(ModItems.NATIVE_GOLD_CLUSTER.get())),
                entry(1, () -> new ItemStack(ModItems.NATIVE_CINNABAR_CLUSTER.get())),
                entry(1, () -> new ItemStack(ModItems.NATIVE_COPPER_CLUSTER.get())),
                entry(1, () -> new ItemStack(ModItems.NATIVE_TIN_CLUSTER.get())),
                entry(1, () -> new ItemStack(ModItems.NATIVE_SILVER_CLUSTER.get())),
                entry(1, () -> new ItemStack(ModItems.NATIVE_LEAD_CLUSTER.get())),
                entry(2, () -> new ItemStack(Items.BLAZE_ROD)),
                entry(2, () -> new ItemStack(ModBlocks.CINDERPEARL.get())),
                entry(3, () -> new ItemStack(Items.EXPERIENCE_BOTTLE)),
                entry(3, () -> new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get())),
                entry(3, () -> new ItemStack(Items.GOLDEN_APPLE)),
                entry(5, () -> new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)),
                entry(5, () -> new ItemStack(ModBlocks.SHIMMERLEAF.get()))
        );
    }

    private static ItemStack book(Enchantment enchantment) {
        return EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(enchantment, 1)
        );
    }

    private static TradeEntry entry(int value, Supplier<ItemStack> stack) {
        return new TradeEntry(value, stack);
    }

    private record TradeEntry(int value, Supplier<ItemStack> stack) {
    }

    public record TradeResult(List<ItemStack> output, boolean losesTrust) {
    }
}
