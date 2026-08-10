package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.InfusionPillarBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the original two-block pillar directly from block states. This also
 * covers formed altars saved before infusion pillars had BlockEntity NBT.
 */
@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class InfusionPillarWorldRenderer {
    private static final ResourceLocation MODEL = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/pillar.obj");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/pillar.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);
    private static final List<BlockPos> PILLARS = new ArrayList<>();
    private static LegacyObjMesh mesh;
    private static ClientLevel cachedLevel;
    private static BlockPos cachedCenter = BlockPos.ZERO;
    private static long lastScan = Long.MIN_VALUE;

    private InfusionPillarWorldRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) return;
        refresh(level, minecraft.player.blockPosition());
        if (PILLARS.isEmpty()) return;
        if (mesh == null) mesh = LegacyObjMesh.load(MODEL);

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        var vertices = buffers.getBuffer(RENDER_TYPE);
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        for (BlockPos position : PILLARS) {
            if (position.distToCenterSqr(camera.x, camera.y, camera.z)
                    >= 64.0D * 64.0D) continue;
            BlockState state = level.getBlockState(position);
            if (!isRenderedBase(level, position, state)) continue;
            pose.pushPose();
            pose.translate(position.getX() + 0.5D, position.getY(),
                    position.getZ() + 0.5D);
            pose.mulPose(Axis.XN.rotationDegrees(90.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(rotation(
                    state.getValue(InfusionPillarBlock.FACING))));
            mesh.render("Box001", pose, vertices,
                    LevelRenderer.getLightColor(level, state, position),
                    1.0F, 1.0F, 1.0F, 1.0F);
            pose.popPose();
        }
        pose.popPose();
        buffers.endBatch(RENDER_TYPE);
    }

    private static void refresh(ClientLevel level, BlockPos center) {
        long now = level.getGameTime();
        if (level == cachedLevel && now - lastScan < 20L
                && center.distManhattan(cachedCenter) <= 8) return;
        cachedLevel = level;
        cachedCenter = center.immutable();
        lastScan = now;
        // Preserve discovered legacy pillars while walking away. Clearing the
        // cache made the four supports disappear outside the 24-block scan.
        PILLARS.removeIf(position -> !level.hasChunkAt(position)
                || !isRenderedBase(level, position,
                level.getBlockState(position)));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = center.getY() - 8; y <= center.getY() + 8; y++) {
            for (int x = center.getX() - 24; x <= center.getX() + 24; x++) {
                for (int z = center.getZ() - 24; z <= center.getZ() + 24; z++) {
                    cursor.set(x, y, z);
                    if (level.hasChunkAt(cursor)
                            && isRenderedBase(level, cursor,
                            level.getBlockState(cursor))) {
                        BlockPos pillar = cursor.immutable();
                        if (!PILLARS.contains(pillar)) PILLARS.add(pillar);
                    }
                }
            }
        }
    }

    private static boolean isRenderedBase(
            ClientLevel level,
            BlockPos position,
            BlockState state
    ) {
        // The saved two-block structure is the source of truth. Older worlds
        // can lack both BlockEntity NBT and a reliable CAP property, but the
        // lower block is always the pillar block with another pillar above it.
        return state.is(ModBlocks.INFUSION_PILLAR.get())
                && level.getBlockState(position.above())
                .is(ModBlocks.INFUSION_PILLAR.get());
    }

    private static float rotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}
