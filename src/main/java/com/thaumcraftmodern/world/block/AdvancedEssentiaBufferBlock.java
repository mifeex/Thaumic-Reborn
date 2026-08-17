package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.api.wand.WandApi;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.entity.AdvancedEssentiaBufferBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class AdvancedEssentiaBufferBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = box(4, 4, 4, 12, 12, 12);

    public AdvancedEssentiaBufferBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos)
                instanceof AdvancedEssentiaBufferBlockEntity buffer)) {
            return InteractionResult.PASS;
        }
        if (WandApi.state(player.getItemInHand(hand)).isPresent()) {
            if (!level.isClientSide) {
                buffer.cycleRole(hit.getDirection());
                player.displayClientMessage(Component.translatable(
                        "message.thaumic_reborn.advanced_buffer.side",
                        Component.translatable("direction.minecraft."
                                + hit.getDirection().getName()),
                        Component.translatable(
                                "message.thaumic_reborn.advanced_buffer.role."
                                        + buffer.role(hit.getDirection()).name()
                                                .toLowerCase())), true);
                level.playSound(null, pos, ModSounds.TOOL.get(),
                        SoundSource.BLOCKS, 0.5F,
                        0.9F + level.random.nextFloat() * 0.2F);
            }
            player.swing(hand, true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player.getItemInHand(hand).isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(
                        "message.thaumic_reborn.advanced_buffer.status",
                        Component.translatable(
                                "state.thaumic_reborn.advanced_buffer."
                                        + buffer.flowState().name().toLowerCase()),
                        buffer.totalAmount(),
                        Component.translatable(buffer.diagnosticReasonKey())),
                        false);
                if (buffer.flowState()
                        == com.thaumcraftmodern.essentia
                                .AdvancedBufferFlowController.State.BLOCKED) {
                    player.displayClientMessage(Component.translatable(
                            "message.thaumic_reborn.advanced_buffer.warning"),
                            false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedEssentiaBufferBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type,
                        ModBlockEntities.ADVANCED_ESSENTIA_BUFFER.get(),
                        AdvancedEssentiaBufferBlockEntity::serverTick)
                : null;
    }
}
