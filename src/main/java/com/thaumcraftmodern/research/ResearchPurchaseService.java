package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.WarpFeedbackPacket;
import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

/**
 * Server-authoritative TC4 secondary-research purchase transaction.
 */
public final class ResearchPurchaseService {
    private ResearchPurchaseService() {
    }

    public static Result purchase(ServerPlayer player, String researchId) {
        Objects.requireNonNull(player, "player");
        ResearchDefinition definition = ResearchRegistry.find(researchId).orElse(null);
        if (definition == null) {
            return reject(player, Result.UNKNOWN_RESEARCH);
        }
        return KnowledgeAccess.get(player)
                .map(knowledge -> {
                    Result result = purchase(knowledge, definition);
                    if (result != Result.PURCHASED) {
                        return reject(player, result);
                    }
                    KnowledgeSync.send(player, "research_purchase:" + definition.id());
                    sendCompletionWarpFeedback(
                            player,
                            definition.completionWarp()
                    );
                    player.level().playSound(
                            null,
                            player.blockPosition(),
                            ModSounds.LEARN.get(),
                            SoundSource.PLAYERS,
                            0.75F,
                            1.0F
                    );
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.thaumcraftmodern.research.purchased",
                                    Component.translatable(definition.titleKey())
                            ),
                            true
                    );
                    ResearchDiagnostics.log(
                            "SERVER_RESEARCH_PURCHASED",
                            "player={} research={} cost={}",
                            player.getGameProfile().getName(),
                            definition.id(),
                            definition.purchaseCost()
                    );
                    return result;
                })
                .orElseGet(() -> reject(player, Result.MISSING_KNOWLEDGE));
    }

    static Result purchase(
            PlayerThaumKnowledge knowledge,
            ResearchDefinition definition
    ) {
        Objects.requireNonNull(knowledge, "knowledge");
        Objects.requireNonNull(definition, "definition");
        if (definition.inactive()) {
            return Result.INACTIVE_RESEARCH;
        }
        if (!definition.purchasable()) {
            return Result.NOT_PURCHASABLE;
        }
        if (knowledge.hasCompletedResearch(definition.id())) {
            return Result.ALREADY_COMPLETED;
        }
        if (!ResearchProgressService.isAvailable(definition, knowledge)) {
            return Result.UNAVAILABLE;
        }
        for (AspectCost cost : definition.purchaseCost()) {
            if (!knowledge.knowsAspect(cost.aspectId())
                    || knowledge.aspectAmount(cost.aspectId()) < cost.amount()) {
                return Result.INSUFFICIENT_ASPECTS;
            }
        }

        // The complete preflight above makes this an all-or-nothing transaction.
        for (AspectCost cost : definition.purchaseCost()) {
            for (int point = 0; point < cost.amount(); point++) {
                if (!knowledge.tryConsumeAspect(cost.aspectId())) {
                    throw new IllegalStateException(
                            "validated research cost became unavailable: "
                                    + cost.aspectId()
                    );
                }
            }
        }
        knowledge.completeResearch(definition.id());
        applyCompletionWarp(knowledge, definition.completionWarp());
        ResearchProgressService.reconcile(knowledge);
        return Result.PURCHASED;
    }

    private static void applyCompletionWarp(
            PlayerThaumKnowledge knowledge,
            int amount
    ) {
        if (amount <= 0) {
            return;
        }
        int normal = amount / 2;
        knowledge.addWarp(WarpType.PERMANENT, amount - normal);
        if (normal > 0) {
            knowledge.addWarp(WarpType.NORMAL, normal);
        }
    }

    private static void sendCompletionWarpFeedback(
            ServerPlayer player,
            int amount
    ) {
        if (amount <= 0) {
            return;
        }
        int normal = amount / 2;
        int permanent = amount - normal;
        if (permanent > 0) {
            ModNetwork.sendTo(player, new WarpFeedbackPacket(
                    WarpFeedbackPacket.PERMANENT,
                    permanent,
                    WarpFeedbackPacket.VISUAL_NONE
            ));
        }
        if (normal > 0) {
            ModNetwork.sendTo(player, new WarpFeedbackPacket(
                    WarpFeedbackPacket.NORMAL,
                    normal,
                    WarpFeedbackPacket.VISUAL_NONE
            ));
        }
    }

    private static Result reject(ServerPlayer player, Result result) {
        player.displayClientMessage(
                Component.translatable(
                        result == Result.INSUFFICIENT_ASPECTS
                                ? "tc.research.short"
                                : "message.thaumcraftmodern.research.purchase_rejected",
                        result.name().toLowerCase(java.util.Locale.ROOT)
                ),
                true
        );
        ResearchDiagnostics.log(
                "SERVER_RESEARCH_PURCHASE_REJECTED",
                "player={} result={}",
                player.getGameProfile().getName(),
                result
        );
        return result;
    }

    public enum Result {
        PURCHASED,
        UNKNOWN_RESEARCH,
        INACTIVE_RESEARCH,
        NOT_PURCHASABLE,
        ALREADY_COMPLETED,
        UNAVAILABLE,
        INSUFFICIENT_ASPECTS,
        MISSING_KNOWLEDGE
    }
}
