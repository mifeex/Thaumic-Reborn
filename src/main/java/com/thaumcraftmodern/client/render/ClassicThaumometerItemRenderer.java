package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Direct renderer for the original TC4 scanner mesh.
 *
 * <p>The geometry is deliberately emitted to the current render buffer rather
 * than converted to {@code BakedQuad}s. This makes shader-pack switches safe:
 * OptiFine may rebuild the active vertex format, but the buffer supplied for
 * that frame always matches it.</p>
 */
public final class ClassicThaumometerItemRenderer
        extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/scanner.obj"
    );
    private static final ResourceLocation FRAME_TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/item/scanner.png"
    );
    private static final ResourceLocation SCREEN_TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/item/scanscreen.png"
    );

    private volatile Mesh mesh;

    public ClassicThaumometerItemRenderer() {
        this(Minecraft.getInstance());
    }

    private ClassicThaumometerItemRenderer(Minecraft minecraft) {
        super(
                minecraft.getBlockEntityRenderDispatcher(),
                minecraft.getEntityModels()
        );
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        Mesh current = mesh();
        if (current.faces().isEmpty()) {
            return;
        }
        boolean legacyBlockCentered =
                displayContext == ItemDisplayContext.NONE;

        RenderType frameRenderType = RenderType.entityCutoutNoCull(
                FRAME_TEXTURE
        );
        renderMaterial(
                current,
                "scanner",
                buffers.getBuffer(frameRenderType),
                poseStack.last(),
                packedLight,
                packedOverlay,
                legacyBlockCentered
        );
        flush(buffers, frameRenderType);

        RenderType screenRenderType = RenderType.entityTranslucent(
                SCREEN_TEXTURE
        );
        boolean previousDepthMask = GL11.glGetBoolean(
                GL11.GL_DEPTH_WRITEMASK
        );
        GL11.glDepthMask(false);
        try {
            renderMaterial(
                    current,
                    "scanscreen",
                    buffers.getBuffer(screenRenderType),
                    poseStack.last(),
                    packedLight,
                    packedOverlay,
                    legacyBlockCentered
            );
            flush(buffers, screenRenderType);
        } finally {
            GL11.glDepthMask(previousDepthMask);
        }
    }

    private static void flush(
            MultiBufferSource buffers,
            RenderType renderType
    ) {
        if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(renderType);
        }
    }

    private Mesh mesh() {
        Mesh current = mesh;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (mesh == null) {
                mesh = loadMesh();
            }
            return mesh;
        }
    }

    private static Mesh loadMesh() {
        Resource resource = Minecraft.getInstance()
                .getResourceManager()
                .getResource(MODEL)
                .orElse(null);
        if (resource == null) {
            ThaumcraftModern.LOGGER.error(
                    "Missing classic Thaumometer model {}",
                    MODEL
            );
            return Mesh.EMPTY;
        }

        List<Vector3> positions = new ArrayList<>();
        List<Vector2> textureCoordinates = new ArrayList<>();
        List<Vector3> normals = new ArrayList<>();
        List<Face> faces = new ArrayList<>();
        String material = "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        resource.open(),
                        StandardCharsets.UTF_8
                )
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] values = trimmed.split("\\s+");
                switch (values[0]) {
                    case "v" -> positions.add(new Vector3(
                            Float.parseFloat(values[1]),
                            Float.parseFloat(values[2]),
                            Float.parseFloat(values[3])
                    ));
                    case "vt" -> textureCoordinates.add(new Vector2(
                            Float.parseFloat(values[1]),
                            Float.parseFloat(values[2])
                    ));
                    case "vn" -> normals.add(new Vector3(
                            Float.parseFloat(values[1]),
                            Float.parseFloat(values[2]),
                            Float.parseFloat(values[3])
                    ));
                    case "usemtl" -> material = values[1];
                    case "f" -> faces.add(parseFace(values, material));
                    default -> {
                        // OBJ metadata and groups do not affect this mesh.
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            ThaumcraftModern.LOGGER.error(
                    "Could not load classic Thaumometer model {}",
                    MODEL,
                    exception
            );
            return Mesh.EMPTY;
        }
        return new Mesh(
                List.copyOf(positions),
                List.copyOf(textureCoordinates),
                List.copyOf(normals),
                List.copyOf(faces)
        );
    }

    private static Face parseFace(String[] values, String material) {
        Vertex[] vertices = new Vertex[values.length - 1];
        for (int index = 1; index < values.length; index++) {
            String[] indices = values[index].split("/");
            vertices[index - 1] = new Vertex(
                    Integer.parseInt(indices[0]) - 1,
                    Integer.parseInt(indices[1]) - 1,
                    Integer.parseInt(indices[2]) - 1
            );
        }
        return new Face(material, vertices);
    }

    private static void renderMaterial(
            Mesh mesh,
            String material,
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int packedLight,
            int packedOverlay,
            boolean legacyBlockCentered
    ) {
        boolean screenFaceRendered = false;
        for (Face face : mesh.faces()) {
            if (!face.material().equals(material)) {
                continue;
            }
            /*
             * scanner.obj contains two opposite-winding copies of the exact
             * same scanscreen plane because Forge's old OBJ path culled back
             * faces. entityTranslucent is already two-sided, so drawing both
             * copies blends the light rays in scanscreen.png twice and lets
             * OptiFine alternate between coplanar fragments while the camera
             * moves. Keep one two-sided plane.
             */
            if (material.equals("scanscreen")) {
                if (screenFaceRendered) {
                    continue;
                }
                screenFaceRendered = true;
            }
            for (Vertex vertex : face.vertices()) {
                Vector3 sourcePosition = mesh.positions()
                        .get(vertex.position());
                Vector2 uv = mesh.textureCoordinates()
                        .get(vertex.textureCoordinate());
                Vector3 sourceNormal = mesh.normals().get(vertex.normal());

                ThaumometerModelCoordinates.Position position =
                        ThaumometerModelCoordinates.transform(
                                sourcePosition.x(),
                                sourcePosition.y(),
                                sourcePosition.z(),
                                legacyBlockCentered
                        );
                float normalX = sourceNormal.x();
                float normalY = sourceNormal.z();
                float normalZ = -sourceNormal.y();

                consumer.vertex(
                                pose.pose(),
                                position.x(),
                                position.y(),
                                position.z()
                        )
                        .color(255, 255, 255, 255)
                        .uv(uv.u(), 1.0F - uv.v())
                        .overlayCoords(
                                packedOverlay == 0
                                        ? OverlayTexture.NO_OVERLAY
                                        : packedOverlay
                        )
                        .uv2(packedLight)
                        .normal(
                                pose.normal(),
                                normalX,
                                normalY,
                                normalZ
                        )
                        .endVertex();
            }
        }
    }

    private record Mesh(
            List<Vector3> positions,
            List<Vector2> textureCoordinates,
            List<Vector3> normals,
            List<Face> faces
    ) {
        private static final Mesh EMPTY = new Mesh(
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private record Vector3(float x, float y, float z) {
    }

    private record Vector2(float u, float v) {
    }

    private record Face(String material, Vertex[] vertices) {
    }

    private record Vertex(
            int position,
            int textureCoordinate,
            int normal
    ) {
    }
}
