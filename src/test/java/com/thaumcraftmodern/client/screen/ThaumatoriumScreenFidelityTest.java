package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumatoriumScreenFidelityTest {
    @Test
    void progressFitsTheOriginalTwelveByFourPixelMaskInsideTheFrame() {
        assertEquals(0, ThaumatoriumScreen.progressWidth(0, 3));
        assertEquals(4, ThaumatoriumScreen.progressWidth(1, 3));
        assertEquals(8, ThaumatoriumScreen.progressWidth(2, 3));
        assertEquals(12, ThaumatoriumScreen.progressWidth(3, 3));
        assertEquals(12, ThaumatoriumScreen.progressWidth(9, 3));
    }

    @Test
    void visibleAspectStripUsesTheOriginalLeftEdge() {
        assertEquals(40, ThaumatoriumScreen.aspectStartX(1));
        assertEquals(40, ThaumatoriumScreen.aspectStartX(2));
        assertEquals(40, ThaumatoriumScreen.aspectStartX(3));
        assertEquals(40, ThaumatoriumScreen.aspectStartX(6));
    }

    @Test
    void aspectArrowsKeepACompleteSixAspectWindow() {
        assertEquals(0, ThaumatoriumScreen.maxAspectStart(6));
        assertEquals(1, ThaumatoriumScreen.maxAspectStart(7));
        assertEquals(2, ThaumatoriumScreen.maxAspectStart(8));
        assertEquals(6, ThaumatoriumScreen.maxAspectStart(12));
    }

    @Test
    void recipeCounterShowsCurrentVariantAmongCatalystOutputs() {
        assertEquals("1/7", ThaumatoriumScreen.recipeCounterText(0, 7));
        assertEquals("4/7", ThaumatoriumScreen.recipeCounterText(3, 7));
        assertEquals("7/7", ThaumatoriumScreen.recipeCounterText(99, 7));
        assertEquals("", ThaumatoriumScreen.recipeCounterText(0, 0));
    }

    @Test
    void serverSynchronizesEveryAvailableVariantAndSelectionState()
            throws IOException {
        String menu = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/menu/ThaumatoriumMenu.java"
        ));
        String packet = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/network/packet/ThaumatoriumRecipeSyncPacket.java"
        ));
        String screen = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ThaumatoriumScreen.java"
        ));
        String machine = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/ThaumatoriumBlockEntity.java"
        ));
        assertTrue(menu.contains("machine.availableRecipes(player)"));
        assertTrue(menu.contains("new ThaumatoriumRecipeSyncPacket("));
        assertTrue(menu.contains("clientRecipes = List.copyOf(recipes)"));
        assertTrue(menu.contains("clientCraftableRecipes = List.copyOf(craftableRecipes)"));
        assertTrue(menu.contains("clientFormulae = List.copyOf(formulae)"));
        assertTrue(packet.contains("List<CrucibleRecipeDefinition> recipes"));
        assertTrue(packet.contains("List<ResourceLocation> craftableRecipes"));
        assertTrue(packet.contains("recipe.aspects().forEach"));
        assertFalse(machine.contains("hasRevealedResearch(recipe.research())"));
        assertTrue(machine.contains("hasCompletedResearch(recipe.research())"));
        assertTrue(menu.contains("if (!craftable(recipe) || !reservedFits(recipe))"));
        assertTrue(screen.contains(
                "lastRecipeRevision != menu.recipeRevision()"));
        assertTrue(machine.contains(
                "reservedFitsRecipe(reserved.view(), recipe.aspects())"));
        assertTrue(machine.contains("ResourceLocation replaced = replaceableFormula()"));
        assertTrue(menu.contains(
                "successful ? ModSounds.HH_ON.get() : ModSounds.HH_OFF.get()"));
        assertTrue(menu.contains("playSelectionResultSound(serverPlayer, successful)"));
        assertTrue(menu.contains("public boolean canSelectOrSwitch("));
        assertTrue(screen.contains("!menu.canSelectOrSwitch(recipe)"));
    }

    @Test
    void selectionIndicatorAndAspectWidgetsUseOriginalTextureRegions()
            throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ThaumatoriumScreen.java"
        ));
        assertTrue(source.contains("drawSelectionState(graphics, recipe, mouseX, mouseY)"));
        assertTrue(source.contains("hoveringOutput || menu.selected(recipe)"));
        assertTrue(source.contains("leftPos + 104"));
        assertTrue(source.contains("topPos + 8"));
        assertTrue(source.contains("176,"));
        assertTrue(source.contains("96,"));
        assertTrue(source.contains("48,"));
        assertTrue(source.contains("leftPos + 88, topPos + 16"));
        assertTrue(source.contains("176, 56, 24, 24"));
        assertTrue(source.contains("RECIPE_ARROW_X = 128"));
        assertTrue(source.contains("RECIPE_PREVIOUS_Y = 16"));
        assertTrue(source.contains("RECIPE_NEXT_Y = 24"));
        assertTrue(source.contains("RECIPE_ARROW_WIDTH = 16"));
        assertTrue(source.contains("RECIPE_ARROW_HEIGHT = 8"));
        assertTrue(source.contains(
                "RECIPE_COUNTER_RIGHT_X = RECIPE_ARROW_X + 10"));
        assertTrue(source.contains(
                "graphics.drawString(font, text, -font.width(text), 0"));
        assertTrue(source.contains("index > 0 ? 192 : 176, 16"));
        assertTrue(source.contains("index < recipeCount - 1 ? 192 : 176, 24"));
        assertTrue(source.contains("leftPos + 32, topPos + 40"));
        assertTrue(source.contains("startAspect > 0 ? 192 : 176"));
        assertTrue(source.contains("leftPos + 136, topPos + 40"));
        assertTrue(source.contains("? 200 : 184"));
        assertTrue(source.contains("inside(mouseX, mouseY, 32, 40, 8, 16)"));
        assertTrue(source.contains("inside(mouseX, mouseY, 136, 40, 8, 16)"));
        assertTrue(source.contains("graphics.blit(TEXTURE, x + 1, topPos + 57"));
        assertTrue(source.contains("176, 8, 14, 6"));
        assertTrue(source.contains("ASPECT_PROGRESS_X_OFFSET = 2"));
        assertTrue(source.contains("ASPECT_PROGRESS_Y_OFFSET = 58"));
        assertTrue(source.contains("ASPECT_PROGRESS_WIDTH = 12"));
        assertTrue(source.contains("ASPECT_PROGRESS_HEIGHT = 4"));
        assertTrue(source.contains("ClassicUiRender.drawAspectTag("));
        assertTrue(source.contains("entry.getValue()"));
        assertTrue(source.contains("graphics.blit(\n"
                + "                            TEXTURE,"));
        assertTrue(source.contains("                            176,\n"
                + "                            0,\n"
                + "                            fill,"));
        assertTrue(source.contains("menu.reservedAmount(entry.getKey())"));
        assertTrue(source.contains("drawRecipeCount(graphics, recipes.size())"));
        assertTrue(source.contains("recipeCounterText(index, recipeCount)"));
    }
}
