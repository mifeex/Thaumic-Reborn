package com.thaumcraftmodern.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Client-only Fortress armor model factory, kept out of common item class verification. */
public final class FortressArmorClientExtensions {
    private FortressArmorClientExtensions() { }

    public static IClientItemExtensions create() {
        return new Extensions();
    }

    private static final class Extensions implements IClientItemExtensions {
        private FortressArmorModel model;
        private FortressArmorModel leggingsModel;

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,
                ItemStack stack, EquipmentSlot slot,
                HumanoidModel<?> defaultModel) {
            // Fortress has no boot-specific accessory geometry. Reusing the
            // model's leg panels here turns the feet item into leggings.
            if (slot == EquipmentSlot.FEET) {
                return defaultModel;
            }
            if (OptiFineArmorCompatibility.active()) {
                return OptiFineArmorCompatibility.invisibleModel();
            }
            FortressArmorModel selected;
            if (slot == EquipmentSlot.LEGS) {
                if (leggingsModel == null) {
                    leggingsModel = new FortressArmorModel(
                            Minecraft.getInstance().getEntityModels()
                                    .bakeLayer(FortressArmorModel.LEGGINGS_LAYER));
                }
                selected = leggingsModel;
            } else {
                if (model == null) model = new FortressArmorModel(
                        Minecraft.getInstance().getEntityModels()
                                .bakeLayer(FortressArmorModel.LAYER));
                selected = model;
            }
            copyPose(defaultModel, selected);
            selected.setAllVisible(false);
            switch (slot) {
                case HEAD -> selected.head.visible = true;
                case CHEST -> {
                    selected.body.visible = true;
                    selected.rightArm.visible = true;
                    selected.leftArm.visible = true;
                }
                case LEGS -> {
                    selected.body.visible = true;
                    selected.rightLeg.visible = true;
                    selected.leftLeg.visible = true;
                }
                case FEET -> {
                    selected.rightLeg.visible = true;
                    selected.leftLeg.visible = true;
                }
                default -> { }
            }
            selected.prepare(entity, stack, slot);
            return selected;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyPose(HumanoidModel<?> source,
            FortressArmorModel target) {
        ((HumanoidModel) source).copyPropertiesTo(target);
    }
}
