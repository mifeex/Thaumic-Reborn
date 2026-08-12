package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableClassicSoundFidelityTest {
    @Test
    void researchTableUsesClassicInteractionSoundSet() throws Exception {
        String screen = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/ResearchTableScreen.java"
        ));
        String menu = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/menu/ResearchTableMenu.java"
        ));
        String sounds = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/sounds.json"
        ));

        assertTrue(screen.contains("ModSounds.CAMERA_CLACK.get()"));
        assertTrue(screen.contains("ModSounds.HH_OFF.get()"));
        assertTrue(menu.contains("successful ? ModSounds.HH_ON.get() : ModSounds.HH_OFF.get()"));
        assertTrue(screen.contains("ModSounds.WRITE.get()"));
        assertTrue(screen.contains("ModSounds.ERASE.get()"));
        assertTrue(screen.contains("ModSounds.KEY.get()"));
        assertTrue(screen.contains("private void playPlacementSound()"));
        assertTrue(screen.contains("private void playEraseSound()"));
        assertTrue(screen.contains("playCombineSound();\n        if (minecraft != null)"));
        assertTrue(menu.contains("ModSounds.LEARN.get()"));
        assertTrue(menu.contains("SoundSource.PLAYERS"));
        assertTrue(sounds.contains("\"key\""));
        assertTrue(Files.exists(Path.of(
                "src/main/resources/assets/thaumcraftmodern/sounds/key.ogg"
        )));

        assertFalse(menu.contains("SoundSource.MASTER"));
    }
}
