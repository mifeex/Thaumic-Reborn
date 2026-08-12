package com.thaumcraftmodern.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.EnumMap;

public final class WingedMantleClientExtensions {
    private WingedMantleClientExtensions() { }

    public static IClientItemExtensions create() {
        return new IClientItemExtensions() {
            private final EnumMap<EquipmentSlot, WingedMantleArmorModel> models =
                    new EnumMap<>(EquipmentSlot.class);
            private VoidRobeArmorModel leggings;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                    HumanoidModel<?> defaultModel) {
                if (slot == EquipmentSlot.LEGS) {
                    if (leggings == null) {
                        leggings = new VoidRobeArmorModel(
                                Minecraft.getInstance().getEntityModels()
                                        .bakeLayer(VoidRobeArmorModel.INNER_LAYER),
                                true
                        );
                    }
                    copyPose(defaultModel, leggings);
                    leggings.setAllVisible(false);
                    leggings.body.visible = true;
                    leggings.rightLeg.visible = true;
                    leggings.leftLeg.visible = true;
                    return leggings;
                }
                WingedMantleArmorModel model = models.computeIfAbsent(slot,
                        ignored -> new WingedMantleArmorModel(
                            Minecraft.getInstance().getEntityModels()
                                    .bakeLayer(WingedMantleArmorModel.LAYER)));
                copyPose(defaultModel, model);
                model.configureForSlot(slot);
                return model;
            }
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyPose(HumanoidModel<?> source,
                                 HumanoidModel<?> target) {
        ((HumanoidModel) source).copyPropertiesTo(target);
    }
}
