package com.thaumcraftmodern.item;

import com.thaumcraftmodern.construction.ConstructionDefinition;
import com.thaumcraftmodern.construction.ConstructionRegistry;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.ResearchTableBlock;
import com.thaumcraftmodern.world.block.ResearchTablePart;
import com.thaumcraftmodern.world.block.entity.ResearchTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ScribingToolsItem extends Item {
    private static final Direction[] TABLE_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    public ScribingToolsItem(Properties properties) {
        super(properties);
    }

    public static boolean hasInk(ItemStack stack) {
        return stack.getItem() instanceof ScribingToolsItem
                && stack.getDamageValue() < stack.getMaxDamage();
    }

    public static void consumeInk(ItemStack stack) {
        if (!hasInk(stack)) {
            return;
        }
        stack.setDamageValue(ScribingToolsInk.nextDamage(
                stack.getDamageValue(),
                stack.getMaxDamage()
        ));
    }

    /**
     * TC4 ItemInkwell.onItemUseFirst: two adjacent plain tables become one
     * two-block Research Table and the complete scribing-tools stack is moved
     * into the table's first inventory slot. This construction consumes no
     * vis and does not require research.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos mainPosition = context.getClickedPos();
        if (!level.getBlockState(mainPosition).is(ModBlocks.THAUMCRAFT_TABLE.get())) {
            return InteractionResult.PASS;
        }

        Direction companionDirection = findCompanionDirection(level, mainPosition);
        if (companionDirection == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ConstructionDefinition definition = ConstructionRegistry.find(
                ConstructionDefinition.Handler.RESEARCH_TABLE_PAIR
        ).orElse(null);
        if (definition == null
                || !definition.matchesItem(context.getItemInHand())
                || definition.trigger().consume() != 1) {
            return InteractionResult.PASS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }

        BlockPos companionPosition = mainPosition.relative(companionDirection);
        BlockState originalMain = level.getBlockState(mainPosition);
        BlockState originalCompanion = level.getBlockState(companionPosition);
        BlockState mainState = ModBlocks.RESEARCH_TABLE.get()
                .defaultBlockState()
                .setValue(ResearchTableBlock.FACING, companionDirection)
                .setValue(ResearchTableBlock.PART, ResearchTablePart.MAIN);
        BlockState companionState = mainState.setValue(
                ResearchTableBlock.PART,
                ResearchTablePart.COMPANION
        );

        if (!level.setBlock(mainPosition, mainState, 3)) {
            return InteractionResult.FAIL;
        }
        if (!level.setBlock(companionPosition, companionState, 3)) {
            level.setBlock(mainPosition, originalMain, 3);
            return InteractionResult.FAIL;
        }
        if (!(level.getBlockEntity(mainPosition)
                instanceof ResearchTableBlockEntity table)) {
            level.setBlock(companionPosition, originalCompanion, 3);
            level.setBlock(mainPosition, originalMain, 3);
            return InteractionResult.FAIL;
        }

        ItemStack installedTools = context.getItemInHand().copy();
        installedTools.setCount(1);
        table.items().setStackInSlot(
                ResearchTableBlockEntity.SCRIBING_TOOLS_SLOT,
                installedTools
        );
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
            player.getInventory().setChanged();
        }
        level.playSound(
                null,
                mainPosition,
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.BLOCKS,
                0.7F,
                0.9F
        );
        return InteractionResult.CONSUME;
    }

    private static @Nullable Direction findCompanionDirection(
            Level level,
            BlockPos mainPosition
    ) {
        for (Direction direction : TABLE_DIRECTIONS) {
            BlockPos candidate = mainPosition.relative(direction);
            if (level.hasChunkAt(candidate)
                    && level.getBlockState(candidate)
                    .is(ModBlocks.THAUMCRAFT_TABLE.get())) {
                return direction;
            }
        }
        return null;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable(
                "tooltip.thaumcraftmodern.scribing_tools.ink",
                Math.max(0, stack.getMaxDamage() - stack.getDamageValue())
        ));
    }
}
