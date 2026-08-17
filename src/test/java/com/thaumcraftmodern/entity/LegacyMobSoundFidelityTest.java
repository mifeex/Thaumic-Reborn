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

class LegacyMobSoundFidelityTest {
    private static final Path SOUNDS = Path.of(
            "src/main/resources/assets/thaumic_reborn/sounds"
    );
    private static final Map<String, String> CLASSIC_HASHES = Map.of(
            "egidle1.ogg",
            "ea3b34ca667a719000e98467a8fc968f23ee15dbc8f0a6eaa3e1ef9ae2686c16",
            "egidle2.ogg",
            "82e7c75308e25dde0eb014da77f39867409a45d6b1e6a55e67ae6fc942e4d5de",
            "egattack.ogg",
            "094b9493d098d76208c026d81c13c9c149d0ff3518e2e472409bda3657f3f810",
            "egdeath.ogg",
            "c10a9971323265fcdfa492a3066530d5d0d4993fb5c0590084d28f7ddfae5d2c",
            "egscreech.ogg",
            "69d18e859b96f97b5f48482584852d945a7013e99bb739f913e61fcf48aeb10c",
            "chant1.ogg",
            "ef08d8f6a40123e76f68f2b82e42cd9e49aaf752970412c0f280ac9bafcf5625",
            "chant2.ogg",
            "530bb900ddfa65a3cc576ca861e4966a3ff468eedc4faef63a034f24095aa602",
            "chant3.ogg",
            "e3e019d5c543edebe7337d4c77ef0256bb31e51259eefd023d961d480b25858f"
    );

    @Test
    void classicMobSoundFilesRemainByteExact() throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (var entry : CLASSIC_HASHES.entrySet()) {
            byte[] digest = sha256.digest(
                    Files.readAllBytes(SOUNDS.resolve(entry.getKey()))
            );
            assertEquals(
                    entry.getValue(),
                    HexFormat.of().formatHex(digest),
                    entry.getKey()
            );
        }
    }

    @Test
    void soundGroupsMatchTc4RandomVariants() throws Exception {
        JsonObject sounds = JsonParser.parseString(Files.readString(
                Path.of(
                        "src/main/resources/assets/thaumic_reborn/"
                                + "sounds.json"
                )
        )).getAsJsonObject();
        assertEquals(2, sounds.getAsJsonObject("egidle")
                .getAsJsonArray("sounds").size());
        assertEquals(1, sounds.getAsJsonObject("egattack")
                .getAsJsonArray("sounds").size());
        assertEquals(1, sounds.getAsJsonObject("egdeath")
                .getAsJsonArray("sounds").size());
        assertEquals(1, sounds.getAsJsonObject("egscreech")
                .getAsJsonArray("sounds").size());
        assertEquals(3, sounds.getAsJsonObject("chant")
                .getAsJsonArray("sounds").size());
        for (String event : new String[]{
                "egidle", "egattack", "egdeath", "egscreech", "chant"
        }) {
            assertEquals(
                    "master",
                    sounds.getAsJsonObject(event).get("category").getAsString()
            );
        }
    }

    @Test
    void runtimeUsesOriginalGuardianAndClericEvents() throws Exception {
        String mob = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/entity/"
                        + "LegacyThaumcraftMob.java"
        ));
        assertTrue(mob.contains(
                "case ELDRITCH_GUARDIAN -> ModSounds.EG_IDLE.get()"
        ));
        assertTrue(mob.contains(
                "case CRIMSON_CLERIC -> ModSounds.CULTIST_CHANT.get()"
        ));
        assertTrue(mob.contains("ModSounds.EG_ATTACK.get()"));
        assertTrue(mob.contains("ModSounds.EG_SCREECH.get()"));
        assertTrue(mob.contains("ModSounds.EG_DEATH.get()"));
        assertFalse(mob.contains("SoundEvents.WITHER_SHOOT"));
        assertFalse(mob.contains("SoundEvents.WARDEN_SONIC_BOOM"));
    }
}
