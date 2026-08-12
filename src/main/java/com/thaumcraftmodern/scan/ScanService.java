package com.thaumcraftmodern.scan;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.aura.AuraNodeScanResult;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.ScanFeedbackPacket;
import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ScanService {
    private ScanService() {
    }

    public static void complete(ServerPlayer player, ScanSessionManager.ScanTarget target) {
        ScanDefinition definition = ScanRegistry.find(target.type(), target.targetId()).orElse(null);
        if (definition == null) {
            playCameraClack(player, target.effectPosition(player));
            sendFailure(player, "message.thaumcraftmodern.scan.unknown", "");
            return;
        }
        Optional<AuraNodeScanResult> nodeResult = nodeResult(player, target);
        if (target instanceof ScanSessionManager.NodeTarget && nodeResult.isEmpty()) {
            playCameraClack(player, target.effectPosition(player));
            sendFailure(player, "message.thaumcraftmodern.scan.error.invalid_target", "");
            return;
        }
        ScanDefinition effectiveDefinition = effectiveDefinition(definition, nodeResult);
        if (effectiveDefinition.aspects().isEmpty()) {
            playCameraClack(player, target.effectPosition(player));
            sendFailure(player, "message.thaumcraftmodern.scan.error.invalid_target", "");
            return;
        }

        KnowledgeAccess.get(player).ifPresentOrElse(knowledge -> {
            for (AspectReward reward : effectiveDefinition.aspects()) {
                AspectDefinition aspect = AspectRegistryRuntime.find(reward.aspectId()).orElse(null);
                if (aspect == null) {
                    playCameraClack(player, target.effectPosition(player));
                    sendFailure(player, "message.thaumcraftmodern.scan.unknown", "");
                    return;
                }
            }

            Optional<String> missingAspect = firstMissingPrerequisite(
                    effectiveDefinition,
                    knowledge
            );
            if (missingAspect.isPresent()) {
                playCameraClack(player, target.effectPosition(player));
                sendFailure(
                        player,
                        "message.thaumcraftmodern.scan.error.missing_parent",
                        "aspect.thaumcraftmodern." + missingAspect.get()
                );
                return;
            }

            String knowledgeKey = target.knowledgeKey();
            if (knowledge.hasScan(knowledgeKey)) {
                return;
            }

            PlayerThaumKnowledge stagedKnowledge = knowledge.copy();
            stagedKnowledge.recordScan(knowledgeKey);
            if (target instanceof ScanSessionManager.NodeTarget) {
                /*
                 * Preserve the shared phenomenon criterion for research while
                 * the UUID-qualified key owns per-node repeat protection.
                 */
                stagedKnowledge.recordScan(definition.scanKey());
            }
            List<ScanFeedbackPacket.AspectGain> aspectGains =
                    ScanAspectGrantService.apply(
                            stagedKnowledge,
                            effectiveDefinition.aspects()
                    ).stream()
                            .map(grant -> new ScanFeedbackPacket.AspectGain(
                                    grant.aspectId(),
                                    grant.amount(),
                                    grant.total(),
                                    grant.newlyDiscovered()
                            ))
                            .toList();
            knowledge.copyFrom(stagedKnowledge);
            KnowledgeSync.send(player, "scan:" + knowledgeKey);
            ModNetwork.sendTo(player, new ScanFeedbackPacket(
                    true,
                    "message.thaumcraftmodern.scan.success",
                    displayKey(effectiveDefinition, target),
                    aspectGains,
                    nodeResult.map(ScanFeedbackPacket.NodeData::from)
            ));

            Vec3 position = target.effectPosition(player);
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        position.x,
                        position.y,
                        position.z,
                        24,
                        0.35D,
                        0.35D,
                        0.35D,
                        0.05D
                );
                playCameraClack(player, position);
            }
        }, () -> sendFailure(player, "message.thaumcraftmodern.scan.error.no_knowledge", ""));
    }

    private static Optional<AuraNodeScanResult> nodeResult(
            ServerPlayer player,
            ScanSessionManager.ScanTarget target
    ) {
        if (!(target instanceof ScanSessionManager.NodeTarget nodeTarget)) {
            return Optional.empty();
        }
        return nodeTarget.snapshot(player)
                .map(AuraNodeScanResult::from);
    }

    private static ScanDefinition effectiveDefinition(
            ScanDefinition definition,
            Optional<AuraNodeScanResult> nodeResult
    ) {
        return nodeResult
                .map(result -> new ScanDefinition(
                        definition.type(),
                        definition.targetId(),
                        definition.displayKey(),
                        result.rewards()
                ))
                .orElse(definition);
    }

    static Optional<String> firstMissingPrerequisite(
            ScanDefinition definition,
            PlayerThaumKnowledge knowledge
    ) {
        for (AspectReward reward : definition.aspects()) {
            Optional<String> missing = firstMissingPrerequisite(
                    reward.aspectId(),
                    knowledge,
                    AspectRegistryRuntime.catalog(),
                    new HashSet<>()
            );
            if (missing.isPresent()) {
                return missing;
            }
        }
        return Optional.empty();
    }

    public static String displayKey(
            ScanDefinition definition,
            ScanSessionManager.ScanTarget target
    ) {
        if (!definition.displayKey().isBlank()) {
            return definition.displayKey();
        }
        ResourceLocation id = ResourceLocation.tryParse(target.targetId());
        if (id == null) {
            return "";
        }
        return switch (target.type()) {
            case BLOCK -> BuiltInRegistries.BLOCK.getOptional(id)
                    .map(block -> block.getDescriptionId())
                    .orElse("");
            case ITEM -> BuiltInRegistries.ITEM.getOptional(id)
                    .map(item -> item.getDescriptionId())
                    .orElse("");
            case ENTITY -> BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                    .map(entity -> entity.getDescriptionId())
                    .orElse("");
            case BLOCK_TAG, ITEM_TAG, PHENOMENON -> "";
        };
    }

    private static Optional<String> firstMissingPrerequisite(
            String aspectId,
            PlayerThaumKnowledge knowledge,
            AspectCatalog catalog,
            Set<String> visited
    ) {
        if (!visited.add(aspectId)) {
            return Optional.empty();
        }
        AspectDefinition definition = catalog.lookup(aspectId).orElse(null);
        if (definition == null || definition.isPrimal()) {
            return Optional.empty();
        }

        /*
         * Report the direct missing component first. For Humanus =
         * Bestia + Cognitio this deliberately asks for Cognitio, rather than
         * jumping straight through it to one of Cognitio's own components.
         */
        for (String component : definition.components()) {
            if (!knowledge.knowsAspect(component)) {
                return Optional.of(component);
            }
        }
        for (String component : definition.components()) {
            Optional<String> nestedMissing = firstMissingPrerequisite(
                    component,
                    knowledge,
                    catalog,
                    visited
            );
            if (nestedMissing.isPresent()) {
                return nestedMissing;
            }
        }
        return Optional.empty();
    }

    public static void sendFailure(ServerPlayer player, String key, String displayKey) {
        ModNetwork.sendTo(player, new ScanFeedbackPacket(false, key, displayKey, List.of()));
    }

    private static void playCameraClack(ServerPlayer player, Vec3 position) {
        player.playNotifySound(
                ModSounds.CAMERA_CLACK.get(),
                SoundSource.PLAYERS,
                0.55F,
                0.95F + player.getRandom().nextFloat() * 0.1F
        );
    }
}
