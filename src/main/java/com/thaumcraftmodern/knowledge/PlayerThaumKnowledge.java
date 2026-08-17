package com.thaumcraftmodern.knowledge;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * Server-owned player knowledge state. Runtime capability code can delegate
 * persistence and copy semantics to this class.
 */
public final class PlayerThaumKnowledge {
    public static final int SERIAL_VERSION = 5;
    public static final int STARTING_PRIMAL_AMOUNT = 5;

    private static final String VERSION_KEY = "version";
    private static final String KNOWN_ASPECTS_KEY = "known_aspects";
    private static final String ASPECT_AMOUNTS_KEY = "aspect_amounts";
    private static final String ASPECT_ID_KEY = "id";
    private static final String ASPECT_AMOUNT_KEY = "amount";
    private static final String SCANS_KEY = "scans";
    private static final String REVEALED_RESEARCH_KEY = "revealed_research";
    private static final String COMPLETED_RESEARCH_KEY = "completed_research";
    private static final String RESEARCH_CRITERIA_KEY = "research_criteria";
    private static final String WARP_KEY = "warp";
    private static final String WARP_PERMANENT_KEY = "permanent";
    private static final String WARP_NORMAL_KEY = "normal";
    private static final String WARP_TEMPORARY_KEY = "temporary";
    private static final String WARP_COUNTER_KEY = "counter";
    private static final String RUNIC_CHARGE_KEY = "runic_charge";

    private static final Set<String> STARTING_PRIMAL_ASPECTS = orderedReadOnlySet(
            "aer",
            "terra",
            "ignis",
            "aqua",
            "ordo",
            "perditio");

    private final LinkedHashSet<String> knownAspects;
    private final LinkedHashMap<String, Integer> aspectAmounts;
    private final LinkedHashSet<String> scans;
    private final LinkedHashSet<String> revealedResearch;
    private final LinkedHashSet<String> completedResearch;
    private final LinkedHashSet<String> researchCriteria;
    private int permanentWarp;
    private int normalWarp;
    private int temporaryWarp;
    private int warpCounter;
    private int runicCharge;

    public PlayerThaumKnowledge() {
        this(
                STARTING_PRIMAL_ASPECTS,
                startingPrimalAmounts(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                0,
                0,
                0,
                0,
                0
        );
    }

    private PlayerThaumKnowledge(
            Set<String> knownAspects,
            Map<String, Integer> aspectAmounts,
            Set<String> scans,
            Set<String> revealedResearch,
            Set<String> completedResearch,
            Set<String> researchCriteria,
            int permanentWarp,
            int normalWarp,
            int temporaryWarp,
            int warpCounter,
            int runicCharge) {
        this.knownAspects = new LinkedHashSet<>(knownAspects);
        this.knownAspects.addAll(STARTING_PRIMAL_ASPECTS);
        this.aspectAmounts = new LinkedHashMap<>();
        aspectAmounts.forEach((aspectId, amount) ->
                this.aspectAmounts.put(
                        requireStableId(aspectId, "aspectId"),
                        requireNonNegative(amount, "aspect amount")
                )
        );
        this.knownAspects.forEach(aspectId -> this.aspectAmounts.putIfAbsent(aspectId, 0));
        STARTING_PRIMAL_ASPECTS.forEach(aspectId ->
                this.aspectAmounts.putIfAbsent(aspectId, STARTING_PRIMAL_AMOUNT)
        );
        this.scans = new LinkedHashSet<>(scans);
        this.revealedResearch = new LinkedHashSet<>(revealedResearch);
        this.completedResearch = new LinkedHashSet<>(completedResearch);
        this.revealedResearch.addAll(this.completedResearch);
        this.researchCriteria = new LinkedHashSet<>(researchCriteria);
        this.permanentWarp = requireNonNegative(permanentWarp, "permanent warp");
        this.normalWarp = requireNonNegative(normalWarp, "normal warp");
        this.temporaryWarp = requireNonNegative(temporaryWarp, "temporary warp");
        this.warpCounter = requireNonNegative(warpCounter, "warp counter");
        this.runicCharge = requireNonNegative(runicCharge, "runic charge");
    }

    public static Set<String> startingPrimalAspects() {
        return STARTING_PRIMAL_ASPECTS;
    }

    public boolean knowsAspect(String aspectId) {
        return aspectId != null && knownAspects.contains(aspectId);
    }

    public boolean learnAspect(String aspectId) {
        String validatedId = requireStableId(aspectId, "aspectId");
        boolean learned = knownAspects.add(validatedId);
        aspectAmounts.putIfAbsent(validatedId, 0);
        return learned;
    }

    public boolean addAspectPoints(String aspectId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("aspect amount must be positive");
        }
        String validatedId = requireStableId(aspectId, "aspectId");
        boolean newlyDiscovered = learnAspect(validatedId);
        aspectAmounts.compute(validatedId, (ignored, current) ->
                Math.addExact(current == null ? 0 : current, amount)
        );
        return newlyDiscovered;
    }

