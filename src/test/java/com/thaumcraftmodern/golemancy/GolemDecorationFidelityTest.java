package com.thaumcraftmodern.golemancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thaumcraftmodern.entity.GolemDecorationType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class GolemDecorationFidelityTest {
    @Test
    void exposesAllEightTc4DecorationsAndMountConflicts() {
        assertEquals(Set.of('H', 'G', 'B', 'F', 'R', 'V', 'P', 'M'),
                Arrays.stream(GolemDecorationType.values())
                        .map(GolemDecorationType::legacyCode).collect(Collectors.toSet()));
        assertEquals(GolemDecorationType.TOP_HAT.mount(), GolemDecorationType.FEZ.mount());
        assertEquals(GolemDecorationType.GLASSES.mount(), GolemDecorationType.VISOR.mount());
        assertEquals(GolemDecorationType.BOW_TIE.mount(), GolemDecorationType.ARMOR.mount());
    }

    @Test
    void entityPersistsSynchronizesDropsAndAppliesEveryTc4Effect() throws Exception {
        String entity = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/entity/ClassicGolemEntity.java"));
        for (String contract : new String[]{
                "entityData.define(DECORATIONS", "tag.putString(\"Decoration\"", "tag.getString(\"Decoration\")",
                "GolemDecorationType.TOP_HAT", "GolemDecorationType.GLASSES", "GolemDecorationType.BOW_TIE",
                "GolemDecorationType.FEZ", "GolemDecorationType.DART_LAUNCHER", "GolemDecorationType.VISOR",
                "GolemDecorationType.ARMOR", "GolemDecorationType.HAMMER", "spawnAtLocation"
        }) assertTrue(entity.contains(contract), contract);
    }

    @Test
    void originalGeometryTextureAndCreativeInventoryAreWired() throws Exception {
        String layer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/GolemAccessoriesLayer.java"));
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/StrawGolemRenderer.java"));
        String tabs = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"));
        assertTrue(layer.contains("textures/entity/models/golem_decoration.png"));
        assertTrue(layer.contains(".addBox(-6.5F, -1F, -7F, 13F, 12F, 13F)"));
        assertTrue(renderer.contains("new GolemAccessoriesLayer<>"));
        assertTrue(tabs.contains("GolemDecorationType.values()"));
    }
}
