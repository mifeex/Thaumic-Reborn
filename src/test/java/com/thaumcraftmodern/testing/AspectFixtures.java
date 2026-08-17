package com.thaumcraftmodern.testing;

import java.util.List;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.aspect.AspectDefinition;

public final class AspectFixtures {
    private AspectFixtures() {
    }

    public static AspectCatalog firstDiscoveryCatalog() {
        return new AspectCatalog(List.of(
                primal("aer", 0xFFFF7E),
                primal("terra", 0x56C000),
                primal("ignis", 0xFF5A01),
                primal("aqua", 0x3CD4FC),
                primal("ordo", 0xD5D4EC),
                primal("perditio", 0x404040),
                compound("lux", 0xFFF663, "aer", "ignis"),
                compound("potentia", 0xC0FFFF, "ordo", "ignis")));
    }

    private static AspectDefinition primal(String id, int color) {
        return new AspectDefinition(id, color, icon(id));
    }

    private static AspectDefinition compound(
            String id,
            int color,
            String firstComponent,
            String secondComponent) {
        return new AspectDefinition(id, color, icon(id), firstComponent, secondComponent);
    }

    private static String icon(String id) {
        return "thaumic_reborn:textures/aspects/" + id + ".png";
    }
}
