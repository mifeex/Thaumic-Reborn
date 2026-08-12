package com.thaumcraftmodern.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class ManaBeanAspectFidelityTest {
    @Test
    void storedBeanAspectIsAddedToItsRegisteredObjectAspects() {
        ScanDefinition base = new ScanDefinition(
                ScanTargetType.ITEM,
                "thaumcraftmodern:mana_bean",
                "",
                List.of(
                        new AspectReward("praecantatio", 2),
                        new AspectReward("herba", 1)
                )
        );

        assertEquals(
                List.of(
                        new AspectReward("praecantatio", 2),
                        new AspectReward("herba", 1),
                        new AspectReward("lucrum", 1)
                ),
                ScanRegistry.withStoredManaBeanAspect(
                        base,
                        Optional.of("lucrum")
                ).aspects()
        );
    }

    @Test
    void matchingBeanAspectMergesIntoTheExistingIcon() {
        ScanDefinition base = new ScanDefinition(
                ScanTargetType.ITEM,
                "thaumcraftmodern:mana_bean",
                "",
                List.of(
                        new AspectReward("praecantatio", 2),
                        new AspectReward("herba", 1)
                )
        );

        assertEquals(
                List.of(
                        new AspectReward("praecantatio", 2),
                        new AspectReward("herba", 2)
                ),
                ScanRegistry.withStoredManaBeanAspect(
                        base,
                        Optional.of("herba")
                ).aspects()
        );
    }
}
