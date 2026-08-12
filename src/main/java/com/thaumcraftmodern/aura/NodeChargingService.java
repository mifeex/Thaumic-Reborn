package com.thaumcraftmodern.aura;

import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Server-owned classic hold-to-drain interaction for aura nodes.
 */
public final class NodeChargingService {
    public static final int TRANSFER_INTERVAL_TICKS = 5;
    public static final double MAXIMUM_DISTANCE = 6.0D;

    /*
     * SUCCESS has shouldSwing=true and adds the vanilla interaction swing on
     * top of the TC4 drain pose rendered by ClassicWandItemRenderer.
     */
    static final InteractionResult CLIENT_HOLD_RESULT =
            InteractionResult.CONSUME;

    private static final String SESSION_KEY = "ThaumcraftModernNodeCharge";
    private static final String DIMENSION_KEY = "dimension";
    private static final String POSITION_KEY = "position";
    private static final String NODE_ID_KEY = "node_id";
    private static final NodeVisTransferService TRANSFER =
            new NodeVisTransferService(new OperationNonceGuard());

    private NodeChargingService() {
    }

    public static InteractionResult begin(
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand
    ) {
        ItemStack held = player.getItemInHand(hand);
        if (!WandVisService.isWand(held)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            player.startUsingItem(hand);
            return CLIENT_HOLD_RESULT;
        }
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(position) instanceof AuraNodeBlockEntity node)
                || WandVisService.state(held).isEmpty()) {
            return InteractionResult.FAIL;
        }

        CompoundTag session = new CompoundTag();
        session.putString(DIMENSION_KEY, level.dimension().location().toString());
        session.putLong(POSITION_KEY, position.asLong());
        session.putUUID(NODE_ID_KEY, node.scanIdentity().nodeId());
        held.getOrCreateTag().put(SESSION_KEY, session);
        serverPlayer.getInventory().setChanged();
        serverPlayer.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    public static void tick(
            Level level,
            LivingEntity entity,
            ItemStack stack,
            int remainingUseDuration
    ) {
        if (level.isClientSide
                || !(level instanceof ServerLevel serverLevel)
                || !(entity instanceof ServerPlayer player)
                || remainingUseDuration % TRANSFER_INTERVAL_TICKS != 0) {
            return;
        }
        Session session = readSession(stack);
        if (session == null
                || !session.dimension().equals(serverLevel.dimension().location().toString())
                || !serverLevel.hasChunkAt(session.position())
                || !(serverLevel.getBlockEntity(session.position())
                instanceof AuraNodeBlockEntity node)
                || !node.scanIdentity().nodeId().equals(session.nodeId())
                || !isStillLookingAt(player, session.position())) {
            stop(player, stack);
            return;
        }

        HeldWandVisStore wand;
        try {
            wand = new HeldWandVisStore(player, player.getUsedItemHand());
        } catch (RuntimeException exception) {
            stop(player, stack);
            return;
        }
        AuraNodeState nodeState = node.snapshotState();
        WandVisStore.Snapshot wandState = wand.snapshot();
        boolean preserveLast = preservesLastVis(player, stack);
        List<PrimalAspect> candidates = new ArrayList<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int available = nodeState.current(aspect) - (preserveLast ? 1 : 0);
            int room = wandState.capacity().get(aspect)
                    - wandState.current().get(aspect);
            if (available > 0 && hasChargeRoom(room)) {
                candidates.add(aspect);
            }
        }
        if (candidates.isEmpty()) {
            node.clearDrain(player.getId());
            return;
        }

        PrimalAspect selected = candidates.get(
                serverLevel.getRandom().nextInt(candidates.size())
        );
        int rate = drainRate(
                hasResearch(player, "nodetapper1"),
                hasResearch(player, "nodetapper2")
        );
        UUID operationId = UUID.nameUUIDFromBytes(
                ("node-drain:"
                        + player.getUUID() + ":"
                        + serverLevel.getGameTime() + ":"
                        + session.position().asLong() + ":"
                        + selected.id())
                        .getBytes(StandardCharsets.UTF_8)
        );
        NodeVisTransferService.Request request =
                new NodeVisTransferService.Request(
                        player.getUUID(),
                        operationId,
                        session.nodeId(),
                        true,
                        true,
                        serverLevel.hasChunkAt(session.position()),
                        Math.sqrt(player.distanceToSqr(
                                session.position().getX() + 0.5D,
                                session.position().getY() + 0.5D,
                                session.position().getZ() + 0.5D
                        )),
                        MAXIMUM_DISTANCE
                );
        NodeVisTransferService.Result result = node.transferToWand(
                TRANSFER,
                request,
                wand,
                selected,
                rate,
                preserveLast
        );
        if (result.status() == NodeVisTransferService.Status.TRANSFERRED) {
            node.markDrain(player.getId(), selected, serverLevel.getGameTime());
        } else {
            node.clearDrain(player.getId());
        }
    }

    public static void stop(LivingEntity entity, ItemStack stack) {
        clear(entity, stack);
        if (entity instanceof ServerPlayer player && player.isUsingItem()) {
            player.stopUsingItem();
        }
    }

    public static void clear(LivingEntity entity, ItemStack stack) {
        Session session = readSession(stack);
        if (session != null
                && entity.level() instanceof ServerLevel level
                && session.dimension().equals(level.dimension().location().toString())
                && level.hasChunkAt(session.position())
                && level.getBlockEntity(session.position())
                instanceof AuraNodeBlockEntity node
                && node.scanIdentity().nodeId().equals(session.nodeId())) {
            node.clearDrain(entity.getId());
        }
        CompoundTag owner = stack.getTag();
        if (owner != null) {
            owner.remove(SESSION_KEY);
            if (owner.isEmpty()) {
                stack.setTag(null);
            }
        }
        if (entity instanceof ServerPlayer player) {
            player.getInventory().setChanged();
        }
    }

    private static boolean isStillLookingAt(
            ServerPlayer player,
            BlockPos expected
    ) {
        HitResult hit = player.pick(MAXIMUM_DISTANCE, 0.0F, false);
        return hit instanceof BlockHitResult blockHit
                && blockHit.getBlockPos().equals(expected);
    }

    private static boolean preservesLastVis(
            ServerPlayer player,
            ItemStack stack
    ) {
        if (player.isShiftKeyDown() || !hasResearch(player, "nodepreserve")) {
            return false;
        }
        WandState wand = WandVisService.state(stack).orElse(null);
        return wand != null
                && !"wood".equals(wand.rodId())
                && !"iron".equals(wand.capId());
    }

    private static boolean hasResearch(ServerPlayer player, String id) {
        return KnowledgeAccess.get(player)
                .map(knowledge -> knowledge.hasCompletedResearch(id))
                .orElse(false);
    }

    static int drainRate(boolean hasNodeTapper1, boolean hasNodeTapper2) {
        if (hasNodeTapper2) {
            return 3;
        }
        return hasNodeTapper1 ? 2 : 1;
    }

    static boolean hasChargeRoom(int roomCentivis) {
        return roomCentivis > 0;
    }

    private static Session readSession(ItemStack stack) {
        CompoundTag owner = stack.getTag();
        if (owner == null || !owner.contains(SESSION_KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag tag = owner.getCompound(SESSION_KEY);
        if (!tag.hasUUID(NODE_ID_KEY)
                || !tag.contains(POSITION_KEY, Tag.TAG_LONG)
                || !tag.contains(DIMENSION_KEY, Tag.TAG_STRING)) {
            return null;
        }
        return new Session(
                tag.getString(DIMENSION_KEY),
                BlockPos.of(tag.getLong(POSITION_KEY)),
                tag.getUUID(NODE_ID_KEY)
        );
    }

    private record Session(String dimension, BlockPos position, UUID nodeId) {
    }
}
