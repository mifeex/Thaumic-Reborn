package com.thaumcraftmodern.item;

import com.thaumcraftmodern.focus.WandFocusType;
import com.thaumcraftmodern.focus.FocusUpgradeType;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumicreborn.api.focus.FocusItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/** A TC4 focus item with the original five ordered upgrade ranks. */
public final class WandFocusItem extends Item implements FocusItem {
    private final WandFocusType type;

    public WandFocusItem(WandFocusType type, Properties properties) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    public WandFocusType type() { return type; }

    @Override
    public net.minecraft.resources.ResourceLocation focusId() {
        return new net.minecraft.resources.ResourceLocation(
                ThaumcraftModern.MOD_ID, type.itemId());
    }

    public static short[] appliedUpgrades(ItemStack stack) {
        short[] result = {-1, -1, -1, -1, -1};
        CompoundTag owner = stack.getTag();
        if (owner == null || !owner.contains("upgrade", Tag.TAG_LIST)) return result;
        ListTag list = owner.getList("upgrade", Tag.TAG_COMPOUND);
        for (int index = 0; index < result.length && index < list.size(); index++) {
            result[index] = list.getCompound(index).getShort("id");
        }
        return result;
    }

    public static int nextRank(ItemStack stack) {
        short[] upgrades = appliedUpgrades(stack);
        for (int index = 0; index < upgrades.length; index++) {
            if (upgrades[index] < 0) return index + 1;
        }
        return -1;
    }

    public static int upgradeLevel(ItemStack stack, FocusUpgradeType type) {
        int level = 0;
        for (short id : appliedUpgrades(stack)) if (id == type.id()) level++;
        return level;
    }

    public static boolean applyUpgrade(ItemStack stack, FocusUpgradeType type,
                                       int rank) {
        if (rank < 1 || rank > 5) return false;
        short[] upgrades = appliedUpgrades(stack);
        if (upgrades[rank - 1] >= 0) return false;
        upgrades[rank - 1] = type.id();
        ListTag list = new ListTag();
        for (short id : upgrades) {
            CompoundTag entry = new CompoundTag();
            entry.putShort("id", id);
            list.add(entry);
        }
        stack.getOrCreateTag().put("upgrade", list);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(type.perTickCost()
                ? "item.Focus.cost2" : "item.Focus.cost1").withStyle(ChatFormatting.DARK_PURPLE));
        type.centivisCost().forEach((aspect, amount) -> tooltip.add(Component.literal(
                "  " + Component.translatable("aspect.thaumic_reborn." + aspect).getString()
                        + " x " + String.format(Locale.ROOT, "%.2f", amount / 100.0D))
                .withStyle(ChatFormatting.GRAY)));
        java.util.LinkedHashMap<FocusUpgradeType, Integer> upgrades =
                new java.util.LinkedHashMap<>();
        for (short id : appliedUpgrades(stack)) {
            if (id < 0) continue;
            FocusUpgradeType upgrade = FocusUpgradeType.byIdOrNull(id);
            if (upgrade != null) upgrades.merge(upgrade, 1, Integer::sum);
        }
        upgrades.forEach((upgrade, amount) -> tooltip.add(
                Component.translatable(upgrade.nameKey())
                        .append(amount > 1 ? " " + amount : "")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
    }
}
