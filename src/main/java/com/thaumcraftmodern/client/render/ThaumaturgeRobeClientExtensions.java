package com.thaumcraftmodern.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class ThaumaturgeRobeClientExtensions {
    private ThaumaturgeRobeClientExtensions() {
    }

    public static IClientItemExtensions create() {
        return new Extensions();
    }

    private static final class Extensions implements IClientItemExtensions {
        private ThaumaturgeRobeArmorModel model;
        private ThaumaturgeRobeArmorModel boots;

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity,
                ItemStack stack,
                EquipmentSlot slot,
                HumanoidModel<?> defaultModel
        ) {
            if (slot == EquipmentSlot.FEET) {
                if (boots == null) {
                    boots = new ThaumaturgeRobeArmorModel(
                            Minecraft.getInstance().getEntityModels()
                                    .bakeLayer(ThaumaturgeRobeArmorModel.BOOTS_LAYER)
                    );
                }
                copyPose(defaultModel, boots);
                boots.setAllVisible(false);
                boots.rightLeg.visible = true;
                boots.leftLeg.visible = true;
                return boots;
            }
            if (slot != EquipmentSlot.CHEST) {
                return defaultModel;
            }
            if (model == null) {
                model = new ThaumaturgeRobeArmorModel(
                        Minecraft.getInstance().getEntityModels()
                                .bakeLayer(ThaumaturgeRobeArmorModel.OUTER_LAYER)
                );
            }
            copyPose(defaultModel, model);
            model.setAllVisible(false);
            model.body.visible = true;
            model.rightArm.visible = true;
            model.leftArm.visible = true;
            return model;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static void copyPose(
                HumanoidModel<?> source,
                ThaumaturgeRobeArmorModel target
        ) {
            ((HumanoidModel) source).copyPropertiesTo(target);
        }
    }
}
