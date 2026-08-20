package com.thaumcraftmodern.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.world.block.ArcaneDoorBlock;
import net.minecraft.world.level.block.DoorBlock;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class ArcaneDoorVanillaFidelityTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path VANILLA_CLIENT = Path.of(
            System.getProperty("user.home"),
            ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/"
                    + "client-extra.jar"
    );

    @Test
    void arcaneDoorUsesVanillaDoorStateRotations() throws Exception {
        JsonObject vanilla;
        try (ZipFile zip = new ZipFile(VANILLA_CLIENT.toFile())) {
            String json = new String(zip.getInputStream(zip.getEntry(
                    "assets/minecraft/blockstates/iron_door.json"
            )).readAllBytes());
            vanilla = JsonParser.parseString(json.replace(
                    "minecraft:block/iron_door",
                    "thaumic_reborn:block/arcane_door"
            )).getAsJsonObject();
        }
        JsonObject arcane = JsonParser.parseString(Files.readString(
                RESOURCES.resolve(
                        "assets/thaumic_reborn/blockstates/arcane_door.json"
                )
        )).getAsJsonObject();

        assertEquals(vanilla, arcane);
        assertEquals(32, arcane.getAsJsonObject("variants").size());
    }

    @Test
    void everyArcaneDoorModelUsesTheMatchingVanillaDoorGeometry()
            throws Exception {
        for (String half : List.of("bottom", "top")) {
            for (String hinge : List.of("left", "right")) {
                for (String open : List.of("", "_open")) {
                    String name = "arcane_door_" + half + "_" + hinge + open;
                    JsonObject model = JsonParser.parseString(Files.readString(
                            RESOURCES.resolve(
                                    "assets/thaumic_reborn/models/block/"
                                            + name + ".json"
                            )
                    )).getAsJsonObject();
                    assertEquals(
                            "minecraft:block/door_" + half + "_" + hinge + open,
                            model.get("parent").getAsString(),
                            name
                    );
                    assertEquals("thaumic_reborn:block/adoorbot", model
                            .getAsJsonObject("textures").get("bottom")
                            .getAsString());
                    assertEquals("thaumic_reborn:block/adoortop", model
                            .getAsJsonObject("textures").get("top")
                            .getAsString());
                }
            }
        }
    }

    @Test
    void arcaneDoorInheritsVanillaHitboxWithoutAnOverride() {
        assertEquals(DoorBlock.class, ArcaneDoorBlock.class.getSuperclass());
        assertFalse(List.of(ArcaneDoorBlock.class.getDeclaredMethods()).stream()
                .anyMatch(method -> method.getName().equals("getShape")));
    }
}
