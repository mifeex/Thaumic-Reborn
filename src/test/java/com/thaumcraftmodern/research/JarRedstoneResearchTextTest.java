package com.thaumcraftmodern.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class JarRedstoneResearchTextTest {
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy");
    private static final Path LANG = Path.of(
            "src/main/resources/assets/thaumic_reborn/lang");

    @Test
    void jarResearchIncludesEnglishAndRussianRedstonePages() throws IOException {
        assertTrue(hasTextPage(read(RESEARCH.resolve("jarlabel.json")),
                "tc.research_page.JARLABEL.REDSTONE"));
        assertTrue(hasTextPage(read(RESEARCH.resolve("jarlabel.json")),
                "tc.research_page.JARLABEL.REDSTONE_IMAGE"));
        assertTrue(hasTextPage(read(RESEARCH.resolve("jarvoid.json")),
                "tc.research_page.JARVOID.REDSTONE"));

        JsonObject english = read(LANG.resolve("en_us.json"));
        JsonObject russian = read(LANG.resolve("ru_ru.json"));
        assertTrue(english.get("tc.research_page.JARLABEL.REDSTONE")
                .getAsString().contains("64 points — 10"));
        assertTrue(russian.get("tc.research_page.JARLABEL.REDSTONE")
                .getAsString().contains("64 единицы — 10"));
        assertTrue(english.get("tc.research_page.JARVOID.REDSTONE")
                .getAsString().contains("§l11§r"));
        assertTrue(russian.get("tc.research_page.JARVOID.REDSTONE")
                .getAsString().contains("§l11§r"));

        String imageMarkup = english.get(
                "tc.research_page.JARLABEL.REDSTONE_IMAGE").getAsString();
        assertTrue(imageMarkup.contains(
                "thaumic_reborn:textures/misc/jar_redstone_comparator.png"));
        assertTrue(russian.has("tc.research_page.JARLABEL.REDSTONE_IMAGE"));
        var image = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumic_reborn/textures/misc/"
                        + "jar_redstone_comparator.png").toFile());
        assertTrue(image != null && image.getWidth() == 256
                && image.getHeight() == 256);
    }

    private static boolean hasTextPage(JsonObject research, String body) {
        JsonArray pages = research.getAsJsonArray("pages");
        return pages.asList().stream()
                .map(element -> element.getAsJsonObject())
                .anyMatch(page -> "text".equals(page.get("type").getAsString())
                        && body.equals(page.get("body").getAsString()));
    }

    private static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
