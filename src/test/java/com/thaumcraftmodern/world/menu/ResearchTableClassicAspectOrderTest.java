package com.thaumcraftmodern.world.menu;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableClassicAspectOrderTest {
    @Test
    void paletteUsesTc4AspectRegistrationOrderInsteadOfAlphabeticalIds()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/menu/ResearchTableMenu.java"
        ));

        assertTrue(source.contains(
                ".comparingInt(com.thaumcraftmodern.aspect.AspectDefinition::order)\n"
                        + "                        .thenComparing(com.thaumcraftmodern.aspect.AspectDefinition::id))\n"
                        + "                .map(com.thaumcraftmodern.aspect.AspectDefinition::id)\n"
                        + "                .toList()"
        ));
        assertTrue(!source.contains(
                ".map(com.thaumcraftmodern.aspect.AspectDefinition::id)\n"
                        + "                .sorted()"
        ));
    }

    @Test
    void bundledAspectOrdersExactlyMatchTc4AspectDeclarationOrder()
            throws Exception {
        List<String> expected = List.of(
                "aer", "terra", "ignis", "aqua", "ordo", "perditio",
                "vacuos", "lux", "tempestas", "motus", "gelum", "vitreus",
                "victus", "venenum", "potentia", "permutatio", "metallum",
                "mortuus", "volatus", "tenebrae", "spiritus", "sano", "iter",
                "alienis", "praecantatio", "auram", "vitium", "limus", "herba",
                "arbor", "bestia", "corpus", "exanimis", "cognitio", "sensus",
                "humanus", "messis", "perfodio", "instrumentum", "meto", "telum",
                "tutamen", "fames", "lucrum", "fabrico", "pannus", "machina",
                "vinculum"
        );
        Path aspects = Path.of(
                "src/main/resources/data/thaumcraftmodern/thaumcraft/aspects"
        );
        List<OrderedAspect> actual = new ArrayList<>();
        try (var files = Files.list(aspects)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                var json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                actual.add(new OrderedAspect(
                        json.get("id").getAsString(),
                        json.get("order").getAsInt()
                ));
            }
        }
        actual.sort(Comparator.comparingInt(OrderedAspect::order)
                .thenComparing(OrderedAspect::id));
        assertEquals(expected, actual.stream().map(OrderedAspect::id).toList());
    }

    private record OrderedAspect(String id, int order) {
    }
}
