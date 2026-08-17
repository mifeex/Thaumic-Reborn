package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.PrimalAspect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NodeJarResearchRecipeTest {
    @Test
    void matchesClassicCompoundPageOrderAndCost() {
        NodeJarResearchRecipe.Snapshot recipe =
                NodeJarResearchRecipe.snapshot();

        assertEquals("thaumic_reborn:node_jar_capture",
                NodeJarResearchRecipe.ID.toString());
        assertEquals(3, recipe.width());
        assertEquals(4, recipe.height());
        assertEquals(3, recipe.depth());
        assertEquals(36, recipe.cells().size());
        assertEquals(
                List.of(
                        PrimalAspect.IGNIS,
                        PrimalAspect.TERRA,
                        PrimalAspect.AER,
                        PrimalAspect.AQUA,
                        PrimalAspect.ORDO,
                        PrimalAspect.PERDITIO
                ),
                recipe.costs().stream()
                        .map(NodeJarResearchRecipe.AspectCost::aspect)
                        .toList()
        );
        recipe.costs().forEach(cost ->
                assertEquals(NodeJarCost.BASE_PER_PRIMAL, cost.amount()));

        assertAllCells(
                recipe.cells().subList(0, 9),
                NodeJarStructure.CellKind.WOODEN_SLAB
        );
        assertAllCells(
                recipe.cells().subList(9, 22),
                NodeJarStructure.CellKind.GLASS
        );
        assertEquals(
                NodeJarStructure.CellKind.AURA_NODE,
                recipe.cells().get(22)
        );
        assertAllCells(
                recipe.cells().subList(23, 36),
                NodeJarStructure.CellKind.GLASS
        );
    }

    private static void assertAllCells(
            List<NodeJarStructure.CellKind> cells,
            NodeJarStructure.CellKind expected
    ) {
        cells.forEach(cell -> assertEquals(expected, cell));
    }
}
