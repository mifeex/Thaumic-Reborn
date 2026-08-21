package com.thaumcraftmodern.infusion;

/** Exact TC4 TileInfusionMatrix instability roll table and probability. */
public final class InfusionInstability {
    public enum Event {
        EJECT,
        EJECT_GOO,
        EJECT_GAS,
        DESTROY_GOO,
        DESTROY_GAS,
        EJECT_EXPLODE,
        ZAP_ONE,
        ZAP_ALL,
        HARM_ONE,
        HARM_ALL,
        MATRIX_EXPLOSION,
        WARP
    }

    private InfusionInstability() {
    }

    public static boolean triggers(int instability, int rollOutOf500) {
        if (rollOutOf500 < 0 || rollOutOf500 >= 500) {
            throw new IllegalArgumentException("Instability roll must be in [0, 500)");
        }
        return instability > 0 && rollOutOf500 <= instability;
    }

    public static boolean triggers(float instability, float rollOutOf500) {
        if (rollOutOf500 < 0.0F || rollOutOf500 >= 500.0F) {
            throw new IllegalArgumentException("Instability roll must be in [0, 500)");
        }
        return instability > 0.0F && rollOutOf500 <= instability;
    }

    public static Event eventForRoll(int rollOutOf21) {
        return switch (rollOutOf21) {
            case 0, 2, 10, 13 -> Event.EJECT;
            case 6, 17 -> Event.EJECT_GOO;
            case 1, 11 -> Event.EJECT_GAS;
            case 3, 8, 14 -> Event.ZAP_ONE;
            case 5, 16 -> Event.HARM_ONE;
            case 12 -> Event.ZAP_ALL;
            case 19 -> Event.DESTROY_GOO;
            case 7 -> Event.DESTROY_GAS;
            case 4, 15 -> Event.EJECT_EXPLODE;
            case 18 -> Event.HARM_ALL;
            case 9 -> Event.MATRIX_EXPLOSION;
            case 20 -> Event.WARP;
            default -> throw new IllegalArgumentException(
                    "Instability event roll must be in [0, 21)"
            );
        };
    }

    public static int increaseCapped(int instability) {
        return Math.min(25, instability + 1);
    }

    public static float increaseCapped(float instability) {
        return Math.min(25.0F, instability + 1.0F);
    }
}
