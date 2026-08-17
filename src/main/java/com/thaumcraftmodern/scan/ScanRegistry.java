package com.thaumcraftmodern.scan;

import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.item.EtherealEssenceItem;
import com.thaumcraftmodern.item.ManaBeanItem;

public final class ScanRegistry {
    private static volatile Map<String, ScanDefinition> definitions = Map.of();
    private static volatile Map<String, ScanDefinition> explicitDefinitions = Map.of();
    private static final Map<String, Optional<ScanDefinition>> AUTOMATIC_DEFINITIONS =
            new ConcurrentHashMap<>();
    private static final Map<String, String> BLOCK_SCAN_ALIASES = Map.of(
            "thaumic_reborn:deepslate_air_infused_stone",
            "thaumic_reborn:air_infused_stone",
            "thaumic_reborn:deepslate_fire_infused_stone",
            "thaumic_reborn:fire_infused_stone",
            "thaumic_reborn:deepslate_water_infused_stone",
            "thaumic_reborn:water_infused_stone",
            "thaumic_reborn:deepslate_earth_infused_stone",
            "thaumic_reborn:earth_infused_stone",
            "thaumic_reborn:deepslate_order_infused_stone",
            "thaumic_reborn:order_infused_stone",
            "thaumic_reborn:deepslate_entropy_infused_stone",
            "thaumic_reborn:entropy_infused_stone"
    );

    private ScanRegistry() {
    }

    public static synchronized void replace(Collection<ScanDefinition> values) {
        Map<String, ScanDefinition> next = new LinkedHashMap<>();
        values.stream()
                .sorted(Comparator.comparing(ScanDefinition::scanKey))
                .forEach(definition -> {
                    if (next.put(definition.scanKey(), definition) != null) {
                        throw new IllegalArgumentException(
                                "Duplicate scan definition: " + definition.scanKey()
                        );
                    }
                });
        definitions = Map.copyOf(next);
        explicitDefinitions = definitions;
        AUTOMATIC_DEFINITIONS.clear();
    }

    /** Replaces only the runtime recipe-derived layer; datapack definitions win. */
    public static synchronized void replaceGenerated(
            Collection<ScanDefinition> generated
    ) {
        Map<String, ScanDefinition> next = new LinkedHashMap<>(explicitDefinitions);
        generated.stream()
                .sorted(Comparator.comparing(ScanDefinition::scanKey))
                .forEach(definition -> next.putIfAbsent(
                        definition.scanKey(), definition));
        definitions = Map.copyOf(next);
        AUTOMATIC_DEFINITIONS.clear();
    }

    public static Optional<ScanDefinition> find(ScanTargetType type, String targetId) {
        return find(
                type,
                targetId,
                ThaumcraftModernServerConfig.automaticScanFallback(),
                AutomaticScanDefinitionFactory::create
        );
    }

    static Optional<ScanDefinition> find(
            ScanTargetType type,
            String targetId,
            boolean allowAutomaticFallback,
            BiFunction<ScanTargetType, String, Optional<ScanDefinition>> automaticFactory
    ) {
        String key = scanKey(type, targetId);
        ScanDefinition explicit = definitions.get(key);
        if (explicit != null) {
            return Optional.of(explicit);
        }
        Optional<ScanDefinition> tagged = findTagDefinition(type, targetId);
        if (tagged.isPresent()) {
            return tagged;
        }
        if (!allowAutomaticFallback) {
            return Optional.empty();
        }
        return AUTOMATIC_DEFINITIONS.computeIfAbsent(
                key,
                ignored -> automaticFactory.apply(type, targetId)
        );
    }

