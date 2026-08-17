package com.thaumcraftmodern.warding;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PavingStoneOfWardingFidelityTest {
    private static final Path ORIGINAL_ASSETS = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft"
    );
    private static final Path MOD_ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );

    @Test
    void baseTextureIsTheUnmodifiedTc4Texture() throws Exception {
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL_ASSETS.resolve(
                        "textures/blocks/paving_stone_warding.png"
                )),
                Files.readAllBytes(MOD_ASSETS.resolve(
                        "textures/block/paving_stone_warding.png"
                ))
        );
    }

    @Test
    void runeSpritesAreExactTc4AtlasCrops() throws Exception {
        BufferedImage atlas = ImageIO.read(ORIGINAL_ASSETS.resolve(
                "textures/misc/particles.png"
        ).toFile());
        for (int index = 0; index < 16; index++) {
            BufferedImage rune = ImageIO.read(MOD_ASSETS.resolve(
                    "textures/particle/warding_rune_" + index + ".png"
            ).toFile());
            assertEquals(16, rune.getWidth());
            assertEquals(16, rune.getHeight());
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    assertEquals(
                            atlas.getRGB(index * 16 + x, 96 + y),
                            rune.getRGB(x, y),
                            "Rune atlas mismatch at index=" + index
                                    + " x=" + x + " y=" + y
                    );
                }
            }
        }
    }

    @Test
    void recipeAndRuntimeContractMatchTc4() throws Exception {
        String recipe = Files.readString(Path.of(
                "src/main/resources/data/thaumic_reborn/recipes/pave_ward.json"
        ));
        assertTrue(recipe.contains("\"ignis\": 10"));
        assertTrue(recipe.contains("\"ordo\": 10"));
        assertTrue(recipe.contains("\"SAS\""));
        assertTrue(recipe.contains("\"SBS\""));
        assertTrue(recipe.contains("\"count\": 4"));

        String stone = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "PavingStoneOfWardingBlockEntity.java"
        ));
        assertTrue(stone.contains("stone.count % 5 == 0"));
        assertTrue(stone.contains("++stone.count % 100 == 0"));
        assertTrue(stone.contains("position.above(2)"));
        assertTrue(stone.contains("living.onGround()"));
        assertTrue(stone.contains("living instanceof Player"));
        assertTrue(stone.contains("-Mth.sin(angle) * 0.2F"));
        assertTrue(stone.contains("-0.1D"));
        assertTrue(stone.contains("Mth.cos(angle) * 0.2F"));

        String aura = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/WardingAuraBlock.java"
        ));
        assertTrue(aura.contains("entity instanceof Player"));
        assertTrue(aura.contains("return Shapes.block()"));
        assertTrue(aura.contains("offset = 1; offset <= 2"));
        assertTrue(aura.contains("hasNeighborSignal(basePosition)"));
        assertFalse(aura.contains("PAVING_STONE_OF_TRAVEL"));
    }
}
