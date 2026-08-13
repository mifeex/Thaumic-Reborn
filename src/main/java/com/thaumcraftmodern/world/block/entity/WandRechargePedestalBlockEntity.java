package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeSpatialIndex;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.item.VisStorageItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/** Server-authoritative TC4 Wand Recharge Pedestal. */
public final class WandRechargePedestalBlockEntity extends BlockEntity
        implements WorldlyContainer {
    private static final int[] SLOT = {0};
    private static final int RANGE = 8;
    private static final int TRANSFER_INTERVAL = 5;
    private static final int RESCAN_INTERVAL = 100;
    private static final int CENTIVIS_PER_VIS = 100;
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final List<BlockPos> nodes = new ArrayList<>();
    private int counter;
    private @Nullable BlockPos drainPosition;
    private @Nullable String drainAspectId;
    private int drainColor;
    private long drainUntil = Long.MIN_VALUE;

    public WandRechargePedestalBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.WAND_RECHARGE_PEDESTAL.get(), position, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel, BlockPos pos, BlockState state,
            WandRechargePedestalBlockEntity pedestal) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        pedestal.counter++;
        if (pedestal.counter == 1
                || pedestal.counter % RESCAN_INTERVAL == 0) {
            pedestal.findNodes(level);
        }
        if (pedestal.counter % TRANSFER_INTERVAL == 0 && !pedestal.item().isEmpty()) {
            if (!pedestal.tryTransfer(level)) pedestal.clearDrain(level);
        }
    }

    private boolean tryTransfer(ServerLevel level) {
        if (item().getItem() instanceof VisStorageItem storage) {
            return tryTransferToStorage(level, storage);
        }
        Optional<WandState> wandState = WandVisService.state(item());
        if (wandState.isEmpty()) return false;
        WandState wand = wandState.get();
        int capacity = WandVisService.capacityCentivis(item());
        for (BlockPos nodePos : nodes) {
            if (!level.isLoaded(nodePos)
                    || !(level.getBlockEntity(nodePos) instanceof AuraNodeBlockEntity node)) continue;
            AuraNodeState.Snapshot snapshot = node.snapshotState().snapshot();
            boolean compoundFocus = level.getBlockState(worldPosition.above())
                    .is(ModBlocks.COMPOUND_RECHARGE_FOCUS.get());
            Optional<TransferSelection> selected = selectTransfer(
                    wand, capacity, snapshot, compoundFocus, AspectRegistryRuntime.catalog());
            if (selected.isPresent()) {
                TransferSelection transfer = selected.get();
                Map<String, Integer> next = new LinkedHashMap<>(snapshot.aspectsCurrent());
                next.put(transfer.sourceAspectId(),
                        next.get(transfer.sourceAspectId()) - 1);
                if (!node.replaceAspects(snapshot.revision(), next, snapshot.aspectsMaximum())) {
                    continue;
                }
                int accepted = WandVisService.addCentivisUnchecked(
                        item(), transfer.targetPrimal().id(), CENTIVIS_PER_VIS);
                if (accepted != CENTIVIS_PER_VIS) {
                    throw new IllegalStateException("validated pedestal transfer was not accepted");
                }
                setChanged();
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
                setDrain(level, nodePos, transfer.sourceAspectId(), transfer.color());
                return true;
            }
        }
        return false;
    }

    private boolean tryTransferToStorage(ServerLevel level,
            VisStorageItem storage) {
        Map<PrimalAspect, Integer> stored = new java.util.EnumMap<>(
                PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            stored.put(aspect, storage.visCentivis(item(), aspect));
        }
        WandState state = new WandState(1, "vis_storage", "vis_storage", stored);
        for (BlockPos nodePos : nodes) {
            if (!level.isLoaded(nodePos)
                    || !(level.getBlockEntity(nodePos)
                    instanceof AuraNodeBlockEntity node)) continue;
            AuraNodeState.Snapshot snapshot = node.snapshotState().snapshot();
            boolean compoundFocus = level.getBlockState(worldPosition.above())
                    .is(ModBlocks.COMPOUND_RECHARGE_FOCUS.get());
            Optional<TransferSelection> selected = selectTransfer(
                    state, storage.capacityCentivis(), snapshot,
                    compoundFocus, AspectRegistryRuntime.catalog());
            if (selected.isEmpty()) continue;
            TransferSelection transfer = selected.get();
            Map<String, Integer> next = new LinkedHashMap<>(
                    snapshot.aspectsCurrent());
            next.put(transfer.sourceAspectId(),
                    next.get(transfer.sourceAspectId()) - 1);
            if (!node.replaceAspects(snapshot.revision(), next,
                    snapshot.aspectsMaximum())) continue;
            int accepted = storage.addCentivis(item(),
                    transfer.targetPrimal(), CENTIVIS_PER_VIS);
            if (accepted != CENTIVIS_PER_VIS) {
                throw new IllegalStateException(
                        "validated amulet transfer was not accepted");
            }
            setChanged();
            level.updateNeighbourForOutputSignal(worldPosition,
                    getBlockState().getBlock());
            setDrain(level, nodePos, transfer.sourceAspectId(), transfer.color());
            return true;
        }
        return false;
    }

    static Optional<PrimalAspect> selectTransfer(
            WandState wand, int capacityCentivis, AuraNodeState.Snapshot node) {
        return selectDirectTransfer(wand, capacityCentivis, node);
    }

    static Optional<TransferSelection> selectTransfer(
            WandState wand, int capacityCentivis, AuraNodeState.Snapshot node,
            boolean compoundFocus, AspectCatalog aspects) {
        Optional<PrimalAspect> direct = selectDirectTransfer(wand, capacityCentivis, node);
        if (direct.isPresent()) {
            PrimalAspect primal = direct.get();
            return Optional.of(new TransferSelection(
                    primal.id(), primal, aspectColor(aspects, primal.id())));
        }
        if (!compoundFocus) return Optional.empty();

        int minimum = minimumVis(wand);
        for (Map.Entry<String, Integer> source : node.aspectsCurrent().entrySet()) {
            Optional<AspectDefinition> definition = aspects.lookup(source.getKey());
            if (definition.isEmpty() || !definition.get().isCompound()
                    || source.getValue() <= minimum) continue;
            EnumSet<PrimalAspect> reduced = reduceToPrimals(source.getKey(), aspects);
            for (PrimalAspect primal : PrimalAspect.ordered()) {
                if (reduced.contains(primal)
                        && wand.visCentivis(primal) + CENTIVIS_PER_VIS <= capacityCentivis) {
                    return Optional.of(new TransferSelection(
                            source.getKey(), primal, definition.get().color()));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<PrimalAspect> selectDirectTransfer(
            WandState wand, int capacityCentivis, AuraNodeState.Snapshot node) {
        int minimum = minimumVis(wand);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            if (wand.visCentivis(aspect) + CENTIVIS_PER_VIS <= capacityCentivis
                    && node.current().getOrDefault(aspect, 0) > minimum) {
                return Optional.of(aspect);
            }
        }
        return Optional.empty();
    }

    private static int minimumVis(WandState wand) {
        return "iron".equals(wand.capId()) || "wood".equals(wand.rodId()) ? 0 : 1;
    }

    static EnumSet<PrimalAspect> reduceToPrimals(String aspectId, AspectCatalog aspects) {
        EnumSet<PrimalAspect> result = EnumSet.noneOf(PrimalAspect.class);
        reduceToPrimals(aspectId, aspects, result, new HashSet<>());
        return result;
    }

    private static void reduceToPrimals(String aspectId, AspectCatalog aspects,
            EnumSet<PrimalAspect> result, Set<String> visiting) {
        if (!visiting.add(aspectId)) return;
        AspectDefinition definition = aspects.lookup(aspectId).orElse(null);
        if (definition == null) {
            visiting.remove(aspectId);
            return;
        }
        if (definition.isPrimal()) {
            try {
                result.add(PrimalAspect.fromId(aspectId));
            } catch (RuntimeException ignored) {
                // A data-defined primal outside the six wand pools cannot be stored.
            }
        } else {
            for (String component : definition.components()) {
                reduceToPrimals(component, aspects, result, visiting);
            }
        }
        visiting.remove(aspectId);
    }

    private static int aspectColor(AspectCatalog aspects, String aspectId) {
        return aspects.lookup(aspectId).map(AspectDefinition::color).orElse(0xFFFFFF);
    }

    record TransferSelection(String sourceAspectId, PrimalAspect targetPrimal, int color) {}

    private void findNodes(ServerLevel level) {
        nodes.clear();
        nodes.addAll(AuraNodeSpatialIndex.withinCube(
                level,
                worldPosition,
                RANGE
        ));
    }

    private void setDrain(ServerLevel level, BlockPos position, String aspectId, int color) {
        drainPosition = position.immutable();
        drainAspectId = aspectId;
        drainColor = color;
        drainUntil = level.getGameTime() + TRANSFER_INTERVAL + 1L;
        sync();
    }

    private void clearDrain(ServerLevel level) {
        if (drainPosition == null || level.getGameTime() <= drainUntil) return;
        drainPosition = null;
        drainAspectId = null;
        drainColor = 0;
        drainUntil = Long.MIN_VALUE;
        sync();
    }

    public ItemStack item() { return items.get(0); }
    public @Nullable BlockPos drainPosition() { return drainPosition; }
    public @Nullable String drainAspectId() { return drainAspectId; }
    public int drainColor() { return drainColor; }
    public boolean isDraining() {
        return level != null && drainPosition != null && drainAspectId != null
                && level.getGameTime() <= drainUntil;
    }

    public int comparatorLevel() {
        if (item().getItem() instanceof VisStorageItem storage) {
            long total = 0;
            for (PrimalAspect aspect : PrimalAspect.ordered()) {
                total += storage.visCentivis(item(), aspect);
            }
            long maximum = (long) storage.capacityCentivis()
                    * PrimalAspect.ordered().size();
            return maximum <= 0 ? 0
                    : (int) Math.floor(total * 14.0D / maximum) + 1;
        }
        Optional<WandState> state = WandVisService.state(item());
        if (state.isEmpty()) return 0;
        long total = 0;
        for (PrimalAspect aspect : PrimalAspect.ordered()) total += state.get().visCentivis(aspect);
        long maximum = (long) WandVisService.capacityCentivis(item()) * PrimalAspect.ordered().size();
        return maximum <= 0 ? 0 : (int) Math.floor(total * 14.0D / maximum) + 1;
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return item().isEmpty(); }
    @Override public ItemStack getItem(int slot) { return slot == 0 ? item() : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack removed = ContainerHelper.removeItem(items, 0, amount);
        if (!removed.isEmpty()) sync();
        return removed;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack removed = ContainerHelper.takeItem(items, 0);
        if (!removed.isEmpty()) sync();
        return removed;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot != 0) return;
        ItemStack stored = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!stored.isEmpty()) stored.setCount(1);
        items.set(0, stored);
        sync();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + .5D,
                worldPosition.getY() + .5D, worldPosition.getZ() + .5D) <= 64D;
    }
    @Override public void clearContent() { items.set(0, ItemStack.EMPTY); sync(); }
    @Override public int getMaxStackSize() { return 1; }
    @Override public int[] getSlotsForFace(Direction side) { return SLOT; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack,
            @Nullable Direction side) {
        return slot == 0 && item().isEmpty()
                && (WandVisService.isWand(stack)
                || stack.getItem() instanceof VisStorageItem);
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == 0;
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        if (drainPosition != null && drainAspectId != null) {
            tag.putLong("DrainPosition", drainPosition.asLong());
            tag.putString("DrainAspect", drainAspectId);
            tag.putInt("DrainColor", drainColor);
            tag.putLong("DrainUntil", drainUntil);
        }
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        items.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        if (tag.contains("DrainPosition") && tag.contains("DrainAspect")) {
            try {
                drainPosition = BlockPos.of(tag.getLong("DrainPosition"));
                drainAspectId = tag.getString("DrainAspect");
                drainColor = tag.contains("DrainColor")
                        ? tag.getInt("DrainColor")
                        : AspectRegistryRuntime.find(drainAspectId)
                                .map(AspectDefinition::color).orElse(0xFFFFFF);
                drainUntil = tag.getLong("DrainUntil");
            } catch (RuntimeException ignored) {
                drainPosition = null;
                drainAspectId = null;
                drainColor = 0;
            }
        } else {
            drainPosition = null;
            drainAspectId = null;
            drainColor = 0;
        }
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection,
            ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }
}
