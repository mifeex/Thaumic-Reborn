package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.FortressArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Exact 66-cuboid conversion of TC4 ModelFortressArmor. */
public final class FortressArmorModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID,
                    "fortress_armor"), "main");
    private static final String DATA =
            "/assets/thaumcraftmodern/models/entity/fortress_armor.csv";

    public FortressArmorModel(ModelPart root) { super(root); }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(),
                PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(),
                PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(),
                PartPose.offset(-1.9F, 12, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(),
                PartPose.offset(1.9F, 12, 0));
        Map<String, PartDefinition> parents = Map.of(
                "head", root.getChild("head"), "body", root.getChild("body"),
                "rightArm", root.getChild("right_arm"),
                "leftArm", root.getChild("left_arm"),
                "rightLeg", root.getChild("right_leg"),
                "leftLeg", root.getChild("left_leg"));
        try (InputStream stream = FortressArmorModel.class.getResourceAsStream(DATA)) {
            if (stream == null) throw new IllegalStateException("Missing " + DATA);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream, StandardCharsets.UTF_8))) {
                reader.lines().filter(line -> !line.isBlank() && !line.startsWith("#"))
                        .forEach(line -> addPart(line, parents));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read " + DATA, exception);
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void addPart(String line, Map<String, PartDefinition> parents) {
        String[] v = line.split(",");
        if (v.length != 17) throw new IllegalStateException("Invalid fortress row " + line);
        CubeListBuilder cube = CubeListBuilder.create().texOffs(i(v[2]), i(v[3]));
        if (Boolean.parseBoolean(v[4])) cube.mirror();
        cube.addBox(f(v[5]), f(v[6]), f(v[7]), f(v[8]), f(v[9]), f(v[10]));
        parents.get(v[0]).addOrReplaceChild(v[1], cube,
                PartPose.offsetAndRotation(f(v[11]), f(v[12]), f(v[13]),
                        f(v[14]), f(v[15]), f(v[16])));
    }

    public void prepare(LivingEntity entity, ItemStack stack, EquipmentSlot slot) {
        int set = 0;
        for (EquipmentSlot checked : new EquipmentSlot[]{EquipmentSlot.LEGS,
                EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            if (entity.getItemBySlot(checked).getItem() instanceof FortressArmorItem) set++;
        }
        Integer mask = FortressArmorItem.mask(stack);
        boolean goggles = FortressArmorItem.hasGoggles(stack);
        child(head, "Goggles", goggles);
        for (int index = 0; index < 3; index++) child(head, "Mask" + index,
                mask != null && mask == index);
        for (String name : new String[]{"OrnamentL", "OrnamentL2", "OrnamentR",
                "OrnamentR2", "Gemornament", "Gem"}) child(head, name, set >= 3);
        for (String name : new String[]{"flapL", "flapR"}) child(head, name, set >= 2);
        child(body, "Scroll", set >= 3);
        child(body, "Book", set >= 2);
        tieredArm(rightArm, "R", set);
        tieredArm(leftArm, "L", set);
        tieredLeg(rightLeg, "R", set);
        tieredLeg(leftLeg, "L", set);
        suppressChestGeometryForLeggings(slot);
    }

    /**
     * Minecraft deliberately makes a humanoid armor model's body visible for
     * leggings. That is suitable for vanilla's small waistband, but this
     * model's body contains the complete Fortress chest piece. Hide its child
     * cuboids for the legs slot; the armor layer may re-enable the body root
     * afterwards, but it does not re-enable these children.
     */
    private void suppressChestGeometryForLeggings(EquipmentSlot slot) {
        if (slot == EquipmentSlot.LEGS) {
            body.getAllParts().skip(1).forEach(part -> part.visible = false);
        }
    }

    private static void tieredArm(ModelPart arm, String side, int set) {
        child(arm, "Shoulderplate" + side + "top", set >= 2);
        child(arm, "Shoulderplate" + side + "1", set >= 2);
        child(arm, "Shoulderplate" + side + "2", set >= 3);
        child(arm, "Shoulderplate" + side + "3", set >= 3);
    }

    private static void tieredLeg(ModelPart leg, String side, int set) {
        child(leg, "Sidepanel" + side + "2", set >= 2);
        child(leg, "Sidepanel" + side + "3", set >= 3);
    }

    private static void child(ModelPart parent, String name, boolean visible) {
        parent.getChild(name).visible = visible;
    }
    private static int i(String value) { return Integer.parseInt(value); }
    private static float f(String value) {
        return Float.parseFloat(value.replace("f", ""));
    }
}
