package com.thaumcraftmodern.scan;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanKnowledgeRequirementTest {
    @Test
    void reportsNearestMissingComponentAndThenChecksItsComposition() {
        AspectRegistryRuntime.replace(List.of(
                primal("bestia"),
                primal("soul"),
                primal("ignis"),
                compound("cognitio", "soul", "ignis"),
                compound("humanus", "bestia", "cognitio")
        ));
        ScanDefinition target = new ScanDefinition(
                ScanTargetType.ITEM,
                "minecraft:player_head",
                "",
                List.of(new AspectReward("humanus", 1))
        );
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.learnAspect("bestia");

        assertEquals(
                "cognitio",
                ScanService.firstMissingPrerequisite(target, knowledge).orElseThrow()
        );

        knowledge.learnAspect("cognitio");
        assertEquals(
                "soul",
                ScanService.firstMissingPrerequisite(target, knowledge).orElseThrow()
        );

        knowledge.learnAspect("soul");
        assertTrue(ScanService.firstMissingPrerequisite(target, knowledge).isEmpty());
    }

    private static AspectDefinition primal(String id) {
        return new AspectDefinition(id, 0xFFFFFF, icon(id));
    }

    private static AspectDefinition compound(String id, String first, String second) {
        return new AspectDefinition(id, 0xFFFFFF, icon(id), first, second);
    }

    private static String icon(String id) {
        return "thaumic_reborn:textures/aspects/" + id + ".png";
    }
}
