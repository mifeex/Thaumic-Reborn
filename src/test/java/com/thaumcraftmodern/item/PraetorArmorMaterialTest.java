package com.thaumcraftmodern.item;

import net.minecraft.world.item.ArmorItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PraetorArmorMaterialTest {
    @Test
    void usesRequestedDefenseAndIronDurabilityPlusTwenty() {
        assertEquals(3, PraetorArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.HELMET));
        assertEquals(7, PraetorArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.CHESTPLATE));
        assertEquals(6, PraetorArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.LEGGINGS));
        assertEquals(0.0F, PraetorArmorMaterial.INSTANCE.getToughness());
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            assertEquals(baseDurability(type) * 15 + 20,
                    PraetorArmorMaterial.INSTANCE.getDurabilityForType(type));
        }
    }

    private static int baseDurability(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 13;
            case LEGGINGS -> 15;
            case CHESTPLATE -> 16;
            case HELMET -> 11;
        };
    }
}
