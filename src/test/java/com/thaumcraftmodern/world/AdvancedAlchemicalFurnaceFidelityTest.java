package com.thaumcraftmodern.world;

import com.thaumcraftmodern.world.block.entity.AdvancedAlchemicalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.AdvancedAlchemicalFurnaceBlock;
import com.thaumcraftmodern.construction.AdvancedAlchemicalFurnaceResearchRecipe;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class AdvancedAlchemicalFurnaceFidelityTest {
    @Test void thaumonomiconShowsUpperRingAboveTheFurnaceBase() {
        var cells = AdvancedAlchemicalFurnaceResearchRecipe.snapshot().cells();
        assertEquals(AdvancedAlchemicalFurnaceResearchRecipe.Cell.ARCANE_ALEMBIC,
                cells.get(0));
        assertEquals(AdvancedAlchemicalFurnaceResearchRecipe.Cell.EMPTY, cells.get(4));
        assertEquals(AdvancedAlchemicalFurnaceResearchRecipe.Cell.ALCHEMICAL_FURNACE,
                cells.get(13));
    }

    @Test void tc4PowerCostsAndHeatAccelerationRemainExact() {
        assertTrue(AdvancedAlchemicalFurnaceBlockEntity.hasProcessingPower(25, 50, 25, 25));
        assertFalse(AdvancedAlchemicalFurnaceBlockEntity.hasProcessingPower(25, 49, 25, 25));
        assertFalse(AdvancedAlchemicalFurnaceBlockEntity.hasProcessingPower(25, 50, 24, 25));
        assertEquals(5, AdvancedAlchemicalFurnaceBlockEntity.processingDelayForHeat(500));
        assertEquals(55, AdvancedAlchemicalFurnaceBlockEntity.processingDelayForHeat(250));
        assertEquals(105, AdvancedAlchemicalFurnaceBlockEntity.processingDelayForHeat(0));
        assertEquals(48, AdvancedAlchemicalFurnaceBlockEntity.processingDelayForHeat(250, 1));
        assertEquals(41, AdvancedAlchemicalFurnaceBlockEntity.processingDelayForHeat(250, 2));
        assertEquals(34, AdvancedAlchemicalFurnaceBlockEntity.processingDelayForHeat(250, 3));
        assertEquals(27, AdvancedAlchemicalFurnaceBlockEntity.processingDelayForHeat(250, 4));
        assertEquals(13, AdvancedAlchemicalFurnaceBlockEntity.processingDelayForHeat(250, 6));
        assertEquals(13, AdvancedAlchemicalFurnaceBlockEntity.processingDelayForHeat(250, 8));
        assertEquals(0, AdvancedAlchemicalFurnaceBlockEntity.heatLight(100));
        assertEquals(12, AdvancedAlchemicalFurnaceBlockEntity.heatLight(500));
    }

    @Test void ambientBoilingPopIsThrottledWithoutMutingProcessing() {
        assertEquals(200, AdvancedAlchemicalFurnaceBlock.AMBIENT_POP_CHANCE);
    }

    @Test void thaumicBasesBellowsMustFaceTheMultiblockAndRemainUnpowered()
            throws Exception {
        String furnace = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/AdvancedAlchemicalFurnaceBlockEntity.java"));
        assertTrue(furnace.contains("processed = processingDelayForHeat(heat, attachedBellows())"));
        assertTrue(furnace.contains("state.getValue(ArcaneBellowsBlock.FACING)"));
        assertTrue(furnace.contains("== outward.getOpposite()"));
        assertTrue(furnace.contains("!level.hasNeighborSignal(candidate)"));
        assertTrue(furnace.contains("found.size() == Direction.values().length"));
    }

    @Test void rendererSeparatesContinuousWorkFromStoredEssentiaPresentation()
            throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/AdvancedAlchemicalFurnaceBlockEntityRenderer.java"));
        String furnace = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/AdvancedAlchemicalFurnaceBlockEntity.java"));
        assertTrue(renderer.contains("getTextureAtlas(\n                TextureAtlas.LOCATION_BLOCKS)"));
        assertTrue(renderer.contains("TextureAtlasSprite flux = atlasSprite(FLUX)"));
        assertTrue(renderer.contains("TextureAtlasSprite fire = atlasSprite(FIRE)"));
        assertFalse(renderer.contains("textures/block/flux_goo.png"));
        assertFalse(renderer.contains("textures/block/fire_0.png"));
        assertTrue(renderer.contains("boolean working = furnace.isProcessing()"));
        assertTrue(renderer.contains(
                "boolean containsEssentia = furnace.essentiaAmount() > 0"));
        assertTrue(renderer.contains("working ? BASE_ON : BASE"));
        assertTrue(renderer.contains("containsEssentia ? TANK_ON : TANK"));
        assertTrue(renderer.contains("if (containsEssentia)\n            renderVis"));
        assertTrue(renderer.contains("if (working)\n            renderHeat"));
        assertTrue(furnace.contains("processed > 0 || furnace.hasProcessableInput(level)"));
        assertTrue(furnace.contains("return source != null && source.working"));
    }

    @Test void arcaneBellowsUseTheOriginalAsymmetricClientPumpCycle()
            throws Exception {
        String bellows = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/ArcaneBellowsBlockEntity.java"));
        String block = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/ArcaneBellowsBlock.java"));
        assertTrue(bellows.contains("inflation -= 0.075F"));
        assertTrue(bellows.contains("inflation += 0.025F"));
        assertTrue(bellows.contains("level.hasNeighborSignal(position)"));
        assertTrue(bellows.contains("SoundEvents.GHAST_SHOOT"));
        assertTrue(block.contains("ArcaneBellowsBlockEntity::clientTick"));
    }

    @Test void packagedObjAndFourStateTexturesAreUnmodifiedTc4Assets() throws Exception {
        Path modern = Path.of("src/main/resources/assets/thaumic_reborn/textures/models");
        Path original = Path.of("reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft/textures/models");
        for (String name : new String[]{"adv_alch_furnace.obj", "alch_furnace.png",
                "alch_furnace_on.png", "alch_furnace_tank.png", "alch_furnace_tank_on.png"}) {
            assertArrayEquals(Files.readAllBytes(original.resolve(name)),
                    Files.readAllBytes(modern.resolve(name)), name);
        }
    }
}
