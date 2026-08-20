package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;

/**
 * Pixel-model reconstruction of the approved battle-armor/mantle concept.
 * Every ornament is a rectangular UV-mapped part suitable for Minecraft's
 * native armor render path; wings remain children of the torso.
 */
public final class WingedMantleArmorModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "winged_mantle_armor"),
            "main"
    );
    public static final ModelLayerLocation OPTIFINE_LAYER =
            new ModelLayerLocation(
                    new ResourceLocation(
                            ThaumcraftModern.MOD_ID,
                            "winged_mantle_armor_optifine"
                    ),
                    "main"
            );

    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingMiddle;
    private final ModelPart rightWingMiddle;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingTip;

    public WingedMantleArmorModel(ModelPart root) {
        super(root);
        leftWing = body.getChild("left_wing");
        rightWing = body.getChild("right_wing");
        leftWingMiddle = leftWing.getChild("middle");
        rightWingMiddle = rightWing.getChild("middle");
        leftWingTip = leftWingMiddle.getChild("lower");
        rightWingTip = rightWingMiddle.getChild("lower");
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(4096);
    }

    /**
     * OptiFine 1.20.1 drops CubeDefinition's per-cube 1/16 UV scale while
     * baking custom armor. A logical 256-pixel layer restores the normalized
     * UVs without changing any cuboid, pivot or rotation.
     */
    public static LayerDefinition createOptiFineBodyLayer() {
        return createBodyLayer(256);
    }

    private static LayerDefinition createBodyLayer(int textureSize) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = empty(root, "head", PartPose.ZERO);
        empty(root, "hat", PartPose.ZERO);
        PartDefinition body = empty(root, "body", PartPose.ZERO);
        PartDefinition rightArm = empty(root, "right_arm", PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = empty(root, "left_arm", PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition rightLeg = empty(root, "right_leg", PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition leftLeg = empty(root, "left_leg", PartPose.offset(1.9F, 12.0F, 0.0F));

        // Literal TC4 ModelRobe hood1..hood4 conversion.  The old inflated
        // cube/collar approximation made the hood read as a square helmet;
        // these are the original dimensions, offsets and rearward rotations.
        head.addOrReplaceChild("hood1", cube(0, 96,
                -4.5F, -9.0F, -4.6F, 9.0F, 9.0F, 9.0F), PartPose.ZERO);
        head.addOrReplaceChild("hood2", cube(44, 96,
                -4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F),
                PartPose.rotation(-0.2268928F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood3", cube(74, 96,
                -3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F),
                PartPose.rotation(-0.3490659F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood4", cube(102, 96,
                -3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F),
                PartPose.rotation(-0.5759587F, 0.0F, 0.0F));

        body.addOrReplaceChild("chest_plate", cube(0, 34, -4.5F, 0.0F, -2.5F,
                9.0F, 9.5F, 5.0F), PartPose.ZERO);
        // Complete non-cloak torso construction from TC4 ModelLeaderArmor.
        // UVs point into a lossless recolored copy of the original 256x128
        // Praetor atlas stored in the lower-right quadrant of our atlas.
        body.addOrReplaceChild("praetor_collar_front", cube(145, 159,
                -4.5F, -1.5F, -3.0F, 9.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F,
                        0.2268928F, 0.0F, 0.0F));
        body.addOrReplaceChild("praetor_collar_back", cube(145, 154,
                -4.5F, -1.5F, 7.0F, 9.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F,
                        0.2268928F, 0.0F, 0.0F));
        body.addOrReplaceChild("praetor_collar_right", cube(145, 139,
                -5.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F,
                        0.2268928F, 0.0F, 0.0F));
        body.addOrReplaceChild("praetor_collar_left", cube(145, 139,
                4.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F,
                        0.2268928F, 0.0F, 0.0F));
        body.addOrReplaceChild("praetor_chestplate", cube(184, 173,
                -4.0F, 1.0F, -3.8F, 8.0F, 7.0F, 2.0F), PartPose.ZERO);
        body.addOrReplaceChild("raised_chest_focus", cube(204, 181,
                -3.0F, 2.8125F, -4.8F, 6.0F, 6.0F, 1.0F), PartPose.ZERO);
        body.addOrReplaceChild("praetor_chestcloth_left", mirroredCube(148, 175, true,
                1.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F),
                PartPose.rotation(0.0663225F, 0.0F, 0.0F));
        body.addOrReplaceChild("praetor_chestcloth_right", cube(148, 175,
                -4.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F),
                PartPose.rotation(0.0663225F, 0.0F, 0.0F));
        body.addOrReplaceChild("praetor_backplate", cube(164, 173,
                -4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F), PartPose.ZERO);
        body.addOrReplaceChild("praetor_belt_left", cube(204, 172,
                4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
        body.addOrReplaceChild("praetor_belt_right", cube(204, 172,
                -5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
        // The approved chest ornament is pixel work on cloth, not a stack of
        // protruding cubes. Keep stable part names for compatibility/tests,
        // while the 16x atlas carries the yoke, straps and both focus emblems.
        empty(body, "chest_yoke", PartPose.ZERO);
        empty(body, "chest_strap_top", PartPose.ZERO);
        empty(body, "chest_strap_low", PartPose.ZERO);
        empty(body, "chest_focus", PartPose.ZERO);
        empty(body, "focus_core", PartPose.ZERO);
        empty(body, "focus_crown", PartPose.ZERO);
        empty(body, "focus_stud_left", PartPose.ZERO);
        empty(body, "focus_stud_right", PartPose.ZERO);
        empty(body, "back_focus", PartPose.ZERO);
        empty(body, "back_focus_core", PartPose.ZERO);
        body.addOrReplaceChild("belt", cube(208, 224, -5.7F, 8.6F, -2.9F, 11.4F, 2.0F, 5.8F), PartPose.ZERO);
        // The approved front UV already contains its buckle.
        empty(body, "buckle", PartPose.ZERO);
        body.addOrReplaceChild("left_tail", cube(0, 50, 0.15F, 9.7F, -2.55F, 4.25F, 13.5F, 1.2F), PartPose.rotation(0.03F, 0.0F, 0.025F));
        body.addOrReplaceChild("right_tail", cube(14, 50, -4.4F, 9.7F, -2.55F, 4.25F, 13.5F, 1.2F), PartPose.rotation(0.03F, 0.0F, -0.025F));
        // No hanging rear panel: it protruded between the Elytra wings and
        // overlapped the player's legs in third person.
        empty(body, "back_mantle", PartPose.ZERO);
        body.addOrReplaceChild("elytra_bridge", cube(112, 80,
                -4.2F, 0.5F, 2.4F, 8.4F, 3.5F, 1.6F), PartPose.ZERO);
        // Removed from the body pass: this diagonal block read as an
        // unrelated object stuck to the player's hand in third person.
        empty(body, "book", PartPose.ZERO);
        empty(body, "book_clasp", PartPose.ZERO);
        // The parchment is part of the literal screen-left sleeve transfer.
        empty(body, "scroll", PartPose.ZERO);
        body.addOrReplaceChild("pouch", cube(96, 50, -5.7F, 8.8F, -2.8F, 2.8F, 3.4F, 2.2F), PartPose.ZERO);

        addArmArmor(rightArm, false);
        addArmArmor(leftArm, true);
        addLegArmor(rightLeg, false);
        addLegArmor(leftLeg, true);

        addWing(body, true);
        addWing(body, false);

        return LayerDefinition.create(mesh, textureSize, textureSize);
    }

    private static void addWing(PartDefinition body, boolean left) {
        String name = left ? "left_wing" : "right_wing";
        // Rendering moved to WingedMantleElytraLayer, which uses Minecraft's
        // real ElytraModel and its standalone 64x32 no-cull texture.
        PartDefinition wing = empty(body, name, PartPose.ZERO);
        empty(wing, "panel", PartPose.ZERO);

        // Stable empty nodes retain compatibility with the animation code;
        // the rendered silhouette and UV are now exactly vanilla Elytra.
        empty(wing, "hinge", PartPose.ZERO);
        empty(wing, "upper", PartPose.ZERO);
        empty(wing, "upper_rib", PartPose.ZERO);
        empty(wing, "upper_stud", PartPose.ZERO);
        PartDefinition middle = empty(wing, "middle", PartPose.ZERO);
        empty(middle, "middle_rib", PartPose.ZERO);
        empty(middle, "middle_stud", PartPose.ZERO);
        empty(middle, "glyph", PartPose.ZERO);
        PartDefinition lower = empty(middle, "lower", PartPose.ZERO);
        empty(lower, "lower_rib", PartPose.ZERO);
        empty(lower, "tip_stud", PartPose.ZERO);
    }

    private static void addArmArmor(PartDefinition arm, boolean mirror) {
        int u = mirror ? 64 : 0;
        // One uninterrupted sleeve keeps the player's skin from showing
        // between stacked bracer boxes. Its complete front/back pixel work is
        // transferred from the approved chest-and-arms reference.
        arm.addOrReplaceChild("sleeve", mirroredCube(u, 192, mirror,
                mirror ? -1.5F : -3.5F, -2.5F, -2.5F,
                5.0F, 13.0F, 5.0F), PartPose.ZERO);
        // Exact TC4 ModelRobe forearm layers (RArm2/RArm3 and LArm2/LArm3).
        // Together with the five-pixel sleeve they restore the cleric robe's
        // widening and layered cloth detail around the forearm.
        arm.addOrReplaceChild("cleric_forearm_fold", mirroredCube(208, 240, mirror,
                mirror ? -1.0F : -3.0F, 5.5F, 2.5F,
                4.0F, 4.0F, 2.0F), PartPose.ZERO);
        arm.addOrReplaceChild("cleric_forearm_ridge", mirroredCube(224, 240, mirror,
                mirror ? -0.5F : -2.5F, 3.5F, 2.5F,
                3.0F, 2.0F, 1.0F), PartPose.ZERO);
        // Exact four-step Fortress shoulder construction from TC4
        // ModelFortressArmor, including its 25-degree outward cant. The UVs
        // address a lossless 16x copy of the original Fortress atlas.
        arm.addOrReplaceChild("pauldron_top", mirroredCube(238, 37, mirror,
                mirror ? 3.5F : -5.5F, -2.5F, -3.5F,
                2.0F, 1.0F, 7.0F),
                PartPose.rotation(0.0F, 0.0F, mirror ? -0.4363323F : 0.4363323F));
        arm.addOrReplaceChild("pauldron_mid", mirroredCube(238, 45, mirror,
                mirror ? 3.5F : -4.5F, -1.5F, -3.5F,
                1.0F, 4.0F, 7.0F),
                PartPose.rotation(0.0F, 0.0F, mirror ? -0.4363323F : 0.4363323F));
        arm.addOrReplaceChild("pauldron_low", mirroredCube(222, 45, mirror,
                mirror ? 2.5F : -3.5F, 1.5F, -3.5F,
                1.0F, 3.0F, 7.0F),
                PartPose.rotation(0.0F, 0.0F, mirror ? -0.4363323F : 0.4363323F));
        arm.addOrReplaceChild("pauldron_stud", mirroredCube(222, 45, mirror,
                mirror ? 1.5F : -2.5F, 3.5F, -3.5F,
                1.0F, 3.0F, 7.0F),
                PartPose.rotation(0.0F, 0.0F, mirror ? -0.4363323F : 0.4363323F));
        empty(arm, "bracer", PartPose.ZERO);
        empty(arm, "strap", PartPose.ZERO);
    }

    private static void addLegArmor(PartDefinition leg, boolean mirror) {
        int u = mirror ? 176 : 128;
        leg.addOrReplaceChild("greave", mirroredCube(u, 192, mirror,
                -2.35F, -0.1F, -2.35F, 4.7F, 10.8F, 4.7F), PartPose.ZERO);
        leg.addOrReplaceChild("boot", mirroredCube(u, 216, mirror,
                -2.5F, 8.0F, -3.05F, 5.0F, 4.1F, 5.55F), PartPose.ZERO);
    }

    private static PartDefinition empty(PartDefinition parent, String name, PartPose pose) {
        return parent.addOrReplaceChild(name, CubeListBuilder.create(), pose);
    }

    public void configureForSlot(EquipmentSlot slot) {
        setTreeVisible(head, false);
        setTreeVisible(hat, false);
        setTreeVisible(body, false);
        setTreeVisible(rightArm, false);
        setTreeVisible(leftArm, false);
        setTreeVisible(rightLeg, false);
        setTreeVisible(leftLeg, false);

        switch (slot) {
            case HEAD -> setTreeVisible(head, true);
            case CHEST -> {
                setTreeVisible(body, true);
                setTreeVisible(rightArm, true);
                setTreeVisible(leftArm, true);
                body.getChild("left_tail").visible = false;
                body.getChild("right_tail").visible = false;
            }
            case LEGS -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
                rightLeg.getChild("greave").visible = true;
                leftLeg.getChild("greave").visible = true;
            }
            case FEET -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
                rightLeg.getChild("boot").visible = true;
                leftLeg.getChild("boot").visible = true;
            }
            default -> { }
        }
    }

    private static void setTreeVisible(ModelPart root, boolean visible) {
        root.getAllParts().forEach(part -> part.visible = visible);
    }

    private static CubeListBuilder cube(int u, int v, float x, float y, float z,
                                        float w, float h, float d) {
        return cube(u, v, x, y, z, w, h, d, CubeDeformation.NONE);
    }

    private static CubeListBuilder cube(int u, int v, float x, float y, float z,
                                        float w, float h, float d, CubeDeformation deformation) {
        return CubeListBuilder.create().texOffs(u, v)
                .addBox(x, y, z, w, h, d, deformation, 0.0625F, 0.0625F);
    }

    private static CubeListBuilder mirroredCube(int u, int v, boolean mirror,
                                                float x, float y, float z,
                                                float w, float h, float d) {
        return CubeListBuilder.create().texOffs(u, v).mirror(mirror)
                .addBox(x, y, z, w, h, d, CubeDeformation.NONE, 0.0625F, 0.0625F);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing,
                          float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        animateWings(entity, limbSwing, limbSwingAmount, ageInTicks);
    }

    /**
     * ArmorLayer copies the parent humanoid pose into custom armor models, but
     * does not consistently call their setupAnim method. The client extension
     * invokes this hook explicitly on every rendered frame.
     */
    public void animateWings(LivingEntity entity, float limbSwing,
                             float limbSwingAmount, float ageInTicks) {
        boolean inventoryPreview = isInventoryPreview(entity);
        float idle = Mth.sin(ageInTicks * 0.085F) * 0.025F;

        if (inventoryPreview) {
            setFoldedPreviewPose(idle);
            return;
        }

        float xRot = 0.2617994F;
        float zRot = -0.2617994F;
        float y = 0.0F;
        float yRot = 0.0F;
        if (entity.isFallFlying()) {
            float glide = 1.0F;
            Vec3 velocity = entity.getDeltaMovement();
            if (velocity.y < 0.0D) {
                Vec3 normalized = velocity.normalize();
                glide = 1.0F - (float) Math.pow(-normalized.y, 1.5D);
            }
            xRot = glide * 0.34906584F + (1.0F - glide) * xRot;
            zRot = glide * -1.5707964F + (1.0F - glide) * zRot;
        } else if (entity.isCrouching()) {
            xRot = 0.6981317F;
            zRot = -0.7853982F;
            y = 3.0F;
            yRot = 0.08726646F;
        } else {
            float movement = Mth.clamp(limbSwingAmount * 2.0F, 0.0F, 1.0F);
            float walkSwing = Mth.sin(limbSwing * 0.6662F)
                    * 0.18F * movement;
            xRot += idle + Math.abs(walkSwing) * 0.15F;
            zRot += walkSwing;
            yRot = walkSwing * 0.25F;
        }

        leftWing.y = y;
        if (entity instanceof AbstractClientPlayer player) {
            player.elytraRotX += (xRot - player.elytraRotX) * 0.1F;
            player.elytraRotY += (yRot - player.elytraRotY) * 0.1F;
            player.elytraRotZ += (zRot - player.elytraRotZ) * 0.1F;
            leftWing.xRot = player.elytraRotX;
            leftWing.yRot = player.elytraRotY;
            leftWing.zRot = player.elytraRotZ;
        } else {
            leftWing.xRot = xRot;
            leftWing.yRot = yRot;
            leftWing.zRot = zRot;
        }
        rightWing.y = leftWing.y;
        rightWing.xRot = leftWing.xRot;
        rightWing.yRot = -leftWing.yRot;
        rightWing.zRot = -leftWing.zRot;
    }

    private void setFoldedPreviewPose(float idle) {
        leftWing.y = 0.0F;
        rightWing.y = 0.0F;
        leftWing.xRot = 0.2617994F + idle;
        rightWing.xRot = leftWing.xRot;
        leftWing.yRot = 0.0F;
        rightWing.yRot = 0.0F;
        leftWing.zRot = -0.2617994F;
        rightWing.zRot = 0.2617994F;
    }

    private static boolean isInventoryPreview(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == entity
                && (minecraft.screen instanceof InventoryScreen
                || minecraft.screen instanceof CreativeModeInventoryScreen);
    }
}
