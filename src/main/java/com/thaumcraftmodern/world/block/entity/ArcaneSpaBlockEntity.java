package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.item.BathSaltsItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.menu.ArcaneSpaMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Bytecode-faithful 1.7.10 TileSpa behavior adapted to modern fluid states. */
public final class ArcaneSpaBlockEntity extends BlockEntity
        implements WorldlyContainer, net.minecraft.world.MenuProvider {
    public static final int CAPACITY = 5000;
    public static final int DISPENSE_AMOUNT = 1000;
    public static final int DISPENSE_INTERVAL = 40;
    public static final int DATA_COUNT = 3;
    private static final int[] SALT_SLOT = {0};
    private static final int[] NO_SLOTS = {};

    private final NonNullList<ItemStack> items =
            NonNullList.withSize(1, ItemStack.EMPTY);
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };
    private boolean mix = true;
    private int counter;
    private int syncedAmount;
    private int syncedFluidId;
    private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> tank);
    private LazyOptional<IItemHandlerModifiable>[] itemCapabilities = createItemCapabilities();

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> mix ? 1 : 0;
                case 1 -> level != null && level.isClientSide ? syncedAmount : tank.getFluidAmount();
                case 2 -> level != null && level.isClientSide
                        ? syncedFluidId
                        : BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) mix = value != 0;
            else if (index == 1) syncedAmount = value;
            else if (index == 2) syncedFluidId = value;
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ArcaneSpaBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_SPA.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            ArcaneSpaBlockEntity spa) {
        if (++spa.counter % DISPENSE_INTERVAL != 0
                || level.hasNeighborSignal(pos)
                || !spa.hasIngredients()) {
            return;
        }
        spa.dispenseOnce((ServerLevel) level);
    }

    private void dispenseOnce(ServerLevel level) {
        BlockState output = outputState();
        if (output == null) return;

        BlockPos origin = worldPosition.above();
        if (isOutputSource(level, origin, output)) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos candidate = origin.offset(x, 0, z);
                    if (isValidLocation(level, candidate, output, true)) {
                        placeAndConsume(level, candidate, output);
                        return;
                    }
                }
            }
        } else if (isValidLocation(level, origin, output, false)) {
            placeAndConsume(level, origin, output);
        }
    }

    private void placeAndConsume(ServerLevel level, BlockPos target,
            BlockState output) {
        consumeIngredients();
        level.setBlock(target, output, 3);
    }

    private boolean hasIngredients() {
        FluidStack stored = tank.getFluid();
        if (mix) {
            return stored.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER)
                    && stored.getAmount() >= DISPENSE_AMOUNT
                    && items.get(0).getItem() instanceof BathSaltsItem;
        }
        return stored.getAmount() >= DISPENSE_AMOUNT
                && outputState() != null;
    }

    private @Nullable BlockState outputState() {
        if (mix) return ModBlocks.PURIFYING_FLUID.get().defaultBlockState();
        Fluid fluid = tank.getFluid().getFluid();
        BlockState output = fluid.defaultFluidState().createLegacyBlock();
        return output.isAir() ? null : output;
    }

    private boolean isValidLocation(ServerLevel level, BlockPos target,
            BlockState output, boolean requireConnection) {
        if (output.getFluidState().is(net.minecraft.world.level.material.Fluids.WATER)
                && level.dimensionType().ultraWarm()) {
            return false;
        }
        BlockState current = level.getBlockState(target);
        if (!level.getBlockState(target.below()).isFaceSturdy(level,
                target.below(), Direction.UP)
                || !current.canBeReplaced()
                || isOutputSource(level, target, output)) {
            return false;
        }
        if (!requireConnection) return true;
        for (Direction direction : Direction.values()) {
            if (isOutputSource(level, target.relative(direction), output)) return true;
        }
        return false;
    }

    private static boolean isOutputSource(ServerLevel level, BlockPos pos,
            BlockState output) {
        BlockState state = level.getBlockState(pos);
        if (!output.getFluidState().isEmpty()) {
            return !state.getFluidState().isEmpty()
                    && state.getFluidState().getType().isSame(output.getFluidState().getType())
                    && state.getFluidState().isSource();
        }
        return state.is(output.getBlock());
    }

    private void consumeIngredients() {
        if (mix) items.get(0).shrink(1);
        tank.drain(DISPENSE_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
        sync();
    }

    public FluidTank fluidTank() {
        return tank;
    }

    public ContainerData data() {
        return data;
    }

    public void toggleMix() {
        mix = !mix;
        sync();
    }

    public boolean mixing() {
        return mix;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("mix", mix);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mix = !tag.contains("mix") || tag.getBoolean("mix");
        if (tag.contains("Tank")) tank.readFromNBT(tag.getCompound("Tank"));
        else tank.readFromNBT(tag); // accepts the original flat TileSpa fluid tag.
        ContainerHelper.loadAllItems(tag, items);
        syncedAmount = tank.getFluidAmount();
        syncedFluidId = BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection,
            ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.thaumic_reborn.arcane_spa");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory,
            Player player) {
        return new ArcaneSpaMenu(id, inventory, this, data);
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return items.get(0).isEmpty(); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) sync();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) sync();
        return result;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        sync();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) <= 64.0D;
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && stack.getItem() instanceof BathSaltsItem;
    }
    @Override public void clearContent() { items.clear(); sync(); }
    @Override public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? NO_SLOTS : SALT_SLOT;
    }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack,
            @Nullable Direction side) {
        return side != Direction.UP && canPlaceItem(slot, stack);
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack,
            Direction side) {
        return side != Direction.UP;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER && side != Direction.UP) {
            return fluidCapability.cast();
        }
        if (capability == ForgeCapabilities.ITEM_HANDLER && side != null
                && side != Direction.UP) {
            return itemCapabilities[side.ordinal()].cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCapability.invalidate();
        for (LazyOptional<IItemHandlerModifiable> capability : itemCapabilities) {
            capability.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        fluidCapability = LazyOptional.of(() -> tank);
        itemCapabilities = createItemCapabilities();
    }

    private LazyOptional<IItemHandlerModifiable>[] createItemCapabilities() {
        return SidedInvWrapper.create(this, Direction.values());
    }
}
