package com.thaumcraftmodern.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationCompletenessTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path LANG = RESOURCES.resolve(
            "assets/thaumic_reborn/lang"
    );
    private static final Path CONTENT = RESOURCES.resolve(
            "data/thaumic_reborn/thaumcraft"
    );

    @Test
    void englishAndRussianLocalesHaveIdenticalKeys() throws IOException {
        Set<String> english = localeKeys(LANG.resolve("en_us.json"));
        Set<String> russian = localeKeys(LANG.resolve("ru_ru.json"));

        assertEquals(english, russian);
    }

    @Test
    void thaumcraftContentDoesNotReferenceMissingModLocalization() throws IOException {
        Set<String> localized = localeKeys(LANG.resolve("en_us.json"));
        Set<String> referenced = new LinkedHashSet<>();
        try (Stream<Path> files = Files.walk(CONTENT)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    collectLocalizationReferences(
                            JsonParser.parseReader(reader),
                            referenced
                    );
                }
            }
        }

        Set<String> missing = new LinkedHashSet<>(referenced);
        missing.removeAll(localized);
        assertTrue(missing.isEmpty(), "Missing localization keys: " + missing);
    }

    @Test
    void classicAspectNamesAreNeverTranslated() throws IOException {
        JsonObject english = locale(LANG.resolve("en_us.json"));
        JsonObject russian = locale(LANG.resolve("ru_ru.json"));
        Path aspects = CONTENT.resolve("aspects");

        try (Stream<Path> files = Files.list(aspects)) {
            for (Path file : files
                    .filter(path -> path.toString().endsWith(".json"))
                    .toList()) {
                JsonObject definition;
                try (Reader reader = Files.newBufferedReader(file)) {
                    definition = JsonParser.parseReader(reader)
                            .getAsJsonObject();
                }
                String id = definition.get("id").getAsString();
                String classicName = Character.toUpperCase(id.charAt(0))
                        + id.substring(1);
                String key = "aspect.thaumic_reborn." + id;

                assertTrue(english.has(key), "Missing English aspect name: " + key);
                assertTrue(russian.has(key), "Missing Russian aspect name: " + key);
                assertEquals(classicName, english.get(key).getAsString());
                assertEquals(classicName, russian.get(key).getAsString());
            }
        }
    }

    private static Set<String> localeKeys(Path path) throws IOException {
        return new LinkedHashSet<>(locale(path).keySet());
    }

    private static JsonObject locale(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static void collectLocalizationReferences(
            JsonElement element,
            Set<String> result
    ) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child ->
                    collectLocalizationReferences(child, result)
            );
            return;
        }
        if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet().forEach(entry ->
                    collectLocalizationReferences(entry.getValue(), result)
            );
            return;
        }
        if (!element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isString()) {
            return;
        }
        String value = element.getAsString();
        if (value.startsWith("tc.")
                || value.contains(".thaumic_reborn.")) {
            result.add(value);
        }
    }
}
