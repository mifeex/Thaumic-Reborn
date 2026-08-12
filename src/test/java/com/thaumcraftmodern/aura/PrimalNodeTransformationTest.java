package com.thaumcraftmodern.aura;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimalNodeTransformationTest {
    @Test
    void researchedPearlUsesTc4BoundsAndKeepsStateValid() {
        LinkedHashMap<String, Integer> pools = new LinkedHashMap<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            pools.put(aspect.id(), 10);
        }
        pools.put("alienis", 5);
        AuraNodeState source = AuraNodeState.withAspects(
                UUID.randomUUID(),
                AuraNodeType.NORMAL,
                AuraNodeModifier.FADING,
                pools,
                pools,
                0L
        );

        for (int seed = 0; seed < 256; seed++) {
            PrimalNodeTransformation.Result result =
                    PrimalNodeTransformation.transform(
                            source.snapshot(),
                            RandomSource.create(seed),
                            true
                    );
            for (PrimalAspect aspect : PrimalAspect.ordered()) {
                int maximum = result.maximum().get(aspect.id());
                assertTrue(maximum >= 8 && maximum <= 16);
                assertTrue(result.current().get(aspect.id()) <= maximum);
            }
            assertTrue(result.maximum().get("alienis") == 4
                    || result.maximum().get("alienis") == 5);
            assertTrue(result.modifier() == AuraNodeModifier.FADING
                    || result.modifier() == AuraNodeModifier.PALE);
            assertTrue(result.explosionRadius() >= 3.0F
                    && result.explosionRadius() < 6.0F);
        }
    }

    @Test
    void researchImprovesEveryOriginalRandomBound() {
        assertEquals(33, PrimalNodeTransformation.FLUX_ATTEMPTS);
        assertEquals(3.0F, PrimalNodeTransformation.EXPLOSION_BASE);
        assertEquals(3.0F,
                PrimalNodeTransformation.RESEARCHED_EXPLOSION_RANGE);
        assertEquals(5.0F,
                PrimalNodeTransformation.UNRESEARCHED_EXPLOSION_RANGE);

        AuraNodeState source = AuraNodeState.withAspects(
                UUID.randomUUID(),
                AuraNodeType.NORMAL,
                AuraNodeModifier.BRIGHT,
                Map.of("alienis", 1),
                Map.of("alienis", 1),
                0L
        );
        boolean generatedPrimal = false;
        for (int seed = 0; seed < 100; seed++) {
            PrimalNodeTransformation.Result result =
                    PrimalNodeTransformation.transform(
                            source.snapshot(),
                            RandomSource.create(seed),
                            true
                    );
            generatedPrimal |= PrimalAspect.ordered().stream()
                    .anyMatch(aspect -> result.maximum()
                            .getOrDefault(aspect.id(), 0) > 0);
        }
        assertTrue(generatedPrimal);
    }

    @Test
    void pearlInteractionKeepsTc4ConsumptionExplosionAndFluxSideEffects()
            throws IOException {
        String pearl = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/PrimordialPearlItem.java"
        ));
        assertTrue(pearl.contains("hasCompletedResearch(\"primnode\")"));
        assertTrue(pearl.contains("getItemInHand().shrink(1)"));
        assertTrue(pearl.contains("serverLevel.explode("));
        assertTrue(pearl.contains("Level.ExplosionInteraction.BLOCK"));
        assertTrue(pearl.contains("PrimalNodeTransformation.FLUX_ATTEMPTS"));
        assertTrue(pearl.contains("FluxGooBlock.LEVEL, 7"));
        assertTrue(pearl.contains("FluxGasBlock.LEVEL, 7"));

        String registration = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModItems.java"
        ));
        assertTrue(registration.contains("new PrimordialPearlItem("));
        assertTrue(registration.contains(".stacksTo(1)"));
    }
}
