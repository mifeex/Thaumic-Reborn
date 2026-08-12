package com.thaumcraftmodern.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EssentiaTubeModelFidelityTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumcraftmodern");
    private static final List<String> BLOCK_MODELS = List.of(
            "essentia_tube", "essentia_tube_arm",
            "filtered_essentia_tube", "restricted_essentia_tube",
            "restricted_essentia_tube_arm", "one_way_essentia_tube",
            "essentia_valve");
    private static final List<String> ITEM_MODELS = List.of(
            "essentia_tube", "filtered_essentia_tube",
            "restricted_essentia_tube", "one_way_essentia_tube",
            "essentia_valve");

    @Test
    void everyWorldFaceUsesADeclaredTextureVariable() throws Exception {
        for (String name : BLOCK_MODELS) {
            JsonObject model = read("models/block/" + name + ".json");
            JsonObject textures = model.getAsJsonObject("textures");
            for (JsonElement element : model.getAsJsonArray("elements")) {
                JsonObject faces = element.getAsJsonObject()
                        .getAsJsonObject("faces");
                for (String side : faces.keySet()) {
                    String reference = faces.getAsJsonObject(side)
                            .get("texture").getAsString();
                    assertTrue(reference.startsWith("#"),
                            name + " has a raw face texture: " + reference);
                    assertTrue(textures.has(reference.substring(1)),
                            name + " does not declare " + reference);
                }
            }
        }
    }

    @Test
    void inventoryModelsUseThePreviousStraightTc4Geometry()
            throws Exception {
        JsonObject base = read("models/block/essentia_tube_base.json");
        JsonArray elements = base.getAsJsonArray("elements");
        assertEquals(2, elements.size());
        assertEquals(List.of(7, 0, 7), vector(elements.get(0), "from"));
        assertEquals(List.of(9, 16, 9), vector(elements.get(0), "to"));
        assertEquals(0.625F, base.getAsJsonObject("display")
                .getAsJsonObject("gui").getAsJsonArray("scale")
                .get(0).getAsFloat());
        for (String name : ITEM_MODELS) {
            assertNotEquals("minecraft:item/generated",
                    read("models/item/" + name + ".json")
                            .get("parent").getAsString());
        }
    }

    @Test
    void valveItemUsesTheOriginalThinWheelProportions() throws Exception {
        JsonObject valve = read("models/item/essentia_valve.json");
        assertEquals("thaumcraftmodern:block/pipe_2",
                valve.getAsJsonObject("textures").get("rod").getAsString());
        assertEquals("thaumcraftmodern:block/pipe_valve",
                valve.getAsJsonObject("textures").get("wheel").getAsString());
        JsonArray elements = valve.getAsJsonArray("elements");
        assertEquals(4, elements.size());
        assertEquals(List.of(6.5F, 6.5F, 4.0F),
                floatVector(elements.get(2), "from"));
        assertEquals(List.of(9.5F, 9.5F, 6.5F),
                floatVector(elements.get(2), "to"));
        assertEquals(List.of(4.0F, 4.0F, 3.2F),
                floatVector(elements.get(3), "from"));
        assertEquals(List.of(12.0F, 12.0F, 4.0F),
                floatVector(elements.get(3), "to"));
        assertEquals(List.of(0, 0, 16, 16),
                vector(elements.get(3).getAsJsonObject()
                        .getAsJsonObject("faces").get("north"), "uv"));
        assertEquals(List.of(0, 0, 0), vector(valve.getAsJsonObject("display")
                .getAsJsonObject("gui"), "translation"));
    }

    @Test
    void allFiveInventoryVariantsKeepDistinctClassicBindings()
            throws Exception {
        String previous = null;
        for (String name : ITEM_MODELS) {
            String textures = read("models/item/" + name + ".json")
                    .getAsJsonObject("textures").toString();
            if (previous != null) assertNotEquals(previous, textures);
            previous = textures;
        }
    }

    @Test
    void originalPipeTexturesArePackaged() throws Exception {
        for (String texture : List.of("pipe_1", "pipe_2", "pipe_3",
                "pipe_filter", "pipe_filter_core", "pipe_oneway",
                "pipe_restrict", "pipe_valve")) {
            assertTrue(Files.size(ASSETS.resolve(
                    "textures/block/" + texture + ".png")) > 0);
        }
        assertTrue(Files.size(ASSETS.resolve(
                "textures/model/valve.png")) > 0);
        assertEquals("2b8f4114c5e5a0d18772e9282e92d8b993860a69e5676e01e7c354424ea947e2",
                sha256(ASSETS.resolve("textures/block/pipe_filter_core.png")));
        assertEquals("2ac17e05bff6dd0bda25f6f77355e4dd64dd13e71c85c4279235ecd47940205c",
                sha256(ASSETS.resolve("textures/model/valve.png")));
        assertEquals("866ba8f9110ebac108c59d1698b257c2c6700996fe76f06af470a81453668c5d",
                sha256(ASSETS.resolve("textures/block/pipe_restrict.png")));
        assertEquals("f82122f921bcc0260d92ab9de99213bd3ee4b204981db9899fecb342aac2c16a",
                sha256(ASSETS.resolve("textures/block/pipe_oneway.png")));
    }

    @Test
    void specialTubeModelsExposeTheirOriginalVisualState() throws Exception {
        JsonObject filter = read("models/block/filtered_essentia_tube.json");
        assertEquals(1, filter.getAsJsonArray("elements").size());
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EssentiaTubeBlockEntityRenderer.java"));
        assertTrue(renderer.contains("renderFilterCore(tube"));
        assertTrue(renderer.contains("AspectRegistryRuntime.find(tube.filter())"));
        assertTrue(renderer.contains("tube.filter() == null ? 0xFFFFFF"));
        assertTrue(renderer.contains("float near = 2.0F/16.0F"));
        assertTrue(renderer.contains("float far = 4.0F/16.0F"));
        assertTrue(renderer.contains("float shaftFar = Math.max(near, far - retraction)"));
        assertTrue(renderer.contains(
                "tubeCuboid(buffers.getBuffer(RenderType.entityCutoutNoCull(PIPE))"));

        String oneWay = read("blockstates/one_way_essentia_tube.json")
                .toString();
        assertFalse(oneWay.contains("one_way_essentia_tube_indicator"));
        assertTrue(renderer.contains("renderOneWayRings(tube"));
        assertTrue(renderer.contains("getValue(EssentiaTubeBlock.FACING)"));
        assertTrue(renderer.contains("face.getOpposite()\n        ).isEmpty()"));
        assertTrue(renderer.contains("Axis.XP.rotationDegrees(-90.0F)"));
        assertTrue(renderer.contains("90.0F * face.getStepY()"));
        assertTrue(renderer.contains("for (int ring = 0; ring < 3; ring++)"));
        assertTrue(renderer.contains(".texOffs(0, 10)"));
        assertFalse(renderer.contains("BLUE_RIB"));
        assertFalse(renderer.contains("renderRestrictedRibs"));

        String block = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/EssentiaTubeBlock.java"));
        String entity = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "EssentiaTubeBlockEntity.java"));
        assertTrue(block.contains(
                "setValue(FACING, context.getClickedFace())"));
        assertTrue(entity.contains(
                "facing = state.getValue(EssentiaTubeBlock.FACING)"));

        String valve = read("blockstates/essentia_valve.json").toString();
        assertTrue(renderer.contains("renderValve(tube"));
        assertEquals(1, read("models/block/essentia_valve.json")
                .getAsJsonArray("elements").size());
        JsonElement valveJoint = read("models/block/essentia_valve.json")
                .getAsJsonArray("elements").get(0);
        assertEquals(List.of(6, 6, 6), vector(valveJoint, "from"));
        assertEquals(List.of(10, 10, 10), vector(valveJoint, "to"));
        assertTrue(renderer.contains("tube.valveRotation(partialTick)"));
        assertTrue(renderer.contains("Math.toRadians(-rotation * 1.5F)"));
        assertTrue(renderer.contains("rotation / 360.0F * 0.12F"));
        assertTrue(renderer.contains("extrudedWheel(out"));
        assertTrue(renderer.contains("for(int i=0;i<16;i++)"));
        assertTrue(renderer.contains("0.05F,light,overlay"));
    }

    @Test
    void valveUsesOriginalToolInteractionsSoundsAndAnimation() throws Exception {
        String block = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/EssentiaTubeBlock.java"));
        String entity = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "EssentiaTubeBlockEntity.java"));
        assertTrue(block.contains("TubeWandTargetResolver.hitsCore(x, y, z)"));
        assertTrue(block.contains("implements WandInteractable"));
        assertTrue(block.contains("onWandRightClick("));
        assertTrue(block.contains("TubeFacingRules.toggleFacing("));
        assertTrue(block.contains("tube.rotateFacing()"));
        assertTrue(block.contains("tube.toggleSide(resolveWandSide(hit, pos))"));
        assertTrue(block.contains("ModSounds.TOOL.get()"));
        assertTrue(block.contains("tube.setFlowAllowed(!tube.flowAllowed())"));
        assertFalse(block.contains("hitsValveHandle"));
        assertFalse(block.contains("shape = Shapes.or(shape, valveHandleShape"));
        assertTrue(block.contains("shape = Shapes.or(shape, arm(direction))"));
        assertFalse(block.contains("double minX = 6.0D"));
        assertTrue(entity.contains("ModSounds.SQUEEK.get()"));
        assertTrue(entity.contains("0.9F + level.random.nextFloat() * 0.2F"));
        assertTrue(entity.contains("tube.valveRotation + 20.0F"));
        assertTrue(entity.contains("tube.valveRotation - 20.0F"));
        assertTrue(entity.contains("TubeFacingRules.nextConnectedFacing"));
        assertTrue(entity.contains("side -> isSideOpen(side)"));
        assertTrue(entity.contains("instanceof EssentiaTransport"));
        assertEquals("3ca72a307104786730e6993db9a232aec9d433fc0f0f5c0348a4165746a0936f",
                sha256(ASSETS.resolve("sounds/squeek1.ogg")));
        assertEquals("320dbe9cf411452788af2b3db8bcbdb0d7a102078020c1f5f013896461d19296",
                sha256(ASSETS.resolve("sounds/squeek2.ogg")));
    }

    @Test
    void ventingRestoresOriginalColoredFxAndTubeSounds() throws Exception {
        JsonObject sounds = read("sounds.json");
        assertEquals(List.of("thaumcraftmodern:creak1", "thaumcraftmodern:creak2"),
                sounds.getAsJsonObject("creak").getAsJsonArray("sounds")
                        .asList().stream().map(JsonElement::getAsString).toList());
        assertEquals(List.of("thaumcraftmodern:tool1", "thaumcraftmodern:tool2"),
                sounds.getAsJsonObject("tool").getAsJsonArray("sounds")
                        .asList().stream().map(JsonElement::getAsString).toList());
        assertEquals("39aae2eab75446cf6d1aa716b5b03e75ed291b0c9ce35b02e0ff676903e9744c",
                sha256(ASSETS.resolve("sounds/creak1.ogg")));
        assertEquals("205f6b530af60a69d0d11abec559038ff55ec3f1c5edb3985c5a25eebdb63e08",
                sha256(ASSETS.resolve("sounds/creak2.ogg")));
        assertEquals("c5ab8d94fe89f94514d9b27922266b6ef2335a2c7feffb4547a3d43682c830e8",
                sha256(ASSETS.resolve("sounds/tool1.ogg")));
        assertEquals("b4881997f97c62de46e1e4477e2f5643edb15307ef5191672717215aeca4bec3",
                sha256(ASSETS.resolve("sounds/tool2.ogg")));

        JsonArray particleTextures = read("particles/tube_vent.json")
                .getAsJsonArray("textures");
        assertEquals(5, particleTextures.size());
        for (int frame = 1; frame <= 5; frame++) {
            assertTrue(Files.size(ASSETS.resolve(
                    "textures/particle/tube_vent_" + frame + ".png")) > 0);
        }

        String entity = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "EssentiaTubeBlockEntity.java"));
        String particle = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/particle/"
                        + "TubeVentParticle.java"));
        assertTrue(entity.contains("new TubeVentParticleOptions(tube.ventColor)"));
        assertTrue(entity.contains("venting = 50"));
        assertTrue(entity.contains("ModSounds.CREAK.get()"));
        assertTrue(entity.contains("SoundEvents.FIRE_EXTINGUISH"));
        assertTrue(entity.contains("level.random.nextInt(100) == 0"));
        assertTrue(entity.contains("tag.putInt(\"VentColor\", ventColor)"));
        assertTrue(particle.contains("lifetime = 40"));
        assertTrue(particle.contains("yd += 0.0025D"));
        assertTrue(particle.contains("growthScale *= 1.15F"));
        assertTrue(particle.contains("RENDER_SCALE = 0.3F"));
    }

    @Test
    void endpointOverlapDoesNotRedrawTheBakedTubeArm() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EssentiaTubeBlockEntityRenderer.java"
        ));
        assertTrue(renderer.contains("minX = 1.0F;"));
        assertTrue(renderer.contains("maxX = 0.0F;"));
        assertTrue(renderer.contains("minY = 1.0F;"));
        assertTrue(renderer.contains("maxY = 0.0F;"));
        assertTrue(renderer.contains("minZ = 1.0F;"));
        assertTrue(renderer.contains("maxZ = 0.0F;"));
        assertFalse(renderer.contains("case EAST -> maxX"));
        assertFalse(renderer.contains("case WEST -> minX"));
        assertTrue(renderer.contains("boolean xOutside"));
        assertTrue(renderer.contains("boolean yOutside"));
        assertTrue(renderer.contains("boolean zOutside"));
    }

    private static List<Integer> vector(JsonElement element, String key) {
        JsonArray values = element.getAsJsonObject().getAsJsonArray(key);
        return values.asList().stream().map(JsonElement::getAsInt).toList();
    }

    private static List<Float> floatVector(JsonElement element, String key) {
        JsonArray values = element.getAsJsonObject().getAsJsonArray(key);
        return values.asList().stream().map(JsonElement::getAsFloat).toList();
    }

    private static JsonObject read(String path) throws Exception {
        return JsonParser.parseString(Files.readString(ASSETS.resolve(path)))
                .getAsJsonObject();
    }

    private static String sha256(Path path) throws Exception {
        return java.util.HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
}