    /**
     * Resolves a definition for a scan key that is already present in saved
     * player knowledge. The compatibility config controls creation of new
     * inferred scans, not interpretation of scans completed while it was
     * enabled.
     */
    public static Optional<ScanDefinition> findByScanKey(String scanKey) {
        if (scanKey == null) {
            return Optional.empty();
        }
        int separator = scanKey.indexOf(':');
        if (separator <= 0 || separator >= scanKey.length() - 1) {
            return Optional.empty();
        }
        try {
            ScanTargetType type = ScanTargetType.valueOf(
                    scanKey.substring(0, separator).toUpperCase(Locale.ROOT)
            );
            return findHistorical(type, scanKey.substring(separator + 1));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<ScanDefinition> findHistorical(
            ScanTargetType type,
            String targetId
    ) {
        return find(
                type,
                targetId,
                true,
                AutomaticScanDefinitionFactory::create
        );
    }

    public static ItemScanIdentity identityForItem(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        ScanTargetType type;
        String targetId;
        if (findExplicit(ScanTargetType.ITEM, itemId).isPresent()
                || findTagDefinition(ScanTargetType.ITEM, itemId).isPresent()) {
            type = ScanTargetType.ITEM;
            targetId = itemId;
        } else if (stack.getItem() instanceof BlockItem blockItem) {
            type = ScanTargetType.BLOCK;
            targetId = canonicalBlockId(
                    BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).toString()
            );
        } else {
            type = ScanTargetType.ITEM;
            targetId = itemId;
        }
        String baseKnowledgeKey = knowledgeKey(type, targetId);
        String itemKnowledgeKey = baseKnowledgeKey;
        if (stack.getItem() instanceof EtherealEssenceItem) {
            itemKnowledgeKey = etherealEssenceKnowledgeKey(
                    baseKnowledgeKey,
                    EtherealEssenceItem.aspectId(stack)
            );
        }
        return new ItemScanIdentity(type, targetId, itemKnowledgeKey);
    }

    static String etherealEssenceKnowledgeKey(
            String baseKnowledgeKey,
            Optional<String> aspectId
    ) {
        return aspectId
                .map(aspect -> baseKnowledgeKey + "#aspect=" + aspect)
                .orElse(baseKnowledgeKey);
    }

    public static Optional<ScanDefinition> findForItem(ItemStack stack) {
        ItemScanIdentity identity = identityForItem(stack);
        return find(identity.type(), identity.targetId())
                .map(definition -> withStoredEssence(definition, stack));
    }

    /**
     * Resolves only datapack-defined aspects for an item. Unlike
     * {@link #findForItem(ItemStack)}, this never creates an automatically
     * inferred definition. It is used when the aspects have gameplay value,
     * such as dissolving an item in a Crucible.
     */
    public static Optional<ScanDefinition> findExplicitForItem(
            ItemStack stack
    ) {
        ItemScanIdentity identity = identityForItem(stack);
        return findExplicit(identity.type(), identity.targetId())
                .or(() -> findTagDefinition(
                        identity.type(),
                        identity.targetId()
                ))
                .map(definition -> withStoredEssence(definition, stack));
    }

    /**
     * TC4 adds IEssentiaContainerItem contents as bonus object tags. Ethereal
     * essence therefore exposes both its registered Auram and its NBT aspect.
     */
    static ScanDefinition withStoredEssence(
            ScanDefinition definition,
            ItemStack stack
    ) {
        if (stack.getItem() instanceof EtherealEssenceItem) {
            return withStoredAspect(
                    definition,
                    EtherealEssenceItem.aspectId(stack),
                    EtherealEssenceItem.amount(stack)
            );
        }
        if (stack.getItem() instanceof ManaBeanItem) {
            return withStoredManaBeanAspect(
                    definition,
                    ManaBeanItem.aspect(stack)
            );
        }
        return definition;
    }

    /** TC4 mana beans are essentia containers holding one NBT-authored aspect. */
    static ScanDefinition withStoredManaBeanAspect(
            ScanDefinition definition,
            Optional<String> storedAspect
    ) {
        return withStoredAspect(definition, storedAspect, 1);
    }

    static ScanDefinition withStoredAspect(
            ScanDefinition definition,
            Optional<String> storedAspect,
            int storedAmount
    ) {
        if (storedAspect.isEmpty() || storedAmount <= 0) {
            return definition;
        }
        LinkedHashMap<String, Integer> merged = new LinkedHashMap<>();
        for (AspectReward reward : definition.aspects()) {
            merged.merge(reward.aspectId(), reward.amount(), Math::addExact);
        }
        merged.merge(storedAspect.get(), storedAmount, Math::addExact);
        List<AspectReward> aspects = merged.entrySet().stream()
                .map(entry -> new AspectReward(entry.getKey(), entry.getValue()))
                .toList();
        return new ScanDefinition(
                definition.type(),
                definition.targetId(),
                definition.displayKey(),
                aspects,
                definition.knowledgeKey()
        );
    }

    public static List<ScanDefinition> all() {
        return List.copyOf(definitions.values());
    }

    private static Optional<ScanDefinition> findExplicit(
            ScanTargetType type,
            String targetId
    ) {
        return Optional.ofNullable(definitions.get(scanKey(type, targetId)));
    }

    private static Optional<ScanDefinition> findTagDefinition(
            ScanTargetType requestedType,
            String targetId
    ) {
        ScanTargetType tagType = requestedType == ScanTargetType.ITEM
                ? ScanTargetType.ITEM_TAG
                : requestedType == ScanTargetType.BLOCK
                ? ScanTargetType.BLOCK_TAG
                : null;
        if (tagType == null || definitions.values().stream()
                .noneMatch(definition -> definition.type() == tagType)) {
            return Optional.empty();
        }
        ResourceLocation id = ResourceLocation.tryParse(targetId);
        if (id == null) {
            return Optional.empty();
        }
        if (requestedType == ScanTargetType.ITEM) {
            return BuiltInRegistries.ITEM.getOptional(id).flatMap(item ->
                    definitions.values().stream()
                            .filter(definition ->
                                    definition.type() == ScanTargetType.ITEM_TAG)
                            .filter(definition -> item.builtInRegistryHolder().is(
                                    TagKey.create(
                                            Registries.ITEM,
                                            new ResourceLocation(
                                                    definition.targetId()
                                            )
                                    )
                            ))
                            .min(tagDefinitionComparator(definition ->
                                    BuiltInRegistries.ITEM.getTag(TagKey.create(
                                            Registries.ITEM,
                                            new ResourceLocation(definition.targetId())
                                    )).map(tag -> tag.size()).orElse(Integer.MAX_VALUE)
                            ))
            );
        }
        if (requestedType == ScanTargetType.BLOCK) {
            return BuiltInRegistries.BLOCK.getOptional(id).flatMap(block ->
                    definitions.values().stream()
                            .filter(definition ->
                                    definition.type() == ScanTargetType.BLOCK_TAG)
                            .filter(definition -> block.builtInRegistryHolder().is(
                                    TagKey.create(
                                            Registries.BLOCK,
                                            new ResourceLocation(
                                                    definition.targetId()
                                            )
                                    )
                            ))
                            .min(tagDefinitionComparator(definition ->
                                    BuiltInRegistries.BLOCK.getTag(TagKey.create(
                                            Registries.BLOCK,
                                            new ResourceLocation(definition.targetId())
                                    )).map(tag -> tag.size()).orElse(Integer.MAX_VALUE)
                            ))
            );
        }
        return Optional.empty();
    }

    static Comparator<ScanDefinition> tagDefinitionComparator(
            ToIntFunction<ScanDefinition> tagSize
    ) {
        return Comparator.comparingInt(tagSize)
                .thenComparing(ScanDefinition::scanKey);
    }

    public static String scanKey(ScanTargetType type, String targetId) {
        return type.name().toLowerCase(Locale.ROOT) + ":" + targetId;
    }

    /** Returns the shared player-knowledge key selected by a direct or tag scan. */
    public static String knowledgeKey(ScanTargetType type, String targetId) {
        return find(type, targetId)
                .map(definition -> resolvedKnowledgeKey(definition, type, targetId))
                .orElseGet(() -> scanKey(type, targetId));
    }

    static String resolvedKnowledgeKey(
            ScanDefinition definition,
            ScanTargetType resolvedType,
            String resolvedTargetId
    ) {
        return usesImplicitTagKnowledgeKey(definition)
                ? scanKey(resolvedType, resolvedTargetId)
                : definition.knowledgeKey();
    }

    /**
     * Tags share aspect definitions, not discovery progress, unless the JSON
     * explicitly provides a knowledge_key. This preserves TC4's behaviour for
     * OreDictionary-style material families.
     */
    private static boolean usesImplicitTagKnowledgeKey(ScanDefinition definition) {
        return (definition.type() == ScanTargetType.BLOCK_TAG
                || definition.type() == ScanTargetType.ITEM_TAG)
                && definition.knowledgeKey().equals(definition.scanKey());
    }

    /**
     * Compatibility aliases share one scan definition and one player-knowledge
     * key. Deepslate infused stone differs only in host rock, not in essentia.
     */
    public static String canonicalBlockId(String blockId) {
        return BLOCK_SCAN_ALIASES.getOrDefault(blockId, blockId);
    }

    public static CompoundTag serialize() {
        CompoundTag root = new CompoundTag();
        ListTag entries = new ListTag();
        for (ScanDefinition definition : all()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("type", definition.type().name());
            entry.putString("target", definition.targetId());
            entry.putString("display", definition.displayKey());
            entry.putString("knowledge_key", definition.knowledgeKey());
            ListTag aspects = new ListTag();
            for (AspectReward reward : definition.aspects()) {
                CompoundTag aspect = new CompoundTag();
                aspect.putString("id", reward.aspectId());
                aspect.putInt("amount", reward.amount());
                aspects.add(aspect);
            }
            entry.put("aspects", aspects);
            entries.add(entry);
        }
        root.put("entries", entries);
        return root;
    }

    public static List<ScanDefinition> deserialize(CompoundTag root) {
        List<ScanDefinition> result = new ArrayList<>();
        for (Tag raw : root.getList("entries", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            List<AspectReward> aspects = new ArrayList<>();
            for (Tag rawAspect : entry.getList("aspects", Tag.TAG_COMPOUND)) {
                CompoundTag aspect = (CompoundTag) rawAspect;
                aspects.add(new AspectReward(
                        aspect.getString("id"),
                        aspect.getInt("amount")
                ));
            }
            result.add(new ScanDefinition(
                    ScanTargetType.valueOf(entry.getString("type")),
                    entry.getString("target"),
                    entry.getString("display"),
                    aspects,
                    entry.contains("knowledge_key", Tag.TAG_STRING)
                            ? entry.getString("knowledge_key")
                            : null
            ));
        }
        return result;
    }

    public record ItemScanIdentity(
            ScanTargetType type,
            String targetId,
            String knowledgeKey
    ) {
    }
}
