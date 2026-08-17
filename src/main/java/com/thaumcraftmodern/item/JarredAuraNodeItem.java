package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aura.OperationNonceGuard;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.client.render.JarredAuraNodeItemClientExtensions;
import com.thaumcraftmodern.nodejar.NodeJarCodec;
import com.thaumcraftmodern.nodejar.NodeJarData;
import com.thaumcraftmodern.nodejar.NodeJarKeys;
import com.thaumcraftmodern.nodejar.NodeJarPlacementService;
import com.thaumcraftmodern.nodejar.NodeJarSavedData;
import com.thaumcraftmodern.nodejar.ServerNodeJarWorld;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.ArcanePedestalBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * One placement path for captured and deterministic creative node jars.
 */
public final class JarredAuraNodeItem extends BlockItem {
    private static final double MAXIMUM_DISTANCE = 6.0D;
    private static final NodeJarPlacementService PLACEMENT =
            new NodeJarPlacementService(new OperationNonceGuard());

    public JarredAuraNodeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(
            Consumer<IClientItemExtensions> consumer
    ) {
        consumer.accept(JarredAuraNodeItemClientExtensions.create());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        NodeJarData data;
        try {
            data = NodeJarCodec.read(stack).orElse(null);
        } catch (RuntimeException exception) {
            return InteractionResult.FAIL;
        }
        if (data == null) {
            return InteractionResult.FAIL;
        }

        BlockPos clickedPosition = context.getClickedPos();
        Player playerUsingItem = context.getPlayer();
        if (playerUsingItem != null
                && context.getLevel().getBlockState(clickedPosition).getBlock()
                instanceof ArcanePedestalBlock) {
            InteractionResult pedestalResult =
                    ArcanePedestalBlock.placeHeldItem(
                            context.getLevel(),
                            clickedPosition,
                            playerUsingItem,
                            context.getHand());
            if (pedestalResult.consumesAction()) {
                return pedestalResult;
            }
        }

        BlockPlaceContext placementContext = new BlockPlaceContext(context);
        BlockPos position = placementContext.getClickedPos();
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)
                || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.FAIL;
        }

        CompoundTag expectedTag = stack.getTag() == null
                ? null
                : stack.getTag().copy();
        int expectedCount = stack.getCount();
        NodeJarPlacementService.JarStackReservation reservation =
                new NodeJarPlacementService.JarStackReservation() {
                    @Override
                    public NodeJarData data() {
                        return data;
                    }

                    @Override
                    public boolean stillMatchesHeldStack() {
                        ItemStack current = player.getItemInHand(context.getHand());
                        return current == stack
                                && current.getCount() == expectedCount
                                && java.util.Objects.equals(
                                        expectedTag,
                                        current.getTag()
                                );
                    }

                    @Override
                    public boolean consumeOne() {
                        if (!stillMatchesHeldStack()) {
                            return false;
                        }
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        player.getInventory().setChanged();
                        return true;
                    }
                };

        UUID operationId = UUID.nameUUIDFromBytes(
                ("nodejar-place:"
                        + player.getUUID() + ":"
                        + level.getGameTime() + ":"
                        + context.getHand() + ":"
                        + position.asLong())
                        .getBytes(StandardCharsets.UTF_8)
        );
        NodeJarPlacementService.Request request =
                new NodeJarPlacementService.Request(
                        player.getUUID(),
                        operationId,
                        position,
                        NodeJarKeys.placement(level, position),
                        true,
                        level.hasChunkAt(position),
                        level.getBlockState(position)
                                .canBeReplaced(placementContext),
                        Math.sqrt(player.distanceToSqr(
                                position.getX() + 0.5D,
                                position.getY() + 0.5D,
                                position.getZ() + 0.5D
                        )),
                        MAXIMUM_DISTANCE
                );
        NodeJarPlacementService.Status status = PLACEMENT.place(
                request,
                reservation,
                NodeJarSavedData.get(level),
                new ServerNodeJarWorld(level, getBlock().defaultBlockState())
        );
        if (status != NodeJarPlacementService.Status.PLACED) {
            return InteractionResult.FAIL;
        }

        level.playSound(
                null,
                position,
                ModSounds.JAR.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
        level.gameEvent(player, GameEvent.BLOCK_PLACE, position);
        player.swing(context.getHand(), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        NodeJarData data;
        try {
            data = NodeJarCodec.read(stack).orElse(null);
        } catch (RuntimeException exception) {
            tooltip.add(Component.translatable(
                            "tooltip.thaumic_reborn.node_jar.invalid"
                    )
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (data == null) {
            return;
        }
        if (data.origin() == NodeJarData.Origin.CREATIVE_TEMPLATE) {
            tooltip.add(Component.translatable(
                            "tooltip.thaumic_reborn.node_jar.creative_ready"
                    )
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        tooltip.add(Component.translatable(
                        "tooltip.thaumic_reborn.node_jar.kind",
                        Component.translatable(
                                "node_type.thaumic_reborn."
                                        + data.node().type().name().toLowerCase(
                                        java.util.Locale.ROOT
                                )
                        )
                )
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable(
                        "tooltip.thaumic_reborn.node_jar.modifier",
                        Component.translatable(
                                "node_modifier.thaumic_reborn."
                                        + data.node().modifier().name()
                                        .toLowerCase(java.util.Locale.ROOT)
                        )
                )
                .withStyle(ChatFormatting.DARK_PURPLE));
        var snapshot = data.node().snapshot();
        for (var entry : snapshot.aspectsCurrent().entrySet()) {
            tooltip.add(Component.translatable(
                            "tooltip.thaumic_reborn.node_jar.vis",
                            Component.translatable(
                                    "aspect.thaumic_reborn." + entry.getKey()
                            ),
                            entry.getValue(),
                            snapshot.aspectsMaximum().get(entry.getKey())
                    )
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
