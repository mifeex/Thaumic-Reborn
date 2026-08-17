package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Runtime group renderer for the original TC4 Wavefront meshes. */
public final class LegacyObjMesh {
    private static final List<ResourceLocation> RUNTIME_MESHES = List.of(
            mesh("vis_relay.obj"),
            mesh("node_stabilizer.obj"),
            mesh("pillar.obj"),
            mesh("adv_alch_furnace.obj")
    );
    private static volatile Map<ResourceLocation, LegacyObjMesh> loadedMeshes =
            Map.of();

    private final List<Vector3f> positions;
    private final List<Uv> uvs;
    private final List<Vector3f> normals;
    private final Map<String, List<Face>> groups;

    private LegacyObjMesh(
            List<Vector3f> positions,
            List<Uv> uvs,
            List<Vector3f> normals,
            Map<String, List<Face>> groups
    ) {
        this.positions = positions;
        this.uvs = uvs;
        this.normals = normals;
        this.groups = groups;
    }

    public static void registerReloadListener(
            RegisterClientReloadListenersEvent event
    ) {
        event.registerReloadListener(
                new SimplePreparableReloadListener<
                        Map<ResourceLocation, LegacyObjMesh>>() {
                    @Override
                    protected Map<ResourceLocation, LegacyObjMesh> prepare(
                            ResourceManager resourceManager,
                            ProfilerFiller profiler
                    ) {
                        Map<ResourceLocation, LegacyObjMesh> prepared =
                                new LinkedHashMap<>();
                        for (ResourceLocation location : RUNTIME_MESHES) {
                            prepared.put(location, parse(
                                    resourceManager,
                                    location
                            ));
                        }
                        return Map.copyOf(prepared);
                    }

                    @Override
                    protected void apply(
                            Map<ResourceLocation, LegacyObjMesh> prepared,
                            ResourceManager resourceManager,
                            ProfilerFiller profiler
                    ) {
                        // One publication replaces the complete CPU snapshot;
                        // renderers can never observe a partially reloaded set.
                        loadedMeshes = prepared;
                    }
                }
        );
    }

    static LegacyObjMesh get(ResourceLocation location) {
        LegacyObjMesh mesh = loadedMeshes.get(location);
        if (mesh == null) {
            throw new IllegalStateException(
                    "Classic OBJ was not prepared during resource reload: "
                            + location
            );
        }
        return mesh;
    }

    private static LegacyObjMesh parse(
            ResourceManager resourceManager,
            ResourceLocation location
    ) {
        Resource resource = resourceManager
                .getResource(location)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing classic OBJ " + location));
        List<Vector3f> positions = new ArrayList<>();
        List<Uv> uvs = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        Map<String, List<Face>> groups = new HashMap<>();
        String group = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.open(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split("\\s+");
                if (values.length == 0 || values[0].isEmpty()
                        || values[0].startsWith("#")) {
                    continue;
                }
                switch (values[0]) {
                    case "v" -> positions.add(new Vector3f(
                            Float.parseFloat(values[1]),
                            Float.parseFloat(values[2]),
                            Float.parseFloat(values[3])));
                    case "vt" -> uvs.add(new Uv(
                            Float.parseFloat(values[1]),
                            1.0F - Float.parseFloat(values[2])));
                    case "vn" -> normals.add(new Vector3f(
                            Float.parseFloat(values[1]),
                            Float.parseFloat(values[2]),
                            Float.parseFloat(values[3])));
                    case "g", "o" -> group = values[1];
                    case "f" -> groups.computeIfAbsent(group,
                                    ignored -> new ArrayList<>())
                            .add(parseFace(values));
                    default -> {
                    }
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load " + location, exception);
        }
        Map<String, List<Face>> immutableGroups = new HashMap<>();
        groups.forEach((name, faces) -> immutableGroups.put(
                name,
                List.copyOf(faces)
        ));
        return new LegacyObjMesh(
                List.copyOf(positions),
                List.copyOf(uvs),
                List.copyOf(normals),
                Map.copyOf(immutableGroups)
        );
    }

    private static ResourceLocation mesh(String fileName) {
        return new ResourceLocation(
                "thaumic_reborn",
                "textures/models/" + fileName
        );
    }

    void render(
            String group,
            PoseStack pose,
            VertexConsumer consumer,
            int light,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        List<Face> faces = groups.get(group);
        if (faces == null) {
            return;
        }
        Matrix4f positionMatrix = pose.last().pose();
        Matrix3f normalMatrix = pose.last().normal();
        for (Face face : faces) {
            if (face.vertices().length == 4) {
                for (Index index : face.vertices()) {
                    vertex(index, positionMatrix, normalMatrix, consumer,
                            light, red, green, blue, alpha);
                }
                continue;
            }
            for (int index = 1; index < face.vertices().length - 1; index++) {
                Index first = face.vertices()[0];
                Index second = face.vertices()[index];
                Index third = face.vertices()[index + 1];
                vertex(first, positionMatrix, normalMatrix, consumer,
                        light, red, green, blue, alpha);
                vertex(second, positionMatrix, normalMatrix, consumer,
                        light, red, green, blue, alpha);
                vertex(third, positionMatrix, normalMatrix, consumer,
                        light, red, green, blue, alpha);
                /*
                 * All callers use entityCutoutNoCull, whose vertex mode is
                 * QUADS.  The classic TC4 OBJ files are triangulated.  A
                 * three-vertex submission therefore made the buffer combine
                 * vertices from adjacent faces into the huge crossing quads
                 * seen on the node devices.  Repeating the final corner turns
                 * each original triangle into one geometrically identical
                 * degenerate quad while retaining Minecraft's ordinary entity
                 * cutout render state. Native OBJ quads are emitted unchanged
                 * above; this fan path is only for triangles and polygons.
                 */
                vertex(third, positionMatrix, normalMatrix, consumer,
                        light, red, green, blue, alpha);
            }
        }
    }

    private void vertex(
            Index index,
            Matrix4f positionMatrix,
            Matrix3f normalMatrix,
            VertexConsumer consumer,
            int light,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        Vector3f position = positions.get(index.position());
        Uv uv = index.uv() >= 0 ? uvs.get(index.uv()) : new Uv(0, 0);
        Vector3f normal = index.normal() >= 0
                ? normals.get(index.normal()) : new Vector3f(0, 1, 0);
        consumer.vertex(positionMatrix, position.x(), position.y(), position.z())
                .color(red, green, blue, alpha)
                .uv(uv.u(), uv.v())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, normal.x(), normal.y(), normal.z())
                .endVertex();
    }

    private static Face parseFace(String[] values) {
        Index[] indices = new Index[values.length - 1];
        for (int index = 1; index < values.length; index++) {
            String[] fields = values[index].split("/");
            indices[index - 1] = new Index(
                    Integer.parseInt(fields[0]) - 1,
                    fields.length > 1 && !fields[1].isEmpty()
                            ? Integer.parseInt(fields[1]) - 1 : -1,
                    fields.length > 2 && !fields[2].isEmpty()
                            ? Integer.parseInt(fields[2]) - 1 : -1
            );
        }
        return new Face(indices);
    }

    private record Uv(float u, float v) {
    }

    private record Index(int position, int uv, int normal) {
    }

    private record Face(Index[] vertices) {
    }
}
