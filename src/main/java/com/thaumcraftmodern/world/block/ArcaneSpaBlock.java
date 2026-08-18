package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.ArcaneSpaBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/** TC4 Arcane Spa: a five-bucket fluid tank and 5x5 surface dispenser. */
public final class ArcaneSpaBlock extends BaseEntityBlock {
    public ArcaneSpaBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()
                || !(level.getBlockEntity(pos) instanceof ArcaneSpaBlockEntity spa)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean holdsFluid = FluidUtil.getFluidContained(held).isPresent();
        if (holdsFluid) {
            if (!level.isClientSide
                    && FluidUtil.interactWithFluidHandler(player, hand, spa.fluidTank())) {
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM,
                        SoundSource.BLOCKS, 0.33F,
                        1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F);
            }
            // TC4 consumes the interaction even when the tank is full or incompatible.
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide && player instanceof ServerPlayer server) {
            NetworkHooks.openScreen(server, spa, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof ArcaneSpaBlockEntity spa) {
            Containers.dropContents(level, pos, spa);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneSpaBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type, ModBlockEntities.ARCANE_SPA.get(),
                        ArcaneSpaBlockEntity::serverTick)
                : null;
    }
}
