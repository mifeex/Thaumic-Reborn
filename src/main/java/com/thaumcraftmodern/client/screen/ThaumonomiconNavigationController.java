package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.research.ResearchDefinition;
import java.util.Optional;
import java.util.function.Function;

/** Owns open-research history and two-page navigation state. */
final class ThaumonomiconNavigationController {
    private final ThaumonomiconNavigationHistory history =
            new ThaumonomiconNavigationHistory();
    private ResearchDefinition research;
    private int pagePair;

    ResearchDefinition research() {
        return research;
    }

    int pagePair() {
        return pagePair;
    }

    int depth() {
        return history.depth();
    }

    void openRoot(ResearchDefinition target) {
        research = target;
        pagePair = 0;
        history.clear();
    }

    void openLinked(ResearchDefinition target, String categoryId) {
        if (research == null) {
            return;
        }
        history.push(research.id(), pagePair, categoryId);
        research = target;
        pagePair = 0;
    }

    BackResult back(
            Function<String, Optional<ResearchDefinition>> resolver
    ) {
        String fromResearchId = research == null ? "<tree>" : research.id();
        Optional<ThaumonomiconNavigationHistory.Location> location =
                history.pop();
        if (location.isEmpty()) {
            research = null;
            pagePair = 0;
            return new BackResult(
                    fromResearchId, null, 0, "", history.depth(), false
            );
        }

        ThaumonomiconNavigationHistory.Location previous = location.get();
        ResearchDefinition resolved = resolver.apply(previous.researchId())
                .orElse(null);
        if (resolved == null) {
            history.clear();
            research = null;
            pagePair = 0;
            return new BackResult(
                    fromResearchId, null, 0, "", history.depth(), true
            );
        }

        research = resolved;
        pagePair = previous.pagePair();
        return new BackResult(
                fromResearchId,
                resolved,
                pagePair,
                previous.categoryId(),
                history.depth(),
                true
        );
    }

    Optional<PageChange> previousPage() {
        if (research == null || pagePair <= 0) {
            return Optional.empty();
        }
        int previous = pagePair;
        pagePair = Math.max(0, pagePair - 2);
        return Optional.of(new PageChange(previous, pagePair));
    }

    Optional<PageChange> nextPage() {
        if (research == null || pagePair + 2 >= research.pages().size()) {
            return Optional.empty();
        }
        int previous = pagePair;
        pagePair += 2;
        return Optional.of(new PageChange(previous, pagePair));
    }

    void refresh(Function<String, Optional<ResearchDefinition>> resolver) {
        if (research == null) {
            return;
        }
        research = resolver.apply(research.id()).orElse(null);
        if (research == null) {
            history.clear();
            pagePair = 0;
        }
    }

    record PageChange(int previousPagePair, int pagePair) {
    }

    record BackResult(
            String fromResearchId,
            ResearchDefinition research,
            int pagePair,
            String categoryId,
            int remainingDepth,
            boolean usedHistory
    ) {
    }
}
