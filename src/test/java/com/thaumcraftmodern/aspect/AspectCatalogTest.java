package com.thaumcraftmodern.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.thaumcraftmodern.testing.AspectFixtures;

class AspectCatalogTest {
    @Test
    void compositionIsOrderIndependentAndReturnsClassicCompound() {
        AspectCatalog catalog = AspectFixtures.firstDiscoveryCatalog();

        assertEquals("lux", catalog.compositionResult("aer", "ignis").orElseThrow().id());
        assertEquals("lux", catalog.compositionResult("ignis", "aer").orElseThrow().id());
        assertEquals("potentia", catalog.compositionResult("ignis", "ordo").orElseThrow().id());
        assertTrue(catalog.compositionResult("aer", "ordo").isEmpty());
    }

    @Test
    void relatedOnlyRecognizesDirectComponentRelationships() {
        AspectCatalog catalog = AspectFixtures.firstDiscoveryCatalog();

        assertTrue(catalog.related("aer", "lux"));
        assertTrue(catalog.related("lux", "aer"));
        assertTrue(catalog.related("ignis", "potentia"));

        assertFalse(catalog.related("aer", "ignis"));
        assertFalse(catalog.related("lux", "potentia"));
        assertFalse(catalog.related("ignis", "ignis"));
        assertFalse(catalog.related("missing", "lux"));
    }

    @Test
    void definitionsRequireZeroOrTwoComponentsAndKnownReferences() {
        assertThrows(IllegalArgumentException.class, () -> new AspectDefinition(
                "invalid",
                0x123456,
                "thaumic_reborn:textures/aspects/invalid.png",
                List.of("aer")));

        AspectDefinition aer = new AspectDefinition(
                "aer",
                0xFFFF7E,
                "thaumic_reborn:textures/aspects/aer.png");
        AspectDefinition invalidCompound = new AspectDefinition(
                "invalid",
                0x123456,
                "thaumic_reborn:textures/aspects/invalid.png",
                "aer",
                "missing");

        assertThrows(
                IllegalArgumentException.class,
                () -> new AspectCatalog(List.of(aer, invalidCompound)));
    }
}
