package com.thaumcraftmodern.world.block.entity;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumatoriumRuntimeStateFidelityTest {
    @Test
    void suctionUsesTheSameAlphabeticalAspectOrderAsTc4() {
        Map<String, Integer> required = Map.of(
                "potentia", 3,
                "ignis", 3,
                "perditio", 3);

        assertEquals("ignis", ThaumatoriumBlockEntity.nextRequiredAspect(
                required, Map.of()));
        assertEquals("perditio", ThaumatoriumBlockEntity.nextRequiredAspect(
                required, Map.of("ignis", 3)));
        assertEquals("potentia", ThaumatoriumBlockEntity.nextRequiredAspect(
                required, Map.of("ignis", 3, "perditio", 3)));
        assertNull(ThaumatoriumBlockEntity.nextRequiredAspect(
                required, Map.of("ignis", 3, "perditio", 3, "potentia", 3)));
    }

    @Test
    void allMissingRecipeAspectsRemainEligibleForAvailableSupply() {
        Map<String, Integer> required = Map.of(
                "potentia", 3,
                "ignis", 3,
                "perditio", 3);

        assertEquals(
                java.util.List.of("ignis", "perditio", "potentia"),
                ThaumatoriumBlockEntity.missingAspects(required, Map.of()));
        assertEquals(
                java.util.List.of("ignis", "potentia"),
                ThaumatoriumBlockEntity.missingAspects(
                        required, Map.of("perditio", 3)));
    }

    @Test
    void recipeSwitchKeepsOnlyEssentiaAcceptedByTheNewRecipe() {
        assertTrue(ThaumatoriumBlockEntity.reservedFitsRecipe(
                Map.of(), Map.of("ignis", 4)));
        assertTrue(ThaumatoriumBlockEntity.reservedFitsRecipe(
                Map.of("ignis", 2), Map.of("ignis", 4, "ordo", 1)));
        org.junit.jupiter.api.Assertions.assertFalse(
                ThaumatoriumBlockEntity.reservedFitsRecipe(
                        Map.of("aqua", 1), Map.of("ignis", 4)));
        org.junit.jupiter.api.Assertions.assertFalse(
                ThaumatoriumBlockEntity.reservedFitsRecipe(
                        Map.of("ignis", 5), Map.of("ignis", 4)));
    }

    @Test
    void suctionIsReleasedWhileInactiveAndWhenNoSupplyIsAvailable()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "ThaumatoriumBlockEntity.java"));

        assertTrue(source.contains(
                "if (level.hasNeighborSignal(pos) || machine.catalyst.isEmpty()"));
        assertTrue(source.contains(
                "|| !machine.hasHeat(level)) {\n"
                        + "            machine.currentSuction = null;"));
        assertTrue(source.contains(
                "machine.currentSuction = machine.findAvailableAspect(level, missing);"));
        assertTrue(source.contains(
                "if (machine.currentSuction != null) machine.fill(level);"));
    }

    @Test
    void cancelledCraftRefundPrefersTheAspectRequestedByThePipeNetwork() {
        Map<String, Integer> stored = Map.of(
                "potentia", 5,
                "ignis", 3);

        assertEquals("potentia",
                ThaumatoriumBlockEntity.refundableAspect(stored, "potentia"));
        assertEquals("ignis",
                ThaumatoriumBlockEntity.refundableAspect(stored, null));
        assertNull(ThaumatoriumBlockEntity.refundableAspect(Map.of(), "potentia"));
    }

    @Test
    void blankLegacyRecipeIdsDoNotBecomeMinecraftRootRecipes() {
        assertNull(ThaumatoriumBlockEntity.storedRecipeId(""));
        assertNull(ThaumatoriumBlockEntity.storedRecipeId("minecraft:"));
        assertEquals(
                ResourceLocation.fromNamespaceAndPath(
                        "thaumic_reborn", "alumentum"),
                ThaumatoriumBlockEntity.storedRecipeId(
                        "thaumic_reborn:alumentum"));
    }

    @Test
    void panelRecipeHasIndependentPersistentState() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "ThaumatoriumBlockEntity.java"));

        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("tag.putString(\"DisplayRecipe\""));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("displayedRecipe = storedRecipeId("));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("displayedRecipe = id;"));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("if (id.equals(displayedRecipe)) displayedRecipe = null;"));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("if (level != null && level.isClientSide)"));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("selectedRecipe = null; displayedRecipe = null;"));
    }

    @Test
    void formulaSelectionIsRetainedOnlyForTheSameCatalyst() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "ThaumatoriumBlockEntity.java"));

        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("private ItemStack formulaCatalyst = ItemStack.EMPTY;"));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("ItemStack.isSameItemSameTags(previous, current)"));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("clearFormulaeForDifferentCatalyst(catalyst);"));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("formulae.clear();"));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("formulaOwners.clear();"));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("tag.put(\"FormulaCatalyst\""));
        org.junit.jupiter.api.Assertions.assertTrue(
                source.contains("formulaCatalyst = ItemStack.of("));
    }
}
