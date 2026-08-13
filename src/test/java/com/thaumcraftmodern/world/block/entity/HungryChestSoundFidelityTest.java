package com.thaumcraftmodern.world.block.entity;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class HungryChestSoundFidelityTest {
    @Test
    void firstViewerOpensAndLastViewerClosesWithVanillaChestSounds()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "HungryChestBlockEntity.java"
        ));

        assertTrue(source.contains(
                "if (previous == 0) playChestSound(SoundEvents.CHEST_OPEN)"
        ));
        assertTrue(source.contains("previous > 0 && openers == 0"));
        assertTrue(source.contains("playChestSound(SoundEvents.CHEST_CLOSE)"));
        assertTrue(source.contains("if (level == null || level.isClientSide) return"));
        assertTrue(source.contains("SoundSource.BLOCKS"));
        assertTrue(source.contains("0.5F"));
        assertTrue(source.contains("level.random.nextFloat() * 0.1F + 0.9F"));
    }
}
