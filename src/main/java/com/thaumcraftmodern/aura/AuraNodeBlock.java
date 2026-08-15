package com.thaumcraftmodern.aura;

import com.thaumcraftmodern.item.EtherealEssenceItem;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.registry.ModParticles;
import com.thaumcraftmodern.wand.WandInteractable;
import com.thaumcraftmodern.world.block.EldritchAltarPartBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Invisible logical block with a selectable hit shape. A future provenance-
 * checked BER draws the classic node and applies {@link NodeVisibilityService}.
 */
public class AuraNodeBlock extends BaseEntityBlock
        implements WandInteractable {
    /*
     * TC4 used a 0.3..0.7 selection cube. At modern mouse sensitivities that
     * narrow target makes a held drain session drop on tiny camera movements,
     * because the server validates the same ray every transfer interval.
     * Keep the node non-colliding, but give interaction/selection a forgiving
     * three-quarter-block target.
     */
    private static final VoxelShape HIT_SHAPE =
            box(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D);
    private final Supplier<? extends BlockEntityType<?>> blockEntityType;
    private final boolean embedded;

    public AuraNodeBlock(
            Properties properties,
            Supplier<? extends BlockEntityType<?>> blockEntityType
    ) {
        this(properties, blockEntityType, false);
    }

    public AuraNodeBlock(
            Properties properties,
            Supplier<? extends BlockEntityType<?>> blockEntityType,
            boolean embedded
    ) {
        super(properties);
        this.blockEntityType = Objects.requireNonNull(
                blockEntityType,
                "blockEntityType"
        );
        this.embedded = embedded;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return embedded ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return embedded ? Shapes.block() : HIT_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return embedded ? Shapes.block() : Shapes.empty();
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
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
        return onWandRightClick(
                state,
                level,
                position,
                player,
                hand,
                hit
        );
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
        return NodeChargingService.begin(level, position, player, hand);
    }

    @Override
    public void playerWillDestroy(
            Level level,
            BlockPos position,
            BlockState state,
            Player player
    ) {
        if (!level.isClientSide) {
            level.playSound(
                    null,
                    position,
                    ModSounds.CRAFT_FAIL.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ModParticles.NODE_BURST.get(),
                        position.getX() + 0.5D,
                        position.getY() + 0.5D,
                        position.getZ() + 0.5D,
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
        super.playerWillDestroy(level, position, state, player);
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!level.isClientSide
                && !state.is(newState.getBlock())
                && !newState.is(ModBlocks.OUTER_LANDS_PORTAL.get())) {
            EldritchAltarPartBlock.destroyFromAuraNode(level, position);
        }
        super.onRemove(state, level, position, newState, movedByPiston);
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos position,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool
    ) {
        if (!level.isClientSide
                && !player.getAbilities().instabuild
                && blockEntity instanceof AuraNodeBlockEntity node) {
            for (String aspect : AuraNodeBreakDrops.aspectIdsForDrops(
                    node.snapshotState()
            )) {
                popResource(
                        level,
                        position,
                        EtherealEssenceItem.create(
                                ModItems.ETHEREAL_ESSENCE.get(),
                                aspect,
                                AuraNodeBreakDrops.ESSENCE_ASPECT_AMOUNT
                        )
                );
            }
        }
        super.playerDestroy(level, player, position, state, blockEntity, tool);
        createFluxOnDestruction(level, position);
    }

    @Override
    public void wasExploded(
            Level level,
            BlockPos position,
            Explosion explosion
    ) {
        super.wasExploded(level, position, explosion);
        createFluxOnDestruction(level, position);
    }

    private static void createFluxOnDestruction(
            Level level,
            BlockPos position
    ) {
        if (!level.isClientSide && level.getBlockState(position).isAir()) {
            level.setBlock(
                    position,
                    com.thaumcraftmodern.registry.ModBlocks.FLUX_GOO
                            .get()
                            .defaultBlockState(),
                    3
            );
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new AuraNodeBlockEntity(blockEntityType.get(), position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide || type != blockEntityType.get()) {
            return null;
        }
        return (tickerLevel, position, tickerState, blockEntity) ->
                AuraNodeBlockEntity.serverTick(
                        (ServerLevel) tickerLevel,
                        position,
                        tickerState,
                        (AuraNodeBlockEntity) blockEntity
                );
    }
}
