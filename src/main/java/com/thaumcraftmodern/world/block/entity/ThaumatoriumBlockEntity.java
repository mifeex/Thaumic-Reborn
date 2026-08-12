package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import com.thaumcraftmodern.crucible.CrucibleRecipeRegistry;
import com.thaumcraftmodern.crucible.EssentiaStore;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumcraftmodern.essentia.EssentiaTransport;
import com.thaumcraftmodern.essentia.tube.TubeFlowRules;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.ThaumatoriumEssentiaSyncPacket;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.menu.ThaumatoriumMenu;
import com.thaumcraftmodern.world.block.ThaumatoriumBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Server-authoritative automatic crucible recipe reservation and execution. */
public final class ThaumatoriumBlockEntity extends BlockEntity
        implements EssentiaTransport, WorldlyContainer, MenuProvider {
    private static final int[] AUTOMATION_SLOTS = {0};
    private static final double DISPLAY_OFFSET = 1.0D / 1.99D;
    private static final double DISPLAY_HALF_EXTENT = 0.65D;
    private final EssentiaStore reserved = new EssentiaStore();
    private final List<ResourceLocation> formulae = new ArrayList<>();
    private final Map<ResourceLocation, UUID> formulaOwners = new LinkedHashMap<>();
    private ItemStack catalyst = ItemStack.EMPTY;
    /** Catalyst identity to which the current TC4 formula selection belongs. */
    private ItemStack formulaCatalyst = ItemStack.EMPTY;
    private @Nullable ResourceLocation selectedRecipe;
    private @Nullable ResourceLocation displayedRecipe;
    private @Nullable UUID recipeOwner;
    private @Nullable String currentSuction;
    private int counter;

    public ThaumatoriumBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THAUMATORIUM.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        // The recipe preview is rendered outside the controller block. Keep
        // it inside the frustum box without changing its classic item pose.
        BlockState state = getBlockState();
        BlockPos lowerPosition = state.hasProperty(ThaumatoriumBlock.HALF)
                && state.getValue(ThaumatoriumBlock.HALF)
                        == DoubleBlockHalf.UPPER
                ? worldPosition.below()
                : worldPosition;
        Direction direction = state.hasProperty(ThaumatoriumBlock.FACING)
                ? state.getValue(ThaumatoriumBlock.FACING)
                : Direction.NORTH;
        return renderBoundingBox(lowerPosition, direction);
    }

    static AABB renderBoundingBox(BlockPos position, Direction facing) {
        double centerX = position.getX() + 0.5D
                + facing.getStepX() * DISPLAY_OFFSET;
        double centerZ = position.getZ() + 0.5D
                + facing.getStepZ() * DISPLAY_OFFSET;
        AABB machine = new AABB(
                position.getX(),
                position.getY(),
                position.getZ(),
                position.getX() + 1.0D,
                position.getY() + 2.0D,
                position.getZ() + 1.0D
        );
        AABB display = new AABB(
                centerX - DISPLAY_HALF_EXTENT,
                position.getY() + 0.75D,
                centerZ - DISPLAY_HALF_EXTENT,
                centerX + DISPLAY_HALF_EXTENT,
                position.getY() + 2.1D,
                centerZ + DISPLAY_HALF_EXTENT
        );
        return machine.minmax(display);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel, BlockPos pos,
            BlockState state, ThaumatoriumBlockEntity machine) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        machine.counter++;
        if (machine.counter % 40 == 0) machine.trimFormulaeToCapacity();
        if (machine.counter % 5 != 0) return;
        if (level.hasNeighborSignal(pos) || machine.catalyst.isEmpty()
                || !machine.hasHeat(level)) {
            machine.currentSuction = null;
            return;
        }
        CrucibleRecipeDefinition recipe = machine.resolveRecipe();
        if (recipe == null) {
            machine.currentSuction = null;
            return;
        }
        List<String> missing = missingAspects(
                recipe.aspects(), machine.reserved.view());
        if (missing.isEmpty()) {
            machine.currentSuction = null;
            machine.complete(level, recipe);
            return;
        }
        machine.currentSuction = machine.findAvailableAspect(level, missing);
        if (machine.currentSuction != null) machine.fill(level);
    }

    private boolean hasHeat(ServerLevel level) {
        // The modern compound recipe keeps its Alchemical Furnace as the
        // bottom member, immediately below the Thaumatorium controller.
        // The old pos.down(2) check belongs to TC4's crucible-over-fire stack.
        if (level.getBlockState(worldPosition.below())
                .is(ModBlocks.ALCHEMICAL_FURNACE.get())) {
            return true;
        }
        BlockState heat = level.getBlockState(worldPosition.below(2));
        return heat.is(Blocks.FIRE) || heat.is(Blocks.SOUL_FIRE)
                || heat.is(ModBlocks.NITOR.get())
                || heat.getFluidState().is(Fluids.LAVA);
    }

    static @Nullable String nextRequiredAspect(
            Map<String, Integer> required,
            Map<String, Integer> stored
    ) {
        return missingAspects(required, stored).stream()
                .findFirst()
                .orElse(null);
    }

    static List<String> missingAspects(
            Map<String, Integer> required,
            Map<String, Integer> stored
    ) {
        return required.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> stored.getOrDefault(entry.getKey(), 0)
                        < entry.getValue())
                .map(Map.Entry::getKey)
                .toList();
    }

    static @Nullable String refundableAspect(
            Map<String, Integer> stored,
            @Nullable String requested
    ) {
        if (requested != null && stored.getOrDefault(requested, 0) > 0) {
            return requested;
        }
        return stored.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private @Nullable String findAvailableAspect(
            ServerLevel level,
            List<String> missing
    ) {
        ArrayDeque<SupplyNode> queue = new ArrayDeque<>();
        Direction output = facing();
        for (int y = 0; y <= 1; y++) {
            BlockPos port = worldPosition.above(y);
            for (Direction side : Direction.values()) {
                if (side == output || side == Direction.DOWN
                        || y == 0 && side == Direction.UP) continue;
                BlockPos neighbour = port.relative(side);
                if (!level.hasChunkAt(neighbour)) continue;
                EssentiaTransport transport = EssentiaConnections.neighbour(
                        level, port, side).orElse(null);
                if (transport != null) {
                    queue.addLast(new SupplyNode(
                            neighbour, side.getOpposite(), null));
                }
            }
        }

        Set<SupplyNode> visited = new HashSet<>();
        while (!queue.isEmpty() && visited.size() < 512) {
            SupplyNode node = queue.removeFirst();
            if (!visited.add(node) || !level.hasChunkAt(node.position())) continue;
            BlockEntity entity = level.getBlockEntity(node.position());
            if (!(entity instanceof EssentiaTransport transport)
                    || !transport.canOutputTo(node.towardMachine())) continue;

            String filter = node.filter();
            if (entity instanceof EssentiaTubeBlockEntity tube) {
                if (!TubeFlowRules.acceptsSuctionFrom(
                        tube.policy(), tube.facing(), node.towardMachine())) continue;
                if (tube.filter() != null) {
                    if (filter != null && !filter.equals(tube.filter())) continue;
                    filter = tube.filter();
                }
            }
            if (filter != null && !missing.contains(filter)) continue;

            if (transport.essentiaAmount(node.towardMachine()) > 0) {
                String aspect = transport.essentiaType(node.towardMachine());
                if (aspect == null) aspect = transport.essentiaType(null);
                if (aspect != null && missing.contains(aspect)
                        && (filter == null || filter.equals(aspect))) {
                    return aspect;
                }
            }

            for (Direction side : Direction.values()) {
                if (side == node.towardMachine()
                        || !transport.canInputFrom(side)) continue;
                if (entity instanceof EssentiaTubeBlockEntity tube
                        && !TubeFlowRules.mayPullFrom(
                                tube.policy(), tube.facing(), side)) continue;
                BlockPos neighbour = node.position().relative(side);
                if (!level.hasChunkAt(neighbour)) continue;
                EssentiaTransport remote = EssentiaConnections.neighbour(
                        level, node.position(), side).orElse(null);
                Direction remoteSide = side.getOpposite();
                if (remote != null && remote.canOutputTo(remoteSide)) {
                    queue.addLast(new SupplyNode(neighbour, remoteSide, filter));
                }
            }
        }
        return null;
    }

    private record SupplyNode(
            BlockPos position,
            Direction towardMachine,
            @Nullable String filter
    ) { }

    public boolean selectRecipe(ServerPlayer player, ResourceLocation id) {
        CrucibleRecipeDefinition recipe = CrucibleRecipeRegistry.all().stream()
                .filter(candidate -> candidate.id().equals(id)).findFirst().orElse(null);
        if (recipe == null || !canSelectRecipe(player, recipe)) return false;

        // Clicking the active formula a second time removes it, but never
        // strands essentia which has already been reserved for that craft.
        if (id.equals(selectedRecipe)) {
            if (!reserved.isEmpty()) return false;
            formulae.remove(id);
            formulaOwners.remove(id);
            if (id.equals(displayedRecipe)) displayedRecipe = null;
            selectedRecipe = null;
            recipeOwner = null;
            currentSuction = null;
            if (formulae.isEmpty()) formulaCatalyst = ItemStack.EMPTY;
            syncChanged();
            return true;
        }

        // A running craft can change output only when every already accepted
        // point of essentia is also required by the new recipe. This preserves
        // TC4's aspect reservation without deleting or refunding valid work.
        if (!reservedFitsRecipe(reserved.view(), recipe.aspects())) return false;

        if (!formulae.contains(id)) {
            if (formulae.size() >= formulaCapacity()) {
                ResourceLocation replaced = replaceableFormula();
                if (replaced == null) return false;
                formulae.remove(replaced);
                formulaOwners.remove(replaced);
            }
            formulae.add(id);
            formulaOwners.put(id, player.getUUID());
        }
        formulaCatalyst = catalyst.isEmpty()
                ? ItemStack.EMPTY : catalyst.copyWithCount(1);
        selectedRecipe = id;
        displayedRecipe = id;
        recipeOwner = formulaOwners.getOrDefault(id, player.getUUID());
        currentSuction = null;
        syncChanged();
        return true;
    }

    private @Nullable ResourceLocation replaceableFormula() {
        if (selectedRecipe != null && formulae.contains(selectedRecipe)) {
            return selectedRecipe;
        }
        if (displayedRecipe != null && formulae.contains(displayedRecipe)) {
            return displayedRecipe;
        }
        return null;
    }

    static boolean reservedFitsRecipe(
            Map<String, Integer> stored,
            Map<String, Integer> required
    ) {
        return stored.entrySet().stream().allMatch(entry ->
                entry.getValue() <= 0
                        || entry.getValue() <= required.getOrDefault(
                                entry.getKey(), 0));
    }

    public java.util.List<CrucibleRecipeDefinition> availableRecipes(Player player) {
        if (catalyst.isEmpty()) {
            return List.of();
        }
        return CrucibleRecipeRegistry.all().stream()
                .filter(recipe -> recipe.matchesCatalyst(catalyst))
                .filter(recipe -> formulae.contains(recipe.id())
                        || recipe.research().isBlank()
                        || KnowledgeAccess.get(player)
                                .map(k -> k.hasCompletedResearch(recipe.research()))
                                .orElse(false))
                .toList();
    }

    public boolean canSelectRecipe(Player player, ResourceLocation id) {
        CrucibleRecipeDefinition recipe = findRecipe(id);
        return recipe != null && canSelectRecipe(player, recipe);
    }

    private boolean canSelectRecipe(Player player, CrucibleRecipeDefinition recipe) {
        return recipe.research().isBlank()
                || KnowledgeAccess.get(player)
                        .map(knowledge -> knowledge.hasCompletedResearch(recipe.research()))
                        .orElse(false);
    }

    public boolean insertCatalyst(ServerPlayer player, ItemStack offered) {
        if (offered.isEmpty() || !catalyst.isEmpty()) return false;
        clearFormulaeForDifferentCatalyst(offered);
        CrucibleRecipeDefinition match = formulae.stream()
                .map(this::findRecipe)
                .filter(java.util.Objects::nonNull)
                .filter(recipe -> recipe.matchesCatalyst(offered))
                .findFirst().orElse(null);
        if (match == null) match = CrucibleRecipeRegistry.all().stream()
                .filter(recipe -> recipe.matchesCatalyst(offered))
                .filter(recipe -> recipe.research().isBlank()
                        || KnowledgeAccess.get(player).map(k -> k.hasCompletedResearch(recipe.research())).orElse(false))
                .findFirst().orElse(null);
        if (match == null) return false;
        if (!formulae.contains(match.id()) && !selectRecipe(player, match.id())) return false;
        selectedRecipe = match.id();
        displayedRecipe = match.id();
        recipeOwner = formulaOwners.getOrDefault(match.id(), player.getUUID());
        catalyst = offered.copyWithCount(1);
        formulaCatalyst = catalyst.copyWithCount(1);
        offered.shrink(1);
        syncChanged();
        return true;
    }

    public ItemStack removeCatalyst() {
        ItemStack result = catalyst;
        catalyst = ItemStack.EMPTY;
        selectedRecipe = null; displayedRecipe = null;
        recipeOwner = null; currentSuction = null;
        syncChanged();
        return result;
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() {
        ThaumatoriumBlockEntity controller = controller();
        return controller == this ? catalyst.isEmpty() : controller.isEmpty();
    }
    @Override public ItemStack getItem(int slot) {
        ThaumatoriumBlockEntity controller = controller();
        return controller == this
                ? slot == 0 ? catalyst : ItemStack.EMPTY
                : controller.getItem(slot);
    }
    @Override public ItemStack removeItem(int slot, int amount) {
        ThaumatoriumBlockEntity controller = controller();
        if (controller != this) return controller.removeItem(slot, amount);
        if (slot != 0 || amount <= 0) return ItemStack.EMPTY;
        ItemStack removed = catalyst.split(Math.min(amount, catalyst.getCount()));
        if (!removed.isEmpty()) {
            if (catalyst.isEmpty()) {
                catalyst = ItemStack.EMPTY;
                selectedRecipe = null;
                displayedRecipe = null;
                recipeOwner = null;
                currentSuction = null;
            }
            syncChanged();
        }
        return removed;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ThaumatoriumBlockEntity controller = controller();
        return controller == this
                ? slot == 0 ? removeCatalyst() : ItemStack.EMPTY
                : controller.removeItemNoUpdate(slot);
    }
    @Override public void setItem(int slot, ItemStack stack) {
        ThaumatoriumBlockEntity controller = controller();
        if (controller != this) {
            controller.setItem(slot, stack);
            return;
        }
        if (slot != 0) return;
        catalyst = stack.copy();
        if (catalyst.getCount() > getMaxStackSize()) catalyst.setCount(getMaxStackSize());
        if (level != null && level.isClientSide) {
            setChanged();
            return;
        }
        if (catalyst.isEmpty()) {
            selectedRecipe = null;
            displayedRecipe = null;
            recipeOwner = null;
            currentSuction = null;
        } else {
            clearFormulaeForDifferentCatalyst(catalyst);
        }
        syncChanged();
    }

    private void clearFormulaeForDifferentCatalyst(ItemStack currentCatalyst) {
        if (formulae.isEmpty()
                || sameFormulaCatalyst(formulaCatalyst, currentCatalyst)) return;
        formulae.clear();
        formulaOwners.clear();
        formulaCatalyst = ItemStack.EMPTY;
        selectedRecipe = null;
        displayedRecipe = null;
        recipeOwner = null;
        currentSuction = null;
    }

    static boolean sameFormulaCatalyst(ItemStack previous, ItemStack current) {
        return !previous.isEmpty() && !current.isEmpty()
                && ItemStack.isSameItemSameTags(previous, current);
    }
    @Override public boolean stillValid(Player player) {
        ThaumatoriumBlockEntity controller = controller();
        return controller != this ? controller.stillValid(player)
                : level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + .5,
                worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64;
    }
    @Override public void clearContent() { removeItemNoUpdate(0); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0;
    }
    @Override public int[] getSlotsForFace(Direction side) {
        return AUTOMATION_SLOTS;
    }
    @Override public boolean canPlaceItemThroughFace(
            int slot,
            ItemStack stack,
            @Nullable Direction side
    ) {
        ThaumatoriumBlockEntity controller = controller();
        return controller == this
                ? canPlaceItem(slot, stack)
                : controller.canPlaceItemThroughFace(slot, stack, side);
    }
    @Override public boolean canTakeItemThroughFace(
            int slot,
            ItemStack stack,
            Direction side
    ) {
        return slot == 0;
    }
    @Override public Component getDisplayName() {
        return Component.translatable("block.thaumcraftmodern.thaumatorium");
    }
    @Override public @Nullable AbstractContainerMenu createMenu(int id,
            Inventory inventory, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            syncEssentiaTo(serverPlayer);
        }
        return new ThaumatoriumMenu(id, inventory, this);
    }

    private Optional<CrucibleRecipeDefinition> recipe() {
        if (selectedRecipe == null) return Optional.empty();
        return CrucibleRecipeRegistry.all().stream().filter(recipe -> recipe.id().equals(selectedRecipe)).findFirst();
    }

    private @Nullable CrucibleRecipeDefinition findRecipe(ResourceLocation id) {
        return CrucibleRecipeRegistry.all().stream()
                .filter(recipe -> recipe.id().equals(id)).findFirst().orElse(null);
    }

    private @Nullable CrucibleRecipeDefinition resolveRecipe() {
        CrucibleRecipeDefinition current = recipe().orElse(null);
        if (current != null && formulae.contains(current.id())
                && current.matchesCatalyst(catalyst)) return current;
        for (ResourceLocation id : formulae) {
            CrucibleRecipeDefinition candidate = findRecipe(id);
            if (candidate == null || !candidate.matchesCatalyst(catalyst)) continue;
            selectedRecipe = id;
            displayedRecipe = id;
            recipeOwner = formulaOwners.get(id);
            currentSuction = null;
            syncChanged();
            return candidate;
        }
        selectedRecipe = null;
        displayedRecipe = null;
        recipeOwner = null;
        currentSuction = null;
        return null;
    }

    public int formulaCapacity() {
        ThaumatoriumBlockEntity controller = controller();
        if (controller != this) return controller.formulaCapacity();
        if (level == null) return 1;
        int capacity = 1;
        Direction output = facing();
        for (int y = 0; y <= 1; y++) {
            for (Direction direction : Direction.values()) {
                if (direction == Direction.DOWN || direction == output) continue;
                BlockState state = level.getBlockState(
                        worldPosition.above(y).relative(direction));
                if (state.is(ModBlocks.MNEMONIC_MATRIX.get())
                        && state.getValue(com.thaumcraftmodern.world.block.MnemonicMatrixBlock.FACING)
                        == direction.getOpposite()) capacity += 2;
            }
        }
        return capacity;
    }

    private void trimFormulaeToCapacity() {
        int capacity = formulaCapacity();
        boolean changed = false;
        while (formulae.size() > capacity) {
            ResourceLocation removed = formulae.remove(formulae.size() - 1);
            formulaOwners.remove(removed);
            if (removed.equals(selectedRecipe)) {
                selectedRecipe = null;
                recipeOwner = null;
                currentSuction = null;
            }
            if (removed.equals(displayedRecipe)) displayedRecipe = null;
            changed = true;
        }
        if (formulae.isEmpty()) formulaCatalyst = ItemStack.EMPTY;
        if (changed) syncChanged();
    }

    public boolean hasFormula(ResourceLocation id) {
        ThaumatoriumBlockEntity controller = controller();
        return controller == this ? formulae.contains(id) : controller.hasFormula(id);
    }
    public int formulaCount() {
        ThaumatoriumBlockEntity controller = controller();
        return controller == this ? formulae.size() : controller.formulaCount();
    }
    public List<ResourceLocation> formulae() {
        ThaumatoriumBlockEntity controller = controller();
        return controller == this ? List.copyOf(formulae) : controller.formulae();
    }

    private void fill(ServerLevel level) {
        for (int y = 0; y <= 1; y++) {
            for (Direction side : Direction.values()) {
                if (side == facing() || side == Direction.DOWN || y == 0 && side == Direction.UP) continue;
                BlockPos sourcePos = worldPosition.above(y);
                EssentiaTransport remote = EssentiaConnections.neighbour(level, sourcePos, side).orElse(null);
                if (remote == null || remote.essentiaAmount(side.getOpposite()) <= 0
                        || remote.suctionAmount(side.getOpposite()) >= suctionAmount(side)
                        || suctionAmount(side) < remote.minimumSuction()) continue;
                int taken = remote.takeEssentia(currentSuction, 1, side.getOpposite());
                if (taken > 0) { addEssentia(currentSuction, taken, side); return; }
            }
        }
    }

    private void complete(ServerLevel level, CrucibleRecipeDefinition recipe) {
        if (!reserved.contains(recipe.aspects()) || !recipe.matchesCatalyst(catalyst)) return;
        ItemStack output = recipe.output();
        Direction out = facing();
        BlockEntity target = level.getBlockEntity(worldPosition.relative(out));
        IItemHandler handler = target == null ? null
                : target.getCapability(ForgeCapabilities.ITEM_HANDLER, out.getOpposite()).orElse(null);
        if (handler != null && !insert(handler, output.copy(), true).isEmpty()) return;

        catalyst.shrink(1);
        if (catalyst.isEmpty()) {
            catalyst = ItemStack.EMPTY;
            displayedRecipe = null;
        }
        reserved.clear();
        if (handler != null) output = insert(handler, output, false);
        if (!output.isEmpty()) {
            ItemEntity entity = new ItemEntity(level,
                    worldPosition.getX() + 0.5D + out.getStepX() * 0.66D,
                    worldPosition.getY() + 0.33D,
                    worldPosition.getZ() + 0.5D + out.getStepZ() * 0.66D, output);
            entity.setDeltaMovement(out.getStepX() * 0.075D, 0.025D, out.getStepZ() * 0.075D);
            level.addFreshEntity(entity);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 0.25F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        currentSuction = null;
        selectedRecipe = null;
        recipeOwner = null;
        syncChanged();
    }

    private static ItemStack insert(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) remaining = handler.insertItem(slot, remaining, simulate);
        return remaining;
    }

    private Direction facing() {
        return getBlockState().hasProperty(ThaumatoriumBlock.FACING)
                ? getBlockState().getValue(ThaumatoriumBlock.FACING) : Direction.NORTH;
    }
    private boolean upperHalf() {
        return getBlockState().hasProperty(ThaumatoriumBlock.HALF)
                && getBlockState().getValue(ThaumatoriumBlock.HALF) == DoubleBlockHalf.UPPER;
    }
    private ThaumatoriumBlockEntity controller() {
        if (!upperHalf() || level == null) return this;
        return level.getBlockEntity(worldPosition.below()) instanceof ThaumatoriumBlockEntity lower
                ? lower : this;
    }
    public ItemStack catalyst() { return controller() == this ? catalyst.copy() : controller().catalyst(); }
    public @Nullable ResourceLocation selectedRecipe() { return controller() == this ? selectedRecipe : controller().selectedRecipe(); }
    public @Nullable ResourceLocation displayedRecipe() { return controller() == this ? displayedRecipe : controller().displayedRecipe(); }
    public Map<String, Integer> reservedEssentia() { return controller() == this ? reserved.view() : controller().reservedEssentia(); }
    public @Nullable String currentSuction() { return controller() == this ? currentSuction : controller().currentSuction(); }

    @Override public boolean isConnectable(Direction side) { return side != facing(); }
    @Override public boolean canInputFrom(Direction side) {
        return side != facing() && !refundingEssentia();
    }
    @Override public boolean canOutputTo(Direction side) {
        return side != facing() && refundingEssentia();
    }
    @Override public void setSuction(@Nullable String aspect, int amount) {
        ThaumatoriumBlockEntity controller = controller();
        if (controller == this) currentSuction = aspect; else controller.setSuction(aspect, amount);
    }
    @Override public @Nullable String suctionType(Direction side) { return currentSuction(); }
    @Override public int suctionAmount(Direction side) { return currentSuction() == null ? 0 : 128; }
    @Override public @Nullable String essentiaType(Direction side) {
        ThaumatoriumBlockEntity controller = controller();
        if (!controller.refundingEssentia()) return null;
        String requested = null;
        if (level != null && side != null) {
            EssentiaTransport remote = EssentiaConnections.neighbour(
                    level, worldPosition, side).orElse(null);
            if (remote != null) requested = remote.suctionType(side.getOpposite());
        }
        return refundableAspect(controller.reserved.view(), requested);
    }
    @Override public int essentiaAmount(Direction side) {
        ThaumatoriumBlockEntity controller = controller();
        String aspect = essentiaType(side);
        return aspect == null ? 0 : controller.reserved.amount(aspect);
    }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) {
        ThaumatoriumBlockEntity controller = controller();
        if (!canOutputTo(side) || amount <= 0
                || !controller.reserved.remove(aspect, amount)) return 0;
        controller.syncChanged();
        return amount;
    }
    @Override public int addEssentia(String aspect, int amount, Direction side) {
        ThaumatoriumBlockEntity controller = controller();
        if (controller != this) return controller.addEssentia(aspect, amount, side);
        CrucibleRecipeDefinition recipe = recipe().orElse(null);
        if (!canInputFrom(side) || recipe == null || amount <= 0) return 0;
        int needed = recipe.aspects().getOrDefault(aspect, 0) - reserved.amount(aspect);
        int accepted = Math.min(amount, Math.max(0, needed));
        if (accepted > 0) {
            reserved.add(aspect, accepted);
            syncChanged();
        }
        return accepted;
    }

    private boolean refundingEssentia() {
        ThaumatoriumBlockEntity controller = controller();
        return controller == this
                ? catalyst.isEmpty() && !reserved.isEmpty()
                : controller.refundingEssentia();
    }

    private void syncChanged() {
        EssentiaSync.changed(this);
        if (level instanceof ServerLevel serverLevel) {
            ModNetwork.sendToTrackingChunk(
                    serverLevel,
                    worldPosition,
                    new ThaumatoriumEssentiaSyncPacket(
                            worldPosition,
                            reserved.save()
                    )
            );
        }
    }

    private void syncEssentiaTo(ServerPlayer player) {
        ModNetwork.sendTo(
                player,
                new ThaumatoriumEssentiaSyncPacket(
                        worldPosition,
                        reserved.save()
                )
        );
    }

    public void applyClientEssentiaSnapshot(CompoundTag essentia) {
        if (level != null && level.isClientSide) {
            reserved.load(essentia);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Catalyst", catalyst.save(new CompoundTag()));
        tag.put("FormulaCatalyst", formulaCatalyst.save(new CompoundTag()));
        tag.put("Essentia", reserved.save());
        ListTag storedFormulae = new ListTag();
        for (ResourceLocation id : formulae) {
            CompoundTag formula = new CompoundTag();
            formula.putString("Id", id.toString());
            UUID owner = formulaOwners.get(id);
            if (owner != null) formula.putUUID("Owner", owner);
            storedFormulae.add(formula);
        }
        tag.put("Formulae", storedFormulae);
        if (selectedRecipe != null) tag.putString("Recipe", selectedRecipe.toString());
        if (displayedRecipe != null) tag.putString("DisplayRecipe", displayedRecipe.toString());
        if (recipeOwner != null) tag.putUUID("RecipeOwner", recipeOwner);
        if (currentSuction != null) tag.putString("Suction", currentSuction);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        catalyst = ItemStack.of(tag.getCompound("Catalyst"));
        formulaCatalyst = ItemStack.of(tag.getCompound("FormulaCatalyst"));
        reserved.load(tag.getCompound("Essentia"));
        formulae.clear();
        formulaOwners.clear();
        ListTag storedFormulae = tag.getList("Formulae", Tag.TAG_COMPOUND);
        for (int index = 0; index < storedFormulae.size(); index++) {
            CompoundTag formula = storedFormulae.getCompound(index);
            ResourceLocation id = storedRecipeId(formula.getString("Id"));
            if (id == null || formulae.contains(id)) continue;
            formulae.add(id);
            if (formula.hasUUID("Owner")) formulaOwners.put(id, formula.getUUID("Owner"));
        }
        selectedRecipe = storedRecipeId(tag.getString("Recipe"));
        displayedRecipe = storedRecipeId(tag.getString("DisplayRecipe"));
        recipeOwner = tag.hasUUID("RecipeOwner") ? tag.getUUID("RecipeOwner") : null;
        if (formulae.isEmpty() && selectedRecipe != null) {
            formulae.add(selectedRecipe);
            if (recipeOwner != null) formulaOwners.put(selectedRecipe, recipeOwner);
        }
        if (!formulae.isEmpty() && formulaCatalyst.isEmpty()) {
            if (catalyst.isEmpty()) {
                formulae.clear();
                formulaOwners.clear();
                selectedRecipe = null;
                recipeOwner = null;
            } else {
                formulaCatalyst = catalyst.copyWithCount(1);
            }
        }
        if (catalyst.isEmpty()) {
            displayedRecipe = null;
        } else if (displayedRecipe == null) {
            displayedRecipe = selectedRecipe != null
                    ? selectedRecipe
                    : formulae.stream().findFirst().orElse(null);
        }
        String suction = tag.getString("Suction"); currentSuction = suction.isBlank() ? null : suction;
    }

    static @Nullable ResourceLocation storedRecipeId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        ResourceLocation id = ResourceLocation.tryParse(raw);
        return id == null || id.getPath().isBlank() ? null : id;
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { if (packet.getTag() != null) load(packet.getTag()); }
}
