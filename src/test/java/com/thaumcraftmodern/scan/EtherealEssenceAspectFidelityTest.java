package com.thaumcraftmodern.scan;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EtherealEssenceAspectFidelityTest {
    @Test
    void eachStoredAspectHasIndependentScanKnowledge() {
        assertEquals(
                "item:thaumic_reborn:ethereal_essence#aspect=ignis",
                ScanRegistry.etherealEssenceKnowledgeKey(
                        "item:thaumic_reborn:ethereal_essence",
                        Optional.of("ignis")
                )
        );
        assertEquals(
                "item:thaumic_reborn:ethereal_essence#aspect=ordo",
                ScanRegistry.etherealEssenceKnowledgeKey(
                        "item:thaumic_reborn:ethereal_essence",
                        Optional.of("ordo")
                )
        );
    }

    @Test
    void storedAspectIsAddedToRegisteredObjectAspects() {
        ScanDefinition base = new ScanDefinition(
                ScanTargetType.ITEM,
                "thaumic_reborn:ethereal_essence",
                "",
                List.of(new AspectReward("auram", 2))
        );

        assertEquals(
                List.of(
                        new AspectReward("auram", 2),
                        new AspectReward("ordo", 2)
                ),
                ScanRegistry.withStoredAspect(
                        base,
                        Optional.of("ordo"),
                        2
                ).aspects()
        );
    }

    @Test
    void matchingStoredAspectMergesInsteadOfDuplicatingIcon() {
        ScanDefinition base = new ScanDefinition(
                ScanTargetType.ITEM,
                "thaumic_reborn:ethereal_essence",
                "",
                List.of(new AspectReward("auram", 2))
        );

        assertEquals(
                List.of(new AspectReward("auram", 4)),
                ScanRegistry.withStoredAspect(
                        base,
                        Optional.of("auram"),
                        2
                ).aspects()
        );
    }
}
