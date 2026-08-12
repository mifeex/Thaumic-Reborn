package com.thaumcraftmodern.infusion;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfusionVisualAndRecipeFidelityTest {
    private static final Path ORIGINAL = Path.of(
            "reference/original/Thaumcraft_1.7.10_4.2.3.5.jar");

    @Test
    void originalInfuserAndPedestalTexturesRemainByteIdentical() throws Exception {
        assertJarEntryEquals("assets/thaumcraft/textures/models/infuser.png",
                Path.of("src/main/resources/assets/thaumcraftmodern/textures/block/infuser.png"));
        assertJarEntryEquals("assets/thaumcraft/textures/blocks/pedestal_side.png",
                Path.of("src/main/resources/assets/thaumcraftmodern/textures/block/pedestal_side.png"));
        assertJarEntryEquals("assets/thaumcraft/textures/blocks/pedestal_top.png",
                Path.of("src/main/resources/assets/thaumcraftmodern/textures/block/pedestal_top.png"));
        assertJarEntryEquals("assets/thaumcraft/textures/models/pillar.png",
                Path.of("src/main/resources/assets/thaumcraftmodern/textures/models/pillar.png"));
        assertJarEntryEquals("assets/thaumcraft/textures/models/pillar.obj",
                Path.of("src/main/resources/assets/thaumcraftmodern/textures/models/pillar.obj"));
    }

    @Test
    void modelsKeepExactTc4GeometryContracts() throws Exception {
        String pedestal = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/models/block/arcane_pedestal.json"));
        assertTrue(pedestal.contains("\"from\": [0, 0, 0]")
                && pedestal.contains("\"to\": [16, 4, 16]")
                && pedestal.contains("\"from\": [4, 4, 4]")
                && pedestal.contains("\"to\": [12, 12, 12]")
                && pedestal.contains("\"from\": [2, 12, 2]")
                && pedestal.contains("\"to\": [14, 16, 14]"));

        String matrix = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/RunicMatrixCubeModel.java"));
        assertTrue(matrix.contains("texOffs(0, 0).mirror()")
                && matrix.contains("texOffs(0, 32).mirror()")
                && matrix.contains("addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F)")
                && matrix.contains("LayerDefinition.create(mesh, 64, 64)"),
                "Runic Matrix must use the two exact TC4 ModelCube texture regions and dimensions");

        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/RunicMatrixBlockEntityRenderer.java"));
        assertTrue(!renderer.contains("LegacyObjMesh") && !renderer.contains("runic_matrix.obj"),
                "Runic Matrix must render the source ModelCube geometry directly");
        assertTrue(renderer.contains("-0.25F : 0.25F")
                && renderer.contains("pose.scale(0.45F, 0.45F, 0.45F)")
                && renderer.contains("Axis.XP.rotationDegrees(90.0F)")
                && renderer.contains("Axis.YP.rotationDegrees(90.0F)")
                && renderer.contains("Axis.ZP.rotationDegrees(90.0F)"),
                "Runic Matrix lost the original eight-cube transforms");
        assertTrue(renderer.contains("entityTranslucentEmissive(TEXTURE)")
                        && renderer.contains("model.renderOverlay("),
                "Runic Matrix must render TC4's animated emissive overlay with its glow mask");

        String pillarRenderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/InfusionPillarBlockEntityRenderer.java"));
        assertTrue(pillarRenderer.contains("textures/models/pillar.obj")
                        && pillarRenderer.contains("Axis.XN.rotationDegrees(90.0F)")
                        && pillarRenderer.contains("mesh.render(\"Box001\"")
                        && pillarRenderer.contains("case EAST -> 90.0F")
                        && pillarRenderer.contains("case NORTH -> 180.0F")
                        && pillarRenderer.contains("case WEST -> 270.0F")
                        && pillarRenderer.contains("default -> 0.0F"),
                "Infusion pillars must use the original two-block OBJ and orientation map");

        String clientEvents = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientModEvents.java"));
        assertTrue(clientEvents.contains("ModBlockEntities.INFUSION_PILLAR.get()")
                        && clientEvents.contains(
                        "InfusionPillarBlockEntityRenderer::new"),
                "Pillars must use the normal BlockEntity renderer dispatcher");

        Path pollingRenderer = Path.of("src/main/java/com/thaumcraftmodern/"
                + "client/render/InfusionPillarWorldRenderer.java");
        Path pollingMigration = Path.of("src/main/java/com/thaumcraftmodern/"
                + "client/InfusionPillarClientMigration.java");
        assertFalse(Files.exists(pollingRenderer));
        assertFalse(Files.exists(pollingMigration));

        String migration = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "InfusionPillarMigrationEvents.java"));
        assertTrue(migration.contains("onChunkLoad(ChunkEvent.Load event)")
                        && migration.contains("EntityCreationType.IMMEDIATE")
                        && migration.contains("markMigrated(chunk.getPos())"),
                "Legacy BlockEntities must be restored once per loaded chunk");
    }

    @Test
    void matrixUsesOriginalCraftRampHaloAndParticleSources() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/RunicMatrixBlockEntityRenderer.java"));
        assertTrue(!renderer.contains("VISUAL_Y_OFFSET"),
                "Runic Matrix renderer must not move the block-local TC4 mesh vertically");
        assertTrue(renderer.contains("Math.min(matrix.clientCraftTicks(), 50) / 50.0F")
                        && renderer.contains("renderHalo(")
                        && renderer.contains("matrix.clientCraftTicks() > 0")
                        && renderer.contains("InfusionMatrixVisualEffects.tick(matrix)"),
                "Matrix shake, halo and particles must follow and fade with TC4's client craftCount");
        assertTrue(renderer.contains("shouldRenderOffScreen(RunicMatrixBlockEntity matrix)")
                        && renderer.contains("return true;")
                        && renderer.contains("getViewDistance() { return 64; }"),
                "Runic Matrix model must remain renderable throughout the full 64-block range");

        String effects = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/particle/InfusionMatrixVisualEffects.java"));
        assertTrue(effects.contains("case ESSENTIA -> spawnEssentia")
                        && effects.contains("case COMPONENT -> spawnComponent")
                        && effects.contains("random.nextInt(3) == 0")
                        && effects.contains("source.getY() + 1.23D")
                        && effects.contains("matrix.getBlockPos().getY() + 0.5D")
                        && effects.contains("matrix.getY() - 0.5D"),
                "Essentia must enter the matrix while pedestal disintegration remains a distinct effect");

        String arc = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/particle/InfusionArcParticle.java"));
        assertTrue(arc.contains("TC4_RENDER_SCALE = 0.1F")
                        && !arc.contains("MODERN_QUAD_SCALE"),
                "Essentia trails must retain TC4's visible 0.1 render scale");

        for (String particle : new String[]{
                "InfusionBoreParticle.java", "InfusionRuneParticle.java"}) {
            String source = Files.readString(Path.of(
                    "src/main/java/com/thaumcraftmodern/client/particle/" + particle));
            assertTrue(source.contains("MODERN_QUAD_SCALE = 1.0F / 7.0F"),
                    particle + " must compensate for the modern particle quad scale");
        }

        String entity = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/RunicMatrixBlockEntity.java"));
        assertTrue(entity.contains("EffectType.ESSENTIA")
                        && entity.contains("EffectType.COMPONENT")
                        && entity.contains("clientStartUp -= clientStartUp / 5.0F")
                        && entity.contains("tag.putString(\"EffectType\""),
                "The server must explicitly synchronize the active TC4 FX phase");
    }

    @Test
    void executableRodRecipesAndThaumonomiconUseConfirmedOriginalNumbers()
            throws Exception {
        JsonObject reed = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumcraftmodern/thaumcraft/infusion_recipes/wand_rod_reed.json")))
                .getAsJsonObject();
        assertEquals(3, reed.get("instability").getAsInt());
        assertEquals(12, reed.getAsJsonObject("essentia").get("aer").getAsInt());
        assertEquals(6, reed.getAsJsonObject("essentia").get("praecantatio").getAsInt());
        assertEquals(6, reed.getAsJsonObject("essentia").get("motus").getAsInt());
        assertEquals("minecraft:sugar_cane",
                reed.getAsJsonObject("central").get("item").getAsString());

        JsonObject silverwood = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumcraftmodern/thaumcraft/infusion_recipes/wand_rod_silverwood.json")))
                .getAsJsonObject();
        assertEquals(5, silverwood.get("instability").getAsInt());
        assertEquals("thaumcraftmodern:silverwood_log",
                silverwood.getAsJsonObject("central").get("item").getAsString());
        assertEquals(7, silverwood.getAsJsonArray("components").size());
        assertEquals(7, silverwood.getAsJsonObject("essentia").size());
        silverwood.getAsJsonObject("essentia").entrySet().forEach(
                entry -> assertEquals(9, entry.getValue().getAsInt(), entry.getKey()));
        assertEquals("thaumcraftmodern:silverwood_wand_rod",
                silverwood.getAsJsonObject("result").get("item").getAsString());

        JsonObject research = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumcraftmodern/thaumcraft/research/legacy/infusion.json")))
                .getAsJsonObject();
        assertTrue(!research.get("inactive").getAsBoolean(),
                "Executable Infusion research was left inactive");
        JsonObject silverwoodResearch = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumcraftmodern/thaumcraft/research/legacy/rod_silverwood.json")))
                .getAsJsonObject();
        assertTrue(!silverwoodResearch.get("inactive").getAsBoolean(),
                "Executable Silverwood rod research was left inactive");
        assertEquals("infusion", silverwoodResearch.getAsJsonArray("pages")
                .get(1).getAsJsonObject().get("type").getAsString());
    }

    private static void assertJarEntryEquals(String entry, Path current) throws Exception {
        try (ZipFile jar = new ZipFile(ORIGINAL.toFile());
             InputStream original = jar.getInputStream(jar.getEntry(entry))) {
            assertEquals(hash(original.readAllBytes()), hash(Files.readAllBytes(current)), entry);
        }
    }

    private static String hash(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
