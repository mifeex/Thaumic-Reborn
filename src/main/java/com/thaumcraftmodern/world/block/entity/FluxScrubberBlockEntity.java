package com.thaumcraftmodern.world.block.entity;

import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.FluxGooBlock;
import com.thaumcraftmodern.visnet.VisMachineAccess;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class FluxScrubberBlockEntity extends BlockEntity implements EssentiaTransport {
    public static final int RANGE = 16;
    private int essentia, charges, power;
    private Direction facing = Direction.DOWN;
    private long[] checklist = new long[0];
    private int checklistCursor;
    public FluxScrubberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUX_SCRUBBER.get(), pos, state);
    }
    public static void serverTick(net.minecraft.world.level.Level raw, BlockPos pos,
            BlockState state, FluxScrubberBlockEntity scrubber) {
        if (!(raw instanceof ServerLevel level)) return;
        if (scrubber.charges >= 4) {
            scrubber.charges -= 4;
            if (level.random.nextInt(4) == 0) scrubber.essentia = Math.min(4, scrubber.essentia + 1);
        }
        if (scrubber.power < 5)
            scrubber.power += VisMachineAccess.consumeNearest(level,pos,PrimalAspect.AER,10);
        if (scrubber.power >= 5) scrubber.checkFlux(level);
    }
    private void checkFlux(ServerLevel level) {
        if (checklistCursor >= checklist.length) {
            rebuildChecklist(level);
        }
        for (int checked = 0;
                checked < 16 && checklistCursor < checklist.length;
                checked++) {
            BlockPos target = BlockPos.of(checklist[checklistCursor++]);
            if (target.distSqr(worldPosition) >= RANGE * RANGE) continue;
            BlockState flux = level.getBlockState(target);
            if (!flux.is(ModBlocks.FLUX_GOO.get())) continue;
            power -= 5;
            int amount = flux.getValue(FluxGooBlock.LEVEL);
            level.setBlock(target, amount > 0 ? flux.setValue(FluxGooBlock.LEVEL, amount-1)
                    : Blocks.AIR.defaultBlockState(), 3);
            charges++;
            setChanged();
            return;
        }
    }

    private void rebuildChecklist(ServerLevel level) {
        int diameter = RANGE * 2 + 1;
        checklist = new long[diameter * diameter * diameter];
        checklistCursor = 0;
        int index = 0;
        for (int x = -RANGE; x <= RANGE; x++) {
            for (int y = -RANGE; y <= RANGE; y++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    checklist[index++] = BlockPos.asLong(
                            worldPosition.getX() + x,
                            worldPosition.getY() + y,
                            worldPosition.getZ() + z
                    );
                }
            }
        }
        for (int current = checklist.length - 1; current > 0; current--) {
            int swapIndex = level.random.nextInt(current + 1);
            long swap = checklist[current];
            checklist[current] = checklist[swapIndex];
            checklist[swapIndex] = swap;
        }
    }
    public int charges() { return charges; }
    public int power() { return power; }
    public Direction facing() { return facing; }
    public void setFacing(Direction facing) { this.facing=facing; setChanged(); }
    public int animationSeed() { return Math.floorMod(worldPosition.hashCode(), 1000); }
    @Override public boolean isConnectable(Direction side) { return side == facing; }
    @Override public boolean canInputFrom(Direction side) { return false; }
    @Override public boolean canOutputTo(Direction side) { return side == facing; }
    @Override public void setSuction(@Nullable String aspect, int amount) {}
    @Override public @Nullable String suctionType(Direction side) { return null; }
    @Override public int suctionAmount(Direction side) { return 0; }
    @Override public @Nullable String essentiaType(Direction side) { return "praecantatio"; }
    @Override public int essentiaAmount(Direction side) { return essentia; }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) {
        if (!"praecantatio".equals(aspect) || side != facing) return 0;
        int out=Math.min(amount,essentia); essentia-=out; setChanged(); return out;
    }
    @Override public int addEssentia(String aspect, int amount, Direction side) { return 0; }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag);
        tag.putInt("essentia",essentia); tag.putInt("charges",charges); tag.putInt("power",power); tag.putInt("facing",facing.get3DDataValue()); }
    @Override public void load(CompoundTag tag) { super.load(tag); essentia=tag.getInt("essentia"); charges=tag.getInt("charges"); power=tag.getInt("power"); facing=Direction.from3DDataValue(tag.getInt("facing")); }
}
