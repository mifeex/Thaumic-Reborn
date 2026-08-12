package com.thaumcraftmodern.command;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumcraftWarpCommandFidelityTest {
    @Test
    void commandReportsEveryWarpPoolAndEffectiveEquipmentTotal() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/command/ThaumcraftCommands.java"
        ));

        assertTrue(source.contains(".then(warpCommands())"));
        assertTrue(source.contains("Commands.literal(\"warp\")"));
        assertTrue(source.contains("Commands.argument(\"player\", EntityArgument.player())"));
        assertTrue(source.contains("knowledge.warp(WarpType.PERMANENT)"));
        assertTrue(source.contains("knowledge.warp(WarpType.NORMAL)"));
        assertTrue(source.contains("knowledge.warp(WarpType.TEMPORARY)"));
        assertTrue(source.contains("knowledge.nonTemporaryWarp()"));
        assertTrue(source.contains("knowledge.totalWarp()"));
        assertTrue(source.contains("WarpGearService.equippedWarp(player)"));
        assertTrue(source.contains("Commands.literal(\"add\")"));
        assertTrue(source.contains("IntegerArgumentType.integer(-10000, 10000)"));
        assertTrue(source.contains("knowledge.addWarp(type, amount)"));
        assertTrue(source.contains("knowledge.setWarp(type, Math.max(0, before + amount))"));
        assertTrue(source.contains("knowledge.setWarpCounter(knowledge.totalWarp())"));
        assertTrue(source.contains("KnowledgeSync.send(player, \"command:change_warp\")"));
    }
}
