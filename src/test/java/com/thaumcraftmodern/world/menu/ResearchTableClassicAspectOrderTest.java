package com.thaumcraftmodern.world.menu;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableClassicAspectOrderTest {
    @Test
    void paletteUsesTc4GetAspectsSortedTagOrder()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/menu/ResearchTableMenu.java"
        ));

        assertTrue(source.contains(
                ".map(com.thaumcraftmodern.aspect.AspectDefinition::id)\n"
                        + "                // TC4 AspectList.getAspectsSorted() compares Aspect.getTag()\n"
                        + "                // with String.compareTo; modern aspect IDs are those tags.\n"
                        + "                .sorted()\n"
                        + "                .toList()"
        ));
    }

    @Test
    void bundledPaletteExactlyMatchesTc4LexicographicTagOrder()
            throws Exception {
        List<String> expected = List.of(
                "aer", "alienis", "aqua", "arbor", "auram", "bestia",
                "cognitio", "corpus", "exanimis", "fabrico", "fames", "gelum",
                "herba", "humanus", "ignis", "instrumentum", "iter", "limus",
                "lucrum", "lux", "machina", "messis", "metallum", "meto",
                "mortuus", "motus", "ordo", "pannus", "perditio", "perfodio",
                "permutatio", "potentia", "praecantatio", "sano", "sensus",
                "spiritus", "telum", "tempestas", "tenebrae", "terra", "tutamen",
                "vacuos", "venenum", "victus", "vinculum", "vitium", "vitreus",
                "volatus"
        );
        Path aspects = Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/aspects"
        );
        List<String> actual;
        try (var files = Files.list(aspects)) {
            actual = files.filter(path -> path.toString().endsWith(".json"))
                    .map(ResearchTableClassicAspectOrderTest::aspectId)
                    .sorted()
                    .toList();
        }
        assertEquals(expected, actual);
    }

    private static String aspectId(Path path) {
        try {
            return JsonParser.parseString(Files.readString(path))
                    .getAsJsonObject().get("id").getAsString();
        } catch (java.io.IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }
}
