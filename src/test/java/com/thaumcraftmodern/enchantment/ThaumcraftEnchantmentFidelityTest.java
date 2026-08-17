package com.thaumcraftmodern.enchantment;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumcraftEnchantmentFidelityTest {
    @Test
    void hasteKeepsOriginalLevelsCostsAndBootRestriction() throws Exception {
        String haste = source("HasteEnchantment.java");
        assertTrue(haste.contains("return 15 + (level - 1) * 9;"));
        assertTrue(haste.contains("return 51;"));
        assertTrue(haste.contains("return 3;"));
        assertTrue(haste.contains("armor.getType() == ArmorItem.Type.BOOTS"));
    }

    @Test
    void repairKeepsOriginalLevelsCostsAndMendingConflict() throws Exception {
        String repair = source("RepairEnchantment.java");
        assertTrue(repair.contains("return 20 + (level - 1) * 10;"));
        assertTrue(repair.contains("return 51;"));
        assertTrue(repair.contains("return 2;"));
        assertTrue(repair.contains("instanceof ThaumcraftRepairable"));
        assertTrue(repair.contains("other != Enchantments.MENDING"));
    }

    @Test
    void repairCostReducesCompoundsBeforeApplyingOriginalSquareRoot()
            throws Exception {
        AspectRegistryRuntime.replace(List.of(
                primal("aer"), primal("terra"), primal("ignis"),
                primal("aqua"), primal("ordo"), primal("perditio"),
                new AspectDefinition(
                        "motus", 0x123456,
                        "thaumic_reborn:textures/aspects/motus.png",
                        List.of("aer", "ordo")
                )
        ));
        assertEquals(
                Map.of("aer", 8, "ordo", 8),
                ThaumcraftEnchantmentEvents.repairCost(Map.of("motus", 8), 2)
        );

        String events = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/enchantment/"
                        + "ThaumcraftEnchantmentEvents.java"));
        assertTrue(events.contains("player.tickCount % REPAIR_INTERVAL == 0"));
        assertTrue(events.contains("level * 0.015F"));
        assertTrue(events.contains("player.onGround()"));
        assertTrue(events.contains("player.isInWater()"));
    }

    private static AspectDefinition primal(String id) {
        return new AspectDefinition(
                id, 0xFFFFFF,
                "thaumic_reborn:textures/aspects/" + id + ".png"
        );
    }

    private static String source(String name) throws Exception {
        return Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/enchantment/" + name));
    }
}
