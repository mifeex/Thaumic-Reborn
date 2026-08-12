package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.PrimalNodeTransformation;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.FluxGasBlock;
import com.thaumcraftmodern.world.block.FluxGooBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PrimordialPearlItem extends Item {
    public PrimordialPearlItem(Properties properties) {
        super(properties);
    }

    /**
     * The pearl is a catalyst in ordinary and arcane crafting. TC4's
     * Advanced Alchemical Furnace page explicitly says it is not consumed.
     * Infusion deliberately bypasses this crafting remainder.
     */
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remainder = stack.copy();
        remainder.setCount(1);
        return remainder;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.ItemEldritchObject.text.5")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.ItemEldritchObject.text.6")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos position = context.getClickedPos();
        if (!(level.getBlockEntity(position) instanceof AuraNodeBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)
                || !(context.getPlayer() instanceof ServerPlayer player)
                || !(serverLevel.getBlockEntity(position)
                instanceof AuraNodeBlockEntity node)) {
            return InteractionResult.PASS;
        }

        boolean researched = KnowledgeAccess.get(player)
                .map(knowledge -> knowledge.hasCompletedResearch("primnode"))
                .orElse(false);
        PrimalNodeTransformation.Result result =
                PrimalNodeTransformation.transform(
                        node.snapshotState().snapshot(),
                        serverLevel.random,
                        researched
                );
        if (!node.applyPrimalTransformation(result)) {
            return InteractionResult.FAIL;
        }

        context.getItemInHand().shrink(1);
        player.swing(context.getHand(), true);
        serverLevel.explode(
                null,
                position.getX() + 0.5D,
                position.getY() + 1.5D,
                position.getZ() + 0.5D,
                result.explosionRadius(),
                true,
                Level.ExplosionInteraction.BLOCK
        );
        scatterFlux(serverLevel, position);
        return InteractionResult.CONSUME;
    }

    private static void scatterFlux(ServerLevel level, BlockPos origin) {
        for (int index = 0;
                index < PrimalNodeTransformation.FLUX_ATTEMPTS;
                index++) {
            BlockPos target = origin.offset(
                    level.random.nextInt(6) - level.random.nextInt(6),
                    level.random.nextInt(6) - level.random.nextInt(6),
                    level.random.nextInt(6) - level.random.nextInt(6)
            );
            if (!level.isEmptyBlock(target)) {
                continue;
            }
            BlockState flux = target.getY() < origin.getY()
                    ? ModBlocks.FLUX_GOO.get().defaultBlockState()
                            .setValue(FluxGooBlock.LEVEL, 7)
                    : ModBlocks.FLUX_GAS.get().defaultBlockState()
                            .setValue(FluxGasBlock.LEVEL, 7);
            level.setBlock(target, flux, 3);
        }
    }
}
