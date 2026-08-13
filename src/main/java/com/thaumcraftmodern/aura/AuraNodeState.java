package com.thaumcraftmodern.aura;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Version-independent logical state of one aura node.
 *
 * <p>The mutable revision is server-owned and is used by transactional
 * services to reject stale updates. Serialization versioning lives in
 * {@link AuraNodeCodec}, so this class stays usable by block entities, jar
 * items and tests through the same path.</p>
 */
public final class AuraNodeState {
    private final UUID nodeId;
    private final AuraNodeType type;
    private final AuraNodeModifier modifier;
    private final LinkedHashMap<String, Integer> current;
    private final LinkedHashMap<String, Integer> maximum;
    private long revision;

    public AuraNodeState(
            UUID nodeId,
            AuraNodeType type,
            AuraNodeModifier modifier,
            Map<PrimalAspect, Integer> current,
            Map<PrimalAspect, Integer> maximum,
            long revision
    ) {
        this.nodeId = Objects.requireNonNull(nodeId, "nodeId");
        this.type = Objects.requireNonNull(type, "type");
        this.modifier = Objects.requireNonNull(modifier, "modifier");
        this.current = aspectCopy(current);
        this.maximum = aspectCopy(maximum);
        if (revision < 0L) {
            throw new IllegalArgumentException("revision cannot be negative");
        }
        this.revision = revision;
        validateBounds(this.current, this.maximum);
    }

    private AuraNodeState(
            UUID nodeId,
            AuraNodeType type,
            AuraNodeModifier modifier,
            Map<String, Integer> current,
            Map<String, Integer> maximum,
            long revision,
            boolean allAspects
    ) {
        this.nodeId = Objects.requireNonNull(nodeId, "nodeId");
        this.type = Objects.requireNonNull(type, "type");
        this.modifier = Objects.requireNonNull(modifier, "modifier");
        this.current = allAspectCopy(current, "current");
        this.maximum = allAspectCopy(maximum, "maximum");
        if (revision < 0L) {
            throw new IllegalArgumentException("revision cannot be negative");
        }
        this.revision = revision;
        validateBounds(this.current, this.maximum);
    }

    public static AuraNodeState withAspects(
            UUID nodeId,
            AuraNodeType type,
            AuraNodeModifier modifier,
            Map<String, Integer> current,
            Map<String, Integer> maximum,
            long revision
    ) {
        return new AuraNodeState(
                nodeId,
                type,
                modifier,
                current,
                maximum,
                revision,
                true
        );
    }

    public synchronized UUID nodeId() {
        return nodeId;
    }

    public synchronized AuraNodeType type() {
        return type;
    }

    public synchronized AuraNodeModifier modifier() {
        return modifier;
    }

    public synchronized int current(PrimalAspect aspect) {
        return current.getOrDefault(
                Objects.requireNonNull(aspect, "aspect").id(),
                0
        );
    }

    public synchronized int maximum(PrimalAspect aspect) {
        return maximum.getOrDefault(
                Objects.requireNonNull(aspect, "aspect").id(),
                0
        );
    }

    public synchronized int current(String aspectId) {
        return current.getOrDefault(validateAspectId(aspectId), 0);
    }

    public synchronized int maximum(String aspectId) {
        return maximum.getOrDefault(validateAspectId(aspectId), 0);
    }

