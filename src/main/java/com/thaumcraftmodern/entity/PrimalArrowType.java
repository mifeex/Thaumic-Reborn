package com.thaumcraftmodern.entity;

/** The TC4 metadata order used by ItemPrimalArrow and EntityPrimalArrow. */
public enum PrimalArrowType {
    AER(0, 0xFFFF7E, 1.0D),
    IGNIS(1, 0xFF5A01, 1.0D),
    AQUA(2, 0x3CD4FC, 1.0D),
    TERRA(3, 0x56C000, 1.5D),
    ORDO(4, 0xD5D4EC, 0.8D),
    PERDITIO(5, 0x6E397E, 0.8D);

    private static final PrimalArrowType[] VALUES = values();

    private final int legacyMetadata;
    private final int color;
    private final double damageMultiplier;

    PrimalArrowType(int legacyMetadata, int color, double damageMultiplier) {
        this.legacyMetadata = legacyMetadata;
        this.color = color;
        this.damageMultiplier = damageMultiplier;
    }

    public int legacyMetadata() {
        return legacyMetadata;
    }

    public int color() {
        return color;
    }

    public double damageMultiplier() {
        return damageMultiplier;
    }

    public static PrimalArrowType byLegacyMetadata(int metadata) {
        return metadata >= 0 && metadata < VALUES.length ? VALUES[metadata] : AER;
    }
}
