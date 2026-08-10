package com.thaumcraftmodern.aura;

import java.util.Objects;

/**
 * Dist-safe bridge from common aura-node lifecycle code to optional client
 * indexes. The no-op listener keeps dedicated servers free of client classes.
 */
public final class AuraNodeClientLifecycle {
    private static final Listener NO_OP = new Listener() {
        @Override
        public void changed(AuraNodeBlockEntity node) {
        }

        @Override
        public void removed(AuraNodeBlockEntity node) {
        }
    };

    private static Listener listener = NO_OP;

    private AuraNodeClientLifecycle() {
    }

    public static void install(Listener nextListener) {
        listener = Objects.requireNonNull(nextListener, "nextListener");
    }

    static void changed(AuraNodeBlockEntity node) {
        listener.changed(node);
    }

    static void removed(AuraNodeBlockEntity node) {
        listener.removed(node);
    }

    public interface Listener {
        void changed(AuraNodeBlockEntity node);

        void removed(AuraNodeBlockEntity node);
    }
}
