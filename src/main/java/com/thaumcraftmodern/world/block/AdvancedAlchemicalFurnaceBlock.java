package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.construction.CraftingStructureDisassembly;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModParticles;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.entity.AdvancedAlchemicalFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** The invisible 3x2x3 shell rendered and operated as TC4's advanced furnace. */
public final class AdvancedAlchemicalFurnaceBlock extends BaseEntityBlock {
    public static final int CENTER = 0;
    public static final int LOWER_NOZZLE = 1;
    public static final int UPPER_CORNER = 2;
    public static final int UPPER_CARDINAL = 3;
    public static final int LOWER_CORNER = 4;
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 10);
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    private static final VoxelShape ITEM_CENTER_SHAPE = box(0, 0, 0, 16, 11.2, 16);

    public AdvancedAlchemicalFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, CENTER).setValue(LIGHT, 0));
    }

    public BlockState stateForPart(int part) {
        return defaultBlockState().setValue(PART, part);
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(PART, LIGHT);
    }

    @Override public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        if (state.getValue(PART) == CENTER && context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof ItemEntity) return ITEM_CENTER_SHAPE;
        return Shapes.block();
    }

    @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || state.getValue(PART) != CENTER
                || !(entity instanceof ItemEntity item)
                || !(level.getBlockEntity(pos) instanceof AdvancedAlchemicalFurnaceBlockEntity furnace)
                || !furnace.process(item.getItem())) return;
        ItemStackAccess.consumeOne(item);
        level.playSound(null, pos, ModSounds.BUBBLE.get(), SoundSource.BLOCKS,
                0.2F, 1.0F + level.random.nextFloat() * 0.4F);
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PART) != CENTER
                || !(level.getBlockEntity(pos) instanceof AdvancedAlchemicalFurnaceBlockEntity furnace)
                || !furnace.isProcessing()) return;
        float red = 0.6F - random.nextFloat() * 0.2F;
        float blue = 0.6F + random.nextFloat() * 0.2F;
        level.addParticle(ModParticles.CRUCIBLE_BUBBLE.get(),
                pos.getX() + random.nextFloat(), pos.getY() + 1.0,
                pos.getZ() + random.nextFloat(), red, 0, blue);
        if (random.nextInt(50) == 0) level.playLocalSound(pos.getX() + random.nextFloat(),
                pos.getY() + 1.0, pos.getZ() + random.nextFloat(), SoundEvents.LAVA_POP,
                SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F,
                0.9F + random.nextFloat() * 0.15F, false);
        int x = random.nextInt(2);
        int z = random.nextInt(2);
        level.addParticle(ModParticles.CRUCIBLE_BUBBLE.get(),
                pos.getX() - 0.6 + random.nextFloat() * 0.2 + x * 2.0,
                pos.getY() + 2.0,
                pos.getZ() - 0.6 + random.nextFloat() * 0.2 + z * 2.0,
                red, 0, blue);
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof AdvancedAlchemicalFurnaceBlockEntity furnace
                && furnace.isNozzle() && furnace.essentiaAmount() > 0 ? 1 : 0;
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server)
            CraftingStructureDisassembly.partRemoved(server, pos, state);
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        int part = state.getValue(PART);
        return part == CENTER || part == LOWER_NOZZLE
                ? new AdvancedAlchemicalFurnaceBlockEntity(pos, state) : null;
    }

    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type, ModBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(),
                        AdvancedAlchemicalFurnaceBlockEntity::serverTick) : null;
    }

    /** Keeps the collision handler readable while preserving the surviving stack entity. */
    private static final class ItemStackAccess {
        private static void consumeOne(ItemEntity item) {
            item.getItem().shrink(1);
            if (item.getItem().isEmpty()) item.discard();
            else item.setItem(item.getItem());
        }
    }
}
