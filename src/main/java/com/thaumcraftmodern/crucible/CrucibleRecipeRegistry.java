package com.thaumcraftmodern.crucible;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class CrucibleRecipeRegistry {
    private static volatile Snapshot snapshot = new Snapshot(
            List.of(),
            Map.of(),
            0L
    );

    private CrucibleRecipeRegistry() {
    }

    public static synchronized void replace(
            Collection<CrucibleRecipeDefinition> definitions
    ) {
        List<CrucibleRecipeDefinition> recipes = List.copyOf(definitions);
        Map<ResourceLocation, CrucibleRecipeDefinition> recipesById =
                new LinkedHashMap<>();
        for (CrucibleRecipeDefinition recipe : recipes) {
            // Preserve all()'s historical first-match behavior if a malformed
            // external definition collection contains a duplicate ID.
            recipesById.putIfAbsent(recipe.id(), recipe);
        }
        Snapshot previous = snapshot;
        snapshot = new Snapshot(
                recipes,
                Collections.unmodifiableMap(recipesById),
                previous.revision() + 1L
        );
    }

    public static List<CrucibleRecipeDefinition> all() {
        return snapshot.recipes();
    }

    public static Optional<CrucibleRecipeDefinition> find(
            ResourceLocation id
    ) {
        return Optional.ofNullable(snapshot.recipesById().get(id));
    }

    /** Changes exactly once for every datapack replacement. */
    public static long revision() {
        return snapshot.revision();
    }

    public static Optional<CrucibleRecipeDefinition> findMatching(
            ItemStack catalyst,
            EssentiaStore essentia,
            Predicate<String> knowsResearch
    ) {
        return snapshot.recipes().stream()
                .filter(recipe -> recipe.research().isBlank()
                        || knowsResearch.test(recipe.research()))
                .filter(recipe -> recipe.matchesCatalyst(catalyst))
                .filter(recipe -> essentia.contains(recipe.aspects()))
                // TC4 selects the matching recipe with the greatest number
                // of distinct required aspects. This resolves shared
                // catalysts such as glowstone (Nitor vs duplication).
                .sorted(Comparator.comparingInt(
                        (CrucibleRecipeDefinition recipe) ->
                                recipe.aspects().size()
                ).reversed())
                .findFirst();
    }

    private record Snapshot(
            List<CrucibleRecipeDefinition> recipes,
            Map<ResourceLocation, CrucibleRecipeDefinition> recipesById,
            long revision
    ) {
    }
}
