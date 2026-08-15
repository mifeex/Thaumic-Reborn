package com.thaumcraftmodern.worldgen.outerlands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class OuterLandsPortalAllocationDataTest {
    @Test
    void everyNewPortalGetsAUniquePersistentMazeRegion() {
        OuterLandsPortalAllocationData data =
                new OuterLandsPortalAllocationData();
        var first = data.allocate();
        var second = data.allocate();

        assertEquals(new OuterLandsPortalAllocationData.Destination(256, 0), first);
        assertEquals(new OuterLandsPortalAllocationData.Destination(257, 0), second);
        assertNotEquals(first, second);

        CompoundTag saved = data.save(new CompoundTag());
        OuterLandsPortalAllocationData loaded =
                OuterLandsPortalAllocationData.load(saved);
        assertEquals(
                new OuterLandsPortalAllocationData.Destination(258, 0),
                loaded.allocate()
        );
    }

    @Test
    void allocatorContinuesOnTheNextRowWithoutReusingRegions() {
        OuterLandsPortalAllocationData data =
                new OuterLandsPortalAllocationData();
        OuterLandsPortalAllocationData.Destination destination = null;
        for (int index = 0; index <= 256; index++) {
            destination = data.allocate();
        }
        assertEquals(
                new OuterLandsPortalAllocationData.Destination(256, 1),
                destination
        );
    }
}
