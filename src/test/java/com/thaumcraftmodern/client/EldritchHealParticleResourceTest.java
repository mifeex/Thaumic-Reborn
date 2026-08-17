package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EldritchHealParticleResourceTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );

    @Test
    void classicBlackWispSpriteAndRegistrationExist() throws Exception {
        Path texture = ASSETS.resolve(
                "textures/particle/eldritch_heal.png"
        );
        var image = ImageIO.read(texture.toFile());
        assertEquals(32, image.getWidth());
        assertEquals(32, image.getHeight());

        String particle = Files.readString(
                ASSETS.resolve("particles/eldritch_heal.json")
        );
        assertTrue(particle.contains(
                "\"thaumic_reborn:eldritch_heal\""
        ));
    }
}
