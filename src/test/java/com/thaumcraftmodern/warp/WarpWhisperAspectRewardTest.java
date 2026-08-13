package com.thaumcraftmodern.warp;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WarpWhisperAspectRewardTest {
    @Test
    void whisperRewardsOnePointOfEveryPrimalAspect() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        WarpEvents.grantWhisperPrimalPoints(knowledge);

        for (PrimalAspect primal : PrimalAspect.ordered()) {
            assertEquals(1, knowledge.aspectAmount(primal.id()), primal.id());
        }
        assertEquals(PrimalAspect.ordered().size(), knowledge.knownAspects().size());
    }
}
