package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomBreakParticleModelTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );

    @Test
    void auraNodeUsesItsOwnParticleSpriteInsteadOfAir() throws IOException {
        String blockState = read("blockstates/aura_node.json");
        String model = read("models/block/aura_node_particles.json");

        assertFalse(blockState.contains("minecraft:block/air"));
        assertTrue(blockState.contains(
                "thaumic_reborn:block/aura_node_particles"
        ));
        assertTrue(model.contains(
                "thaumic_reborn:block/aura_node_particle"
        ));
    }

    @Test
    void bothResearchTablePartsUseWoodParticlesInsteadOfAir()
            throws IOException {
        String blockState = read("blockstates/research_table.json");
        String model = read("models/block/research_table_particles.json");

        assertFalse(blockState.contains("minecraft:block/air"));
        assertTrue(blockState.contains(
                "thaumic_reborn:block/research_table_particles"
        ));
        assertTrue(model.contains("thaumic_reborn:block/woodplain"));
    }

    @Test
    void everyEldritchAltarPartUsesItsRenderedTextureForParticles()
            throws IOException {
        String blockState = read("blockstates/eldritch_altar_part.json");
        String altar = read(
                "models/block/eldritch_altar_cap_particles.json"
        );
        String cap = read(
                "models/block/eldritch_obelisk_cap_particles.json"
        );
        String side = read(
                "models/block/eldritch_obelisk_side_particles.json"
        );

        assertFalse(blockState.contains("minecraft:block/air"));
        assertTrue(blockState.contains("\"part=0\""));
        assertTrue(blockState.contains("\"part=1\""));
        assertTrue(blockState.contains("\"part=2\""));
        assertTrue(blockState.contains("\"part=3\""));
        assertTrue(blockState.contains("\"part=4\""));
        assertTrue(altar.contains(
                "thaumic_reborn:block/obelisk_cap_altar"
        ));
        assertTrue(cap.contains("thaumic_reborn:block/obelisk_cap"));
        assertTrue(side.contains("thaumic_reborn:block/obelisk_side"));
        assertTrue(Files.exists(
                ASSETS.resolve("textures/block/obelisk_cap_altar.png")
        ));
        assertTrue(Files.exists(
                ASSETS.resolve("textures/block/obelisk_cap.png")
        ));
        assertTrue(Files.exists(
                ASSETS.resolve("textures/block/obelisk_side.png")
        ));
    }

    @Test
    void blockModelsUseAtlasTexturesForTheirBreakParticles()
            throws IOException {
        List<String> models = List.of(
                "infusion_pillar.json",
                "infusion_pillar_cap.json",
                "advanced_alchemical_furnace_tank.json",
                "advanced_alchemical_furnace_core.json",
                "advanced_alchemical_furnace_upper.json"
        );

        for (String model : models) {
            String source = read("models/block/" + model);
            assertFalse(source.contains("thaumic_reborn:models/"));
            assertTrue(source.contains("thaumic_reborn:block/"));
        }
        assertTrue(Files.exists(ASSETS.resolve("textures/block/pillar.png")));
        assertTrue(Files.exists(ASSETS.resolve("textures/block/thaumatorium.png")));
        assertTrue(Files.exists(ASSETS.resolve("textures/block/alch_furnace.png")));
        assertTrue(Files.exists(ASSETS.resolve("textures/block/alch_furnace_on.png")));
        assertTrue(Files.exists(ASSETS.resolve("textures/block/alch_furnace_tank.png")));
    }

    @Test
    void thaumatoriumKeepsItsClassicObjAndUsesAtlasTexture()
            throws IOException {
        String source = read("models/block/thaumatorium_lower.json");

        assertTrue(source.contains(
                "thaumic_reborn:textures/models/thaumatorium_block.obj"
        ));
        assertTrue(source.contains(
                "\"particle\":\"thaumic_reborn:block/thaumatorium\""
        ));
        assertTrue(source.contains(
                "\"texture0\":\"thaumic_reborn:block/thaumatorium\""
        ));
        assertFalse(source.contains("thaumic_reborn:models/thaumatorium"));
        assertTrue(Files.exists(ASSETS.resolve(
                "textures/models/thaumatorium.png"
        )));
        assertTrue(Files.exists(ASSETS.resolve(
                "textures/models/thaumatorium_block.obj"
        )));
        assertTrue(Files.exists(ASSETS.resolve(
                "textures/block/thaumatorium.png"
        )));
        String upper = read("models/block/thaumatorium_upper.json");
        assertTrue(upper.contains(
                "thaumic_reborn:block/thaumatorium"
        ));
        assertFalse(upper.contains("thaumic_reborn:models/thaumatorium"));
    }

    @Test
    void crystallizerUsesItsClassicTextureFromTheBlockAtlas()
            throws IOException {
        String source = read("models/block/essentia_crystallizer.json");

        assertTrue(source.contains(
                "thaumic_reborn:textures/models/crystalizer.obj"
        ));
        assertTrue(source.contains(
                "\"texture0\":\"thaumic_reborn:block/crystalizer\""
        ));
        assertTrue(source.contains(
                "\"particle\":\"thaumic_reborn:block/crystalizer_particle\""
        ));
        assertFalse(source.contains("thaumic_reborn:models/crystalizer"));
        assertTrue(Files.exists(ASSETS.resolve(
                "textures/block/crystalizer.png"
        )));
        assertTrue(Files.exists(ASSETS.resolve(
                "textures/block/crystalizer_particle.png"
        )));
        var atlasTexture = ImageIO.read(ASSETS.resolve(
                "textures/block/crystalizer.png"
        ).toFile());
        for (int y = 0; y < atlasTexture.getHeight(); y++) {
            for (int x = 0; x < atlasTexture.getWidth(); x++) {
                int colour = atlasTexture.getRGB(x, y);
                int red = colour >> 16 & 255;
                int green = colour >> 8 & 255;
                int blue = colour & 255;
                assertFalse(red >= 254 && green == 0 && blue >= 254,
                        "TC4 chroma key must not bleed from the block atlas");
            }
        }
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ASSETS.resolve(relativePath));
    }
}
