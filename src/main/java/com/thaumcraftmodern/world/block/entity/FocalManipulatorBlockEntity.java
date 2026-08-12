package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.focus.FocusUpgradeCost;
import com.thaumcraftmodern.focus.FocusUpgradeType;
import com.thaumcraftmodern.item.WandFocusItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.visnet.VisMachineAccess;
import com.thaumcraftmodern.world.menu.FocalManipulatorMenu;
import java.util.EnumMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Persistent TC4 TileFocalManipulator state and vis-drain state machine. */
public final class FocalManipulatorBlockEntity extends BlockEntity
        implements Container, MenuProvider {
    public static final int DATA_TOTAL = 0;
    public static final int DATA_REMAINING = 1;
    public static final int DATA_UPGRADE = 2;
    public static final int DATA_RANK = 3;
    public static final int DATA_COST_START = 4;
    public static final int DATA_COUNT = 10;
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final EnumMap<PrimalAspect, Integer> remaining = new EnumMap<>(PrimalAspect.class);
    private int totalCost;
    private int upgradeId = -1;
    private int rank;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            if (index == DATA_TOTAL) return totalCost;
            if (index == DATA_REMAINING) return remaining.values().stream().mapToInt(Integer::intValue).sum();
            if (index == DATA_UPGRADE) return upgradeId;
            if (index == DATA_RANK) return rank;
            int primal = index - DATA_COST_START;
            return primal >= 0 && primal < PrimalAspect.ordered().size()
                    ? remaining.getOrDefault(PrimalAspect.ordered().get(primal), 0) : 0;
        }
        @Override public void set(int index, int value) {
            if (index == DATA_TOTAL) totalCost = value;
            else if (index == DATA_UPGRADE) upgradeId = value;
            else if (index == DATA_RANK) rank = value;
            else if (index >= DATA_COST_START && index < DATA_COUNT)
                remaining.put(PrimalAspect.ordered().get(index - DATA_COST_START), value);
        }
        @Override public int getCount() { return DATA_COUNT; }
    };

    public FocalManipulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOCAL_MANIPULATOR.get(), pos, state);
    }

    public ContainerData data() { return data; }

    public boolean begin(ServerPlayer player, FocusUpgradeType upgrade) {
        ItemStack focus = items.get(0);
        if (!(focus.getItem() instanceof WandFocusItem focusItem) || upgradeId >= 0) return false;
        if (upgrade == FocusUpgradeType.VAMPIRE_BATS && KnowledgeAccess.get(player)
                .map(knowledge -> !knowledge.hasCompletedResearch("vampbat")).orElse(true)) return false;
        int nextRank = WandFocusItem.nextRank(focus);
        if (nextRank < 1 || !focusItem.type().upgradesAtRank(nextRank).contains(upgrade)) return false;
        if (focusItem.type() == com.thaumcraftmodern.focus.WandFocusType.WARDING
                && upgrade == FocusUpgradeType.ENLARGE
                && WandFocusItem.upgradeLevel(focus, FocusUpgradeType.ARCHITECT) == 0) return false;
        if (focusItem.type() == com.thaumcraftmodern.focus.WandFocusType.SHOCK
                && upgrade == FocusUpgradeType.ENLARGE
                && WandFocusItem.upgradeLevel(focus, FocusUpgradeType.CHAIN_LIGHTNING) == 0
                && WandFocusItem.upgradeLevel(focus, FocusUpgradeType.EARTH_SHOCK) == 0) return false;
        int experience = nextRank * 8;
        if (!player.getAbilities().instabuild && player.experienceLevel < experience) return false;
        EnumMap<PrimalAspect, Integer> cost = FocusUpgradeCost.primalCost(upgrade, nextRank);
        if (!player.getAbilities().instabuild) player.giveExperienceLevels(-experience);
        remaining.clear();
        remaining.putAll(cost);
        totalCost = cost.values().stream().mapToInt(Integer::intValue).sum();
        upgradeId = upgrade.id();
        rank = nextRank;
        sync();
        player.level().playSound(null, worldPosition, ModSounds.CRAFT_SUCCESS.get(),
                SoundSource.BLOCKS, 0.25F, 1.0F);
        return true;
    }

    public static void serverTick(Level raw, BlockPos pos, BlockState state,
                                  FocalManipulatorBlockEntity table) {
        if (!(raw instanceof ServerLevel level) || table.upgradeId < 0) return;
        ItemStack focus = table.items.get(0);
        if (!(focus.getItem() instanceof WandFocusItem) || WandFocusItem.nextRank(focus) != table.rank) {
            table.cancel();
            level.playSound(null, pos, ModSounds.CRAFT_FAIL.get(),
                    SoundSource.BLOCKS, 0.33F, 1.0F);
            return;
        }
        if (level.getGameTime() % 5L != 0L) return;
        boolean changed = false;
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int wanted = Math.min(100, table.remaining.getOrDefault(aspect, 0));
            if (wanted <= 0) continue;
            int drained = VisMachineAccess.consumeNearest(level, pos, aspect, wanted);
            if (drained > 0) {
                table.remaining.put(aspect, wanted == drained
                        ? table.remaining.get(aspect) - drained
                        : Math.max(0, table.remaining.get(aspect) - drained));
                changed = true;
            }
        }
        if (table.remaining.values().stream().allMatch(value -> value <= 0)) {
            boolean upgraded = WandFocusItem.applyUpgrade(focus,
                    FocusUpgradeType.byId(table.upgradeId), table.rank);
            table.cancel();
            level.playSound(null, pos,
                    upgraded ? ModSounds.WAND.get() : ModSounds.CRAFT_FAIL.get(),
                    SoundSource.BLOCKS, upgraded ? 1.0F : 0.33F, 1.0F);
            if (upgraded) level.levelEvent(2005, pos.above(), 0);
            return;
        }
        if (changed) table.sync();
    }

    private void cancel() {
        remaining.clear();
        totalCost = 0;
        upgradeId = -1;
        rank = 0;
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(),
                getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) Containers.dropContents(level, worldPosition, this);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Total", totalCost);
        tag.putInt("Upgrade", upgradeId);
        tag.putInt("Rank", rank);
        for (PrimalAspect aspect : PrimalAspect.ordered())
            tag.putInt("Cost_" + aspect.id(), remaining.getOrDefault(aspect, 0));
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        totalCost = tag.getInt("Total");
        upgradeId = tag.contains("Upgrade") ? tag.getInt("Upgrade") : -1;
        rank = tag.getInt("Rank");
        remaining.clear();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int value = tag.getInt("Cost_" + aspect.id());
            if (value > 0) remaining.put(aspect, value);
        }
    }

    @Override public Component getDisplayName() {
        return Component.translatable("container.thaumcraftmodern.focal_manipulator");
    }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FocalManipulatorMenu(id, inventory, this, data);
    }
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return items.get(0).isEmpty(); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) cancel(); return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot); cancel(); return result;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack); if (stack.getCount() > 1) stack.setCount(1); cancel();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + .5D, worldPosition.getY() + .5D,
                worldPosition.getZ() + .5D) <= 64.0D;
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return upgradeId < 0 && stack.getItem() instanceof WandFocusItem;
    }
    @Override public void clearContent() { items.clear(); cancel(); }
}
