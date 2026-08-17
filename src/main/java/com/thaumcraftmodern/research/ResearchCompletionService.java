package com.thaumcraftmodern.research;

import com.thaumcraftmodern.item.DiscoveryItem;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.knowledge.WarpType;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

/**
 * The single server-side authority for consuming a completed research
 * discovery.
 *
 * <p>A research id stored in item NBT is only a lookup key. It never grants
 * progress by itself. The player must already have a server-recorded
 * {@linkplain #markDiscoveryReady(PlayerThaumKnowledge, String) completed-note
 * claim}, and every current research prerequisite is checked again when the
 * item is used.</p>
 */
public final class ResearchCompletionService {
    private static final String DISCOVERY_READY_PREFIX =
            "thaumic_reborn:discovery_ready/";
    private static final String RESEARCH_KEY = "Research";
    private static final String PAYLOAD_VERSION_KEY = "DiscoveryVersion";
    private static final String PAYLOAD_RESEARCH_KEY = "ValidatedResearch";
    private static final int PAYLOAD_VERSION = 1;

    private ResearchCompletionService() {
    }

    public static boolean markDiscoveryReady(
            PlayerThaumKnowledge knowledge,
            String researchId
    ) {
        Objects.requireNonNull(knowledge, "knowledge");
        return knowledge.recordResearchCriterion(discoveryCriterion(researchId));
    }

    public static boolean isDiscoveryReady(
            PlayerThaumKnowledge knowledge,
            String researchId
    ) {
        Objects.requireNonNull(knowledge, "knowledge");
        return knowledge.hasResearchCriterion(discoveryCriterion(researchId));
    }

    public static Result complete(PlayerThaumKnowledge knowledge, ItemStack discovery) {
        Objects.requireNonNull(knowledge, "knowledge");
        String researchId = discoveryResearchId(discovery);
        if (!hasValidDiscoveryPayload(discovery)) {
            return Result.INVALID_DISCOVERY;
        }
        return completeValidatedDiscovery(knowledge, researchId);
    }

    /**
     * Applies the state transition after the Discovery item adapter has
     * authenticated its payload. Package visibility keeps the state machine
     * directly testable without bootstrapping Minecraft item registries.
     */
    static Result completeValidatedDiscovery(
            PlayerThaumKnowledge knowledge,
            String researchId
    ) {
        Objects.requireNonNull(knowledge, "knowledge");
        ResearchDefinition definition = ResearchRegistry.find(researchId).orElse(null);
        if (definition == null) {
            return Result.UNKNOWN_RESEARCH;
        }
        if (definition.inactive()) {
            return Result.INACTIVE_RESEARCH;
        }
        if (definition.autoUnlock()) {
            return Result.TRANSITION_NOT_ALLOWED;
        }
        if (knowledge.hasCompletedResearch(definition.id())) {
            return Result.ALREADY_COMPLETED;
        }
        if (!isDiscoveryReady(knowledge, definition.id())) {
            return Result.INVALID_DISCOVERY;
        }
        if (!knowledge.hasRevealedResearch(definition.id())) {
            return Result.NOT_REVEALED;
        }
        if (!allCompleted(definition.parents(), knowledge)
                || !allCompleted(definition.hiddenParents(), knowledge)) {
            return Result.PARENTS_INCOMPLETE;
        }
        if (!definition.revealWhen().test(knowledge)
                || !definition.unlockWhen().test(knowledge)
                || !definition.revealedBy().isBlank()
                && !knowledge.hasCompletedResearch(definition.revealedBy())) {
            return Result.CONDITIONS_UNMET;
        }
        if (!ResearchProgressService.isVisible(definition, knowledge)) {
            return Result.NOT_REVEALED;
        }
        if (!ResearchProgressService.isAvailable(definition, knowledge)) {
            return Result.CONDITIONS_UNMET;
        }

        knowledge.completeResearch(definition.id());
        applyCompletionWarp(knowledge, definition.completionWarp());
        ResearchProgressService.reconcile(knowledge);
        return Result.COMPLETED;
    }

    public static void writeDiscoveryPayload(ItemStack stack, String researchId) {
        Objects.requireNonNull(stack, "stack");
        if (!(stack.getItem() instanceof DiscoveryItem)) {
            throw new IllegalArgumentException(
                    "discovery payload can only be written to a Discovery item"
            );
        }
        String validatedResearchId = requireResearchId(researchId);
        stack.getOrCreateTag().putString(RESEARCH_KEY, validatedResearchId);
        stack.getOrCreateTag().putInt(PAYLOAD_VERSION_KEY, PAYLOAD_VERSION);
        stack.getOrCreateTag().putString(
                PAYLOAD_RESEARCH_KEY,
                validatedResearchId
        );
    }

    public static String discoveryResearchId(ItemStack stack) {
        if (stack == null || !stack.hasTag()) {
            return "";
        }
        return stack.getTag().getString(RESEARCH_KEY);
    }

    public static boolean hasValidDiscoveryPayload(ItemStack stack) {
        if (stack == null
                || !(stack.getItem() instanceof DiscoveryItem)
                || !stack.hasTag()) {
            return false;
        }
        String researchId = discoveryResearchId(stack);
        return !researchId.isBlank()
                && stack.getTag().getInt(PAYLOAD_VERSION_KEY) == PAYLOAD_VERSION
                && researchId.equals(
                        stack.getTag().getString(PAYLOAD_RESEARCH_KEY)
                );
    }

    private static boolean allCompleted(
            List<String> researchIds,
            PlayerThaumKnowledge knowledge
    ) {
        return researchIds.stream().allMatch(knowledge::hasCompletedResearch);
    }

    private static void applyCompletionWarp(
            PlayerThaumKnowledge knowledge,
            int amount
    ) {
        if (amount <= 0) {
            return;
        }
        int normal = amount / 2;
        int permanent = amount - normal;
        knowledge.addWarp(WarpType.PERMANENT, permanent);
        if (normal > 0) {
            knowledge.addWarp(WarpType.NORMAL, normal);
        }
    }

    private static String discoveryCriterion(String researchId) {
        return DISCOVERY_READY_PREFIX + requireResearchId(researchId);
    }

    private static String requireResearchId(String researchId) {
        if (researchId == null || researchId.isBlank()) {
            throw new IllegalArgumentException("researchId must be non-blank");
        }
        return researchId;
    }

    public enum Result {
        COMPLETED,
        UNKNOWN_RESEARCH,
        INACTIVE_RESEARCH,
        TRANSITION_NOT_ALLOWED,
        INVALID_DISCOVERY,
        NOT_REVEALED,
        PARENTS_INCOMPLETE,
        CONDITIONS_UNMET,
        ALREADY_COMPLETED
    }
}
