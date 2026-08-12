package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.ArcaneLampBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/** TC4 Lamp of Fertility: Victus-fed, density-limited passive breeding. */
public final class FertilityLampBlockEntity extends BlockEntity implements EssentiaTransport {
    public static final String ASPECT = "victus";
    private int charges;
    private int count;
    private int drawDelay;

    public FertilityLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FERTILITY_LAMP.get(), pos, state);
    }

    public static void serverTick(Level rawLevel, BlockPos pos, BlockState state,
            FertilityLampBlockEntity lamp) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        boolean wasLit = lamp.charges > 0;
        if (lamp.charges < 4 && lamp.drawEssentia(level)) lamp.charges++;
        if (lamp.charges > 1 && ++lamp.count % 300 == 0) lamp.updateAnimals(level);
        lamp.syncIfNeeded(wasLit);
    }

    private void updateAnimals(ServerLevel level) {
        List<Animal> animals = level.getEntitiesOfClass(
                Animal.class, new AABB(worldPosition).inflate(7.0D));
        for (Animal animal : animals) {
            if (animal.getAge() != 0 || animal.isInLove()) continue;
            List<Animal> sameClass = animals.stream()
                    .filter(other -> other.getClass().equals(animal.getClass())).toList();
            if (sameClass.size() > 7) continue;
            Animal mate = null;
            for (Animal other : sameClass) {
                if (other.getAge() != 0 || other.isInLove()) continue;
                if (mate == null) mate = other;
                else {
                    charges -= 2;
                    other.setInLove(null);
                    mate.setInLove(null);
                    setChanged();
                    return;
                }
            }
        }
    }

    private boolean drawEssentia(ServerLevel level) {
        if (++drawDelay % 5 != 0) return false;
        Direction input = inputSide();
        EssentiaTransport remote = EssentiaConnections.neighbour(
                level, worldPosition, input).orElse(null);
        return remote != null && remote.canOutputTo(input.getOpposite())
                && remote.suctionAmount(input.getOpposite()) < suctionAmount(input)
                && remote.takeEssentia(ASPECT, 1, input.getOpposite()) == 1;
    }

    private Direction inputSide() {
        BlockState state = getBlockState();
        return state.hasProperty(ArcaneLampBlock.FACING)
                ? state.getValue(ArcaneLampBlock.FACING) : Direction.DOWN;
    }

    private void syncIfNeeded(boolean wasLit) {
        boolean lit = charges > 0;
        setChanged();
        if (level != null && wasLit != lit) {
            level.setBlock(worldPosition, getBlockState().setValue(
                    ArcaneLampBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    public int charges() { return charges; }
    @Override public boolean isConnectable(Direction side) { return side == inputSide(); }
    @Override public boolean canInputFrom(Direction side) { return side == inputSide(); }
    @Override public boolean canOutputTo(Direction side) { return false; }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return ASPECT; }
    @Override public int suctionAmount(Direction side) {
        return side == inputSide() ? 128 - charges * 10 : 0;
    }
    @Override public @Nullable String essentiaType(Direction side) { return null; }
    @Override public int essentiaAmount(Direction side) { return 0; }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) { return 0; }
    @Override public int addEssentia(String aspect, int amount, Direction side) { return 0; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.putInt("Charges", charges); tag.putInt("Count", count);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); charges = tag.getInt("Charges"); count = tag.getInt("Count");
    }
}
