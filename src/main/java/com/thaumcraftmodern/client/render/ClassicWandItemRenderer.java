package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.focus.WandFocusService;
import com.thaumcraftmodern.focus.WandFocusType;
import com.thaumcraftmodern.wand.WandForm;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * NBT-aware renderer for assembled casting tools.
 *
 * <p>Unlike fixed JSON models, this renderer displays the actual rod, cap and
 * form stored in the stack. It also restores the full-bright pulse used by
 * TC4's glowing primal staff core.</p>
 */
public final class ClassicWandItemRenderer
        extends BlockEntityWithoutLevelRenderer {
    private static final String TEXTURE_PREFIX = "textures/item/";
    private static final ResourceLocation RUNES = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/script.png"
    );
    private static final ResourceLocation FOCUS_CUBE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/wand.png");
    private static final Map<HumanoidArm, ReleaseState> RELEASE_STATES =
            new EnumMap<>(HumanoidArm.class);

    private final ClassicWandModel model = new ClassicWandModel();

    public ClassicWandItemRenderer() {
        this(Minecraft.getInstance());
    }

    private ClassicWandItemRenderer(Minecraft minecraft) {
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
        if (!(stack.getItem() instanceof WandItem wand)) {
            return;
        }
        WandState state = WandVisService.state(stack).orElse(null);
        if (state == null) {
            return;
        }

        ResourceLocation rodTexture = rodTexture(state.rodId());
        ResourceLocation capTexture = componentTexture(
                "wand_cap_" + state.capId() + "_model"
        );

        poseStack.pushPose();
        ClassicWandRenderCalibration.Form calibration =
                ClassicWandRenderCalibration.form(wand.form());
        if (displayContext == ItemDisplayContext.GUI) {
            applyClassicGuiPose(poseStack, wand.form());
            if (calibration.gui().override()) {
                applyGuiOverride(poseStack, calibration.gui());
            }
        } else if (isHand(displayContext)) {
            translate(poseStack, calibration.handPreOffset());
            translate(poseStack, calibration.handOffset());
            scale(
                    poseStack,
                    isFirstPerson(displayContext)
                            ? calibration.firstPersonScale()
                            : calibration.thirdPersonScale()
            );
        } else {
            poseStack.translate(0.5D, 1.15D, 0.5D);
        }
        AnimationPose usePose = currentUsePose(
                stack,
                displayContext
        );
        if (usePose != null && usePose.modern() != null) {
            poseStack.translate(
                    usePose.modern().orbitX(),
                    usePose.modern().orbitY(),
                    0.0D
            );
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        if (usePose != null && usePose.modern() != null) {
            applyUseTilt(
                    poseStack,
                    calibration.castingPivot(),
                    usePose.modern().forwardTiltX(),
                    usePose.modern().tiltZ()
            );
        } else if (usePose != null && usePose.classic() != null) {
            applyClassicUsePose(poseStack, usePose.classic());
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (usePose != null
                && isFirstPerson(displayContext)
                && minecraft.level != null) {
            FirstPersonWandTipTracker.capture(
                    minecraft.level,
                    poseStack,
                    calibration.primaryCapTip()
            );
        }
        renderAssembledTool(
                stack,
                wand,
                state,
                poseStack,
                buffers,
                rodTexture,
                capTexture,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    /** Renders an assembled tool with TC4's exact workbench placement. */
    public void renderOnArcaneWorkbench(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (!(stack.getItem() instanceof WandItem wand)) {
            return;
        }
        WandState state = WandVisService.state(stack).orElse(null);
        if (state == null) {
            return;
        }

        ResourceLocation rodTexture = rodTexture(state.rodId());
        ResourceLocation capTexture = componentTexture(
                "wand_cap_" + state.capId() + "_model"
        );
        poseStack.pushPose();
        ArcaneWorkbenchWandTransform.apply(poseStack, wand.form());
        renderAssembledTool(
                stack,
                wand,
                state,
                poseStack,
                buffers,
                rodTexture,
                capTexture,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private void renderAssembledTool(
            ItemStack stack,
            WandItem wand,
            WandState state,
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation rodTexture,
            ResourceLocation capTexture,
            int packedLight,
            int packedOverlay
    ) {
        switch (wand.form()) {
            case WAND -> renderWand(
                    poseStack,
                    buffers,
                    rodTexture,
                    capTexture,
                    packedLight,
                    packedOverlay
            );
            case SCEPTRE -> {
                renderSceptre(
                        poseStack,
                        buffers,
                        rodTexture,
                        capTexture,
                        packedLight,
                        packedOverlay
                );
                renderSceptreRunes(
                        poseStack,
                        buffers,
                        packedOverlay,
                        animationTime()
                );
            }
            case STAFF -> renderStaff(
                    poseStack,
                    buffers,
                    rodTexture,
                    capTexture,
                    packedLight,
                    packedOverlay
            );
        }

        WandFocusService.type(stack).ifPresent(type -> renderFocus(
                type, wand.form(), poseStack, buffers, packedOverlay));

        if (state.rodId().equals("primal_staff")) {
            float time = animationTime();
            float pulse = 0.28F + (Mth.sin(time * 0.35F) + 1.0F) * 0.18F;
            VertexConsumer glow = buffers.getBuffer(
                    RenderType.entityTranslucentEmissive(rodTexture)
            );
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.2D, 0.0D);
            renderStaffRodPass(
                    poseStack,
                    glow,
                    LightTexture.FULL_BRIGHT,
                    packedOverlay,
                    pulse
            );
            renderStaffRunes(poseStack, buffers, packedOverlay, time);
            poseStack.popPose();
        }
    }

    private void renderFocus(
            WandFocusType type,
            WandForm form,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedOverlay
    ) {
        int color = type.color();
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        poseStack.pushPose();
        if (form == WandForm.STAFF) {
            poseStack.translate(0.0D, 0.1525D, 0.0D);
            poseStack.scale(0.525F, 0.5525F, 0.525F);
        } else {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
        model.renderFocus(
                poseStack,
                buffers.getBuffer(RenderType.entityTranslucentEmissive(FOCUS_CUBE)),
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                red,
                green,
                blue,
                0.95F
        );
        poseStack.popPose();
    }

    private static void translate(
            PoseStack poseStack,
            ClassicWandRenderCalibration.Vector vector
    ) {
        poseStack.translate(vector.x(), vector.y(), vector.z());
    }

    private static void scale(
            PoseStack poseStack,
            ClassicWandRenderCalibration.Vector vector
    ) {
        poseStack.scale(vector.x(), vector.y(), vector.z());
    }

    private static void applyGuiOverride(
            PoseStack poseStack,
            ClassicWandRenderCalibration.Gui gui
    ) {
        ClassicWandRenderCalibration.Vector translation =
                gui.translationPixels();
        poseStack.translate(
                translation.x() / 16.0F,
                translation.y() / 16.0F,
                translation.z() / 16.0F
        );
        ClassicWandRenderCalibration.Vector rotation =
                gui.rotationDegrees();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation.x()));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation.y()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.z()));
        scale(poseStack, gui.scale());
    }

    /**
     * Exact TC4 {@code ItemWandRenderer} inventory transform. The original
     * builtin/entity JSON is identity; all slot sizing and centering happens
     * here in this order.
     */
    private static void applyClassicGuiPose(
            PoseStack poseStack,
            WandForm form
    ) {
        if (form == WandForm.STAFF) {
            poseStack.translate(0.0D, 0.5D, 0.0D);
        }
        poseStack.translate(0.5D, 0.5D, 0.0D);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        if (form == WandForm.STAFF) {
            poseStack.scale(0.8F, 0.8F, 0.8F);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
        if (form == WandForm.STAFF) {
            poseStack.translate(-0.7D, 1.2D, 0.0D);
        } else {
            poseStack.translate(0.0D, 0.6D, 0.0D);
        }
    }

    private static AnimationPose currentUsePose(
            ItemStack stack,
            ItemDisplayContext displayContext
    ) {
        boolean rightHand = isRightHand(displayContext);
        boolean leftHand = isLeftHand(displayContext);
        if (!rightHand && !leftHand) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return null;
        }

        HumanoidArm renderedArm = rightHand
                ? HumanoidArm.RIGHT
                : HumanoidArm.LEFT;
        float renderTime = minecraft.player.tickCount
                + minecraft.getFrameTime();
        boolean activelyUsingThisArm = false;
        if (minecraft.player.isUsingItem()
                && stack.getItem()
                == minecraft.player.getUseItem().getItem()) {
            InteractionHand usedHand = minecraft.player.getUsedItemHand();
            HumanoidArm usedArm = usedHand == InteractionHand.MAIN_HAND
                    ? minecraft.player.getMainArm()
                    : minecraft.player.getMainArm().getOpposite();
            activelyUsingThisArm = usedArm == renderedArm;
        }

        ClassicWandRenderCalibration.DrainAnimationMode mode =
                ClassicWandRenderCalibration.drainAnimationMode();
        if (activelyUsingThisArm) {
            float elapsedUseTicks = minecraft.player.getTicksUsingItem()
                    + minecraft.getFrameTime();
            if (mode
                    == ClassicWandRenderCalibration.DrainAnimationMode.CLASSIC) {
                RELEASE_STATES.remove(renderedArm);
                return AnimationPose.classic(
                        ClassicWandDrainAnimation.sample(
                                elapsedUseTicks,
                                renderedArm,
                                isFirstPerson(displayContext)
                        )
                );
            }

            WandDrainAnimation.Transform modern =
                    WandDrainAnimation.sample(
                            elapsedUseTicks,
                            renderedArm
                    );
            RELEASE_STATES.put(
                    renderedArm,
                    new ReleaseState(
                            stack.getItem(),
                            renderTime,
                            modern
                    )
            );
            return AnimationPose.modern(modern);
        }

        if (mode
                == ClassicWandRenderCalibration.DrainAnimationMode.CLASSIC) {
            RELEASE_STATES.remove(renderedArm);
            return null;
        }

        ReleaseState release = RELEASE_STATES.get(renderedArm);
        if (release == null || release.item() != stack.getItem()) {
            return null;
        }
        float elapsedReturnTicks = renderTime - release.lastActiveTime();
        if (elapsedReturnTicks < 0.0F
                || elapsedReturnTicks > WandDrainAnimation.RETURN_TICKS) {
            RELEASE_STATES.remove(renderedArm);
            return null;
        }
        return AnimationPose.modern(WandDrainAnimation.sampleReturn(
                elapsedReturnTicks,
                release.releasePose()
        ));
    }

    private static void applyUseTilt(
            PoseStack poseStack,
            ClassicWandRenderCalibration.Vector pivot,
            float forwardTiltX,
            float tiltZ
    ) {
        poseStack.translate(pivot.x(), pivot.y(), pivot.z());
        poseStack.mulPose(Axis.XP.rotationDegrees(forwardTiltX));
        poseStack.mulPose(Axis.ZP.rotationDegrees(tiltZ));
        poseStack.translate(-pivot.x(), -pivot.y(), -pivot.z());
    }

    private static void applyClassicUsePose(
            PoseStack poseStack,
            ClassicWandDrainAnimation.Transform usePose
    ) {
        poseStack.translate(0.0D, 1.0D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(
                usePose.contextRotationX()
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                usePose.contextRotationZ()
        ));
        poseStack.mulPose(Axis.XP.rotationDegrees(
                usePose.startupRotationX()
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                usePose.waveRotationZ()
        ));
        poseStack.mulPose(Axis.XP.rotationDegrees(
                usePose.waveRotationX()
        ));
        poseStack.translate(0.0D, -1.0D, 0.0D);
    }

    private record AnimationPose(
            WandDrainAnimation.Transform modern,
            ClassicWandDrainAnimation.Transform classic
    ) {
        private static AnimationPose modern(
                WandDrainAnimation.Transform transform
        ) {
            return new AnimationPose(transform, null);
        }

        private static AnimationPose classic(
                ClassicWandDrainAnimation.Transform transform
        ) {
            return new AnimationPose(null, transform);
        }
    }

    private record ReleaseState(
            Item item,
            float lastActiveTime,
            WandDrainAnimation.Transform releasePose
    ) {
    }

    private static boolean isFirstPerson(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    private static boolean isHand(ItemDisplayContext context) {
        return isRightHand(context) || isLeftHand(context);
    }

    private static boolean isRightHand(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private static boolean isLeftHand(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    private void renderWand(
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation rodTexture,
            ResourceLocation capTexture,
            int packedLight,
            int packedOverlay
    ) {
        model.renderRod(
                poseStack,
                cutoutBuffer(buffers, rodTexture),
                packedLight,
                packedOverlay,
                1.0F
        );
        poseStack.pushPose();
        poseStack.scale(1.2F, 1.0F, 1.2F);
        model.renderTopCap(
                poseStack,
                cutoutBuffer(buffers, capTexture),
                packedLight,
                packedOverlay
        );
        model.renderBottomCap(
                poseStack,
                cutoutBuffer(buffers, capTexture),
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private void renderSceptre(
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation rodTexture,
            ResourceLocation capTexture,
            int packedLight,
            int packedOverlay
    ) {
        model.renderRod(
                poseStack,
                cutoutBuffer(buffers, rodTexture),
                packedLight,
                packedOverlay,
                1.0F
        );

        /*
         * Exact TC4 cap stack: normal 1.2-wide cap transform, a 1.3x enlarged
         * head, then the same cap compressed to 66% height at y=0.3. The rune
         * ring is rendered separately between these two cap silhouettes.
         */
        poseStack.pushPose();
        poseStack.scale(1.2F, 1.0F, 1.2F);
        poseStack.pushPose();
        poseStack.scale(1.3F, 1.3F, 1.3F);
        model.renderTopCap(
                poseStack,
                cutoutBuffer(buffers, capTexture),
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.3D, 0.0D);
        poseStack.scale(1.0F, 0.66F, 1.0F);
        model.renderTopCap(
                poseStack,
                cutoutBuffer(buffers, capTexture),
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
        model.renderBottomCap(
                poseStack,
                cutoutBuffer(buffers, capTexture),
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private void renderStaff(
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation rodTexture,
            ResourceLocation capTexture,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.2D, 0.0D);
        renderStaffRodPass(
                poseStack,
                cutoutBuffer(buffers, rodTexture),
                packedLight,
                packedOverlay,
                1.0F
        );

        /*
         * These are the original ModelWand staff transforms. In particular,
         * the lower cap ends at the stretched rod instead of being placed at
         * a guessed model-space coordinate.
         */
        poseStack.pushPose();
        poseStack.scale(1.3F, 1.1F, 1.3F);
        model.renderTopCap(
                poseStack,
                cutoutBuffer(buffers, capTexture),
                packedLight,
                packedOverlay
        );
        poseStack.translate(0.0D, 0.225D, 0.0D);
        poseStack.pushPose();
        poseStack.scale(1.0F, 0.66F, 1.0F);
        model.renderTopCap(
                poseStack,
                cutoutBuffer(buffers, capTexture),
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        model.renderBottomCap(
                poseStack,
                cutoutBuffer(buffers, capTexture),
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
        poseStack.popPose();
    }

    private void renderStaffRodPass(
            PoseStack poseStack,
            VertexConsumer vertices,
            int packedLight,
            int packedOverlay,
            float alpha
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.1D, 0.0D);
        poseStack.scale(1.2F, 2.0F, 1.2F);
        model.renderRod(
                poseStack,
                vertices,
                packedLight,
                packedOverlay,
                alpha
        );
        poseStack.popPose();
    }

    private void renderSceptreRunes(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedOverlay,
            float time
    ) {
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityTranslucentEmissive(RUNES)
        );
        float rotation = time;
        for (int index = 0; index < 10; index++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    rotation + index * 36.0F
            ));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            poseStack.translate(0.16D, -0.01D, -0.125D);
            renderClassicRune(
                    index,
                    poseStack,
                    vertices,
                    packedOverlay,
                    time
            );
            poseStack.popPose();
        }
    }

    private void renderStaffRunes(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedOverlay,
            float time
    ) {
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityTranslucentEmissive(RUNES)
        );
        for (int side = 0; side < 4; side++) {
            for (int row = 0; row < 14; row++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(
                        (side + 1) * 90.0F
                ));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                poseStack.translate(
                        0.36D + row * 0.14D,
                        -0.01D,
                        -0.08D
                );
                renderClassicRune(
                        row + side * 3,
                        poseStack,
                        vertices,
                        packedOverlay,
                        time
                );
                poseStack.popPose();
            }
        }
    }

    private void renderClassicRune(
            int index,
            PoseStack poseStack,
            VertexConsumer vertices,
            int packedOverlay,
            float time
    ) {
        float phase = time + index * 5.0F;
        float red = 0.88F + Mth.sin(phase / 5.0F) * 0.1F;
        float green = 0.63F + Mth.sin(phase / 7.0F) * 0.1F;
        float blue = 0.2F;
        float sizePulse = Mth.sin(phase / 10.0F) * 0.2F;
        float alpha = 0.6F + sizePulse;
        float halfSize = 0.06F + sizePulse / 40.0F;
        model.renderRune(
                index,
                poseStack,
                vertices,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                red,
                green,
                blue,
                alpha,
                halfSize
        );
    }

    private static float animationTime() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null
                ? 0.0F
                : minecraft.player.tickCount + minecraft.getFrameTime();
    }

    private static VertexConsumer cutoutBuffer(
            MultiBufferSource buffers,
            ResourceLocation texture
    ) {
        /*
         * BufferSource may finish the previous non-fixed RenderType when
         * getBuffer switches textures. Fetch each component buffer immediately
         * before writing that component's vertices; retaining both rod and cap
         * consumers at once makes assembled tools intermittently lose or
         * scramble parts.
         */
        return buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
    }

    private static ResourceLocation rodTexture(String rodId) {
        String base = rodId.endsWith("_staff")
                ? rodId.substring(0, rodId.length() - "_staff".length())
                : rodId;
        if (base.equals("codex")) {
            base = "silverwood";
        }
        return componentTexture("wand_rod_" + base + "_model");
    }

    private static ResourceLocation componentTexture(String name) {
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                TEXTURE_PREFIX + name + ".png"
        );
    }
}