    public int aspectAmount(String aspectId) {
        return aspectId == null ? 0 : aspectAmounts.getOrDefault(aspectId, 0);
    }

    public boolean tryConsumeAspect(String aspectId) {
        String validatedId = requireStableId(aspectId, "aspectId");
        int current = aspectAmount(validatedId);
        if (current < 1) {
            return false;
        }
        aspectAmounts.put(validatedId, current - 1);
        return true;
    }

    public boolean tryConsumeAspects(String firstAspectId, String secondAspectId) {
        String first = requireStableId(firstAspectId, "firstAspectId");
        String second = requireStableId(secondAspectId, "secondAspectId");
        int requiredFirst = first.equals(second) ? 2 : 1;
        if (aspectAmount(first) < requiredFirst || (!first.equals(second) && aspectAmount(second) < 1)) {
            return false;
        }
        aspectAmounts.put(first, aspectAmount(first) - requiredFirst);
        if (!first.equals(second)) {
            aspectAmounts.put(second, aspectAmount(second) - 1);
        }
        return true;
    }

    public boolean hasScan(String scanId) {
        return scanId != null && scans.contains(scanId);
    }

    public boolean recordScan(String scanId) {
        return scans.add(requireStableId(scanId, "scanId"));
    }

    public boolean hasRevealedResearch(String researchId) {
        return researchId != null && revealedResearch.contains(researchId);
    }

    public boolean revealResearch(String researchId) {
        return revealedResearch.add(requireStableId(researchId, "researchId"));
    }

    public boolean hasCompletedResearch(String researchId) {
        return researchId != null && completedResearch.contains(researchId);
    }

    public boolean completeResearch(String researchId) {
        String validatedId = requireStableId(researchId, "researchId");
        revealedResearch.add(validatedId);
        return completedResearch.add(validatedId);
    }

    public boolean hasResearchCriterion(String criterionId) {
        return criterionId != null && researchCriteria.contains(criterionId);
    }

    /**
     * Records a monotonic custom gameplay criterion. Other systems may use
     * namespaced ids such as {@code thaumic_reborn:entered_outer_lands}.
     */
    public boolean recordResearchCriterion(String criterionId) {
        return researchCriteria.add(requireStableId(criterionId, "criterionId"));
    }

    public int warp(WarpType type) {
        return switch (Objects.requireNonNull(type, "type")) {
            case PERMANENT -> permanentWarp;
            case NORMAL -> normalWarp;
            case TEMPORARY -> temporaryWarp;
        };
    }

    /**
     * Classic research gates ignore temporary warp.
     */
    public int nonTemporaryWarp() {
        return Math.addExact(permanentWarp, normalWarp);
    }

    public int totalWarp() {
        return Math.addExact(nonTemporaryWarp(), temporaryWarp);
    }

    public int warpCounter() {
        return warpCounter;
    }

    public int runicCharge() { return runicCharge; }
    public int setRunicCharge(int amount) {
        runicCharge = requireNonNegative(amount, "runic charge");
        return runicCharge;
    }

    public int setWarpCounter(int amount) {
        warpCounter = requireNonNegative(amount, "warp counter");
        return warpCounter;
    }

