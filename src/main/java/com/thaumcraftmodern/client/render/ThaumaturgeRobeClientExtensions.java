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
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity,
                ItemStack stack,
                EquipmentSlot slot,
                HumanoidModel<?> defaultModel
        ) {
            if (slot == EquipmentSlot.FEET) {
                // TC4 ItemRobeArmor never replaces the boot model. Its exact
                // robes_1 texture is rendered on Minecraft's outer armor legs.
                return OptiFineArmorCompatibility.active()
                        ? OptiFineArmorCompatibility.invisibleModel()
                        : defaultModel;
            }
            if (OptiFineArmorCompatibility.active()) {
                return OptiFineArmorCompatibility.invisibleModel();
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
