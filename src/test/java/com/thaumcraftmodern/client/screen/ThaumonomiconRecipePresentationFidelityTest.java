package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.research.InfusionDisplayDefinition;
import com.thaumcraftmodern.research.ResearchPageDefinition;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconRecipePresentationFidelityTest {
    private static final Path SCREEN = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconScreen.java"
    );
    private static final Path PAGE_RENDERER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconPageRenderer.java"
    );
    private static final Path BOOK_RENDERER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconOpenBookRenderer.java"
    );
    private static final Path OUTPUT_RENDERER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/"
                    + "ThaumonomiconRecipeOutputRenderer.java"
    );
    private static final Path OVERLAY = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/gui/"
                    + "gui_researchbook_overlay.png"
    );

    @Test
    void usesExactOriginalResearchBookOverlay() throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(OVERLAY));
        assertEquals(
                "40b5aeea15fd2003f579dc717accebb0348dafa6837789f51e80e80bfbf22c58",
                HexFormat.of().formatHex(digest)
        );
    }

    @Test
    void sharesOneOutputPresentationAcrossRecipeKinds() throws Exception {
        String screen = sources();
        assertEquals(
                3,
                occurrences(
                        screen,
                        "ThaumonomiconRecipeOutputRenderer.render("
                )
        );

        String renderer = Files.readString(OUTPUT_RENDERER);
        assertTrue(renderer.contains("static final int WIDTH = 112"));
        assertTrue(renderer.contains("static final int HEIGHT = 34"));
        assertTrue(renderer.contains("static final int ITEM_OFFSET_X = 48"));
        assertTrue(renderer.contains("static final int ITEM_OFFSET_Y = 8"));
        assertTrue(renderer.contains("graphics.renderItemDecorations("));
        assertFalse(renderer.contains("graphics.renderTooltip("));
        assertTrue(screen.contains("renderItemLinkTooltip("));
        assertTrue(screen.contains(
                "stack -> getTooltipFromItem(minecraft, stack)"));
        assertTrue(screen.contains(
                "tooltipProvider.apply(hovered.stack())"));
        assertFalse(screen.contains(
                "tooltip.add(hovered.stack().getHoverName())"));
        assertTrue(screen.contains("durabilityPreview(stack)"));
        assertTrue(screen.contains(
                "isDamageableTransformation(central, output)"));
        assertTrue(screen.contains(
                "central.isDamageableItem() && output.isDamageableItem()"));
        assertTrue(screen.contains(
                "central.getDamageValue(), output.getMaxDamage()"));
        assertTrue(screen.contains(
                "renderLinkedItemExact(graphics, central, centralX, centralY)"));
        assertTrue(screen.contains(
                "graphics.renderItemDecorations(font, displayed, x, y)"));
    }

    @Test
    void crucibleUsesOriginalCauldronAndArrowAtlasRegions()
            throws Exception {
        String screen = sources();
        assertTrue(screen.contains(
                "ThaumonomiconCrucibleRecipeLayout.CAULDRON_TOP"
        ));
        assertTrue(screen.contains(
                "ThaumonomiconCrucibleRecipeLayout.CONTENT_OFFSET_Y"
        ));
        assertTrue(screen.contains(
                "ThaumonomiconCrucibleRecipeLayout.ARROW_Y"
        ));
        assertTrue(screen.contains(
                "ThaumonomiconAspectCostRenderer.renderCrucibleGrid("
        ));
        assertTrue(screen.contains(
                ".sorted(Comparator.comparing(AspectCost::aspectId))"
        ));
    }

    @Test
    void nonGridTransformationsUseOriginalSmeltingArrow()
            throws Exception {
        String screen = sources();
        assertTrue(screen.contains("renderTransformationRecipe("));
        assertTrue(screen.contains(
                "ThaumonomiconTransformationRecipeLayout.OVERLAY_SOURCE_Y"
        ));
        assertTrue(screen.contains(
                "recipe instanceof AbstractCookingRecipe"
        ));
    }

    @Test
    void recipePagesCycleThroughEveryIngredientAlternative() throws Exception {
        String screen = sources();
        assertTrue(screen.contains("cyclingIngredient(ingredients.get(index), index)"));
        assertTrue(screen.contains("cyclingIngredient(recipe.catalyst(), 0)"));
        assertTrue(screen.contains("Util.getMillis() / 1_000L + slotIndex"));
        assertFalse(screen.contains("renderLinkedItem(graphics, options[0]"));
    }

    @Test
    void runicAugmentationPageCyclesTheFiveClassicHardeningPreviews() {
        InfusionDisplayDefinition display = new InfusionDisplayDefinition(
                "minecraft:iron_chestplate", "minecraft:iron_chestplate",
                List.of(
                        new InfusionDisplayDefinition.ComponentStack(
                                "minecraft:ender_pearl", 1),
                        new InfusionDisplayDefinition.ComponentStack(
                                "thaumic_reborn:salis_mundus", 1)),
                InfusionDisplayDefinition.Instability.MODERATE, "");
        ResearchPageDefinition page = new ResearchPageDefinition(
                ResearchPageDefinition.Type.INFUSION, "", "",
                "thaumic_reborn:runic_augmentation", List.of(), display);

        var first = RunicAugmentationPreview.atTime(
                page, display, 0L);
        var fifth = RunicAugmentationPreview.atTime(
                page, display, 4_000L);
        assertEquals(2, first.components().size());
        assertEquals(6, fifth.components().size());
        assertEquals(1, first.outputHardening());
        assertEquals(4, fifth.inputHardening());
        assertEquals(5, fifth.outputHardening());
        assertEquals(512, fifth.costs().stream()
                .filter(cost -> cost.aspectId().equals("potentia"))
                .findFirst().orElseThrow().amount());
        assertEquals(InfusionDisplayDefinition.Instability.HIGH,
                fifth.instability());
    }

    @Test
    void shapedRecipesKeepTheirCompactPatternWidth() throws Exception {
        String screen = sources();
        assertTrue(screen.contains("int recipeWidth = craftingRecipeWidth(recipe)"));
        assertTrue(screen.contains("recipe instanceof ShapedRecipe shaped"));
        assertTrue(screen.contains("recipe instanceof ArcaneShapedRecipe shaped"));
        assertTrue(screen.contains("index % recipeWidth"));
        assertTrue(screen.contains("index / recipeWidth"));
    }

    @Test
    void infusionLayoutUsesTheRequestedCompactVerticalSpacing() throws Exception {
        String screen = sources();
        assertTrue(screen.contains("INFUSION_OUTPUT_Y_OFFSET = 10"));
        assertTrue(screen.contains("INFUSION_MATRIX_Y_OFFSET = 22"));
        assertTrue(screen.contains("INFUSION_RECIPE_CONTENT_BOTTOM = 156"));
        assertTrue(screen.contains("INFUSION_SECTION_GAP = 5"));
        assertTrue(screen.contains("INFUSION_INSTABILITY_Y = 193"));
        assertTrue(screen.contains(
                "y + INFUSION_CENTRAL_Y + INFUSION_MATRIX_Y_OFFSET"
        ));
        assertTrue(screen.contains(
                "+ INFUSION_COMPONENT_CENTER_Y\n"
                        + "                    + INFUSION_MATRIX_Y_OFFSET"
        ));
        assertTrue(screen.contains(
                "aspectBottom + INFUSION_SECTION_GAP"
        ));
        assertTrue(screen.contains(
                "int bottomAlignedAspectTop = y"
        ));
        assertTrue(screen.contains(
                "int aspectTop = Math.max(minimumAspectTop, bottomAlignedAspectTop)"
        ));
        assertFalse(screen.contains(
                "Component.translatable(display.detailKey())"
        ));
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = text.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }

    private static String sources() throws Exception {
        return Files.readString(SCREEN)
                + Files.readString(PAGE_RENDERER)
                + Files.readString(BOOK_RENDERER);
    }
}
