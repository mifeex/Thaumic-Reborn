package com.thaumcraftmodern.world.block.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.client.render.InfernalFurnaceBakedModel;
import com.thaumcraftmodern.construction.InfernalFurnaceResearchRecipe;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

final class InfernalFurnaceFidelityTest {
    @Test
    void originalInventoryTimingSuctionAndBoostConstantsArePreserved() {
        assertEquals(32, InfernalFurnaceBlockEntity.INVENTORY_SIZE);
        assertEquals(140, InfernalFurnaceBlockEntity.NORMAL_COOK_TIME);
        assertEquals(80, InfernalFurnaceBlockEntity.SPEEDY_COOK_TIME);
        assertEquals(20, InfernalFurnaceBlockEntity.BELLOWS_REDUCTION);
        assertEquals(600, InfernalFurnaceBlockEntity.ESSENTIA_SPEED_TICKS);
        assertEquals(128, InfernalFurnaceBlockEntity.ESSENTIA_SUCTION);
    }

    @Test
    void compoundPageUsesOriginalNetherBrickObsidianLavaAndBarsLayout() {
        var recipe = InfernalFurnaceResearchRecipe.snapshot();
        assertEquals(List.of(3, 3, 3),
                List.of(recipe.width(), recipe.height(), recipe.depth()));
        assertEquals(27, recipe.cells().size());
        assertEquals(InfernalFurnaceResearchRecipe.Cell.EMPTY,
                recipe.cells().get(4));
        assertEquals(InfernalFurnaceResearchRecipe.Cell.LAVA,
                recipe.cells().get(13));
        assertEquals(InfernalFurnaceResearchRecipe.Cell.IRON_BARS,
                recipe.cells().get(14));
        assertEquals(12, recipe.cells().stream().filter(cell ->
                cell == InfernalFurnaceResearchRecipe.Cell.NETHER_BRICKS).count());
    }

    @Test
    void dynamicModelKeepsClassicTextureAddressing() {
        assertEquals(2, InfernalFurnaceBakedModel.textureForSide(
                1, 0, -1, Direction.NORTH));
        assertEquals(11, InfernalFurnaceBakedModel.textureForSide(
                1, 9, -1, Direction.NORTH));
        assertEquals(20, InfernalFurnaceBakedModel.textureForSide(
                1, 18, -1, Direction.NORTH));
        assertEquals(16, InfernalFurnaceBakedModel.textureForSide(
                2, 18, -1, Direction.UP));
        assertEquals(6, InfernalFurnaceBakedModel.textureForSide(
                5, 9, Direction.UP.get3DDataValue(), Direction.UP));
    }

    @Test
    void coreAndNozzleKeepOriginalLavaAndTransparentLayerContracts()
            throws Exception {
        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/InfernalFurnaceBakedModel.java"));
        String client = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/WorldContentClientEvents.java"));
        assertTrue(model.contains("\"minecraft\", \"block/lava_still\""));
        int fire = model.indexOf("addNozzleFace(quads, outward, 11, 12");
        int opening = model.indexOf("addNozzleFace(quads, outward, 12, 13");
        int grate = model.indexOf("addNozzleFace(quads, outward, 14, 15");
        assertTrue(fire >= 0 && fire < opening && opening < grate,
                "the animated face must remain recessed behind the iron grate");
        assertTrue(model.contains("sprite(13), true"));
        assertTrue(model.contains("block/fire_0\")), false"));
        assertTrue(model.contains("sprite(15), true"));
        assertTrue(client.contains("ModBlocks.INFERNAL_FURNACE.get(),"));
    }

    @Test
    void nozzleAnimationAndSmeltingSoundsKeepOriginalContracts()
            throws Exception {
        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/InfernalFurnaceBakedModel.java"));
        String furnace = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/InfernalFurnaceBlockEntity.java"));
        assertTrue(model.contains("block/fire_0"),
                "the original nozzle animation is its vanilla animated fire layer");
        assertTrue(furnace.contains("for (int i = 0; i < 5; i++)"));
        assertTrue(furnace.contains("0.1F + level.random.nextFloat() * 0.1F"));
        assertTrue(furnace.contains("0.9F + level.random.nextFloat() * 0.15F"));
        assertTrue(furnace.contains("SoundEvents.FIRE_EXTINGUISH"),
                "invalid inputs retain the original high-pitched extinguish sound");
    }

    @Test
    void arcaneBellowsAreAvailableInTheCreativeTab() throws Exception {
        String creativeTab = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"));
        assertTrue(creativeTab.contains(".get(\"arcane_bellows\").get()"));
    }

    @Test
    void lavaCoreUsesVanillaAmbientSoundAndParticleTiming() throws Exception {
        String block = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/InfernalFurnaceBlock.java"));
        assertTrue(block.contains("random.nextInt(100) == 0"));
        assertTrue(block.contains("ParticleTypes.LAVA"));
        assertTrue(block.contains("SoundEvents.LAVA_POP"));
        assertTrue(block.contains("random.nextInt(200) == 0"));
        assertTrue(block.contains("SoundEvents.LAVA_AMBIENT"));
    }

    @Test
    void researchIsActiveAndUsesTheExecutableCompoundPage() throws Exception {
        JsonObject research = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumcraftmodern/thaumcraft/research/legacy/infernalfurnace.json"),
                StandardCharsets.UTF_8)).getAsJsonObject();
        assertFalse(research.get("inactive").getAsBoolean());
        assertEquals("thaumcraftmodern:textures/misc/r_infernalfurnace.png",
                research.get("icon_resource").getAsString());
        assertEquals("compound_crafting", research.getAsJsonArray("pages")
                .get(1).getAsJsonObject().get("type").getAsString());
    }

    @Test
    void everyPackagedFurnaceTextureIsByteExactOriginal() throws Exception {
        Path jar = Path.of("reference/original/Thaumcraft_1.7.10_4.2.3.5.jar");
        try (ZipFile original = new ZipFile(jar.toFile())) {
            for (int index : List.of(0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11,
                    12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 25, 26)) {
                byte[] expected = original.getInputStream(original.getEntry(
                        "assets/thaumcraft/textures/blocks/furnace" + index + ".png"))
                        .readAllBytes();
                byte[] actual = Files.readAllBytes(Path.of(
                        "build/resources/main/assets/thaumcraftmodern/textures/block/furnace"
                                + index + ".png"));
                assertEquals(sha(expected), sha(actual), "furnace" + index);
            }
        }
    }

    private static String sha(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
