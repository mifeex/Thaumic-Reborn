package com.thaumcraftmodern.essentia;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EssentiaTransportDefinitionTest {
    private static final ResourceLocation ALEMBIC = new ResourceLocation(
            "thaumic_reborn", "arcane_alembic");

    @Test
    void arcaneAlembicIsDeclaredAsSourceOnly() throws Exception {
        Path path = Path.of("src/main/resources/data/thaumic_reborn/thaumcraft/"
                + "essentia_transports/arcane_alembic.json");
        JsonObject json = JsonParser.parseString(Files.readString(path))
                .getAsJsonObject();
        assertFalse(json.get("canReturnEssentia").getAsBoolean());
        assertTrue(ALEMBIC.toString().equals(json.get("block").getAsString()));
    }

    @Test
    void registryDefaultsToReturnableAndHonoursSourceOnlyFlag() {
        EssentiaTransportRegistry.replace(List.of(
                new EssentiaTransportDefinition(ALEMBIC, false)));
        assertFalse(EssentiaTransportRegistry.canReturnEssentia(ALEMBIC));
        assertTrue(EssentiaTransportRegistry.canReturnEssentia(
                new ResourceLocation("thaumic_reborn", "warded_jar")));
    }
}
