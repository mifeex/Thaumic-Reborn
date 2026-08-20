package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.item.CultistArmorItem;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Client-only Cultist armor model factory, kept out of common item class verification. */
public final class CultistArmorClientExtensions {
    private CultistArmorClientExtensions() { }

    public static IClientItemExtensions create(Supplier<CultistArmorItem.Set> set) {
        return new Extensions(set);
    }

    private static final class Extensions implements IClientItemExtensions {
        private final Supplier<CultistArmorItem.Set> set;
        private CrimsonCultArmorModel outerModel;
        private CrimsonCultArmorModel leggingsModel;

        private Extensions(Supplier<CultistArmorItem.Set> set) {
            this.set = set;
        }

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                HumanoidModel<?> defaultModel) {
            if (OptiFineArmorCompatibility.active()) {
                return OptiFineArmorCompatibility.invisibleModel();
            }
            if (outerModel == null || leggingsModel == null) {
                // Item's base constructor invokes initializeClient before the
                // completed CultistArmorItem has assigned its set field.
                CultistArmorItem.Set armorSet = set.get();
                if (armorSet == null) return defaultModel;
                outerModel = new CrimsonCultArmorModel(
                        Minecraft.getInstance().getEntityModels()
                                .bakeLayer(switch (armorSet) {
                                    case KNIGHT -> CrimsonCultArmorModel.KNIGHT_LAYER;
                                    case CLERIC -> CrimsonCultArmorModel.CLERIC_LAYER;
                                    case PRAETOR -> CrimsonCultArmorModel.PRAETOR_LAYER;
                                    case BOOTS -> CrimsonCultArmorModel.BOOTS_LAYER;
                                }));
                leggingsModel = new CrimsonCultArmorModel(
                        Minecraft.getInstance().getEntityModels()
                                .bakeLayer(switch (armorSet) {
                                    case KNIGHT -> CrimsonCultArmorModel.KNIGHT_LEGGINGS_LAYER;
                                    case CLERIC -> CrimsonCultArmorModel.CLERIC_LEGGINGS_LAYER;
                                    case PRAETOR -> CrimsonCultArmorModel.PRAETOR_LEGGINGS_LAYER;
                                    case BOOTS -> CrimsonCultArmorModel.BOOTS_LAYER;
                                }));
            }
            CrimsonCultArmorModel model = slot == EquipmentSlot.LEGS
                    ? leggingsModel : outerModel;
            copyPose(defaultModel, model);
            model.configureForSlot(slot);
            return model;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyPose(HumanoidModel<?> source,
            CrimsonCultArmorModel target) {
        ((HumanoidModel) source).copyPropertiesTo(target);
    }
}
