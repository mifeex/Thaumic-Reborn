package com.thaumcraftmodern.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Slot routing from TC4 {@code ItemVoidRobeArmor#getArmorModel}. */
public final class VoidRobeClientExtensions {
    private VoidRobeClientExtensions() {
    }

    public static IClientItemExtensions create() {
        return new Extensions();
    }

    private static final class Extensions implements IClientItemExtensions {
        private VoidRobeArmorModel outer;
        private VoidRobeArmorModel inner;

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity,
                ItemStack stack,
                EquipmentSlot slot,
                HumanoidModel<?> defaultModel
        ) {
            if (OptiFineArmorCompatibility.active()) {
                return OptiFineArmorCompatibility.invisibleModel();
            }
            boolean innerLayer = slot == EquipmentSlot.HEAD
                    || slot == EquipmentSlot.LEGS;
            VoidRobeArmorModel model = innerLayer ? inner() : outer();
            copyPose(defaultModel, model);
            model.setAllVisible(false);
            switch (slot) {
                case HEAD -> model.head.visible = true;
                case CHEST -> {
                    model.body.visible = true;
                    model.rightArm.visible = true;
                    model.leftArm.visible = true;
                }
                case LEGS -> {
                    model.body.visible = true;
                    model.rightLeg.visible = true;
                    model.leftLeg.visible = true;
                }
                case FEET -> {
                    model.rightLeg.visible = true;
                    model.leftLeg.visible = true;
                }
                default -> {
                }
            }
            return model;
        }

        private VoidRobeArmorModel outer() {
            if (outer == null) {
                outer = new VoidRobeArmorModel(
                        Minecraft.getInstance().getEntityModels()
                                .bakeLayer(VoidRobeArmorModel.OUTER_LAYER),
                        false
                );
            }
            return outer;
        }

        private VoidRobeArmorModel inner() {
            if (inner == null) {
                inner = new VoidRobeArmorModel(
                        Minecraft.getInstance().getEntityModels()
                                .bakeLayer(VoidRobeArmorModel.INNER_LAYER),
                        true
                );
            }
            return inner;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static void copyPose(
                HumanoidModel<?> source,
                VoidRobeArmorModel target
        ) {
            ((HumanoidModel) source).copyPropertiesTo(target);
        }
    }
}
