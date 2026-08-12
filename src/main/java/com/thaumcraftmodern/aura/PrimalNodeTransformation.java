package com.thaumcraftmodern.aura;

import net.minecraft.util.RandomSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Exact server-side number rules from TC4 ItemEldritchObject#transformNode. */
public final class PrimalNodeTransformation {
    public static final int FLUX_ATTEMPTS = 33;
    public static final float EXPLOSION_BASE = 3.0F;
    public static final float RESEARCHED_EXPLOSION_RANGE = 3.0F;
    public static final float UNRESEARCHED_EXPLOSION_RANGE = 5.0F;

    private static final Set<String> PRIMALS = Set.of(
            "aer", "terra", "ignis", "aqua", "ordo", "perditio"
    );

    private PrimalNodeTransformation() {
    }

    public static Result transform(
            AuraNodeState.Snapshot source,
            RandomSource random,
            boolean researched
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(random, "random");
        LinkedHashMap<String, Integer> current =
                new LinkedHashMap<>(source.aspectsCurrent());
        LinkedHashMap<String, Integer> maximum =
                new LinkedHashMap<>(source.aspectsMaximum());

        for (String aspect : source.aspectsMaximum().keySet()) {
            int oldMaximum = maximum.get(aspect);
            if (PRIMALS.contains(aspect)) {
                int next = Math.max(
                        0,
                        oldMaximum - 2 + random.nextInt(researched ? 9 : 6)
                );
                maximum.put(aspect, next);
                current.put(aspect, Math.min(current.get(aspect), next));
            } else if (random.nextBoolean()) {
                int next = Math.max(0, oldMaximum - 1);
                maximum.put(aspect, next);
                current.put(aspect, Math.min(current.get(aspect), next));
            }
        }

        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            String id = aspect.id();
            int base = maximum.getOrDefault(id, 0);
            int roll = random.nextInt(researched ? 4 : 3);
            if (roll > 0 && roll > base) {
                maximum.put(id, roll);
                current.put(id, Math.min(
                        roll,
                        current.getOrDefault(id, 0) + 1
                ));
            }
        }

        AuraNodeModifier modifier = improveModifier(
                source.modifier(),
                random
        );
        float explosionRadius = EXPLOSION_BASE + random.nextFloat()
                * (researched
                ? RESEARCHED_EXPLOSION_RANGE
                : UNRESEARCHED_EXPLOSION_RANGE);
        return new Result(current, maximum, modifier, explosionRadius);
    }

    private static AuraNodeModifier improveModifier(
            AuraNodeModifier modifier,
            RandomSource random
    ) {
        return switch (modifier) {
            case FADING -> random.nextBoolean()
                    ? AuraNodeModifier.PALE : AuraNodeModifier.FADING;
            case PALE -> random.nextBoolean()
                    ? AuraNodeModifier.NORMAL : AuraNodeModifier.PALE;
            case NORMAL -> random.nextInt(5) == 0
                    ? AuraNodeModifier.BRIGHT : AuraNodeModifier.NORMAL;
            case BRIGHT -> AuraNodeModifier.BRIGHT;
        };
    }

    public record Result(
            Map<String, Integer> current,
            Map<String, Integer> maximum,
            AuraNodeModifier modifier,
            float explosionRadius
    ) {
        public Result {
            current = Map.copyOf(current);
            maximum = Map.copyOf(maximum);
            Objects.requireNonNull(modifier, "modifier");
        }
    }
}
