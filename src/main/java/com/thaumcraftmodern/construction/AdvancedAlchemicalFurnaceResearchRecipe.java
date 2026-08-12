package com.thaumcraftmodern.construction;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectCost;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Exact TC4 3x2x3 wand-construction page for the advanced furnace. */
public final class AdvancedAlchemicalFurnaceResearchRecipe {
    public static final ResourceLocation ID = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "advanced_alchemical_furnace_construct");

    private AdvancedAlchemicalFurnaceResearchRecipe() { }

    public static Snapshot snapshot() {
        return new Snapshot(3, 2, 3,
                List.of(new AspectCost("ignis", 50), new AspectCost("aqua", 50),
                        new AspectCost("ordo", 50)),
                List.of(
                        Cell.ARCANE_ALEMBIC, Cell.ALCHEMICAL_CONSTRUCT, Cell.ARCANE_ALEMBIC,
                        Cell.ALCHEMICAL_CONSTRUCT, Cell.EMPTY, Cell.ALCHEMICAL_CONSTRUCT,
                        Cell.ARCANE_ALEMBIC, Cell.ALCHEMICAL_CONSTRUCT, Cell.ARCANE_ALEMBIC,

                        Cell.ADVANCED_CONSTRUCT, Cell.ADVANCED_CONSTRUCT, Cell.ADVANCED_CONSTRUCT,
                        Cell.ADVANCED_CONSTRUCT, Cell.ALCHEMICAL_FURNACE, Cell.ADVANCED_CONSTRUCT,
                        Cell.ADVANCED_CONSTRUCT, Cell.ADVANCED_CONSTRUCT, Cell.ADVANCED_CONSTRUCT
                ));
    }

    public record Snapshot(int width, int height, int depth,
            List<AspectCost> costs, List<Cell> cells) {
        public Snapshot {
            costs = List.copyOf(costs);
            cells = List.copyOf(cells);
            if (cells.size() != width * height * depth)
                throw new IllegalArgumentException("invalid advanced furnace layout");
        }
    }

    public enum Cell {
        EMPTY, ADVANCED_CONSTRUCT, ALCHEMICAL_FURNACE,
        ARCANE_ALEMBIC, ALCHEMICAL_CONSTRUCT
    }
}
