package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Test;

class ManaPodGrowthFidelityTest {
    private static final Path ROOT = Path.of("src/main");

    @Test
    void naturalGrowthMatchesTc4AndDoesNotRequireCropLight()
            throws IOException {
        String source = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/world/block/ManaPodBlock.java"
        ));
        assertTrue(source.contains("extends CropBlock"));
        assertTrue(source.contains("random.nextInt(30) == 0"));
        assertTrue(source.contains("pod.checkGrowth(level)"));
        assertTrue(source.contains("level.setBlockEntity(pod)"));
        assertTrue(source.contains("Block.UPDATE_CLIENTS"));
        assertFalse(source.contains("getRawBrightness"));
    }

    @Test
    void wildPodsUseEveryTc4PostGrowthStageWithoutLiveLevelAccess()
            throws IOException {
        String source = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/worldgen/"
                        + "LegacyVegetationFeature.java"
        ));
        assertTrue(source.contains("3 + random.nextInt(5)"));
        assertFalse(source.contains("2 + random.nextInt(5)"));
        assertTrue(source.contains("manaPod.initializeWorldgen(random)"));
        assertFalse(source.contains("manaPod.checkGrowth(level.getLevel())"));

        String entity = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/world/block/entity/"
                        + "ManaPodBlockEntity.java"
        ));
        int start = entity.indexOf("public void initializeWorldgen");
        int end = entity.indexOf("/** Exact modern equivalent", start);
        assertTrue(start >= 0 && end > start);
        String initializer = entity.substring(start, end);
        assertFalse(initializer.contains("ServerLevel"));
        assertFalse(initializer.contains("sync()"));
        assertFalse(initializer.contains("setChanged()"));
    }

    @Test
    void outlineMatchesTc4AgeDependentManaPodBounds() throws IOException {
        String source = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/world/block/ManaPodBlock.java"
        ));
        int[] minimumY = {12, 10, 8, 6, 5, 4, 3, 2};
        for (int y : minimumY) {
            assertTrue(source.contains(
                    "Block.box(4.0D, " + y + ".0D, 4.0D, "
                            + "12.0D, 16.0D, 12.0D)"
            ));
        }
        assertTrue(source.contains("return SHAPES[getAge(state)]"));
    }

    @Test
    void allEightAgesHaveModels() throws IOException {
        JsonObject variants = read(
                "resources/assets/thaumic_reborn/blockstates/mana_pod.json"
        ).getAsJsonObject("variants");
        assertEquals(8, variants.size());
        assertTrue(variants.getAsJsonObject("age=0").get("model")
                .getAsString().endsWith("mana_pod_0"));
        assertTrue(variants.getAsJsonObject("age=1").get("model")
                .getAsString().endsWith("mana_pod_1"));
        for (int age = 2; age <= 7; age++) {
            assertTrue(variants.getAsJsonObject("age=" + age).get("model")
                    .getAsString().endsWith("mana_pod_2"));
        }
    }

    @Test
    void classicRendererUsesOriginalModelsTexturesAndAspectColorMix()
            throws IOException {
        String model = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/client/render/ClassicManaPodModel.java"
        ));
        assertTrue(model.contains("-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F"));
        assertTrue(model.contains("-3.5F, 0.0F, -3.5F, 7.0F, 9.0F, 7.0F"));

        String renderer = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/client/render/"
                        + "ManaPodBlockEntityRenderer.java"
        ));
        assertTrue(renderer.contains("textures/models/manapod_0.png"));
        assertTrue(renderer.contains("textures/models/manapod_2.png"));
        assertTrue(renderer.contains("0.125F * age * pulse"));
        assertTrue(renderer.contains("0.15F * age"));
        assertTrue(renderer.contains("definition.color()"));
        assertTrue(renderer.contains("if (age == ManaPodBlock.MAX_AGE)"));

        assertEquals(
                "0dc08210ddc8b2a60119c74fa470657fdf6910ac80d593f26f1cd5fb452c4844",
                sha256("resources/assets/thaumic_reborn/textures/models/manapod_0.png")
        );
        assertEquals(
                "b8d64786fdd4414a509276f5c30b34e1c40868eed42e33e06a0647748092ee83",
                sha256("resources/assets/thaumic_reborn/textures/models/manapod_2.png")
        );
    }

    @Test
    void podAspectIsServerSavedSyncedAndAssignedLikeTc4()
            throws IOException {
        String source = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/world/block/entity/"
                        + "ManaPodBlockEntity.java"
        ));
        assertTrue(source.contains("ASPECT_TAG = \"aspect\""));
        assertTrue(source.contains("random.nextInt(8) == 0"));
        for (String primal : List.of(
                "aer", "ignis", "aqua", "terra", "ordo", "perditio"
        )) {
            assertTrue(source.contains("\"" + primal + "\""));
        }
        assertTrue(source.contains("tag.putString(ASPECT_TAG, aspect)"));
        assertTrue(source.contains("getUpdatePacket()"));
        assertTrue(source.contains("chooseCrossbredAspect(level)"));

        String migration = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/world/block/entity/"
                        + "ManaPodMigrationEvents.java"
        ));
        assertTrue(migration.contains("ChunkEvent.Load"));
        assertTrue(migration.contains("chunk.findBlocks("));
        assertTrue(migration.contains("state.is(ModBlocks.MANA_POD.get())"));
        assertTrue(migration.contains("chunk.setBlockEntity(pod)"));
    }

    @Test
    void youngPodsDropNothingAndRipePodsDropOneOrTwoBeans()
            throws IOException {
        String loot = Files.readString(ROOT.resolve(
                "resources/data/thaumic_reborn/loot_tables/blocks/"
                        + "mana_pod.json"
        ));
        assertFalse(loot.contains("\"age\": \"0\""));
        assertFalse(loot.contains("\"age\": \"1\""));
        for (int age = 2; age <= 7; age++) {
            assertTrue(loot.contains("\"age\": \"" + age + "\""));
        }
        assertTrue(loot.contains("\"chance\": 0.67"));
        assertFalse(loot.contains("\"max\": 2"));
        assertTrue(loot.contains("\"source\": \"aspect\""));
        assertTrue(loot.contains("\"target\": \"Aspect\""));
    }

    @Test
    void manaBeanReplantsBelowWoodInMagicalBiomes()
            throws IOException {
        String source = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/item/ManaBeanItem.java"
        ));
        assertTrue(source.contains("Direction.DOWN"));
        assertTrue(source.contains("\"forge\", \"is_magical\""));
        assertTrue(source.contains("pod.canSurvive(level, position)"));
        assertTrue(source.contains("manaPod.setAspect(aspect("));
        assertTrue(source.contains("context.getItemInHand().shrink(1)"));
    }

    private static JsonObject read(String relative) throws IOException {
        return JsonParser.parseString(
                Files.readString(ROOT.resolve(relative))
        ).getAsJsonObject();
    }

    private static String sha256(String relative) throws IOException {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(Files.readAllBytes(ROOT.resolve(relative)))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
