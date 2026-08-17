package com.thaumcraftmodern.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.ClassicGolemEntity;
import com.thaumcraftmodern.entity.GolemMarker;
import com.thaumcraftmodern.item.GolemBellItem;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** TC4 bell UI: animated home/marker glyphs attached to exact block faces and golem links. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class GolemBellMarkerRenderer {
    private static final ResourceLocation MARK = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/misc/mark.png");
    private static final ResourceLocation HOME = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/misc/home.png");
    private static final ResourceLocation EMPTY = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/block/empty.png");
    private static final ResourceLocation SCRIPT = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/misc/script.png");
    private static final double RANGE_SQUARED = 4096D;
    /** TC4's default golem_link_quality. Values below four disabled the link. */
    private static final int LINK_QUALITY = 16;
    private static final RenderType HOME_TYPE = GolemBellRenderTypes.overlay("home", HOME);
    private static final RenderType MARK_TYPE = GolemBellRenderTypes.overlay("mark", MARK);
    private static final RenderType EMPTY_TYPE = GolemBellRenderTypes.overlay("empty", EMPTY);
    private static final RenderType LINK_TYPE = GolemBellRenderTypes.link(SCRIPT);

    private GolemBellMarkerRenderer() {}

    /** Persistent feedback missing from the previous port: link, core, state and aimed face. */
    @SubscribeEvent
    public static void renderBellStatus(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;
        ItemStack bell = heldBell(minecraft);
        if (bell.isEmpty()) return;

        int x = 8;
        int y = 8;
        java.util.UUID selected = GolemBellItem.selectedUuid(bell);
        ClassicGolemEntity golem = linkedGolem(minecraft, bell);
        Component status;
        if (selected == null) {
            status = Component.translatable(
                    "hud.thaumic_reborn.golem_bell.unlinked");
        } else if (golem == null) {
            status = Component.translatable(
                    "hud.thaumic_reborn.golem_bell.unavailable",
                    GolemBellItem.markers(bell).size());
        } else {
            Component core = golem.core() == null
                    ? Component.translatable(
                            "hud.thaumic_reborn.golem_bell.no_core")
                    : Component.translatable(
                            ModItems.golemCore(golem.core()).get()
                                    .getDescriptionId());
            Component state = Component.translatable(golem.isInactive()
                    ? "hud.thaumic_reborn.golem_bell.inactive"
                    : "hud.thaumic_reborn.golem_bell.active");
            status = Component.translatable(
                    "hud.thaumic_reborn.golem_bell.linked",
                    golem.getDisplayName(), core,
                    GolemBellItem.markers(bell).size(), state);
        }
        event.getGuiGraphics().drawString(
                minecraft.font, status, x, y, 0xE8D7FF, true);

        if (minecraft.hitResult instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            Component face = Component.translatable(
                    "hud.thaumic_reborn.golem_bell.direction."
                            + hit.getDirection().getName());
            Component target = Component.translatable(
                    "hud.thaumic_reborn.golem_bell.target",
                    pos.getX(), pos.getY(), pos.getZ(), face);
            event.getGuiGraphics().drawString(
                    minecraft.font, target, x, y + 11, 0xB9E7FF, true);
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        ItemStack bell = heldBell(minecraft);
        if (bell.isEmpty()) return;
        ClassicGolemEntity golem = linkedGolem(minecraft, bell);

        PoseStack poses = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        var buffers = minecraft.renderBuffers().bufferSource();
        float time = (System.nanoTime() / 30_000_000L) % 32767L;

        poses.pushPose();
        poses.translate(-camera.x, -camera.y, -camera.z);
        BlockPos home = GolemBellItem.home(bell);
        Direction homeFace = GolemBellItem.homeFace(bell);
        if (home != null && homeFace != null && minecraft.player.distanceToSqr(Vec3.atCenterOf(home)) < RANGE_SQUARED) {
            drawFace(poses.last(), buffers.getBuffer(HOME_TYPE),
                    home, homeFace, .325F,
                    markerColor((byte) -1, homeFace, time, 0));
            buffers.endBatch(HOME_TYPE);
        }
        for (GolemMarker marker : GolemBellItem.markers(bell)) {
            Vec3 target = Vec3.atCenterOf(marker.pos()).add(Vec3.atLowerCornerOf(marker.side().getNormal()).scale(.5D));
            if (minecraft.player.distanceToSqr(target) >= RANGE_SQUARED) continue;
            int color = markerColor(marker.color(), marker.side(), time, 0);
            // BufferSource reuses one BufferBuilder for these non-fixed render
            // types. Finish each texture pass immediately; otherwise asking
            // for EMPTY after MARK makes the round mark geometry sample the
            // empty-block texture instead of TC4's mark.png.
            drawFace(poses.last(), buffers.getBuffer(MARK_TYPE),
                    marker.pos(), marker.side(), .2F, color);
            buffers.endBatch(MARK_TYPE);
            if (minecraft.level.isEmptyBlock(marker.pos())) {
                VertexConsumer empty = buffers.getBuffer(EMPTY_TYPE);
                for (Direction side : Direction.values()) {
                    drawFace(poses.last(), empty,
                            marker.pos(), side, .49F, color);
                }
                buffers.endBatch(EMPTY_TYPE);
            }
            if (golem != null) {
                drawLink(poses.last(), golem, marker, event.getPartialTick(), time);
            }
        }
        poses.popPose();
    }

    private static ClassicGolemEntity linkedGolem(Minecraft minecraft, ItemStack bell) {
        java.util.UUID uuid = GolemBellItem.selectedUuid(bell);
        if (uuid == null) return null;
        Entity byId = minecraft.level.getEntity(GolemBellItem.selectedEntityId(bell));
        if (byId instanceof ClassicGolemEntity golem && golem.getUUID().equals(uuid)) return golem;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof ClassicGolemEntity golem && golem.getUUID().equals(uuid)) return golem;
        }
        return null;
    }

    private static ItemStack heldBell(Minecraft minecraft) {
        ItemStack main = minecraft.player.getMainHandItem();
        if (main.getItem() instanceof GolemBellItem) return main;
        ItemStack off = minecraft.player.getOffhandItem();
        return off.getItem() instanceof GolemBellItem ? off : ItemStack.EMPTY;
    }

    private static void drawFace(PoseStack.Pose pose, VertexConsumer out, BlockPos pos,
            Direction face, float radius, int rgb) {
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        // TC4 aligns the quad to the face at 0.5 and then pulls it another
        // 0.01 outward. Keep that separation so the block face does not show
        // through the additive marker.
        Vec3 center = Vec3.atCenterOf(pos).add(normal.scale(.51D));
        Vec3 u; Vec3 v;
        switch (face) {
            case UP -> { u = new Vec3(1, 0, 0); v = new Vec3(0, 0, 1); }
            case DOWN -> { u = new Vec3(1, 0, 0); v = new Vec3(0, 0, -1); }
            case NORTH -> { u = new Vec3(1, 0, 0); v = new Vec3(0, 1, 0); }
            case SOUTH -> { u = new Vec3(-1, 0, 0); v = new Vec3(0, 1, 0); }
            case EAST -> { u = new Vec3(0, 0, 1); v = new Vec3(0, 1, 0); }
            case WEST -> { u = new Vec3(0, 0, -1); v = new Vec3(0, 1, 0); }
            default -> throw new IllegalStateException("Unexpected face " + face);
        }
        // Exact drawTexturedQuad UV orientation from TC4. The previous port
        // reversed U and rendered mark.png horizontally mirrored.
        vertex(pose, out, center.subtract(u.scale(radius)).subtract(v.scale(radius)), 0, 1, rgb, normal);
        vertex(pose, out, center.subtract(u.scale(radius)).add(v.scale(radius)), 0, 0, rgb, normal);
        vertex(pose, out, center.add(u.scale(radius)).add(v.scale(radius)), 1, 0, rgb, normal);
        vertex(pose, out, center.add(u.scale(radius)).subtract(v.scale(radius)), 1, 1, rgb, normal);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer out, Vec3 point,
            float u, float v, int rgb, Vec3 normal) {
        Matrix4f matrix = pose.pose(); Matrix3f normals = pose.normal();
        out.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, 255).uv(u, v)
                // TC4 UtilsFX.renderQuadCenteredFromTexture receives packed
                // light 200 here; FULL_BRIGHT washes the pale rune into a
                // nearly solid mint disc on modern additive rendering.
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(200)
                .normal(normals, (float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }

    /** Direct 1.20.1 port of TC4 RenderEventHandler#drawMarkerLine. */
    private static void drawLink(PoseStack.Pose pose, ClassicGolemEntity golem,
            GolemMarker marker, float partialTick, float time) {
        // TC4 begins and draws a separate GL_TRIANGLE_STRIP for every marker.
        // Sharing one strip across links lets the GPU connect unrelated
        // vertices into the giant rune fans visible in the broken port.
        Vec3 source = new Vec3(
                golem.xOld + (golem.getX() - golem.xOld) * partialTick,
                golem.yOld + (golem.getY() - golem.yOld) * partialTick + golem.getBbHeight(),
                golem.zOld + (golem.getZ() - golem.zOld) * partialTick);
        Vec3 normal = Vec3.atLowerCornerOf(marker.side().getNormal());
        Vec3 outerDelta = Vec3.atCenterOf(marker.pos()).add(normal).subtract(source);
        Vec3 faceDelta = Vec3.atCenterOf(marker.pos()).add(normal.scale(.5D)).subtract(source);
        Vec3 blockDelta = Vec3.atLowerCornerOf(marker.pos()).subtract(source);
        float distance = (float) blockDelta.length();
        int length = Math.round(distance) * LINK_QUALITY;
        if (length <= 0) return;

        var linkBuffers = MultiBufferSource.immediate(new BufferBuilder(4096));
        VertexConsumer out = linkBuffers.getBuffer(LINK_TYPE);
        Matrix4f matrix = pose.pose();
        for (int segment = 0; segment <= length; segment++) {
            float progress = segment / (float) length;
            float alpha = Math.min(.75F, segment * 1.5F / length);
            float centerWeight = 1F - Math.abs(segment - length / 2F) / (length / 2F);
            double phase = marker.side().get3DDataValue() * 20D
                    + distance * (1F - progress) * LINK_QUALITY - time / 5F;
            double waveX = Math.sin((phase + marker.pos().getZ() % 16D) / 4D) * .5D * centerWeight;
            double waveY = Math.sin((phase + marker.pos().getX() % 16D) / 3D) * .5D * centerWeight;
            double waveZ = Math.sin((phase + marker.pos().getY() % 16D) / 2D) * .5D * centerWeight;
            Vec3 line = outerDelta.add(waveX, waveY, waveZ);
            float tail = 0F;
            if (segment > length - LINK_QUALITY / 2F) {
                float outerWeight = (length - segment) / (LINK_QUALITY / 2F);
                tail = 1F - outerWeight;
                line = line.scale(outerWeight).add(faceDelta.add(waveX, waveY, waveZ).scale(tail));
            }
            int rgb = markerColor(marker.color(), marker.side(), time, segment);
            float textureU = (1F - progress) * distance - time * .005F;
            int vertexAlpha = (int) (255F * alpha * (1F - tail));
            Vec3 point = source.add(line.scale(progress));
            Vec3 low = point.add(0, -.05D, 0);
            Vec3 high = point.add(0, .05D, 0);
            GolemBellVertexWriter.write(
                    out, matrix, low, textureU, 1F, rgb, vertexAlpha);
            GolemBellVertexWriter.write(
                    out, matrix, high, textureU, 0F, rgb, vertexAlpha);
        }
        linkBuffers.endBatch(LINK_TYPE);
    }

    private static int markerColor(byte color, Direction face, float time, int segment) {
        if (color >= 0 && color < 16) return DyeColor.byId(15 - color).getTextColor();
        int red = channel(time / 12F + face.get3DDataValue() + segment);
        int green = channel(time / 14F + face.get3DDataValue() + segment);
        int blue = channel(time / 16F + face.get3DDataValue() + segment);
        return red << 16 | green << 8 | blue;
    }

    private static int channel(float phase) {
        return Math.max(0, Math.min(255, (int) ((Math.sin(phase) * .2F + .8F) * 255F)));
    }
}