    public int addWarp(WarpType type, int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("warp amount must be positive");
        }
        int result = setWarp(type, Math.addExact(warp(type), amount));
        warpCounter = totalWarp();
        return result;
    }

    public int setWarp(WarpType type, int amount) {
        int validated = requireNonNegative(amount, "warp amount");
        switch (Objects.requireNonNull(type, "type")) {
            case PERMANENT -> permanentWarp = validated;
            case NORMAL -> normalWarp = validated;
            case TEMPORARY -> temporaryWarp = validated;
        }
        return validated;
    }

    /**
     * Applies data-driven research that should be known without player action.
     *
     * @return {@code true} when at least one research entry was newly completed
     */
    public boolean applyAutomaticResearchUnlocks(Collection<String> researchIds) {
        Objects.requireNonNull(researchIds, "researchIds");
        boolean changed = false;
        for (String researchId : researchIds) {
            changed |= completeResearch(researchId);
        }
        return changed;
    }

    public Set<String> knownAspects() {
        return readOnlyCopy(knownAspects);
    }

    public Map<String, Integer> aspectAmounts() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(aspectAmounts));
    }

    public Set<String> scans() {
        return readOnlyCopy(scans);
    }

    public Set<String> revealedResearch() {
        return readOnlyCopy(revealedResearch);
    }

    public Set<String> completedResearch() {
        return readOnlyCopy(completedResearch);
    }

    public Set<String> researchCriteria() {
        return readOnlyCopy(researchCriteria);
    }

    public PlayerThaumKnowledge copy() {
        return new PlayerThaumKnowledge(
                knownAspects,
                aspectAmounts,
                scans,
                revealedResearch,
                completedResearch,
                researchCriteria,
                permanentWarp,
                normalWarp,
                temporaryWarp,
                warpCounter,
                runicCharge
        );
    }

    public void copyFrom(PlayerThaumKnowledge source) {
        Objects.requireNonNull(source, "source");
        if (source == this) {
            return;
        }
        knownAspects.clear();
        knownAspects.addAll(source.knownAspects);
        knownAspects.addAll(STARTING_PRIMAL_ASPECTS);
        aspectAmounts.clear();
        aspectAmounts.putAll(source.aspectAmounts);
        knownAspects.forEach(aspectId -> aspectAmounts.putIfAbsent(aspectId, 0));
        scans.clear();
        scans.addAll(source.scans);
        revealedResearch.clear();
        revealedResearch.addAll(source.revealedResearch);
        completedResearch.clear();
        completedResearch.addAll(source.completedResearch);
        revealedResearch.addAll(completedResearch);
        researchCriteria.clear();
        researchCriteria.addAll(source.researchCriteria);
        permanentWarp = source.permanentWarp;
        normalWarp = source.normalWarp;
        temporaryWarp = source.temporaryWarp;
        warpCounter = source.warpCounter;
        runicCharge = source.runicCharge;
    }

    public CompoundTag serialize() {
        CompoundTag result = new CompoundTag();
        result.putInt(VERSION_KEY, SERIAL_VERSION);
        result.put(KNOWN_ASPECTS_KEY, serializeStrings(knownAspects));
        result.put(ASPECT_AMOUNTS_KEY, serializeAspectAmounts(aspectAmounts));
        result.put(SCANS_KEY, serializeStrings(scans));
        result.put(REVEALED_RESEARCH_KEY, serializeStrings(revealedResearch));
        result.put(COMPLETED_RESEARCH_KEY, serializeStrings(completedResearch));
        result.put(RESEARCH_CRITERIA_KEY, serializeStrings(researchCriteria));
        CompoundTag warp = new CompoundTag();
        warp.putInt(WARP_PERMANENT_KEY, permanentWarp);
        warp.putInt(WARP_NORMAL_KEY, normalWarp);
        warp.putInt(WARP_TEMPORARY_KEY, temporaryWarp);
        warp.putInt(WARP_COUNTER_KEY, warpCounter);
        result.put(WARP_KEY, warp);
        result.putInt(RUNIC_CHARGE_KEY, runicCharge);
        return result;
    }

    public static PlayerThaumKnowledge deserialize(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        int version = tag.getInt(VERSION_KEY);
        if (version < 1 || version > SERIAL_VERSION) {
            throw new IllegalArgumentException(
                    "unsupported PlayerThaumKnowledge version " + version
                            + "; expected 1 through " + SERIAL_VERSION);
        }

        Set<String> knownAspects = deserializeStrings(tag, KNOWN_ASPECTS_KEY);
        Map<String, Integer> aspectAmounts = version == 1
                ? migratedAspectAmounts(knownAspects)
                : deserializeAspectAmounts(tag);
        Set<String> completedResearch = deserializeStrings(
                tag,
                COMPLETED_RESEARCH_KEY
        );
        Set<String> revealedResearch = version >= 3
                ? deserializeStrings(tag, REVEALED_RESEARCH_KEY)
                : completedResearch;
        Set<String> researchCriteria = version >= 3
                ? deserializeStrings(tag, RESEARCH_CRITERIA_KEY)
                : Set.of();
        CompoundTag warp = version >= 3 ? tag.getCompound(WARP_KEY) : new CompoundTag();
        return new PlayerThaumKnowledge(
                knownAspects,
                aspectAmounts,
                deserializeStrings(tag, SCANS_KEY),
                revealedResearch,
                completedResearch,
                researchCriteria,
                warp.getInt(WARP_PERMANENT_KEY),
                warp.getInt(WARP_NORMAL_KEY),
                warp.getInt(WARP_TEMPORARY_KEY),
                version >= 4
                        ? warp.getInt(WARP_COUNTER_KEY)
                        : warp.getInt(WARP_PERMANENT_KEY)
                                + warp.getInt(WARP_NORMAL_KEY)
                                + warp.getInt(WARP_TEMPORARY_KEY),
                version >= 5 ? tag.getInt(RUNIC_CHARGE_KEY) : 0);
    }

    public CompoundTag serializeNBT() {
        return serialize();
    }

    public void deserializeNBT(CompoundTag tag) {
        copyFrom(deserialize(tag));
    }

    private static ListTag serializeStrings(Set<String> values) {
        ListTag result = new ListTag();
        values.stream()
                .sorted()
                .map(StringTag::valueOf)
                .forEach(result::add);
        return result;
    }

    private static Set<String> deserializeStrings(CompoundTag tag, String key) {
        ListTag serialized = tag.getList(key, Tag.TAG_STRING);
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (int index = 0; index < serialized.size(); index++) {
            values.add(requireStableId(serialized.getString(index), key));
        }
        return values;
    }

    private static ListTag serializeAspectAmounts(Map<String, Integer> amounts) {
        ListTag result = new ListTag();
        amounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    CompoundTag amount = new CompoundTag();
                    amount.putString(ASPECT_ID_KEY, entry.getKey());
                    amount.putInt(ASPECT_AMOUNT_KEY, entry.getValue());
                    result.add(amount);
                });
        return result;
    }

    private static Map<String, Integer> deserializeAspectAmounts(CompoundTag tag) {
        LinkedHashMap<String, Integer> amounts = new LinkedHashMap<>();
        ListTag serialized = tag.getList(ASPECT_AMOUNTS_KEY, Tag.TAG_COMPOUND);
        for (Tag raw : serialized) {
            CompoundTag amount = (CompoundTag) raw;
            amounts.put(
                    requireStableId(amount.getString(ASPECT_ID_KEY), ASPECT_ID_KEY),
                    requireNonNegative(amount.getInt(ASPECT_AMOUNT_KEY), ASPECT_AMOUNT_KEY)
            );
        }
        return amounts;
    }

    private static Map<String, Integer> migratedAspectAmounts(Set<String> knownAspects) {
        LinkedHashMap<String, Integer> amounts = new LinkedHashMap<>();
        knownAspects.forEach(aspectId -> amounts.put(aspectId, 0));
        STARTING_PRIMAL_ASPECTS.forEach(aspectId ->
                amounts.put(aspectId, STARTING_PRIMAL_AMOUNT)
        );
        return amounts;
    }

    private static Map<String, Integer> startingPrimalAmounts() {
        LinkedHashMap<String, Integer> amounts = new LinkedHashMap<>();
        STARTING_PRIMAL_ASPECTS.forEach(aspectId ->
                amounts.put(aspectId, STARTING_PRIMAL_AMOUNT)
        );
        return amounts;
    }

    private static String requireStableId(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank() || !value.equals(value.trim())) {
            throw new IllegalArgumentException(fieldName + " must be non-blank and trimmed");
        }
        return value;
    }

    private static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    private static Set<String> readOnlyCopy(Set<String> source) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(source));
    }

    private static Set<String> orderedReadOnlySet(String... values) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Collections.addAll(result, values);
        return Collections.unmodifiableSet(result);
    }
}
