package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.ThaumatoriumRecipeSyncPacket;
import com.thaumcraftmodern.world.block.entity.ThaumatoriumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public final class ThaumatoriumMenu extends AbstractContainerMenu {
    private final Container container;
    private final Inventory inventory;
    private final ContainerLevelAccess access;
    private final ThaumatoriumBlockEntity machine;
    private final List<String> aspectIds;
    private final int[] synchronizedReserved;
    private final ContainerData reservedData;
    private List<CrucibleRecipeDefinition> clientRecipes = List.of();
    private List<ResourceLocation> clientCraftableRecipes = List.of();
    private List<ResourceLocation> clientFormulae = List.of();
    private int clientFormulaCapacity = 1;
    private ResourceLocation clientDisplayedRecipe;
    private int recipeRevision;
    private List<ResourceLocation> lastSentRecipes = List.of();
    private List<ResourceLocation> lastSentCraftableRecipes = List.of();
    private List<ResourceLocation> lastSentFormulae = List.of();
    private int lastSentCapacity = -1;
    private ResourceLocation lastSentDisplayedRecipe;

    public static ThaumatoriumMenu fromNetwork(int id, Inventory inventory,
            FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos)
                instanceof ThaumatoriumBlockEntity machine) {
            return new ThaumatoriumMenu(id, inventory, machine);
        }
        return new ThaumatoriumMenu(id, inventory, new SimpleContainer(1), null);
    }

    public ThaumatoriumMenu(int id, Inventory inventory,
            ThaumatoriumBlockEntity machine) {
        this(id, inventory, machine, machine);
    }

    private ThaumatoriumMenu(int id, Inventory inventory,
            Container container, ThaumatoriumBlockEntity machine) {
        super(ModMenus.THAUMATORIUM.get(), id);
        this.container = container;
        this.inventory = inventory;
        this.machine = machine;
        this.aspectIds = AspectRegistryRuntime.catalog().definitions().stream()
                .map(definition -> definition.id())
                .sorted()
                .toList();
        this.synchronizedReserved = new int[aspectIds.size()];
        this.reservedData = new ContainerData() {
            @Override public int get(int index) {
                if (!inventory.player.level().isClientSide && machine != null) {
                    return machine.reservedEssentia()
                            .getOrDefault(aspectIds.get(index), 0);
                }
                return synchronizedReserved[index];
            }

            @Override public void set(int index, int value) {
                synchronizedReserved[index] = Math.max(0, value);
            }

            @Override public int getCount() {
                return aspectIds.size();
            }
        };
        this.access = machine == null ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos());
        checkContainerSize(container, 1);
        container.startOpen(inventory.player);
        addSlot(new Slot(container, 0, 48, 16));
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column + row * 9 + 9,
                    8 + column * 18, 84 + row * 18));
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(reservedData);
    }

    public List<CrucibleRecipeDefinition> recipes() {
        return machine == null || inventory.player.level().isClientSide
                ? clientRecipes : machine.availableRecipes(inventory.player);
    }

    public boolean selected(CrucibleRecipeDefinition recipe) {
        return inventory.player.level().isClientSide
                ? clientFormulae.contains(recipe.id())
                : machine != null && machine.hasFormula(recipe.id());
    }

    public boolean craftable(CrucibleRecipeDefinition recipe) {
        return inventory.player.level().isClientSide
                ? clientCraftableRecipes.contains(recipe.id())
                : machine != null && machine.canSelectRecipe(inventory.player, recipe.id());
    }

    public boolean canSelectOrSwitch(CrucibleRecipeDefinition recipe) {
        if (!craftable(recipe) || !reservedFits(recipe)) return false;
        if (selected(recipe)) return true;
        if (formulaCount() < formulaCapacity()) return true;
        ResourceLocation active = displayedRecipeId();
        return active != null && (inventory.player.level().isClientSide
                ? clientFormulae.contains(active)
                : machine != null && machine.hasFormula(active));
    }

    private boolean reservedFits(CrucibleRecipeDefinition recipe) {
        for (String aspect : aspectIds) {
            int stored = reservedAmount(aspect);
            if (stored > recipe.aspects().getOrDefault(aspect, 0)) return false;
        }
        return true;
    }

    public ResourceLocation displayedRecipeId() {
        return inventory.player.level().isClientSide
                ? clientDisplayedRecipe
                : machine == null ? null : machine.displayedRecipe();
    }

    public int formulaCount() {
        return inventory.player.level().isClientSide
                ? clientFormulae.size()
                : machine == null ? 0 : machine.formulaCount();
    }
    public int formulaCapacity() {
        return inventory.player.level().isClientSide
                ? clientFormulaCapacity
                : machine == null ? 1 : machine.formulaCapacity();
    }
    public int recipeRevision() { return recipeRevision; }

    public void applyRecipeSnapshot(
            List<CrucibleRecipeDefinition> recipes,
            List<ResourceLocation> craftableRecipes,
            List<ResourceLocation> formulae,
            int capacity,
            ResourceLocation displayedRecipe
    ) {
        clientRecipes = List.copyOf(recipes);
        clientCraftableRecipes = List.copyOf(craftableRecipes);
        clientFormulae = List.copyOf(formulae);
        clientFormulaCapacity = Math.max(1, capacity);
        clientDisplayedRecipe = displayedRecipe;
        recipeRevision++;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (!(inventory.player instanceof net.minecraft.server.level.ServerPlayer player)
                || machine == null) {
            return;
        }
        List<CrucibleRecipeDefinition> recipes = machine.availableRecipes(player);
        List<ResourceLocation> recipeIds = recipes.stream()
                .map(CrucibleRecipeDefinition::id).toList();
        List<ResourceLocation> formulae = machine.formulae();
        List<ResourceLocation> craftableRecipes = recipes.stream()
                .filter(recipe -> machine.canSelectRecipe(player, recipe.id()))
                .map(CrucibleRecipeDefinition::id)
                .toList();
        int capacity = machine.formulaCapacity();
        ResourceLocation displayed = machine.displayedRecipe();
        if (recipeIds.equals(lastSentRecipes)
                && craftableRecipes.equals(lastSentCraftableRecipes)
                && formulae.equals(lastSentFormulae)
                && capacity == lastSentCapacity
                && Objects.equals(displayed, lastSentDisplayedRecipe)) {
            return;
        }
        lastSentRecipes = new ArrayList<>(recipeIds);
        lastSentCraftableRecipes = new ArrayList<>(craftableRecipes);
        lastSentFormulae = new ArrayList<>(formulae);
        lastSentCapacity = capacity;
        lastSentDisplayedRecipe = displayed;
        ModNetwork.sendTo(player, new ThaumatoriumRecipeSyncPacket(
                containerId, recipes, craftableRecipes, formulae, capacity, displayed));
    }
    public int reservedAmount(String aspect) {
        int index = aspectIds.indexOf(aspect);
        if (index < 0) {
            return 0;
        }
        int menuSnapshot = reservedData.get(index);
        if (inventory.player.level().isClientSide && machine != null) {
            return Math.max(
                    menuSnapshot,
                    machine.reservedEssentia().getOrDefault(aspect, 0)
            );
        }
        return menuSnapshot;
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        List<CrucibleRecipeDefinition> recipes = recipes();
        if (machine == null
                || !(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                || id < 0 || id >= recipes.size()) {
            return false;
        }
        boolean successful = machine.selectRecipe(
                serverPlayer, recipes.get(id).id());
        playSelectionResultSound(serverPlayer, successful);
        return successful;
    }

    private static void playSelectionResultSound(
            net.minecraft.server.level.ServerPlayer player,
            boolean successful
    ) {
        player.playNotifySound(
                successful ? ModSounds.HH_ON.get() : ModSounds.HH_OFF.get(),
                SoundSource.PLAYERS,
                successful ? 0.3F : 0.2F,
                successful
                        ? 1.0F
                        : 1.0F + player.getRandom().nextFloat() * 0.1F
        );
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack live = slot.getItem();
        ItemStack original = live.copy();
        if (index == 0) {
            if (!moveItemStackTo(live, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(live, 0, 1, false)) {
            if (index < 28) {
                if (!moveItemStackTo(live, 28, 37, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(live, 1, 28, false)) return ItemStack.EMPTY;
        }
        if (live.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (live.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, live);
        return original;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.THAUMATORIUM.get());
    }
    @Override public void removed(Player player) { container.stopOpen(player); super.removed(player); }
}
