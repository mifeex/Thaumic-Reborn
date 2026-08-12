package com.thaumcraftmodern.client;

import com.thaumcraftmodern.item.ThaumometerItem;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeScanIdentity;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlockEntity;
import com.thaumcraftmodern.visnet.EnergizedAuraNodeBlockEntity;
import com.thaumcraftmodern.scan.AspectReward;
import com.thaumcraftmodern.scan.ScanDefinition;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.scan.ScanSessionManager;
import com.thaumcraftmodern.scan.ScanTargetType;
import com.thaumcraftmodern.scan.ScanTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Resolves the live block under the first-person Thaumometer reticle. The
 * result deliberately exists even when a block has no scan definition so
 * every block can still be named and highlighted.
 */
final class ClientThaumometerTarget {
    private ClientThaumometerTarget() {
    }

    static Optional<TargetedBlock> findBlock(Minecraft minecraft, float partialTick) {
        if (minecraft.player == null
                || minecraft.level == null
                || minecraft.screen != null
                || minecraft.options.hideGui
                || !minecraft.options.getCameraType().isFirstPerson()
                || !isHoldingThaumometer(minecraft)) {
            return Optional.empty();
        }

        BlockHitResult blockHit = ScanTargeting.findBlock(
                minecraft.player,
                partialTick
        ).orElse(null);
        if (blockHit == null) {
            return Optional.empty();
        }

        BlockPos position = blockHit.getBlockPos().immutable();
        BlockState state = minecraft.level.getBlockState(position);
        AuraNodeState.Snapshot nodeSnapshot = null;
        List<AspectReward> energizedAspects = null;
        Component nodeDisplayName = Component.empty();
        if (minecraft.level.getBlockEntity(position)
                instanceof AuraNodeBlockEntity node) {
            nodeSnapshot = node.snapshotState().snapshot();
            nodeDisplayName = Component.translatable(
                    "block.thaumcraftmodern.aura_node"
            );
        } else if (minecraft.level.getBlockEntity(position)
                instanceof JarredAuraNodeBlockEntity jar) {
            nodeSnapshot = jar.data()
                    .map(data -> data.node().snapshot())
                    .orElse(null);
            nodeDisplayName = Component.translatable(
                    "block.thaumcraftmodern.jarred_aura_node"
            );
        } else if (minecraft.level.getBlockEntity(position)
                instanceof EnergizedAuraNodeBlockEntity energized) {
            nodeSnapshot = energized.originalState().snapshot();
            energizedAspects =
                    ClientAspectContainerReadout.energizedNodeContents(
                            energized.visBase());
            nodeDisplayName = Component.translatable(
                    "block.thaumcraftmodern.energized_aura_node"
            );
        }
        if (nodeSnapshot != null) {
            AuraNodeScanIdentity identity =
                    new AuraNodeScanIdentity(nodeSnapshot.nodeId());
            boolean studied = KnowledgeAccess.get(minecraft.player)
                    .map(knowledge -> knowledge.hasScan(identity.scanKey()))
                    .orElse(false);
            List<AspectReward> aspects = discloseNodeAspects(studied)
                    ? energizedAspects != null
                            ? energizedAspects
                            : ClientAspectContainerReadout
                                    .nodeContents(nodeSnapshot)
                    : List.of();
            return Optional.of(new TargetedBlock(
                    position,
                    state,
                    AuraNodeScanIdentity.TARGET_ID.toString(),
                    nodeDisplayName,
                    studied,
                    aspects,
                    nodeDescription(nodeSnapshot, studied)
            ));
        }
        String targetId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        boolean studied = KnowledgeAccess.get(minecraft.player)
                .map(knowledge -> knowledge.hasScan(
                        ScanRegistry.knowledgeKey(ScanTargetType.BLOCK, targetId)
                ))
                .orElse(false);
        Optional<ScanDefinition> definition = studied
                ? ScanRegistry.findHistorical(ScanTargetType.BLOCK, targetId)
                : ScanRegistry.find(ScanTargetType.BLOCK, targetId);
        Component displayName = definition
                .filter(value -> !value.displayKey().isBlank())
                .map(value -> (Component) Component.translatable(value.displayKey()))
                .orElseGet(state.getBlock()::getName);
        List<AspectReward> aspects = studied
                ? definition.map(ScanDefinition::aspects).orElseGet(List::of)
                : List.of();
        return Optional.of(new TargetedBlock(
                position,
                state,
                targetId,
                displayName,
                studied,
                aspects,
                Component.empty()
        ));
    }

