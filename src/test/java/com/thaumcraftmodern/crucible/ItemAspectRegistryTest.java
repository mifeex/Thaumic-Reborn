package com.thaumcraftmodern.crucible;

import com.thaumcraftmodern.scan.AspectReward;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ItemAspectRegistryTest {
    @Test
    void mergesEveryExplicitScanAspectForCrucibleUse() {
        assertEquals(
                Map.of("aer", 3, "volatus", 2),
                ItemAspectRegistry.merge(List.of(
                        new AspectReward("aer", 1),
                        new AspectReward("volatus", 2),
                        new AspectReward("aer", 2)
                ))
        );
    }

    @Test
    void scanRegistryIsTheSingleGameplayAspectSource()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/crucible/"
                        + "ItemAspectRegistry.java"
        ));
        int shared = source.indexOf("ScanRegistry.findExplicitForItem(stack)");
        assertTrue(shared >= 0);
        assertTrue(!source.contains("ItemAspectDefinition"));

        Path legacyDirectory = Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/item_aspects"
        );
        if (Files.isDirectory(legacyDirectory)) {
            try (Stream<Path> files = Files.list(legacyDirectory)) {
                assertEquals(0, files.filter(Files::isRegularFile).count());
            }
        }

        String crucible = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "CrucibleBlockEntity.java"
        ));
        String furnace = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "AlchemicalFurnaceBlockEntity.java"
        ));
        assertTrue(crucible.contains("ItemAspectRegistry.aspects(one)"));
        assertTrue(furnace.contains("ItemAspectRegistry.aspects(input)"));
    }
}
