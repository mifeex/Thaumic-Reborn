package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.HungryChestBlock;
import com.thaumcraftmodern.world.block.entity.HungryChestBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class HungryChestBlockEntityRenderer implements BlockEntityRenderer<HungryChestBlockEntity> {
    private static final ResourceLocation TEXTURE=new ResourceLocation(
            ThaumcraftModern.MOD_ID,"textures/entity/models/chesthungry.png");
    private final HungryChestModel model;
    public HungryChestBlockEntityRenderer(BlockEntityRendererProvider.Context context){model=new HungryChestModel(context.bakeLayer(HungryChestModel.LAYER));}
    @Override public void render(HungryChestBlockEntity chest,float partial,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        renderModel(model,chest.getBlockState().getValue(HungryChestBlock.FACING).toYRot(),
                chest.openness(partial),poses,buffers,light);
    }

    static void renderModel(HungryChestModel model,float yaw,float openness,PoseStack poses,
            MultiBufferSource buffers,int light){
        poses.pushPose();
        // TC4 TileChestHungryRenderer: translate(0,1,1), scale(1,-1,-1),
        // center, rotate for metadata, then uncenter.
        poses.translate(0,1,1);
        poses.scale(1,-1,-1);
        poses.translate(.5,.5,.5);
        poses.mulPose(Axis.YP.rotationDegrees(yaw));
        poses.translate(-.5,-.5,-.5);
        model.renderLidAndLock(poses,buffers.getBuffer(RenderType.entityCutout(TEXTURE)),light,
                OverlayTexture.NO_OVERLAY,openness);
        poses.popPose();
    }
}
