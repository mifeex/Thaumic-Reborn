package com.thaumcraftmodern.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FacelessWitnessFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );

    @Test
    void usesDedicatedTallModelAndBoxUvAtlas() throws Exception {
        String model = source(
                "com/thaumcraftmodern/client/render/FacelessWitnessModel.java"
        );
        assertTrue(model.contains("extends EntityModel<FacelessWitnessEntity>"));
        assertFalse(model.contains("extends HumanoidModel"));
        assertTrue(model.contains("LayerDefinition.create(mesh, 128, 128)"));
        assertTrue(model.contains("3.0F, 38.0F, 3.0F"));
        assertTrue(model.contains("addLimb(root, \"upper_left\""));
        assertTrue(model.contains("addLimb(root, \"upper_right\""));
        assertTrue(model.contains("addLimb(root, \"lower\""));

        var texture = ImageIO.read(ASSETS.resolve(
                "textures/entity/models/faceless_witness.png"
        ).toFile());
        assertEquals(128, texture.getWidth());
        assertEquals(128, texture.getHeight());
        assertTrue(texture.getColorModel().hasAlpha());
    }

    @Test
    void rendererAndFourAttackPosesAreWired() throws Exception {
        String client = source(
                "com/thaumcraftmodern/client/WorldContentClientEvents.java"
        );
        assertTrue(client.contains("FacelessWitnessRenderer::new"));
        assertTrue(client.contains("FacelessWitnessModel.LAYER"));

        String model = source(
                "com/thaumcraftmodern/client/render/FacelessWitnessModel.java"
        );
        assertTrue(model.contains("case 0 ->"));
        assertTrue(model.contains("case 1 ->"));
        assertTrue(model.contains("case 2 ->"));
        assertTrue(model.contains("case 3 ->"));
        assertTrue(model.contains("idleLimb(upperLeft"));
        assertTrue(model.contains("idleLimb(upperRight"));
        assertTrue(model.contains("idleLimb(lower"));
        assertTrue(model.contains("applyIdleGestures(ageInTicks, walk)"));
        assertTrue(model.contains("float listen = window("));
        assertTrue(model.contains("float reach = window("));
        assertTrue(model.contains("float unfold = window("));
        assertTrue(model.contains("rightLeg.getChild(\"boot\")"));
        assertTrue(model.contains("rightBoot.xRot"));
    }

    @Test
    void entityDimensionsEggAndSoundVariantsAreRegistered() throws Exception {
        String entities = source(
                "com/thaumcraftmodern/registry/ModEntities.java"
        );
        assertTrue(entities.contains("FACELESS_WITNESS"));
        assertTrue(entities.contains(".sized(0.8F, 2.7F)"));

        String items = source("com/thaumcraftmodern/registry/ModItems.java");
        assertTrue(items.contains("FACELESS_WITNESS_SPAWN_EGG"));
        assertTrue(Files.isRegularFile(ASSETS.resolve(
                "models/item/faceless_witness_spawn_egg.json"
        )));

        JsonObject sounds = JsonParser.parseString(Files.readString(
                ASSETS.resolve("sounds.json")
        )).getAsJsonObject();
        assertSoundCount(sounds, "witness_idle", 4);
        assertSoundCount(sounds, "witness_alert", 3);
        assertSoundCount(sounds, "witness_attack", 3);
        assertSoundCount(sounds, "witness_hurt", 2);
        assertSoundCount(sounds, "witness_death", 2);
        for (String kind : new String[]{
                "idle1", "idle2", "idle3", "idle4",
                "alert1", "alert2", "alert3",
                "attack1", "attack2", "attack3",
                "hurt1", "hurt2", "death1", "death2"
        }) {
            assertTrue(Files.isRegularFile(
                    ASSETS.resolve("sounds/witness_" + kind + ".ogg")
            ));
        }
    }

    @Test
    void warpCombatEffectsAndTeleportStayBounded() throws Exception {
        String entity = source(
                "com/thaumcraftmodern/entity/FacelessWitnessEntity.java"
        );
        assertTrue(entity.contains("TELEPORT_RANGE = 8.0D"));
        assertTrue(entity.contains("BLINDNESS_RANGE = 3.0D"));
        assertTrue(entity.contains(
                "tickCount + 60 + getRandom().nextInt(41)"
        ));
        assertTrue(entity.contains("MobEffects.BLINDNESS, 60, 1"));
        assertTrue(entity.contains("MobEffects.CONFUSION, 120, 1"));
        assertTrue(entity.contains("distanceToSqr(x, getY(), z)"));
        assertTrue(entity.contains("level().noCollision(this, destination)"));
        assertTrue(entity.contains("level().containsAnyLiquid(destination)"));
        assertTrue(entity.contains("isCrimsonCultist(mob)"));
        assertTrue(entity.contains("int behindAttempts = blinded ? 14"));
        assertTrue(entity.contains("tickCount + 4"));
        assertTrue(entity.contains("playerAnchor.getLookAngle()"));
        assertTrue(entity.contains("getNavigation().moveTo(attackTarget, 1.2D)"));
        assertTrue(entity.contains("getLookControl().setLookAt(attackTarget"));
    }

    private static void assertSoundCount(
            JsonObject sounds,
            String event,
            int expected
    ) {
        assertEquals(
                expected,
                sounds.getAsJsonObject(event).getAsJsonArray("sounds").size()
        );
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }
}
