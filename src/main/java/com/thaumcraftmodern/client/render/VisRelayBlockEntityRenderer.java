package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.PrimalAspectColors;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.visnet.VisChargeRelayBlockEntity;
import com.thaumcraftmodern.visnet.VisNetworkNodeBlockEntity;
import com.thaumcraftmodern.visnet.VisRelayBlock;
import com.thaumcraftmodern.visnet.VisRelayBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class VisRelayBlockEntityRenderer<T extends VisRelayBlockEntity>
        implements BlockEntityRenderer<T> {
    private static final int BEAM_COLOR_BANDS = 24;
    private static final ResourceLocation MESH = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/vis_relay.obj"
    );
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/vis_relay.png"
    );
    /** Thin TC4-style connection while retaining the two-strip motion. */
    private static final float BEAM_HALF_WIDTH = 0.045F;
    private static final float IMPACT_SIZE = 1.15F;
    private static final float BEAM_ROTATION_DEGREES_PER_TICK = 5.0F;
    public VisRelayBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            T tile,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        float ticks = Minecraft.getInstance().player == null
                ? partialTick
                : Minecraft.getInstance().player.tickCount + partialTick;
        float scale = Mth.sin(ticks / 2.0F) * 0.05F + 0.95F;
        int rawGlow = Mth.clamp((tile.parentPosition() == null ? 0 : 50)
                + (int) (150 * scale), 0, 240);
        int glow = LightTexture.pack(rawGlow / 16, 0);
        float[] tint = tint(tile.attunement());

        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        Direction facing = tile.getBlockState().hasProperty(VisRelayBlock.FACING)
                ? tile.getBlockState().getValue(VisRelayBlock.FACING)
                : Direction.UP;
        if (tile instanceof VisChargeRelayBlockEntity) {
            pose.mulPose(Axis.XN.rotationDegrees(90.0F));
            pose.mulPose(Axis.XP.rotationDegrees(180.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
            render("RingFloat", pose, buffers, packedLight, 1, 1, 1);
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(180.0F));
            pose.translate(0.0D, 0.0D, 0.5D);
            for (int index = 0; index < 4; index++) {
                render("Support", pose, buffers, packedLight, 1, 1, 1);
                pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            pose.popPose();
        } else {
            orient(pose, facing);
            pose.mulPose(Axis.XP.rotationDegrees(180.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
            pose.pushPose();
            pose.scale(0.75F, 0.75F, 0.75F);
            pose.translate(0.0D, 0.0D, -0.16D);
            render("RingBase", pose, buffers, packedLight, 1, 1, 1);
            pose.popPose();
            render("RingFloat", pose, buffers, packedLight, 1, 1, 1);
        }
        render("Crystal", pose, buffers, glow, tint[0], tint[1], tint[2]);
        pose.popPose();

        if (tile.parentPosition() != null) {
            renderBeam(tile, pose, buffers, facing);
        }
    }

    private static void renderBeam(
            VisNetworkNodeBlockEntity tile,
            PoseStack pose,
            MultiBufferSource buffers,
            Direction facing
    ) {
        BlockPos parent = tile.parentPosition();
        if (parent == null) {
            return;
        }
        Direction parentFacing = Direction.UP;
        if (tile.getLevel() != null
                && tile.getLevel().getBlockEntity(parent)
                instanceof VisRelayBlockEntity parentRelay
                && parentRelay.getBlockState().hasProperty(
                VisRelayBlock.FACING)) {
            parentFacing = parentRelay.getBlockState().getValue(
                    VisRelayBlock.FACING);
        }
        Vec3 start = new Vec3(
                parent.getX() - tile.getBlockPos().getX() + 0.5D
                        - parentFacing.getStepX() * 0.05D,
                parent.getY() - tile.getBlockPos().getY() + 0.5D
                        - parentFacing.getStepY() * 0.05D,
                parent.getZ() - tile.getBlockPos().getZ() + 0.5D
                        - parentFacing.getStepZ() * 0.05D
        );
        Vec3 end = new Vec3(
                0.5D - facing.getStepX() * 0.05D,
                0.5D - facing.getStepY() * 0.05D,
                0.5D - facing.getStepZ() * 0.05D
        );
        Vec3 direction = end.subtract(start);
        if (direction.lengthSqr() < 1.0E-8D) {
            return;
        }
        Vec3 axis = direction.normalize();
        Vec3 reference = Math.abs(axis.y) > 0.95D
                ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 baseSide = axis.cross(reference).normalize();
        float ticks = tile.getLevel() == null ? 0.0F
                : tile.getLevel().getGameTime()
                + Minecraft.getInstance().getFrameTime();
        // FXBeamPower rotates its two strips by 5 degrees per tick. Rotating
        // one orthonormal basis around the beam axis reproduces that smooth
        // movement without stacking extra glow geometry.
        double rotation = Math.toRadians(
                (ticks * BEAM_ROTATION_DEGREES_PER_TICK) % 360.0F);
        Vec3 quarterTurn = axis.cross(baseSide);
        Vec3 sideUnit = baseSide.scale(Math.cos(rotation))
                .add(quarterTurn.scale(Math.sin(rotation)));
        Vec3 secondSideUnit = axis.cross(sideUnit).normalize();
        float uvSlide = -ticks * 0.2F - Mth.floor(-ticks * 0.1F);
        boolean revealed = Minecraft.getInstance().player != null
                && Minecraft.getInstance().player
                .getItemBySlot(EquipmentSlot.HEAD)
                .getItem() instanceof com.thaumcraftmodern.item.RevealingGear gear
                && gear.reveals(Minecraft.getInstance().player
                        .getItemBySlot(EquipmentSlot.HEAD));
        // A thin ribbon cannot use the old 10% fallback that only remained
        // visible because two very broad crossed quads overlapped.
        boolean pulsing = tile.pulseTicks() > 0;
        float opacity = pulsing ? tile.beamOpacity() : 0.3F;
        float visibility = revealed ? 1.0F : 0.1F;
        float alpha = opacity * visibility;
        PrimalAspect attunedAspect = tile.attunement() >= 0
                && tile.attunement() < PrimalAspect.ordered().size()
                ? PrimalAspect.ordered().get(tile.attunement()) : null;
        List<PrimalAspect> palette;
        if (attunedAspect != null) {
            // A filtered relay represents exactly one primal. If the source
            // has none of it, TC4 has no transfer to visualize: suppress both
            // the beam and its receiver impact instead of borrowing another
            // available aspect's colour.
            if (tile.availableVis(attunedAspect) <= 0) {
                return;
            }
            palette = List.of(attunedAspect);
        } else {
            palette = tile.beamAspectBands(BEAM_COLOR_BANDS);
            if (palette.isEmpty()) {
                return;
            }
        }
        // One colour across the entire connection. Change only once per
        // second; the weighted palette makes the node's largest primal the
        // most frequent colour without producing rainbow stripes.
        int paletteIndex = Math.floorMod(
                (int) ((tile.getLevel() == null ? 0L
                        : tile.getLevel().getGameTime()) / 20L),
                palette.size());
        int color = PrimalAspectColors.color(palette.get(paletteIndex));
        boolean fixedAttunement = attunedAspect != null;
        float red = pulsing && !fixedAttunement ? tile.pulseRed()
                : ((color >> 16) & 255) / 255.0F;
        float green = pulsing && !fixedAttunement ? tile.pulseGreen()
                : ((color >> 8) & 255) / 255.0F;
        float blue = pulsing && !fixedAttunement ? tile.pulseBlue()
                : (color & 255) / 255.0F;
        VertexConsumer beam = buffers.getBuffer(ClassicNodeRenderTypes.visBeam());
        float beamLength = (float) direction.length();
        renderCrossedBeam(beam, pose.last(), start, end,
                sideUnit, secondSideUnit, BEAM_HALF_WIDTH,
                uvSlide, beamLength, red, green, blue, alpha);
        // The TC4 impact flare is a separate additive pass and must visually
        // burn over the beam at the receiving crystal. Do not reuse the very
        // low hidden-beam visibility multiplier here: that turns the flare
        // into a translucent coloured blob instead of a white-hot impact.
        float impactVisibility = revealed ? 1.75F : 1.0F;
        float impactAlpha = Mth.clamp(
                opacity * impactVisibility, 0.0F, 1.0F);
        renderImpact(tile, pose, buffers, end, red, green, blue,
                impactAlpha);
    }

    private static void renderCrossedBeam(
            VertexConsumer beam,
            PoseStack.Pose pose,
            Vec3 start,
            Vec3 end,
            Vec3 sideUnit,
            Vec3 secondSideUnit,
            float halfWidth,
            float uvSlide,
            float beamLength,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        beamStrip(beam, pose, start, end, sideUnit.scale(halfWidth),
                uvSlide, beamLength + uvSlide, red, green, blue, alpha);
        // Original FXBeamPower uses two strips rotated by 90 degrees and a
        // one-third V offset so their bright texture bands do not coincide.
        beamStrip(beam, pose, start, end, secondSideUnit.scale(halfWidth),
                uvSlide + 1.0F / 3.0F,
                beamLength + uvSlide + 1.0F / 3.0F,
                red, green, blue, alpha);
    }

    private static void renderImpact(
            VisNetworkNodeBlockEntity tile,
            PoseStack pose,
            MultiBufferSource buffers,
            Vec3 center,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        int frame = Math.floorMod((int) (tile.getLevel() == null ? 0L
                : tile.getLevel().getGameTime()), 16);
        float u0 = frame / 16.0F;
        float u1 = u0 + 0.0624375F;
        float v0 = 0.3125F;
        float v1 = v0 + 0.0624375F;
        float size = IMPACT_SIZE * (tile.pulseTicks() > 0
                ? tile.beamOpacity() : 0.3F);

        pose.pushPose();
        pose.translate(center.x, center.y, center.z);
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera()
                .rotation());
        VertexConsumer out = buffers.getBuffer(
                ClassicNodeRenderTypes.visImpact());
        // Coloured halo, then two white-hot additive cores. Raising only the
        // halo alpha leaves a saturated aspect-coloured blob; the white passes
        // are what make the receiver visibly burn through the beam, as in the
        // reference screenshots.
        impactQuad(out, pose.last(), size, u0, u1, v0, v1,
                red, green, blue, alpha);
        impactQuad(out, pose.last(), size * 0.80F, u0, u1, v0, v1,
                1.0F, 1.0F, 1.0F, 1.0F);
        impactQuad(out, pose.last(), size * 0.48F, u0, u1, v0, v1,
                1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }

    private static void impactQuad(
            VertexConsumer out,
            PoseStack.Pose pose,
            float size,
            float u0,
            float u1,
            float v0,
            float v1,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        impactVertex(out, pose, -size, -size, u1, v1,
                red, green, blue, alpha);
        impactVertex(out, pose, size, -size, u1, v0,
                red, green, blue, alpha);
        impactVertex(out, pose, size, size, u0, v0,
                red, green, blue, alpha);
        impactVertex(out, pose, -size, size, u0, v1,
                red, green, blue, alpha);
    }

    private static void impactVertex(
            VertexConsumer out,
            PoseStack.Pose pose,
            float x,
            float y,
            float u,
            float v,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        out.vertex(pose.pose(), x, y, 0)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 0, 1)
                .endVertex();
    }

    private static void renderBeamCore(
            VertexConsumer out,
            PoseStack.Pose pose,
            Vec3 start,
            Vec3 end,
            Vec3 normal,
            float red,
            float green,
            float blue
    ) {
        lineVertex(out, pose, start, normal, red, green, blue);
        lineVertex(out, pose, end, normal, red, green, blue);
    }

    private static void lineVertex(
            VertexConsumer out,
            PoseStack.Pose pose,
            Vec3 point,
            Vec3 normal,
            float red,
            float green,
            float blue
    ) {
        out.vertex(pose.pose(), (float) point.x, (float) point.y,
                        (float) point.z)
                .color(red, green, blue, 1.0F)
                .normal(pose.normal(), (float) normal.x,
                        (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private static void beamStrip(
            VertexConsumer out,
            PoseStack.Pose pose,
            Vec3 start,
            Vec3 end,
            Vec3 side,
            float v0,
            float v1,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        beamVertex(out, pose, end.subtract(side), 1, v1,
                red, green, blue, alpha);
        beamVertex(out, pose, start.subtract(side), 1, v0,
                red, green, blue, alpha);
        beamVertex(out, pose, start.add(side), 0, v0,
                red, green, blue, alpha);
        beamVertex(out, pose, end.add(side), 0, v1,
                red, green, blue, alpha);
    }

    private static void beamVertex(
            VertexConsumer out,
            PoseStack.Pose pose,
            Vec3 point,
            float u,
            float v,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        out.vertex(pose.pose(), (float) point.x, (float) point.y,
                        (float) point.z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
    }

    private void render(
            String group,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            float red,
            float green,
            float blue
    ) {
        LegacyObjMesh.get(MESH).render(group, pose,
                buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                light, red, green, blue, 1.0F);
    }

    private static float[] tint(byte attunement) {
        if (attunement < 0 || attunement > 5) {
            return new float[]{1, 1, 1};
        }
        PrimalAspect aspect = PrimalAspect.ordered().get(attunement);
        int color = PrimalAspectColors.color(aspect);
        return new float[]{
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F
        };
    }

    private static void orient(PoseStack pose, Direction facing) {
        switch (facing) {
            case DOWN -> pose.mulPose(Axis.XN.rotationDegrees(90));
            case UP -> pose.mulPose(Axis.XP.rotationDegrees(90));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> pose.mulPose(Axis.YN.rotationDegrees(90));
            case NORTH -> {
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(T tile) {
        // The original beam is a separate particle and remains visible even
        // when its relay's one-block AABB leaves the frustum.
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
