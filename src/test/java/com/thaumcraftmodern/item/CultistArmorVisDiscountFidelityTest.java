package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CultistArmorVisDiscountFidelityTest {
    @Test
    void robesAndSharedBootsKeepTheOriginalOnePercentDiscount()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/CultistArmorItem.java"
        ));

        assertTrue(source.contains("implements VisDiscountGear"));
        assertTrue(source.contains(
                "CULTIST_ROBE_VIS_DISCOUNT_PERCENT = 1"));
        assertTrue(source.contains(
                "set == Set.CLERIC || set == Set.BOOTS"));
        assertTrue(source.contains("Component.translatable(\"tc.visdiscount\")"));
        assertTrue(source.contains("public int classicWarp()"));
    }
}
