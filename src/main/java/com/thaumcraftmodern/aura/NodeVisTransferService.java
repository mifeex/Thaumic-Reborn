package com.thaumcraftmodern.aura;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Server-owned, bounded and replay-protected node-to-wand transfer.
 */
public final class NodeVisTransferService {
    private final OperationNonceGuard nonceGuard;

    public NodeVisTransferService(OperationNonceGuard nonceGuard) {
        this.nonceGuard = Objects.requireNonNull(nonceGuard, "nonceGuard");
    }

    public Result transfer(
            Request request,
            AuraNodeState node,
            WandVisStore wand
    ) {
        return transfer(request, node, wand, null, Integer.MAX_VALUE, false);
    }

    /**
     * Classic node tapping transfers one randomly selected primal at a time.
     * This bounded form shares the same validation, replay guard and rollback
     * transaction as a full transfer.
     */
    public Result transferAspect(
            Request request,
            AuraNodeState node,
            WandVisStore wand,
            PrimalAspect aspect,
            int maximumWholeVis,
            boolean preserveLastVis
    ) {
        Objects.requireNonNull(aspect, "aspect");
        if (maximumWholeVis < 1) {
            throw new IllegalArgumentException("maximumWholeVis must be positive");
        }
        return transfer(
                request,
                node,
                wand,
                aspect,
                maximumWholeVis,
                preserveLastVis
        );
    }

    private Result transfer(
            Request request,
            AuraNodeState node,
            WandVisStore wand,
            PrimalAspect selectedAspect,
            int maximumWholeVis,
            boolean preserveLastVis
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(wand, "wand");

        Status validation = validate(request, node);
        if (validation != Status.TRANSFERRED) {
            return Result.empty(validation);
        }
        if (!nonceGuard.tryBegin(request.actorId(), request.operationId())) {
            return Result.empty(Status.DUPLICATE_OPERATION);
        }

        Object wandLock = Objects.requireNonNull(
                wand.transactionLock(),
                "wand transactionLock"
        );
        synchronized (node) {
            synchronized (wandLock) {
                AuraNodeState.Snapshot nodeBefore = node.snapshot();
                WandVisStore.Snapshot wandBefore = wand.snapshot();
                int unitsPerNodeVis = wand.unitsPerNodeVis();
                if (unitsPerNodeVis < 1) {
                    nonceGuard.release(request.actorId(), request.operationId());
                    return Result.empty(Status.INVALID_WAND_SCALE);
                }
                EnumMap<PrimalAspect, Integer> moved = new EnumMap<>(PrimalAspect.class);
                EnumMap<PrimalAspect, Integer> nextNode =
                        new EnumMap<>(nodeBefore.current());
                EnumMap<PrimalAspect, Integer> nextWand =
                        new EnumMap<>(wandBefore.current());

                for (PrimalAspect aspect : PrimalAspect.ordered()) {
                    if (selectedAspect != null && aspect != selectedAspect) {
                        moved.put(aspect, 0);
                        continue;
                    }
                    int missingStoreUnits = wandBefore.capacity().get(aspect)
                            - wandBefore.current().get(aspect);
                    // TC4's addVis accepts the fractional tail that still fits
                    // and drains the corresponding whole node-vis point. For
                    // example, 49.95/50 accepts 5 centivis from a one-vis tap.
                    int missingWholeVis = missingStoreUnits <= 0
                            ? 0
                            : 1 + (missingStoreUnits - 1) / unitsPerNodeVis;
                    int availableNodeVis = nodeBefore.current().get(aspect)
                            - (preserveLastVis ? 1 : 0);
                    int amount = Math.min(
                            Math.min(missingWholeVis, Math.max(0, availableNodeVis)),
                            maximumWholeVis
                    );
                    moved.put(aspect, amount);
                    nextNode.put(aspect, nodeBefore.current().get(aspect) - amount);
                    int acceptedStoreUnits = Math.min(
                            missingStoreUnits,
                            Math.multiplyExact(amount, unitsPerNodeVis)
                    );
                    nextWand.put(
                            aspect,
                            Math.addExact(
                                    wandBefore.current().get(aspect),
                                    acceptedStoreUnits
                            )
                    );
                }

                boolean anyMoved = moved.values().stream().anyMatch(amount -> amount > 0);
                if (!anyMoved) {
                    return new Result(Status.NOTHING_TO_TRANSFER, immutable(moved));
                }

                if (!wand.replaceCurrent(wandBefore.revision(), nextWand)) {
                    nonceGuard.release(request.actorId(), request.operationId());
                    return Result.empty(Status.STALE_WAND);
                }
                if (!node.replaceCurrent(nodeBefore.revision(), nextNode)) {
                    boolean rolledBack = wand.restore(
                            wandBefore,
                            Math.addExact(wandBefore.revision(), 1L)
                    );
                    nonceGuard.release(request.actorId(), request.operationId());
                    return Result.empty(
                            rolledBack ? Status.STALE_NODE : Status.ROLLBACK_FAILED
                    );
                }
                return new Result(Status.TRANSFERRED, immutable(moved));
            }
        }
    }

    private static Status validate(Request request, AuraNodeState node) {
        if (!request.serverSide()) {
            return Status.NOT_SERVER;
        }
        if (!request.sameDimension()) {
            return Status.WRONG_DIMENSION;
        }
        if (!request.nodeChunkLoaded()) {
            return Status.NODE_NOT_LOADED;
        }
        if (!Double.isFinite(request.distance())
                || request.distance() < 0.0D
                || request.distance() > request.maximumDistance()) {
            return Status.TOO_FAR;
        }
        if (!request.expectedNodeId().equals(node.nodeId())) {
            return Status.NODE_CHANGED;
        }
        return Status.TRANSFERRED;
    }

    private static Map<PrimalAspect, Integer> immutable(
            EnumMap<PrimalAspect, Integer> values
    ) {
        return Collections.unmodifiableMap(new EnumMap<>(values));
    }

    public record Request(
            UUID actorId,
            UUID operationId,
            UUID expectedNodeId,
            boolean serverSide,
            boolean sameDimension,
            boolean nodeChunkLoaded,
            double distance,
            double maximumDistance
    ) {
        public Request {
            actorId = Objects.requireNonNull(actorId, "actorId");
            operationId = Objects.requireNonNull(operationId, "operationId");
            expectedNodeId = Objects.requireNonNull(expectedNodeId, "expectedNodeId");
            if (!Double.isFinite(maximumDistance) || maximumDistance < 0.0D) {
                throw new IllegalArgumentException("maximumDistance must be finite and non-negative");
            }
        }
    }

    public record Result(Status status, Map<PrimalAspect, Integer> moved) {
        public Result {
            status = Objects.requireNonNull(status, "status");
            moved = PrimalVis.exact(moved, "moved");
        }

        static Result empty(Status status) {
            return new Result(status, PrimalVis.uniform(0));
        }
    }

    public enum Status {
        TRANSFERRED,
        NOTHING_TO_TRANSFER,
        DUPLICATE_OPERATION,
        NOT_SERVER,
        WRONG_DIMENSION,
        NODE_NOT_LOADED,
        TOO_FAR,
        NODE_CHANGED,
        STALE_WAND,
        STALE_NODE,
        ROLLBACK_FAILED,
        INVALID_WAND_SCALE
    }
}
