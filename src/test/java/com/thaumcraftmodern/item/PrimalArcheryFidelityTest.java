package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.PrimalArrowType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PrimalArcheryFidelityTest {
    private static final Path ARROW_SOURCE = Path.of(
            "src/main/java/com/thaumcraftmodern/entity/PrimalArrowEntity.java");
    private static final Path ITEM_REGISTRY = Path.of(
            "src/main/java/com/thaumcraftmodern/registry/ModItems.java");
    private static final Path BONE_BOW_SOURCE = Path.of(
            "src/main/java/com/thaumcraftmodern/item/BoneBowItem.java");

    @Test
    void boneBowUsesTc4DrawCurveAndStats() throws Exception {
        assertEquals(0.41666666F, BoneBowMechanics.powerForTime(5), 0.00001F);
        assertEquals(1.0F, BoneBowMechanics.powerForTime(10));
        assertEquals(10, BoneBowMechanics.FULL_DRAW_TICKS);
        assertEquals(19, BoneBowMechanics.FORCED_RELEASE_TICKS);
        assertEquals(2.5F, BoneBowMechanics.ARROW_VELOCITY);
        String source = Files.readString(BONE_BOW_SOURCE);
        assertTrue(source.contains("properties.durability(512)"));
        assertTrue(source.contains("return 3;"));
    }

    @Test
    void primalMetadataOrderAndDamageMatchTc4() {
        assertEquals(PrimalArrowType.AER, PrimalArrowType.byLegacyMetadata(0));
        assertEquals(PrimalArrowType.IGNIS, PrimalArrowType.byLegacyMetadata(1));
        assertEquals(PrimalArrowType.AQUA, PrimalArrowType.byLegacyMetadata(2));
        assertEquals(PrimalArrowType.TERRA, PrimalArrowType.byLegacyMetadata(3));
        assertEquals(PrimalArrowType.ORDO, PrimalArrowType.byLegacyMetadata(4));
        assertEquals(PrimalArrowType.PERDITIO, PrimalArrowType.byLegacyMetadata(5));
        assertEquals(1.5D, PrimalArrowType.TERRA.damageMultiplier());
        assertEquals(0.8D, PrimalArrowType.ORDO.damageMultiplier());
        assertEquals(0.8D, PrimalArrowType.PERDITIO.damageMultiplier());
    }

    @Test
    void allSixPlaceholdersAreRealArrowItems() throws Exception {
        String registry = Files.readString(ITEM_REGISTRY);
        for (String aspect : new String[]{"AER", "IGNIS", "AQUA", "TERRA", "ORDO", "PERDITIO"}) {
            assertTrue(registry.contains("new PrimalArrowItem(PrimalArrowType." + aspect));
        }
        assertTrue(registry.contains("new BoneBowItem("));
    }

    @Test
    void elementalHitEffectsRemainPresent() throws Exception {
        String source = Files.readString(ARROW_SOURCE);
        assertTrue(source.contains("MobEffects.MOVEMENT_SLOWDOWN, 200, 4"));
        assertTrue(source.contains("MobEffects.WEAKNESS, 200, 4"));
        assertTrue(source.contains("MobEffects.WITHER, 100"));
        assertTrue(source.contains("isOnFire() ? 10 : 5"));
        assertTrue(source.contains("PrimalArrowType.TERRA ? 1 : 0"));
        assertTrue(source.contains("AIR_ARROW"));
        assertTrue(source.contains("ORDER_ARROW"));
    }
}