    static Optional<TargetedReadout> findReadout(Minecraft minecraft, float partialTick) {
        if (!canTarget(minecraft)) {
            return Optional.empty();
        }

        Entity entity = ScanTargeting.findEntity(
                minecraft.player,
                partialTick,
                Entity::isAlive
        ).orElse(null);
        if (entity instanceof ItemEntity itemEntity) {
            return Optional.of(readoutForItem(minecraft, itemEntity.getItem()));
        }
        if (entity != null) {
            String targetId =
                    com.thaumcraftmodern.scan.EntityScanIdentity
                            .targetId(entity);
            return Optional.of(readout(
                    minecraft,
                    entity.getDisplayName(),
                    ScanTargetType.ENTITY,
                    targetId
            ));
        }

        TargetedBlock block = findBlock(minecraft, partialTick).orElse(null);
        if (block != null) {
            return Optional.of(new TargetedReadout(
                    block.displayName(),
                    block.studied(),
                    block.aspects(),
                    block.nodeDescription()
            ));
        }

        ItemStack heldTarget = heldScanTarget(minecraft);
        return heldTarget.isEmpty()
                ? Optional.empty()
                : Optional.of(readoutForItem(minecraft, heldTarget));
    }

    private static TargetedReadout readoutForItem(Minecraft minecraft, ItemStack stack) {
        ScanRegistry.ItemScanIdentity identity = ScanRegistry.identityForItem(stack);
        String scanKey = identity.knowledgeKey();
        boolean studied = KnowledgeAccess.get(minecraft.player)
                .map(knowledge -> knowledge.hasScan(scanKey))
                .orElse(false);
        Optional<ScanDefinition> definition = ScanRegistry.findForItem(stack);
        Component displayName = definition
                .filter(value -> !value.displayKey().isBlank())
                .map(value -> (Component) Component.translatable(value.displayKey()))
                .orElse(stack.getHoverName());
        return new TargetedReadout(
                displayName,
                studied,
                studied
                        ? definition.map(ScanDefinition::aspects).orElseGet(List::of)
                        : List.of(),
                Component.empty()
        );
    }

    private static TargetedReadout readout(
            Minecraft minecraft,
            Component fallbackName,
            ScanTargetType type,
            String targetId
    ) {
        String scanKey = ScanRegistry.knowledgeKey(type, targetId);
        boolean studied = KnowledgeAccess.get(minecraft.player)
                .map(knowledge -> knowledge.hasScan(scanKey))
                .orElse(false);
        Optional<ScanDefinition> definition = studied
                ? ScanRegistry.findHistorical(type, targetId)
                : ScanRegistry.find(type, targetId);
        Component displayName = definition
                .filter(value -> !value.displayKey().isBlank())
                .map(value -> (Component) Component.translatable(value.displayKey()))
                .orElse(fallbackName);
        List<AspectReward> aspects = studied
                ? definition.map(ScanDefinition::aspects).orElseGet(List::of)
                : List.of();
        return new TargetedReadout(
                displayName,
                studied,
                aspects,
                Component.empty()
        );
    }

    private static ItemStack heldScanTarget(Minecraft minecraft) {
        ItemStack mainHand = minecraft.player.getMainHandItem();
        ItemStack offHand = minecraft.player.getOffhandItem();
        if (mainHand.getItem() instanceof ThaumometerItem
                && !(offHand.getItem() instanceof ThaumometerItem)) {
            return offHand;
        }
        if (offHand.getItem() instanceof ThaumometerItem
                && !(mainHand.getItem() instanceof ThaumometerItem)) {
            return mainHand;
        }
        return ItemStack.EMPTY;
    }

    private static boolean canTarget(Minecraft minecraft) {
        return minecraft.player != null
                && minecraft.level != null
                && minecraft.screen == null
                && !minecraft.options.hideGui
                && minecraft.options.getCameraType().isFirstPerson()
                && isHoldingThaumometer(minecraft);
    }

    static boolean isHoldingThaumometer(Minecraft minecraft) {
        if (minecraft.player == null) {
            return false;
        }
        return isThaumometer(minecraft.player.getMainHandItem())
                || isThaumometer(minecraft.player.getOffhandItem());
    }

    private static boolean isThaumometer(ItemStack stack) {
        return stack.getItem() instanceof ThaumometerItem;
    }

    static boolean discloseNodeAspects(boolean studied) {
        return studied;
    }

    static Component nodeDescription(
            AuraNodeState.Snapshot snapshot,
            boolean studied
    ) {
        if (!studied) {
            return Component.empty();
        }
        Component type = Component.translatable(
                "node_type.thaumcraftmodern."
                        + snapshot.type().name().toLowerCase(Locale.ROOT)
        );
        if (snapshot.modifier() == AuraNodeModifier.NORMAL) {
            return type;
        }
        return Component.empty()
                .append(type)
                .append(", ")
                .append(Component.translatable(
                        "node_modifier.thaumcraftmodern."
                                + snapshot.modifier()
                                .name()
                                .toLowerCase(Locale.ROOT)
                ));
    }

    record TargetedBlock(
            BlockPos position,
            BlockState state,
            String targetId,
            Component displayName,
            boolean studied,
            List<AspectReward> aspects,
            Component nodeDescription
    ) {
        TargetedBlock {
            aspects = List.copyOf(aspects);
            nodeDescription = nodeDescription.copy();
        }
    }

    record TargetedReadout(
            Component displayName,
            boolean studied,
            List<AspectReward> aspects,
            Component nodeDescription
    ) {
        TargetedReadout {
            aspects = List.copyOf(aspects);
            nodeDescription = nodeDescription.copy();
        }
    }
}
