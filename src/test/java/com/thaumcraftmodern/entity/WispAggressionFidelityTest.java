package com.thaumcraftmodern.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class WispAggressionFidelityTest {
    private static final Path MOB_SOURCE = Path.of(
            "src/main/java/com/thaumcraftmodern/entity/LegacyThaumcraftMob.java"
    );

    @Test
    void zapDecisionLogicRunsEveryTickLikeTc4UpdateAiTasks()
            throws Exception {
        String goal = wispGoal();
        assertTrue(goal.contains("requiresUpdateEveryTick()"));
        assertTrue(goal.contains("return true;"));
        assertTrue(goal.contains("nextInt(1000) == 0"));
        assertTrue(goal.contains("wisp.wispAggroCooldown = 50"));
    }

    @Test
    void livingTargetIsRememberedOutsideAttackRange()
            throws Exception {
        String goal = wispGoal();
        String targetValidation = goal.substring(
                goal.indexOf("LivingEntity target"),
                goal.indexOf("wisp.wispAggroCooldown--")
        );
        assertTrue(targetValidation.contains("!target.isAlive()"));
        assertFalse(targetValidation.contains("distanceToSqr"));
    }

    @Test
    void damageRetaliationRestoresOriginalTwoHundredTickAggro()
            throws Exception {
        String source = Files.readString(MOB_SOURCE);
        assertTrue(source.contains("source.getDirectEntity()"));
        assertTrue(source.contains("wispAggroCooldown = 200"));
    }

    private static String wispGoal() throws Exception {
        String source = Files.readString(MOB_SOURCE);
        int start = source.indexOf("private static final class WispZapGoal");
        int end = source.indexOf(
                "private static final class FirebatAttackGoal",
                start
        );
        return source.substring(start, end);
    }
}
