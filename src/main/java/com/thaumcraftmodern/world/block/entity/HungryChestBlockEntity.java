package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent 27-slot inventory and TC4's 0.2 bite impulse/0.1-per-tick lid timing. */
public final class HungryChestBlockEntity extends RandomizableContainerBlockEntity {
    /** Original block event id/value: event 2 raises lidAngle to 2/10 immediately. */
    public static final int EAT_EVENT = 2;
    public static final int EAT_LID_KICK = 2;
    private static final float NORMAL_LID_SPEED = 0.1F;
    private static final int ITEM_LIFT_TICKS = 2;
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openers;
    private float lid;
    private float previousLid;
    private final Map<UUID,Integer> liftingItems = new HashMap<>();

    public HungryChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HUNGRY_CHEST.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity chest) {
        if (!level.isClientSide) {
            chest.tickLiftingItems((ServerLevel) level);
            return;
        }
        chest.previousLid = chest.lid;
        boolean shouldOpen = chest.openers > 0;
        chest.lid = net.minecraft.util.Mth.clamp(
                chest.lid + (shouldOpen ? NORMAL_LID_SPEED : -NORMAL_LID_SPEED), 0, 1);
    }

    public boolean startEating(ItemEntity item) {
        if (!(level instanceof ServerLevel) || liftingItems.containsKey(item.getUUID())
                || !canAccept(item.getItem())) return false;
        liftingItems.put(item.getUUID(), ITEM_LIFT_TICKS);
        item.setPickUpDelay(ITEM_LIFT_TICKS + 2);
        item.setNoGravity(true);
        Vec3 mouth = new Vec3(worldPosition.getX() + .5D, worldPosition.getY() + 1.02D,
                worldPosition.getZ() + .5D);
        item.setDeltaMovement(mouth.subtract(item.position()).scale(1D / ITEM_LIFT_TICKS));
        item.hurtMarked = true;
        level.blockEvent(worldPosition, ModBlocks.HUNGRY_CHEST.get(), EAT_EVENT, EAT_LID_KICK);
        return true;
    }

    private void tickLiftingItems(ServerLevel server) {
        var iterator = liftingItems.entrySet().iterator();
        while (iterator.hasNext()) {
            var capture = iterator.next();
            Entity raw = server.getEntity(capture.getKey());
            if (!(raw instanceof ItemEntity item) || !item.isAlive()
                    || item.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 9D) {
                if (raw instanceof ItemEntity item) item.setNoGravity(false);
                iterator.remove();
                continue;
            }
            Vec3 mouth = new Vec3(worldPosition.getX() + .5D, worldPosition.getY() + 1.02D,
                    worldPosition.getZ() + .5D);
            Vec3 next = item.position().lerp(mouth, .65D);
            item.setPos(next.x, next.y, next.z);
            item.setDeltaMovement(mouth.subtract(next).scale(.65D));
            item.hurtMarked = true;
            int ticks = capture.getValue() - 1;
            if (ticks > 0) {
                capture.setValue(ticks);
                continue;
            }
            ItemStack remainder = insert(item.getItem());
            item.setNoGravity(false);
            if (remainder.isEmpty()) item.discard();
            else item.setItem(remainder);
            iterator.remove();
        }
    }

    private boolean canAccept(ItemStack source) {
        int remaining = source.getCount();
        for (ItemStack stored : items) {
            if (!stored.isEmpty() && ItemStack.isSameItemSameTags(stored, source)) {
                remaining -= Math.max(0, stored.getMaxStackSize() - stored.getCount());
                if (remaining <= 0) return true;
            }
        }
        for (ItemStack stored : items) if (stored.isEmpty()) return true;
        return false;
    }

    public ItemStack insert(ItemStack source) {
        ItemStack remainder = source.copy();
        for (int pass = 0; pass < 2 && !remainder.isEmpty(); pass++) {
            for (int slot = 0; slot < items.size() && !remainder.isEmpty(); slot++) {
                ItemStack stored = items.get(slot);
                if (pass == 0 && !stored.isEmpty() && ItemStack.isSameItemSameTags(stored, remainder)) {
                    int moved = Math.min(remainder.getCount(), stored.getMaxStackSize() - stored.getCount());
                    if (moved > 0) { stored.grow(moved); remainder.shrink(moved); }
                } else if (pass == 1 && stored.isEmpty()) {
                    int moved = Math.min(remainder.getCount(), remainder.getMaxStackSize());
                    items.set(slot, remainder.copyWithCount(moved));
                    remainder.shrink(moved);
                }
            }
        }
        if (remainder.getCount() != source.getCount()) setChanged();
        return remainder;
    }

    public float openness(float partialTick) {
        float value = net.minecraft.util.Mth.lerp(partialTick, previousLid, lid);
        value = 1.0F - value;
        return 1.0F - value * value * value;
    }

    @Override public boolean triggerEvent(int id, int value) {
        if (id == 1) { openers = value; return true; }
        if (id == EAT_EVENT) {
            lid = Math.max(lid, value / 10F);
            if (level != null && level.isClientSide) {
                level.playLocalSound(worldPosition.getX() + .5D, worldPosition.getY() + 1D,
                        worldPosition.getZ() + .5D, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS,
                        .25F, (level.random.nextFloat() - level.random.nextFloat()) * .2F + 1F, false);
            }
            return true;
        }
        return super.triggerEvent(id, value);
    }
    @Override
    public void startOpen(Player player) {
        if (player.isSpectator()) return;
        int previous = openers++;
        if (previous == 0) playChestSound(SoundEvents.CHEST_OPEN);
        syncOpeners();
    }

    @Override
    public void stopOpen(Player player) {
        if (player.isSpectator()) return;
        int previous = openers;
        openers = Math.max(0, openers - 1);
        if (previous > 0 && openers == 0) {
            playChestSound(SoundEvents.CHEST_CLOSE);
        }
        syncOpeners();
    }

    private void playChestSound(net.minecraft.sounds.SoundEvent sound) {
        if (level == null || level.isClientSide) return;
        level.playSound(
                null,
                worldPosition,
                sound,
                SoundSource.BLOCKS,
                0.5F,
                level.random.nextFloat() * 0.1F + 0.9F
        );
    }
    private void syncOpeners() { if (level != null) level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openers); }
    @Override protected Component getDefaultName() { return Component.translatable("container.thaumcraftmodern.hungry_chest"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory, this); }
    @Override public int getContainerSize() { return items.size(); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items); }
    @Override public void load(CompoundTag tag) { super.load(tag); items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY); if (!tryLoadLootTable(tag)) ContainerHelper.loadAllItems(tag, items); }
}
