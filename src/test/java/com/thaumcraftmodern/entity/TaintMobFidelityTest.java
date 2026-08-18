package com.thaumcraftmodern.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TaintMobFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );
    private static final Map<String, String> CLASSIC_SOUND_HASHES = Map.of(
            "gore1.ogg",
            "0c1f6a94bdd10b373740447fa153cfdcfdf4768bb052ea59e3fa707789145fc3",
            "gore2.ogg",
            "d2053692c388553b83e00a85fb9489319abe09fea761a627b35945e28b8685b3",
            "roots1.ogg",
            "0ade94a100d8fdf271b00440000a7459c845c31d256b0bf72189ac368d9fc4ff",
            "roots2.ogg",
            "148d37919d58b50f578c35b58c3383e506cc68b78c12bb5635b3a8752bdf8200",
            "roots3.ogg",
            "efce54fccb006743bd7f9d6529fe2ae1565cc0233c81bcf36aad3460b2eed552",
            "swarm1.ogg",
            "81699611babdab45030467c799bf5a8d5885ca6095684ec75501e478529f540a",
            "swarm2.ogg",
            "ee1d6d605fe487d83a27a3b07e191fd1a6ce90b7ff16d22c959c55f78367ca31",
            "swarm3.ogg",
            "6e480af4aa27fc1b49c09042c36ecd09a70f23f3a1321a8c7d8b1e8dd5e44b3a"
    );

    @Test
    void combatStatsAndDimensionsMatchTc4() {
        assertKind(LegacyMobKind.TAINTED_CRAWLER, 5.0D, 2.0D, 0.4F, 0.3F);
        assertKind(LegacyMobKind.TAINT_SPORE, 1.0D, 1.0D, 0.5F, 0.5F);
        assertKind(
                LegacyMobKind.TAINT_SPORE_SWARMER,
                75.0D,
                1.0D,
                1.0F,
                1.0F
        );
        assertFalse(LegacyMobKind.TAINT_SPORE_SWARMER.flying());
    }

    @Test
    void originalTexturesRemainByteExact() throws Exception {
        assertHash(
                ASSETS.resolve("textures/entity/models/chicken.png"),
                "c0f7cb5ac0cc62b130bd87ceb2d90b7d1227fda1de637f401371c7404e0210d8"
        );
        assertHash(
                ASSETS.resolve("textures/entity/models/cow.png"),
                "9aa12f95d71d39f369043c45be49370e509eebb57becf2925de4cec9ee2b5086"
        );
        assertHash(
                ASSETS.resolve("textures/entity/models/creeper.png"),
                "ac55f40649e6d20f74d3fd8f90c4031d335a20c1af12fb93776302f2972fb5b4"
        );
        assertHash(
                ASSETS.resolve("textures/entity/models/pig.png"),
                "a4c55ac16a5abdcd5390a1387bbfc8ec79c8347e45747484524226c448b143d5"
        );
        assertHash(
                ASSETS.resolve("textures/entity/models/sheep.png"),
                "efb840c68a8015480ee6129a31f7def5c959fadeb50d22420f86e4bf77c2474e"
        );
        assertHash(
                ASSETS.resolve("textures/entity/models/sheep_fur.png"),
                "c9ce724eb092301dd1c4eca2e7e06ad321e502bb1a596e97dbd7c301a7a96459"
        );
        assertHash(
                ASSETS.resolve("textures/entity/models/villager.png"),
                "859429bdba57690a28da296045b3148bb030b6beecc20de0a89d89dbeb397b8a"
        );
        assertHash(
                ASSETS.resolve("textures/entity/models/taint_spider.png"),
                "900311dfe5e143c8e0e3f2f6700ce081d303fb9235cec945bb575ca92859272c"
        );
        assertHash(
                ASSETS.resolve(
                        "textures/entity/models/taint_spider_eyes.png"
                ),
                "29aab356cb087b21bb43aae60a301325bc651e8117acef3b38d36aae2691be58"
        );
        assertHash(
                ASSETS.resolve("textures/entity/models/taint_spore.png"),
                "f5dfd5186552a5b38bc9f6df61ec8706fcb90cb2ed739f785afe96727bbba9e6"
        );
    }

    @Test
    void originalSoundFilesAndGroupsRemainExact() throws Exception {
        for (var entry : CLASSIC_SOUND_HASHES.entrySet()) {
            assertHash(
                    ASSETS.resolve("sounds").resolve(entry.getKey()),
                    entry.getValue()
            );
        }

        JsonObject sounds = JsonParser.parseString(Files.readString(
                ASSETS.resolve("sounds.json")
        )).getAsJsonObject();
        assertSoundGroup(sounds, "gore", 2);
        assertSoundGroup(sounds, "roots", 3);
        assertSoundGroup(sounds, "swarm", 3);
    }

    @Test
    void dedicatedRenderersAndClassicSoundContractsAreWired()
            throws Exception {
        String registration = Files.readString(JAVA.resolve(
                "com/thaumcraftmodern/client/WorldContentClientEvents.java"
        ));
        assertTrue(registration.contains("TaintedCrawlerRenderer::new"));
        assertTrue(registration.contains("TaintedChickenRenderer::new"));
        assertTrue(registration.contains("TaintedCowRenderer::new"));
        assertTrue(registration.contains("TaintedCreeperRenderer::new"));
        assertTrue(registration.contains("TaintedPigRenderer::new"));
        assertTrue(registration.contains("TaintedSheepRenderer::new"));
        assertTrue(registration.contains("TaintedVillagerRenderer::new"));
        assertTrue(registration.contains("TaintSporeRenderer::new"));
        assertTrue(registration.contains("TaintSporeSwarmerRenderer::new"));

        String crawler = renderer("TaintedCrawlerRenderer.java");
        assertTrue(crawler.contains("ModelLayers.SPIDER"));
        assertTrue(crawler.contains("pose.scale(0.4F, 0.5F, 0.4F)"));
        assertTrue(crawler.contains("RenderType.eyes(EYES)"));

        String chicken = renderer("TaintedChickenRenderer.java");
        assertTrue(chicken.contains("ModelLayers.CHICKEN"));
        assertTrue(chicken.contains("ChickenModel<LegacyThaumcraftMob>"));
        assertTrue(chicken.contains("0.3F"));
        assertTrue(chicken.contains("taintedChickenWingBob(partialTick)"));
        assertFalse(chicken.contains("entity.tickCount"));
        assertFalse(chicken.contains("HumanoidModel"));

        String cow = renderer("TaintedCowRenderer.java");
        assertTrue(cow.contains("ModelLayers.COW"));
        assertTrue(cow.contains("CowModel<LegacyThaumcraftMob>"));
        assertTrue(cow.contains("0.7F"));
        assertFalse(cow.contains("HumanoidModel"));

        String creeper = renderer("TaintedCreeperRenderer.java");
        assertTrue(creeper.contains("ModelLayers.CREEPER"));
        assertTrue(creeper.contains("CreeperModel<LegacyThaumcraftMob>"));
        assertTrue(creeper.contains("0.5F"));
        assertFalse(creeper.contains("HumanoidModel"));

        String pig = renderer("TaintedPigRenderer.java");
        assertTrue(pig.contains("ModelLayers.PIG"));
        assertTrue(pig.contains("PigModel<LegacyThaumcraftMob>"));
        assertTrue(pig.contains("0.5F"));
        assertFalse(pig.contains("HumanoidModel"));

        String sheep = renderer("TaintedSheepModel.java");
        assertTrue(sheep.contains("fur ? -4.0F : -6.0F"));
        assertTrue(sheep.contains("fur ? 6.0F : 8.0F"));
        assertTrue(sheep.contains("new CubeDeformation(fur ? 1.75F : 0.0F)"));
        assertTrue(sheep.contains("new CubeDeformation(fur ? 0.5F : 0.0F)"));
        assertTrue(sheep.contains("float legHeight = fur ? 6.0F : 12.0F"));
        assertTrue(sheep.contains("4.0F, height, 4.0F"));
        assertTrue(sheep.contains("LayerDefinition.create(mesh, 64, 32)"));
        String sheepRenderer = renderer("TaintedSheepRenderer.java");
        assertTrue(sheepRenderer.contains("sheep_fur.png"));
        assertTrue(sheepRenderer.contains("0.7F"));
        assertFalse(sheepRenderer.contains("HumanoidModel"));

        String villager = renderer("TaintedVillagerRenderer.java");
        assertTrue(villager.contains("TaintedVillagerModel.LAYER"));
        assertFalse(villager.contains("ModelLayers.VILLAGER"));
        assertTrue(villager.contains("0.9375F"));
        assertTrue(villager.contains("0.5F"));
        assertFalse(villager.contains("HumanoidModel"));

        String villagerModel = renderer("TaintedVillagerModel.java");
        assertTrue(villagerModel.contains("-4.0F, -10.0F, -4.0F"));
        assertTrue(villagerModel.contains("8.0F, 10.0F, 8.0F"));
        assertTrue(villagerModel.contains(".texOffs(24, 0).addBox"));
        assertTrue(villagerModel.contains("8.0F, 18.0F, 6.0F"));
        assertTrue(villagerModel.contains("new CubeDeformation(0.5F)"));
        assertTrue(villagerModel.contains("PartPose.offsetAndRotation("));
        assertTrue(villagerModel.contains("-0.75F, 0.0F, 0.0F"));
        assertTrue(villagerModel.contains("LayerDefinition.create(mesh, 64, 64)"));

        String spore = renderer("TaintSporeModel.java");
        assertTrue(spore.contains(
                ".addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F)"
        ));
        String swarmer = renderer("TaintSporeSwarmerModel.java");
        assertTrue(swarmer.contains(".texOffs(0, 32)"));
        assertTrue(swarmer.contains("LightTexture.FULL_BRIGHT"));

        String mob = Files.readString(JAVA.resolve(
                "com/thaumcraftmodern/entity/LegacyThaumcraftMob.java"
        ));
        assertTrue(mob.contains(
                "case TAINT_SPORE -> ModSounds.SWARM.get()"
        ));
        assertTrue(mob.contains(
                "case TAINT_SPORE_SWARMER -> ModSounds.ROOTS.get()"
        ));
        assertTrue(mob.contains("taintSwarmSpawnCounter = 500"));
        assertTrue(mob.contains("spawnTaintSwarm()"));
        assertTrue(mob.contains("burstSporeIntoCrawlers()"));
        assertTrue(mob.contains("ModEntities.TAINTED_CRAWLER.get()"));
        assertTrue(mob.contains("ModEntities.TAINT_SWARM.get()"));
        assertTrue(mob.contains("kind == LegacyMobKind.TAINT_SPORE"));
        assertTrue(mob.contains("getNavigation().stop()"));
        assertTrue(mob.contains("ModSounds.GORE.get()"));
    }

    private static void assertKind(
            LegacyMobKind kind,
            double health,
            double damage,
            float width,
            float height
    ) {
        assertEquals(health, kind.health());
        assertEquals(damage, kind.damage());
        assertEquals(width, kind.width());
        assertEquals(height, kind.height());
    }

    private static void assertSoundGroup(
            JsonObject sounds,
            String name,
            int variants
    ) {
        JsonObject group = sounds.getAsJsonObject(name);
        assertEquals("master", group.get("category").getAsString());
        assertEquals(variants, group.getAsJsonArray("sounds").size());
    }

    private static String renderer(String name) throws Exception {
        return Files.readString(JAVA.resolve(
                "com/thaumcraftmodern/client/render/" + name
        ));
    }

    private static void assertHash(Path path, String expected)
            throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path));
        assertEquals(expected, HexFormat.of().formatHex(digest));
    }
}
