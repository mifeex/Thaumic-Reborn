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

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,
                ItemStack stack, EquipmentSlot slot,
                HumanoidModel<?> defaultModel) {
            if (model == null) model = new FortressArmorModel(
                    Minecraft.getInstance().getEntityModels()
                            .bakeLayer(FortressArmorModel.LAYER));
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
                default -> { }
            }
            model.prepare(entity, stack, slot);
            return model;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyPose(HumanoidModel<?> source,
            FortressArmorModel target) {
        ((HumanoidModel) source).copyPropertiesTo(target);
    }
}
