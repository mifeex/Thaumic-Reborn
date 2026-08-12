package com.thaumcraftmodern.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ResearchCategoryRegistry {
    private static volatile Snapshot snapshot = new Snapshot(
            Map.of(),
            List.of(),
            0L
    );

    private ResearchCategoryRegistry() {
    }

    public static synchronized void replace(Collection<ResearchCategoryDefinition> values) {
        Map<String, ResearchCategoryDefinition> next = new LinkedHashMap<>();
        values.stream()
                .sorted(Comparator
                        .comparingInt(ResearchCategoryDefinition::order)
                        .thenComparing(ResearchCategoryDefinition::id))
                .forEach(definition -> {
                    if (next.put(definition.id(), definition) != null) {
                        throw new IllegalArgumentException(
                                "Duplicate research category: " + definition.id()
                        );
                    }
                });
        Snapshot previous = snapshot;
        snapshot = new Snapshot(
                java.util.Collections.unmodifiableMap(next),
                List.copyOf(next.values()),
                previous.revision() + 1L
        );
    }

    public static Optional<ResearchCategoryDefinition> find(String id) {
        return Optional.ofNullable(snapshot.definitions().get(id));
    }

    public static List<ResearchCategoryDefinition> all() {
        return snapshot.orderedDefinitions();
    }

    public static long revision() {
        return snapshot.revision();
    }

    public static CompoundTag serialize() {
        CompoundTag root = new CompoundTag();
        ListTag entries = new ListTag();
        for (ResearchCategoryDefinition definition : all()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", definition.id());
            entry.putString("title", definition.titleKey());
            entry.putString("icon", definition.iconItem());
            entry.putString("iconResource", definition.iconResource());
            entry.putString("background", definition.backgroundTexture());
            entry.putInt("order", definition.order());
            entries.add(entry);
        }
        root.put("entries", entries);
        return root;
    }

    public static List<ResearchCategoryDefinition> deserialize(CompoundTag root) {
        List<ResearchCategoryDefinition> result = new ArrayList<>();
        for (Tag raw : root.getList("entries", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            result.add(new ResearchCategoryDefinition(
                    entry.getString("id"),
                    entry.getString("title"),
                    entry.getString("icon"),
                    entry.getString("iconResource"),
                    entry.getString("background"),
                    entry.getInt("order")
            ));
        }
        return result;
    }

    private record Snapshot(
            Map<String, ResearchCategoryDefinition> definitions,
            List<ResearchCategoryDefinition> orderedDefinitions,
            long revision
    ) {
    }
}
