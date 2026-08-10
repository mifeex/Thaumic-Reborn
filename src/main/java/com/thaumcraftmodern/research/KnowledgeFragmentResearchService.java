package com.thaumcraftmodern.research;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;

import java.util.List;

/** Selects the same class of hidden, currently eligible research as TC4. */
public final class KnowledgeFragmentResearchService {
    private KnowledgeFragmentResearchService() {
    }

    public static List<ResearchDefinition> candidates(PlayerThaumKnowledge knowledge) {
        return ResearchRegistry.all().stream()
                .filter(definition -> !definition.inactive())
                .filter(definition -> !definition.autoUnlock())
                .filter(definition -> definition.nodeFrame()
                        == ResearchDefinition.NodeFrame.HIDDEN)
                .filter(definition -> !knowledge.hasCompletedResearch(definition.id()))
                .filter(definition -> definition.parents().stream()
                        .allMatch(knowledge::hasCompletedResearch))
                .filter(definition -> definition.hiddenParents().stream()
                        .allMatch(knowledge::hasCompletedResearch))
                .filter(definition -> definition.revealWhen().test(knowledge))
                .filter(definition -> definition.unlockWhen().test(knowledge))
                .toList();
    }
}
