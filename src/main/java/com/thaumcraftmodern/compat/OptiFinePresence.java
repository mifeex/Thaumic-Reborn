package com.thaumcraftmodern.compat;

/** Dist-safe OptiFine presence check without linking common code to clients. */
public final class OptiFinePresence {
    private static final boolean LOADED = detect();

    private OptiFinePresence() {
    }

    public static boolean loaded() {
        return LOADED;
    }

    private static boolean detect() {
        try {
            Class.forName("net.optifine.Config", false,
                    OptiFinePresence.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
