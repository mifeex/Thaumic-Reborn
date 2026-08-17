package com.thaumcraftmodern.deconstruction;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.aspect.AspectDefinition;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DeconstructionTableLogicTest {
    private static final AspectCatalog CATALOG = new AspectCatalog(List.of(
            primal("aer"),
            primal("terra"),
            primal("ignis"),
            primal("ordo"),
            compound("lux", "aer", "ignis"),
            compound("motus", "aer", "ordo"),
            compound("volatus", "aer", "motus")
    ));

    @Test
    void recursivelyReducesEveryCompoundAmountToPrimals() {
        assertEquals(
                Map.of("aer", 8, "ordo", 3, "ignis", 2, "terra", 1),
                DeconstructionTableLogic.reduceToPrimals(
                        Map.of("volatus", 3, "lux", 2, "terra", 1),
                        CATALOG
                )
        );
    }

    @Test
    void usesOriginalStrictEightyPointChanceBoundary() {
        assertTrue(DeconstructionTableLogic.rollDiscovery(
                Map.of("terra", 79), CATALOG, bound -> 79
        ).isEmpty());
        AtomicInteger calls = new AtomicInteger();
        assertEquals("terra", DeconstructionTableLogic.rollDiscovery(
                Map.of("terra", 80),
                CATALOG,
                bound -> calls.getAndIncrement() == 0 ? 79 : 0
        ).orElseThrow());
    }

    @Test
    void choosesUniformlyBetweenDistinctPrimalsInsteadOfWeightingAmounts() {
        AtomicInteger call = new AtomicInteger();
        LinkedHashMap<String, Integer> aspects = new LinkedHashMap<>();
        aspects.put("aer", 79);
        aspects.put("terra", 1);
        String selected = DeconstructionTableLogic.rollDiscovery(
                aspects,
                CATALOG,
                bound -> call.getAndIncrement() == 0 ? 0 : 1
        ).orElseThrow();
        assertEquals("terra", selected);
    }

    private static AspectDefinition primal(String id) {
        return new AspectDefinition(id, 0, "thaumic_reborn:" + id);
    }

    private static AspectDefinition compound(
            String id,
            String first,
            String second
    ) {
        return new AspectDefinition(
                id,
                0,
                "thaumic_reborn:" + id,
                first,
                second
        );
    }
}
