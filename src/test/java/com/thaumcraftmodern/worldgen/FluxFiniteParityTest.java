package com.thaumcraftmodern.worldgen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FluxFiniteParityTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );

    @Test
    void portsTc4EightQuantaFlowDirectionsAndTickRates()
            throws Exception {
        String flow = source(
                "com/thaumcraftmodern/world/block/FiniteFluxFlow.java"
        );
        String goo = source(
                "com/thaumcraftmodern/world/block/FluxGooBlock.java"
        );
        String gas = source(
                "com/thaumcraftmodern/world/block/FluxGasBlock.java"
        );
        assertTrue(flow.contains("QUANTA_PER_BLOCK = 8"));
        assertTrue(flow.contains("Direction.Plane.HORIZONTAL"));
        assertTrue(flow.contains("flowVertically("));
        assertTrue(goo.contains("DENSITY = 8"));
        assertTrue(goo.contains("FLOW_TICK_DELAY = 30"));
        assertTrue(goo.contains("Direction.DOWN"));
        assertTrue(gas.contains("DENSITY = -4"));
        assertTrue(gas.contains("FLOW_TICK_DELAY = 12"));
        assertTrue(gas.contains("Direction.UP"));
        assertFalse(gas.contains("random.nextInt(20)"));
    }

    @Test
    void allEightGooAndGasAmountsHaveDistinctHeights()
            throws Exception {
        JsonObject gooStates = json("blockstates/flux_goo.json");
        JsonObject gasStates = json("blockstates/flux_gas.json");
        assertEquals(8, gooStates.getAsJsonObject("variants").size());
        assertEquals(8, gasStates.getAsJsonObject("variants").size());
        for (int level = 0; level < 8; level++) {
            JsonObject gooModel = json(
                    "models/block/flux_goo_level_" + level + ".json"
            );
            JsonObject gasModel = json(
                    "models/block/flux_gas_level_" + level + ".json"
            );
            int gooHeight = gooModel.getAsJsonArray("elements")
                    .get(0).getAsJsonObject()
                    .getAsJsonArray("to").get(1).getAsInt();
            int gasBottom = gasModel.getAsJsonArray("elements")
                    .get(0).getAsJsonObject()
                    .getAsJsonArray("from").get(1).getAsInt();
            assertEquals((level + 1) * 2, gooHeight);
            assertEquals(14 - level * 2, gasBottom);
        }
    }

    @Test
    void gooUsesLiquidSurfaceWithoutSelectionOutlineAndAcceptsWater()
            throws Exception {
        String goo = source(
                "com/thaumcraftmodern/world/block/FluxGooBlock.java"
        );
        String fluids = source(
                "com/thaumcraftmodern/registry/ModFluids.java"
        );
        assertTrue(goo.contains("implements LiquidBlockContainer"));
        assertTrue(goo.contains("RenderShape.INVISIBLE"));
        assertTrue(goo.contains("public FluidState getFluidState"));
        assertTrue(goo.contains("public VoxelShape getShape"));
        assertTrue(goo.contains("fluid == Fluids.WATER"));
        assertTrue(fluids.contains("getStillTexture"));
        assertTrue(fluids.contains("getFlowingTexture"));
        assertTrue(fluids.contains("extends ForgeFlowingFluid.Flowing"));
    }

    @Test
    void gooDoesNotPushEntitiesButKeepsItsHorizontalSlowdown()
            throws Exception {
        String goo = source(
                "com/thaumcraftmodern/world/block/FluxGooBlock.java"
        );
        String fluids = source(
                "com/thaumcraftmodern/registry/ModFluids.java"
        );
        assertTrue(fluids.contains(".canPushEntity(false)"));
        assertTrue(goo.contains("multiply(drag, 1.0D, drag)"));
    }

    @Test
    void gasHasNoSelectionOrCollisionFrame() throws Exception {
        String gas = source(
                "com/thaumcraftmodern/world/block/FluxGasBlock.java"
        );
        assertTrue(gas.contains("public VoxelShape getShape"));
        assertTrue(gas.contains("public VoxelShape getCollisionShape"));
        assertEquals(2, occurrences(gas, "return Shapes.empty();"));
    }

    @Test
    void placedGooItemStartsAsOneQuantum() throws Exception {
        String item = source(
                "com/thaumcraftmodern/item/FluxGooBlockItem.java"
        );
        assertTrue(item.contains("setValue(FluxGooBlock.LEVEL, 0)"));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }

    private static JsonObject json(String relativePath) throws Exception {
        return JsonParser.parseString(
                Files.readString(ASSETS.resolve(relativePath))
        ).getAsJsonObject();
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        int from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }
}
