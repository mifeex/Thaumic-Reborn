package com.thaumcraftmodern.scan;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeScanIdentity;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.item.ThaumometerItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.visnet.EnergizedAuraNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ScanSessionManager {
    public static final int REQUIRED_TICKS = 20;
    public static final double MAX_DISTANCE = 10.0D;
    public static final double MAX_DISTANCE_SQUARED = 100.0D;
    private static final Map<UUID, ScanSession> SESSIONS = new ConcurrentHashMap<>();

    private ScanSessionManager() {
    }

    public static void startBlock(ServerPlayer player, InteractionHand hand, BlockPos position) {
        if (player.level().getBlockEntity(position) instanceof AuraNodeBlockEntity node) {
            AuraNodeScanIdentity identity = node.scanIdentity();
            start(player, new ScanSession(
                    hand,
                    new NodeTarget(
                            player.level().dimension(),
                            position.immutable(),
                            identity.nodeId()
                    )
            ));
            return;
        }
        if (player.level().getBlockEntity(position)
                instanceof EnergizedAuraNodeBlockEntity node) {
            start(player, new ScanSession(
                    hand,
                    new NodeTarget(
                            player.level().dimension(),
                            position.immutable(),
                            node.originalState().nodeId()
                    )
            ));
            return;
        }
        String id = ScanRegistry.canonicalBlockId(BuiltInRegistries.BLOCK
                .getKey(player.level().getBlockState(position).getBlock()).toString());
        start(player, new ScanSession(
                hand,
                new BlockTarget(player.level().dimension(), position.immutable(), id)
        ));
    }

    public static void startEntity(ServerPlayer player, InteractionHand hand, Entity target) {
        String id = EntityScanIdentity.targetId(target);
        start(player, new ScanSession(
                hand,
                new EntityTarget(player.level().dimension(), target.getId(), id)
        ));
    }

    public static void startItem(
            ServerPlayer player,
            InteractionHand thaumometerHand,
            InteractionHand targetHand,
            ItemStack target
    ) {
        ScanRegistry.ItemScanIdentity identity = ScanRegistry.identityForItem(target);
        start(player, new ScanSession(
                thaumometerHand,
                new ItemTarget(
                        player.level().dimension(),
                        targetHand,
                        identity.type(),
                        identity.targetId(),
                        identity.knowledgeKey()
                )
        ));
    }

    public static void startDroppedItem(
            ServerPlayer player,
            InteractionHand thaumometerHand,
            ItemEntity target
    ) {
        ScanRegistry.ItemScanIdentity identity = ScanRegistry.identityForItem(target.getItem());
        start(player, new ScanSession(
                thaumometerHand,
                new DroppedItemTarget(
                        player.level().dimension(),
                        target.getId(),
                        identity.type(),
                        identity.targetId(),
                        identity.knowledgeKey()
                )
        ));
    }

    private static void start(ServerPlayer player, ScanSession session) {
        boolean alreadyStudied = KnowledgeAccess.get(player)
                .map(knowledge -> knowledge.hasScan(
                        session.target.knowledgeKey()))
                .orElse(false);
        if (alreadyStudied) {
            SESSIONS.remove(player.getUUID());
            player.stopUsingItem();
            return;
        }
        SESSIONS.put(player.getUUID(), session);
    }

    public static void tick(ServerPlayer player, ItemStack usingStack) {
        ScanSession session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }

        TargetFacts facts = collectTargetFacts(session.target, player);
        int candidateElapsedTicks = session.elapsedTicks + 1;
        ItemStack heldStack = player.getItemInHand(session.thaumometerHand);
        boolean holdingThaumometer = player.getUsedItemHand() == session.thaumometerHand
                && heldStack.getItem() instanceof ThaumometerItem
                && usingStack.getItem() instanceof ThaumometerItem;
        ScanValidation.Request request = new ScanValidation.Request(
                holdingThaumometer,
                player.level().dimension().location().toString(),
                session.target.dimension().location().toString(),
                facts.targetChunkLoaded(),
                facts.distance(),
                MAX_DISTANCE,
                facts.lineOfSight(),
                ScanRegistry.find(session.target.type(), session.target.targetId()).isPresent(),
                facts.stableTarget() ? candidateElapsedTicks : 0,
                REQUIRED_TICKS
        );
        ScanValidation.Reason reason = ScanValidation.validate(request);
        boolean expectedResult = candidateElapsedTicks < REQUIRED_TICKS
                ? reason == ScanValidation.Reason.STABLE_DURATION_NOT_REACHED
                : reason == ScanValidation.Reason.VALID;
        boolean completedWithoutDefinition = candidateElapsedTicks >= REQUIRED_TICKS
                && reason == ScanValidation.Reason.TARGET_NOT_REGISTERED;
        if (!facts.stableTarget() || (!expectedResult && !completedWithoutDefinition)) {
            String key = !facts.stableTarget()
                    && reason == ScanValidation.Reason.STABLE_DURATION_NOT_REACHED
                    ? "message.thaumcraftmodern.scan.error.invalid_target"
                    : "message.thaumcraftmodern.scan.error."
                    + reason.name().toLowerCase(Locale.ROOT);
            interrupt(player, key);
            return;
        }

        session.elapsedTicks = candidateElapsedTicks;
        if (reason == ScanValidation.Reason.VALID || completedWithoutDefinition) {
            SESSIONS.remove(player.getUUID());
            player.stopUsingItem();
            ScanService.complete(player, session.target);
        }
    }

    public static void cancel(UUID playerId) {
        SESSIONS.remove(playerId);
    }

    private static void interrupt(ServerPlayer player, String messageKey) {
        if (SESSIONS.remove(player.getUUID()) != null) {
            player.stopUsingItem();
            ScanService.sendFailure(player, messageKey, "");
        }
    }

    private static TargetFacts collectTargetFacts(ScanTarget target, ServerPlayer player) {
        if (target instanceof NodeTarget nodeTarget) {
            return collectNodeFacts(nodeTarget, player);
        }
        if (target instanceof BlockTarget blockTarget) {
            return collectBlockFacts(blockTarget, player);
        }
        if (target instanceof EntityTarget entityTarget) {
            return collectEntityFacts(entityTarget, player);
        }
        if (target instanceof ItemTarget itemTarget) {
            return collectItemFacts(itemTarget, player);
        }
        if (target instanceof DroppedItemTarget droppedItemTarget) {
            return collectDroppedItemFacts(droppedItemTarget, player);
        }
        throw new IllegalStateException("Unsupported scan target: " + target.getClass().getName());
    }

    private static TargetFacts collectNodeFacts(NodeTarget target, ServerPlayer player) {
        Vec3 center = Vec3.atCenterOf(target.position());
        double distance = Math.sqrt(player.distanceToSqr(center));
        if (!player.level().dimension().equals(target.dimension())) {
            return new TargetFacts(false, distance, false, false);
        }

        boolean chunkLoaded = player.level().hasChunkAt(target.position());
        if (!chunkLoaded) {
            return new TargetFacts(false, distance, false, false);
        }

        boolean stableTarget = target.snapshot(player).isPresent();
        HitResult hit = player.pick(MAX_DISTANCE, 1.0F, true);
        boolean lineOfSight = stableTarget
                && hit instanceof BlockHitResult blockHit
                && blockHit.getBlockPos().equals(target.position());
        return new TargetFacts(true, distance, lineOfSight, stableTarget);
    }

    private static TargetFacts collectBlockFacts(BlockTarget target, ServerPlayer player) {
        Vec3 center = Vec3.atCenterOf(target.position());
        double distance = Math.sqrt(player.distanceToSqr(center));
        if (!player.level().dimension().equals(target.dimension())) {
            return new TargetFacts(false, distance, false, false);
        }

        boolean chunkLoaded = player.level().hasChunkAt(target.position());
        if (!chunkLoaded) {
            return new TargetFacts(false, distance, false, false);
        }

        HitResult hit = player.pick(MAX_DISTANCE, 1.0F, true);
        boolean lineOfSight = hit instanceof BlockHitResult blockHit
                && blockHit.getBlockPos().equals(target.position());
        String currentId = ScanRegistry.canonicalBlockId(BuiltInRegistries.BLOCK
                .getKey(player.level().getBlockState(target.position()).getBlock())
                .toString());
        return new TargetFacts(true, distance, lineOfSight, target.targetId().equals(currentId));
    }

    private static TargetFacts collectEntityFacts(EntityTarget target, ServerPlayer player) {
        if (!player.level().dimension().equals(target.dimension())
                || !(player.level() instanceof ServerLevel serverLevel)) {
            return new TargetFacts(false, 0.0D, false, false);
        }

        Entity entity = serverLevel.getEntity(target.entityId());
        if (entity == null) {
            return new TargetFacts(false, MAX_DISTANCE + 1.0D, false, false);
        }

        boolean chunkLoaded = serverLevel.hasChunkAt(entity.blockPosition());
        double distance = Math.sqrt(player.distanceToSqr(entity));
        boolean stableTarget = entity.isAlive()
                && target.targetId().equals(
                        EntityScanIdentity.targetId(entity)
                );
        boolean lineOfSight = false;
        if (stableTarget) {
            Vec3 eyeToTarget = entity.getBoundingBox()
                    .getCenter()
                    .subtract(player.getEyePosition())
                    .normalize();
            lineOfSight = player.hasLineOfSight(entity)
                    && player.getLookAngle().dot(eyeToTarget) >= 0.96D;
        }
        return new TargetFacts(chunkLoaded, distance, lineOfSight, stableTarget);
    }

    private static TargetFacts collectItemFacts(ItemTarget target, ServerPlayer player) {
        boolean sameDimension = player.level().dimension().equals(target.dimension());
        boolean chunkLoaded = sameDimension && player.level().hasChunkAt(player.blockPosition());
        ItemStack current = player.getItemInHand(target.targetHand());
        boolean stableTarget = sameDimension
                && !current.isEmpty()
                && itemIdentityMatches(current, target.type(), target.targetId(),
                target.knowledgeKey());
        return new TargetFacts(chunkLoaded, 0.0D, true, stableTarget);
    }

    private static TargetFacts collectDroppedItemFacts(
            DroppedItemTarget target,
            ServerPlayer player
    ) {
        if (!player.level().dimension().equals(target.dimension())
                || !(player.level() instanceof ServerLevel serverLevel)) {
            return new TargetFacts(false, 0.0D, false, false);
        }
        Entity entity = serverLevel.getEntity(target.entityId());
        if (!(entity instanceof ItemEntity itemEntity)) {
            return new TargetFacts(false, MAX_DISTANCE + 1.0D, false, false);
        }

        double distance = Math.sqrt(player.distanceToSqr(itemEntity));
        boolean chunkLoaded = serverLevel.hasChunkAt(itemEntity.blockPosition());
        boolean stableTarget = itemEntity.isAlive()
                && !itemEntity.getItem().isEmpty()
                && itemIdentityMatches(
                        itemEntity.getItem(),
                        target.type(),
                        target.targetId(),
                        target.knowledgeKey()
                );
        boolean lineOfSight = false;
        if (stableTarget) {
            Vec3 eyeToTarget = itemEntity.getBoundingBox()
                    .getCenter()
                    .subtract(player.getEyePosition())
                    .normalize();
            lineOfSight = player.hasLineOfSight(itemEntity)
                    && player.getLookAngle().dot(eyeToTarget) >= 0.96D;
        }
        return new TargetFacts(chunkLoaded, distance, lineOfSight, stableTarget);
    }

    private static boolean itemIdentityMatches(
            ItemStack stack,
            ScanTargetType type,
            String targetId,
            String knowledgeKey
    ) {
        ScanRegistry.ItemScanIdentity identity = ScanRegistry.identityForItem(stack);
        return identity.type() == type
                && identity.targetId().equals(targetId)
                && identity.knowledgeKey().equals(knowledgeKey);
    }

    private static final class ScanSession {
        private final InteractionHand thaumometerHand;
        private final ScanTarget target;
        private int elapsedTicks;

        private ScanSession(InteractionHand thaumometerHand, ScanTarget target) {
            this.thaumometerHand = thaumometerHand;
            this.target = target;
        }
    }

    public sealed interface ScanTarget permits
            BlockTarget,
            NodeTarget,
            EntityTarget,
            ItemTarget,
            DroppedItemTarget,
            InventoryItemTarget {
        ResourceKey<Level> dimension();

        ScanTargetType type();

        String targetId();

        default String scanKey() {
            return ScanRegistry.scanKey(type(), targetId());
        }

        default String knowledgeKey() {
            return this instanceof NodeTarget
                    ? scanKey()
                    : ScanRegistry.knowledgeKey(type(), targetId());
        }

        Vec3 effectPosition(ServerPlayer player);
    }

    /**
     * Pins the persistent node UUID captured at scan start. Replacing the
     * block entity at the same position cannot substitute another node into
     * an in-flight scan.
     */
    public record NodeTarget(
            ResourceKey<Level> dimension,
            BlockPos position,
            UUID nodeId
    ) implements ScanTarget {
        public NodeTarget {
            position = position.immutable();
        }

        @Override
        public ScanTargetType type() {
            return ScanTargetType.PHENOMENON;
        }

        @Override
        public String targetId() {
            return AuraNodeScanIdentity.TARGET_ID.toString();
        }

        @Override
        public String scanKey() {
            return new AuraNodeScanIdentity(nodeId).scanKey();
        }

        @Override
        public Vec3 effectPosition(ServerPlayer player) {
            return Vec3.atCenterOf(position);
        }

        Optional<AuraNodeState.Snapshot> snapshot(ServerPlayer player) {
            if (!player.level().dimension().equals(dimension)
                    || !player.level().hasChunkAt(position)) {
                return Optional.empty();
            }
            if (player.level().getBlockEntity(position) instanceof AuraNodeBlockEntity node
                    && nodeId.equals(node.scanIdentity().nodeId())) {
                return Optional.of(node.snapshotState().snapshot());
            }
            if (player.level().getBlockEntity(position)
                    instanceof EnergizedAuraNodeBlockEntity node
                    && nodeId.equals(node.originalState().nodeId())) {
                return Optional.of(node.originalState().snapshot());
            }
            return Optional.empty();
        }
    }

    public record BlockTarget(ResourceKey<Level> dimension, BlockPos position, String targetId)
            implements ScanTarget {
        @Override
        public ScanTargetType type() {
            return ScanTargetType.BLOCK;
        }

        @Override
        public Vec3 effectPosition(ServerPlayer player) {
            return Vec3.atCenterOf(position);
        }
    }

    public record EntityTarget(ResourceKey<Level> dimension, int entityId, String targetId)
            implements ScanTarget {
        @Override
        public ScanTargetType type() {
            return ScanTargetType.ENTITY;
        }

        @Override
        public Vec3 effectPosition(ServerPlayer player) {
            if (player.level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(entityId);
                if (entity != null) {
                    return entity.getBoundingBox().getCenter();
                }
            }
            return player.position();
        }
    }

    public record ItemTarget(
            ResourceKey<Level> dimension,
            InteractionHand targetHand,
            ScanTargetType type,
            String targetId,
            String knowledgeKey
    )
            implements ScanTarget {
        @Override
        public Vec3 effectPosition(ServerPlayer player) {
            return player.getEyePosition().add(player.getLookAngle().scale(0.75D));
        }
    }

    public record DroppedItemTarget(
            ResourceKey<Level> dimension,
            int entityId,
            ScanTargetType type,
            String targetId,
            String knowledgeKey
    ) implements ScanTarget {
        @Override
        public Vec3 effectPosition(ServerPlayer player) {
            if (player.level() instanceof ServerLevel serverLevel
                    && serverLevel.getEntity(entityId) instanceof ItemEntity itemEntity) {
                return itemEntity.getBoundingBox().getCenter();
            }
            return player.getEyePosition().add(player.getLookAngle().scale(0.75D));
        }
    }

    public record InventoryItemTarget(
            ResourceKey<Level> dimension,
            ScanTargetType type,
            String targetId,
            String knowledgeKey
    ) implements ScanTarget {
        @Override
        public Vec3 effectPosition(ServerPlayer player) {
            return player.getEyePosition();
        }
    }

    private record TargetFacts(
            boolean targetChunkLoaded,
            double distance,
            boolean lineOfSight,
            boolean stableTarget) {
    }
}
