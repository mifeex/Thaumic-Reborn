package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class PrimordialPearlConsumptionFidelityTest {
    private static final Path ORIGINAL = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main");

    @Test
    void advancedFurnaceLoreDeclaresThePearlReusable() throws Exception {
        String language = Files.readString(ORIGINAL.resolve(
                "resources/assets/thaumcraft/lang/en_us.lang"));

        assertTrue(language.contains(
                "tc.research_page.ADVALCHEMYFURNACE.2="));
        assertTrue(language.contains(
                "The pearl is not consumed during the crafting process."));
    }

    @Test
    void gridCraftingReturnsOneUnchangedPearl() throws Exception {
        String pearl = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/PrimordialPearlItem.java"));

        assertTrue(pearl.contains("hasCraftingRemainingItem(ItemStack stack)"));
        assertTrue(pearl.contains("ItemStack remainder = stack.copy()"));
        assertTrue(pearl.contains("remainder.setCount(1)"));
        assertTrue(pearl.contains("return remainder"));
    }

    @Test
    void infusionOverridesTheCraftingRemainderAndConsumesThePearl()
            throws Exception {
        String matrix = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/RunicMatrixBlockEntity.java"));

        assertTrue(matrix.contains("ItemStack remainder = infusionRemainder(stack)"));
        assertTrue(matrix.contains("stack.getItem() instanceof PrimordialPearlItem"));
        assertTrue(matrix.contains("return ItemStack.EMPTY"));
        assertTrue(matrix.contains(
                "center.setInfusionItem(recipe.createResult(center.item()))"));
    }

    @Test
    void originalPearlIsFiniteButHasNoDurabilityCharges() throws Exception {
        String original = Files.readString(ORIGINAL.resolve(
                "java/thaumcraft/common/items/ItemEldritchObject.java"));

        assertTrue(original.contains("this.setMaxDamage(0)"));
        assertTrue(original.contains("stack.shrink(1)"));
    }
}
