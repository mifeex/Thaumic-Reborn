package com.thaumcraftmodern.aspect;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;

import java.util.Objects;

/**
 * Server-side, atomic aspect combination rule used by the Research Table.
 */
public final class AspectCombinationService {
    private AspectCombinationService() {
    }

    public static Result combine(
            AspectCatalog catalog,
            PlayerThaumKnowledge knowledge,
            String firstAspectId,
            String secondAspectId
    ) {
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(knowledge, "knowledge");

        if (catalog.lookup(firstAspectId).isEmpty() || catalog.lookup(secondAspectId).isEmpty()) {
            return new Result(Status.ASPECT_NOT_REGISTERED, "", false);
        }
        if (!knowledge.knowsAspect(firstAspectId) || !knowledge.knowsAspect(secondAspectId)) {
            return new Result(Status.ASPECT_NOT_KNOWN, "", false);
        }

        if (!knowledge.tryConsumeAspects(firstAspectId, secondAspectId)) {
            return new Result(Status.NOT_ENOUGH_POINTS, "", false);
        }

        // TC4 consumes both selected research points for every affordable
        // attempt, including pairs that do not form a compound aspect.
        AspectDefinition result = catalog.compositionResult(
                firstAspectId,
                secondAspectId
        ).orElse(null);
        if (result == null) {
            return new Result(Status.NO_COMBINATION, "", false);
        }

        boolean newlyDiscovered = !knowledge.knowsAspect(result.id());
        int createdAmount = newlyDiscovered ? 3 : 1;
        knowledge.addAspectPoints(result.id(), createdAmount);
        return new Result(Status.COMBINED, result.id(), newlyDiscovered, createdAmount);
    }

    public record Result(
            Status status,
            String resultAspectId,
            boolean newlyDiscovered,
            int createdAmount
    ) {
        public Result(Status status, String resultAspectId, boolean newlyDiscovered) {
            this(status, resultAspectId, newlyDiscovered, 0);
        }

        public boolean combined() {
            return status == Status.COMBINED;
        }
    }

    public enum Status {
        COMBINED,
        ASPECT_NOT_REGISTERED,
        ASPECT_NOT_KNOWN,
        NO_COMBINATION,
        NOT_ENOUGH_POINTS
    }
}
