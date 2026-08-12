package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.wand.WandInteractable;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.block.entity.CrucibleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Classic TC4 Crucible water container.
 *
 * <p>The tank accepts water only and has a capacity of exactly 1000 mB.
 * A water bucket fills any remaining capacity; water bottles add one of the
 * three vanilla-cauldron portions.</p>
 */
public final class CrucibleBlock extends Block
        implements EntityBlock, WandInteractable {
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");
    static final int SCALD_INTERVAL_TICKS = 10;
    static final float SCALD_DAMAGE = 1.0F;

    private static final VoxelShape COLLISION_SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 2.0D, 13.6D, 16.0D),
            Block.box(14.0D, 0.0D, 0.0D, 16.0D, 13.6D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.6D, 2.0D),
            Block.box(0.0D, 0.0D, 14.0D, 16.0D, 13.6D, 16.0D)
    );

    public CrucibleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FILLED, false));
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return COLLISION_SHAPE;
    }

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos position,
            Entity entity
    ) {
        if (level.isClientSide
                || !(entity instanceof LivingEntity living)
                || living.tickCount % SCALD_INTERVAL_TICKS != 0
                || !(level.getBlockEntity(position)
                        instanceof CrucibleBlockEntity crucible)
                || !crucible.canProcessItems()) {
            return;
        }
        living.hurt(level.damageSources().generic(), SCALD_DAMAGE);
        level.playSound(
                null,
                position,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.4F,
                2.0F + level.random.nextFloat() * 0.4F
        );
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
        BlockHitResult hit
    ) {
        ItemStack held = player.getItemInHand(hand);
        if (WandVisService.isWand(held)) {
            InteractionResult wandResult = onWandRightClick(
                    state,
                    level,
                    position,
                    player,
                    hand,
                    hit
            );
            if (wandResult != InteractionResult.PASS) {
                return wandResult;
            }
        }
        boolean waterBucket = held.is(Items.WATER_BUCKET);
        boolean waterBottle = held.is(Items.POTION)
                && PotionUtils.getPotion(held) == Potions.WATER;
        if (!waterBucket && !waterBottle) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(position)
                instanceof CrucibleBlockEntity crucible)
                || crucible.water()
                >= CrucibleBlockEntity.FLUID_CAPACITY_MB) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            boolean filled = waterBucket
                    ? crucible.fillWater()
                    : crucible.fillWaterBottle();
            if (!filled) {
                return InteractionResult.CONSUME;
            }
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(held.getItem()));
            player.setItemInHand(
                    hand,
                    ItemUtils.createFilledResult(
                            held,
                            player,
                            new ItemStack(
                                    waterBucket
                                            ? Items.BUCKET
                                            : Items.GLASS_BOTTLE
                            )
                    )
            );
            level.playSound(
                    null,
                    position,
                    waterBucket
                            ? SoundEvents.BUCKET_EMPTY
                            : SoundEvents.BOTTLE_EMPTY,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
            level.gameEvent(player, GameEvent.FLUID_PLACE, position);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult onWandRightClick(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide
                && level.getBlockEntity(position)
                instanceof CrucibleBlockEntity crucible) {
            crucible.spillRemnants((ServerLevel) level);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new CrucibleBlockEntity(position, state);
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide
                && level.getBlockEntity(position)
                instanceof CrucibleBlockEntity crucible) {
            crucible.spillRemnantsOnRemoval((ServerLevel) level);
            level.removeBlockEntity(position);
        }
        super.onRemove(
                state,
                level,
                position,
                newState,
                movedByPiston
        );
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (type != ModBlockEntities.CRUCIBLE.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (clientLevel, position, blockState, blockEntity) ->
                    CrucibleBlockEntity.clientTick(
                            clientLevel,
                            position,
                            blockState,
                            (CrucibleBlockEntity) blockEntity
                    );
        }
        return (serverLevel, position, blockState, blockEntity) ->
                CrucibleBlockEntity.serverTick(
                        (ServerLevel) serverLevel,
                        position,
                        blockState,
                        (CrucibleBlockEntity) blockEntity
                );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FILLED);
    }
}
