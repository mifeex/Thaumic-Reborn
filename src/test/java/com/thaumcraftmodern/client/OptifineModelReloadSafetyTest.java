package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptifineModelReloadSafetyTest {
    private static final Path MODELS = Path.of(
            "src/main/resources/assets/thaumic_reborn/models"
    );

    @Test
    void thaumometerUsesDirectRendererInsteadOfForgeObjBaker()
            throws IOException {
        JsonObject model = read("item/thaumometer.json");

        assertEquals(
                "minecraft:builtin/entity",
                model.get("parent").getAsString()
        );
        assertFalse(model.has("loader"));
    }

    @Test
    void runicMatrixUsesVanillaElementsInsteadOfForgeObjBaker()
            throws IOException {
        JsonObject model = read("block/runic_matrix.json");

        assertFalse(model.has("loader"));
        assertTrue(model.has("elements"));
        assertEquals(8, model.getAsJsonArray("elements").size());
    }

    @Test
    void arcaneAlembicUsesReloadSafeObjBaker() throws IOException {
        assertReloadSafeObj("block/arcane_alembic.json");
        assertReloadSafeObj("item/arcane_alembic.json");
    }

    @Test
    void scannerDocumentsItsTwoCoplanarScreenFaces() throws IOException {
        List<String> lines = Files.readAllLines(Path.of(
                "src/main/resources/assets/thaumic_reborn/"
                        + "textures/models/scanner.obj"
        ));
        int screenGroup = lines.indexOf("g scanscreen");
        long screenFaces = lines.subList(screenGroup, lines.size())
                .stream()
                .filter(line -> line.startsWith("f "))
                .count();

        assertEquals(2, screenFaces);
    }

    private static void assertReloadSafeObj(String relativePath)
            throws IOException {
        JsonObject model = read(relativePath);
        assertEquals(
                "thaumic_reborn:reload_safe_obj",
                model.get("loader").getAsString()
        );
    }

    private static JsonObject read(String relativePath) throws IOException {
        return JsonParser.parseString(
                Files.readString(MODELS.resolve(relativePath))
        ).getAsJsonObject();
    }
}