    public synchronized long revision() {
        return revision;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                nodeId,
                type,
                modifier,
                primalProjection(current),
                primalProjection(maximum),
                current,
                maximum,
                revision
        );
    }

    public synchronized AuraNodeState copy() {
        return snapshot().toState();
    }

    /**
     * Compare-and-set used by server transactions. The maximum pool and node
     * identity are deliberately immutable in this vertical.
     */
    public synchronized boolean replaceCurrent(
            long expectedRevision,
            Map<PrimalAspect, Integer> nextCurrent
    ) {
        if (revision != expectedRevision) {
            return false;
        }
        EnumMap<PrimalAspect, Integer> validated = PrimalVis.mutableCopy(nextCurrent);
        LinkedHashMap<String, Integer> updated = new LinkedHashMap<>(current);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            String aspectId = aspect.id();
            int nextAmount = validated.get(aspect);
            if (updated.containsKey(aspectId)) {
                updated.put(aspectId, nextAmount);
            } else if (nextAmount != 0) {
                throw new IllegalArgumentException(
                        "cannot add absent primal aspect " + aspectId
                );
            }
        }
        validateBounds(updated, maximum);
        current.clear();
        current.putAll(updated);
        revision = Math.addExact(revision, 1L);
        return true;
    }

    /** Atomically removes up to {@code maximum} units of one primal aspect. */
    public synchronized int drain(PrimalAspect aspect, int maximum) {
        Objects.requireNonNull(aspect, "aspect");
        if (maximum <= 0) {
            return 0;
        }
        String aspectId = aspect.id();
        int available = current.getOrDefault(aspectId, 0);
        int consumed = Math.min(maximum, available);
        if (consumed <= 0) {
            return 0;
        }
        current.put(aspectId, available - consumed);
        revision = Math.addExact(revision, 1L);
        return consumed;
    }

    /**
     * Server tick mutation used by regeneration and node-to-node discharge.
     * It atomically replaces every aspect pool, including compound aspects.
     */
    public synchronized boolean replaceAspects(
            long expectedRevision,
            Map<String, Integer> nextCurrent,
            Map<String, Integer> nextMaximum
    ) {
        if (revision != expectedRevision) {
            return false;
        }
        LinkedHashMap<String, Integer> validatedCurrent =
                allAspectCopy(nextCurrent, "current");
        LinkedHashMap<String, Integer> validatedMaximum =
                allAspectCopy(nextMaximum, "maximum");
        validateBounds(validatedCurrent, validatedMaximum);
        current.clear();
        current.putAll(validatedCurrent);
        maximum.clear();
        maximum.putAll(validatedMaximum);
        revision = Math.addExact(revision, 1L);
        return true;
    }

    /**
     * Restores an exact snapshot during a transaction rollback.
     */
    public synchronized boolean restore(Snapshot snapshot, long expectedRevision) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (revision != expectedRevision
                || !nodeId.equals(snapshot.nodeId())
                || type != snapshot.type()
                || modifier != snapshot.modifier()
                || !maximum.equals(snapshot.aspectsMaximum())) {
            return false;
        }
        current.clear();
        current.putAll(snapshot.aspectsCurrent());
        revision = snapshot.revision();
        return true;
    }

    private static void validateBounds(
            Map<String, Integer> current,
            Map<String, Integer> maximum
    ) {
        if (!current.keySet().equals(maximum.keySet())) {
            throw new IllegalArgumentException(
                    "current and maximum aura node aspects must match"
            );
        }
        for (String aspect : current.keySet()) {
            int currentAmount = current.get(aspect);
            int maximumAmount = maximum.get(aspect);
            if (currentAmount > maximumAmount) {
                throw new IllegalArgumentException(
                        aspect + " current vis " + currentAmount
                                + " exceeds maximum " + maximumAmount
                );
            }
        }
    }

    private static LinkedHashMap<String, Integer> aspectCopy(
            Map<PrimalAspect, Integer> source
    ) {
        EnumMap<PrimalAspect, Integer> exact = PrimalVis.mutableCopy(source);
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            result.put(aspect.id(), exact.get(aspect));
        }
        return result;
    }

    private static LinkedHashMap<String, Integer> allAspectCopy(
            Map<String, Integer> source,
            String field
    ) {
        Objects.requireNonNull(source, field);
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        source.forEach((rawId, amount) -> {
            String id = validateAspectId(rawId);
            if (amount == null || amount < 0) {
                throw new IllegalArgumentException(
                        field + " aspect " + id + " cannot be negative"
                );
            }
            if (result.put(id, amount) != null) {
                throw new IllegalArgumentException("duplicate aspect " + id);
            }
        });
        if (result.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return result;
    }

    private static String validateAspectId(String rawId) {
        Objects.requireNonNull(rawId, "aspectId");
        String id = rawId.trim();
        if (id.isEmpty()
                || !id.equals(rawId)
                || !id.equals(id.toLowerCase(java.util.Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "aspect id must be non-blank, trimmed and lowercase: " + rawId
            );
        }
        return id;
    }

    private static Map<PrimalAspect, Integer> primalProjection(
            Map<String, Integer> source
    ) {
        EnumMap<PrimalAspect, Integer> result =
                new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            result.put(aspect, source.getOrDefault(aspect.id(), 0));
        }
        return result;
    }

    public record Snapshot(
            UUID nodeId,
            AuraNodeType type,
            AuraNodeModifier modifier,
            Map<PrimalAspect, Integer> current,
            Map<PrimalAspect, Integer> maximum,
            Map<String, Integer> aspectsCurrent,
            Map<String, Integer> aspectsMaximum,
            long revision
    ) {
        public Snapshot {
            nodeId = Objects.requireNonNull(nodeId, "nodeId");
            type = Objects.requireNonNull(type, "type");
            modifier = Objects.requireNonNull(modifier, "modifier");
            current = PrimalVis.exact(current, "current");
            maximum = PrimalVis.exact(maximum, "maximum");
            aspectsCurrent = Map.copyOf(
                    allAspectCopy(aspectsCurrent, "aspectsCurrent")
            );
            aspectsMaximum = Map.copyOf(
                    allAspectCopy(aspectsMaximum, "aspectsMaximum")
            );
            validateBounds(aspectsCurrent, aspectsMaximum);
            if (revision < 0L) {
                throw new IllegalArgumentException("revision cannot be negative");
            }
        }

        public AuraNodeState toState() {
            return AuraNodeState.withAspects(
                    nodeId,
                    type,
                    modifier,
                    aspectsCurrent,
                    aspectsMaximum,
                    revision
            );
        }
    }
}
