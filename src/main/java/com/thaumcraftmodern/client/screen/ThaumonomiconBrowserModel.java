package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.research.ResearchCategoryDefinition;
import com.thaumcraftmodern.research.ResearchCategoryRegistry;
import com.thaumcraftmodern.research.ResearchDefinition;
import com.thaumcraftmodern.research.ResearchRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/** Immutable registry-derived data used by the Thaumonomicon browser. */
final class ThaumonomiconBrowserModel {
    private final List<ResearchDefinition> research;
    private final List<ResearchCategoryDefinition> categories;
    private final Map<String, List<ResearchDefinition>> researchByCategory;
    private final long researchRevision;
    private final long categoryRevision;

    private ThaumonomiconBrowserModel(
            List<ResearchDefinition> research,
            List<ResearchCategoryDefinition> categories,
            Map<String, List<ResearchDefinition>> researchByCategory,
            long researchRevision,
            long categoryRevision
    ) {
        this.research = research;
        this.categories = categories;
        this.researchByCategory = researchByCategory;
        this.researchRevision = researchRevision;
        this.categoryRevision = categoryRevision;
    }

    static ThaumonomiconBrowserModel create() {
        List<ResearchDefinition> research = ResearchRegistry.all();
        List<ResearchCategoryDefinition> categories =
                ResearchCategoryRegistry.all();
        Map<String, List<ResearchDefinition>> grouped = new LinkedHashMap<>();
        for (ResearchDefinition definition : research) {
            if (!definition.virtual()) {
                grouped.computeIfAbsent(
                        definition.categoryId(),
                        ignored -> new ArrayList<>()
                ).add(definition);
            }
        }
        Map<String, List<ResearchDefinition>> immutableGroups =
                new LinkedHashMap<>();
        grouped.forEach((category, definitions) -> immutableGroups.put(
                category,
                List.copyOf(definitions)
        ));
        return new ThaumonomiconBrowserModel(
                research,
                categories,
                Map.copyOf(immutableGroups),
                ResearchRegistry.revision(),
                ResearchCategoryRegistry.revision()
        );
    }

    boolean isCurrent() {
        return researchRevision == ResearchRegistry.revision()
                && categoryRevision == ResearchCategoryRegistry.revision();
    }

    List<ResearchDefinition> research() {
        return research;
    }

    List<ResearchCategoryDefinition> categories() {
        return categories;
    }

    Optional<ResearchCategoryDefinition> category(String id) {
        return categories.stream()
                .filter(category -> category.id().equals(id))
                .findFirst();
    }

    List<ResearchCategoryDefinition> visibleCategories(
            Predicate<ResearchDefinition> visible
    ) {
        return categories.stream()
                .filter(category -> researchByCategory
                        .getOrDefault(category.id(), List.of())
                        .stream()
                        .anyMatch(visible))
                .toList();
    }

    CategoryView categoryView(
            String categoryId,
            Predicate<ResearchDefinition> visible
    ) {
        List<ResearchDefinition> definitions = researchByCategory
                .getOrDefault(categoryId, List.of())
                .stream()
                .filter(visible)
                .toList();
        if (definitions.isEmpty()) {
            return CategoryView.EMPTY;
        }

        Map<String, ResearchDefinition> byId = new LinkedHashMap<>();
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (ResearchDefinition definition : definitions) {
            byId.put(definition.id(), definition);
            minX = Math.min(minX, definition.x());
            maxX = Math.max(maxX, definition.x());
            minY = Math.min(minY, definition.y());
            maxY = Math.max(maxY, definition.y());
        }

        List<Connection> connections = new ArrayList<>();
        Set<String> drawn = new HashSet<>();
        for (ResearchDefinition definition : definitions) {
            for (String parentId : definition.parents()) {
                addConnection(
                        definition,
                        byId.get(parentId),
                        false,
                        drawn,
                        connections
                );
            }
            for (String siblingId : definition.siblings()) {
                ResearchDefinition sibling = byId.get(siblingId);
                if (sibling == null
                        || sibling.parents().contains(definition.id())) {
                    continue;
                }
                addConnection(
                        definition,
                        sibling,
                        true,
                        drawn,
                        connections
                );
            }
        }
        return new CategoryView(
                definitions,
                Map.copyOf(byId),
                List.copyOf(connections),
                new Bounds(minX, maxX, minY, maxY)
        );
    }

    private static void addConnection(
            ResearchDefinition first,
            ResearchDefinition second,
            boolean sibling,
            Set<String> drawn,
            List<Connection> connections
    ) {
        if (second == null) {
            return;
        }
        String edge = first.id().compareTo(second.id()) < 0
                ? first.id() + "\u0000" + second.id()
                : second.id() + "\u0000" + first.id();
        if (drawn.add(edge)) {
            connections.add(new Connection(first, second, sibling));
        }
    }

    record CategoryView(
            List<ResearchDefinition> research,
            Map<String, ResearchDefinition> researchById,
            List<Connection> connections,
            Bounds bounds
    ) {
        private static final CategoryView EMPTY = new CategoryView(
                List.of(),
                Map.of(),
                List.of(),
                Bounds.EMPTY
        );
    }

    record Connection(
            ResearchDefinition first,
            ResearchDefinition second,
            boolean sibling
    ) {
    }

    record Bounds(int minX, int maxX, int minY, int maxY) {
        private static final Bounds EMPTY = new Bounds(0, 0, 0, 0);
    }
}
