package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.entity.LegacyMobKind;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClassicMobModelRestorationTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path ASSETS = RESOURCES.resolve(
            "assets/thaumcraftmodern"
    );

    @Test
    void affectedMobStatsAndDimensionsMatchClassicSources() {
        assertKind(LegacyMobKind.PECH, 30, 3, 0.25, 0.7F, 1.5F);
        assertKind(
                LegacyMobKind.ELDRITCH_CONSTRUCT,
                250,
                10,
                0.30,
                1.75F,
                3.5F
        );
        assertKind(
                LegacyMobKind.ELDRITCH_CRAB,
                20,
                4,
                0.30,
                0.8F,
                0.6F
        );
        assertKind(
                LegacyMobKind.TAINT_SWARM,
                30,
                2,
                0.35,
                2.0F,
                2.0F
        );
    }

    @Test
    void originalParticleAtlasOverlayAndSoundsRemainByteExact()
            throws Exception {
        assertHash(
                ASSETS.resolve("textures/entity/misc/particles.png"),
                "1fb548c3bc2bb99e7a6db1295214e6f119d2a3eddc36214b3c0abcb7dcc22d18"
        );
        assertHash(
                ASSETS.resolve("textures/entity/models/craboverlay.png"),
                "ad0358bd7ccfcb8c3ca53bceb7eea97182defcce977ea28d2504eec2d5f76dcb"
        );
        assertHash(
                ASSETS.resolve("sounds/swarmattack.ogg"),
                "f981ff683c175b78cd5c91da7f0dbcb79068cf124284f9db0293b90e97901210"
        );
        assertHash(
                ASSETS.resolve("sounds/pech_idle1.ogg"),
                "642ae70b9270ab21224f5eba40998e5d5a58886ee804eaaedb49acddc8817e16"
        );
        assertHash(
                ASSETS.resolve("sounds/crabtalk1.ogg"),
                "4840e3557f34378ac9027f255ea0634ac95fc33b8c298fa7b309e436edb7de9e"
        );
    }

    @Test
    void dedicatedRenderersAndBothConstructPhasesAreWired()
            throws Exception {
        String registrations = source(
                "com/thaumcraftmodern/client/WorldContentClientEvents.java"
        );
        assertTrue(registrations.contains("PechRenderer::new"));
        assertTrue(registrations.contains("EldritchConstructRenderer::new"));
        assertTrue(registrations.contains("EldritchCrabRenderer::new"));
        assertTrue(registrations.contains("TaintSwarmRenderer::new"));

        String construct = source(
                "com/thaumcraftmodern/client/render/"
                        + "EldritchConstructModel.java"
        );
        assertTrue(construct.contains("\"head\""));
        assertTrue(construct.contains("\"core\""));
        assertTrue(construct.contains("entity.isConstructHeadless()"));
        assertTrue(construct.contains("pose.scale(2.15F")
                || source(
                        "com/thaumcraftmodern/client/render/"
                                + "EldritchConstructRenderer.java"
                ).contains("pose.scale(2.15F"));

        String entity = source(
                "com/thaumcraftmodern/entity/LegacyThaumcraftMob.java"
        );
        assertTrue(entity.contains("CONSTRUCT_HEADLESS"));
        assertTrue(entity.contains("CONSTRUCT_RECOVERY_TIMER"));
        assertTrue(entity.contains(
                "EldritchConstructBehavior.SPAWN_RECOVERY_TICKS"
        ));
        assertTrue(entity.contains("CRAB_HELM"));
    }

    @Test
    void eldritchBiomeKeepsOriginalGuardianAndInhabitedZombieSpawns()
            throws Exception {
        JsonObject modifier = JsonParser.parseString(Files.readString(
                RESOURCES.resolve(
                        "data/thaumcraftmodern/forge/biome_modifier/"
                                + "add_eldritch_mobs.json"
                )
        )).getAsJsonObject();
        assertEquals(
                "thaumcraftmodern:eldritch",
                modifier.get("biomes").getAsString()
        );
        assertTrue(modifier.toString().contains(
                "thaumcraftmodern:inhabited_zombie"
        ));
        assertTrue(modifier.toString().contains(
                "thaumcraftmodern:eldritch_guardian"
        ));
        assertFalse(modifier.toString().contains(
                "thaumcraftmodern:eldritch_crab"
        ));
        String entity = source(
                "com/thaumcraftmodern/entity/LegacyThaumcraftMob.java"
        );
        assertTrue(entity.contains(
                "sample.kind == LegacyMobKind.ELDRITCH_CRAB"
        ));
        assertTrue(entity.contains(
                "is(ModWorldgenKeys.ELDRITCH)"
        ));
    }

    private static void assertKind(
            LegacyMobKind kind,
            double health,
            double damage,
            double speed,
            float width,
            float height
    ) {
        assertEquals(health, kind.health());
        assertEquals(damage, kind.damage());
        assertEquals(speed, kind.speed());
        assertEquals(width, kind.width());
        assertEquals(height, kind.height());
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }

    private static void assertHash(Path path, String expected)
            throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path));
        assertEquals(expected, HexFormat.of().formatHex(digest));
    }
}
