package com.thaumcraftmodern.integration.api;

import com.thaumicreborn.api.focus.FocusBehavior;
import com.thaumicreborn.api.focus.FocusDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Runtime owner for addon focus definitions registered through the stable API. */
public final class AddonFocusRegistry {
    private static final Map<ResourceLocation, Entry> ENTRIES = new ConcurrentHashMap<>();

    private AddonFocusRegistry() { }

    public static void register(FocusDefinition definition, FocusBehavior behavior) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(behavior, "behavior");
        if (definition.id().getNamespace().equals("thaumic_reborn")) {
            throw new IllegalArgumentException("The thaumic_reborn focus namespace is reserved");
        }
        Entry previous = ENTRIES.putIfAbsent(definition.id(), new Entry(definition, behavior));
        if (previous != null) {
            throw new IllegalStateException("Focus already registered: " + definition.id());
        }
    }

    public static Optional<Entry> find(ResourceLocation id) {
        return Optional.ofNullable(ENTRIES.get(id));
    }

    public static List<FocusDefinition> all() {
        return ENTRIES.values().stream().map(Entry::definition)
                .sorted(Comparator.comparing(value -> value.id().toString())).toList();
    }

    public record Entry(FocusDefinition definition, FocusBehavior behavior) { }
}
