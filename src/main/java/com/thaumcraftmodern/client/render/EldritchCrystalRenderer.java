package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.world.block.EldritchCrystalBlock;
import com.thaumcraftmodern.world.block.entity.CrystalClusterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Direct runtime rendering of TC4's byte-exact vcrystal.obj groups. */
final class EldritchCrystalRenderer {
    private static final ResourceLocation MODEL = new ResourceLocation(
            "thaumcraft", "textures/models/vcrystal.obj"
    );
    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(
            "thaumcraft", "textures/blocks/crust.png"
    );
    private static final ResourceLocation CRYSTAL_TEXTURE = new ResourceLocation(
            "thaumcraft", "textures/models/vcrystal.png"
    );

    private EldritchCrystalRenderer() {
    }

    static void renderBlock(
            CrystalClusterBlockEntity crystal,
            float partialTick,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight
    ) {
        Direction facing = crystal.getBlockState().getValue(
                EldritchCrystalBlock.FACING
        );
        float ticks = crystal.getLevel() == null
                ? partialTick
                : crystal.getLevel().getGameTime() + partialTick;
        render(
                poses,
                buffers,
                packedLight,
                facing,
                Math.floorMod(crystal.hashCode(), 4),
                ticks
        );
    }

    static void renderItem(
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight
    ) {
        float ticks = Minecraft.getInstance().player == null
                ? 0.0F
                : Minecraft.getInstance().player.tickCount;
        render(poses, buffers, packedLight, Direction.UP, 0, ticks);
    }

    private static void render(
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            Direction facing,
            int quarterTurns,
            float ticks
    ) {
        LegacyObjMesh model = LegacyObjMesh.get(MODEL);
        poses.pushPose();
        poses.translate(0.5D, 0.5D, 0.5D);
        orient(poses, facing);
        poses.translate(0.0D, 0.0D, -0.5D);
        poses.mulPose(Axis.ZP.rotationDegrees(90.0F * quarterTurns));

        model.render(
                "Base",
                poses,
                buffers.getBuffer(RenderType.entityCutoutNoCull(BASE_TEXTURE)),
                packedLight,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        float glow = Mth.sin(ticks / 6.0F) * 0.075F + 0.925F;
        int glowLight = LightTexture.pack(
                Mth.clamp(Math.round(13.0F * glow), 0, 15),
                LightTexture.sky(packedLight)
        );
        model.render(
                "Crystal",
                poses,
                buffers.getBuffer(RenderType.entityTranslucent(CRYSTAL_TEXTURE)),
                glowLight,
                1.0F, 1.0F, 1.0F, 0.7F
        );
        poses.popPose();
    }

    private static void orient(PoseStack poses, Direction facing) {
        switch (facing) {
            case DOWN -> poses.mulPose(Axis.XP.rotationDegrees(90.0F));
            case UP -> poses.mulPose(Axis.XN.rotationDegrees(90.0F));
            case NORTH -> poses.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            case EAST -> poses.mulPose(Axis.YP.rotationDegrees(90.0F));
            default -> {
            }
        }
    }
}
