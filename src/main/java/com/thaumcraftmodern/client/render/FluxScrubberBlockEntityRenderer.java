package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.FluxScrubberBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class FluxScrubberBlockEntityRenderer implements BlockEntityRenderer<FluxScrubberBlockEntity> {
    public static final ResourceLocation TEXTURE=new ResourceLocation(ThaumcraftModern.MOD_ID,"textures/block/fluxscrubber.png");
    private final ClassicFluxScrubberModel model=new ClassicFluxScrubberModel();
    public FluxScrubberBlockEntityRenderer(BlockEntityRendererProvider.Context ignored){}
    @Override public void render(FluxScrubberBlockEntity tile,float partial,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        float ticks=Minecraft.getInstance().player==null ? partial : Minecraft.getInstance().player.tickCount+partial;
        renderModel(poses,buffers,light,overlay,tile.facing(),Mth.sin((ticks+tile.animationSeed())/8f)*.075f+.075f);
    }
    static void renderModel(PoseStack poses,MultiBufferSource buffers,int light,int overlay,Direction facing,float bob){
        poses.pushPose(); poses.translate(.5,.5,.5); rotate(facing,poses); poses.translate(0,0,-.5);
        // obelisk_cap.obj is triangulated. A vanilla entity cutout buffer is
        // QUADS and groups every four submitted vertices, producing the huge
        // crossed polygons seen in-game. Use the legacy OBJ triangle pass.
        var out=buffers.getBuffer(EldritchRenderTypes.capTriangles(TEXTURE));
        ClassicFluxScrubberModel model=new ClassicFluxScrubberModel();
        model.renderCap(poses,out,light,overlay);
        poses.pushPose(); poses.translate(0,0,-bob); model.renderTip(poses,out,light,overlay); poses.popPose(); poses.popPose();
    }
    private static void rotate(Direction f,PoseStack p){ switch(f){case DOWN->p.mulPose(Axis.XP.rotationDegrees(-90));case UP->p.mulPose(Axis.XP.rotationDegrees(90));case SOUTH->p.mulPose(Axis.YP.rotationDegrees(180));case WEST->p.mulPose(Axis.YP.rotationDegrees(90));case EAST->p.mulPose(Axis.YP.rotationDegrees(-90));default->{}} }
}
