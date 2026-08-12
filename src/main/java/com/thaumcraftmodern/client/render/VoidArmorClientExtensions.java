package com.thaumcraftmodern.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Custom model routing only for the ordinary void chestplate. */
public final class VoidArmorClientExtensions {
    private VoidArmorClientExtensions() {
    }

    public static IClientItemExtensions create() {
        return new Extensions();
    }

    private static final class Extensions implements IClientItemExtensions {
        private VoidArmorChestModel chest;

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity,
                ItemStack stack,
                EquipmentSlot slot,
                HumanoidModel<?> defaultModel
        ) {
            if (slot != EquipmentSlot.CHEST) {
                return defaultModel;
            }
            if (chest == null) {
                chest = new VoidArmorChestModel(
                        Minecraft.getInstance().getEntityModels()
                                .bakeLayer(VoidArmorChestModel.LAYER)
                );
            }
            copyPose(defaultModel, chest);
            chest.narrowSleevesHorizontally();
            chest.setAllVisible(false);
            chest.body.visible = true;
            chest.rightArm.visible = true;
            chest.leftArm.visible = true;
            return chest;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static void copyPose(
                HumanoidModel<?> source,
                VoidArmorChestModel target
        ) {
            ((HumanoidModel) source).copyPropertiesTo(target);
        }
    }
}
