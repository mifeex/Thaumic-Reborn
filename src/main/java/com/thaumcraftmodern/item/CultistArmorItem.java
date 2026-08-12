package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.api.wand.VisDiscountGear;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.client.render.CrimsonCultArmorModel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/** Wearable TC4 Crimson Cult armor using the restored original models. */
public final class CultistArmorItem extends ArmorItem
        implements VisDiscountGear {
    public static final int CULTIST_ROBE_VIS_DISCOUNT_PERCENT = 1;

    public enum Set {
        KNIGHT("cultist_plate_armor.png"),
        CLERIC("cultist_robe_armor.png"),
        PRAETOR("cultist_leader_armor.png"),
        BOOTS("cultistboots.png");

        private final String texture;

        Set(String texture) {
            this.texture = texture;
        }
    }

    private final Set set;

    public CultistArmorItem(Set set, Type type, Properties properties) {
        super(ThaumiumArmorMaterial.INSTANCE, type, properties);
        this.set = set;
    }

    @Override
    public int visDiscountPercent(
            ItemStack stack,
            Player player,
            PrimalAspect aspect
    ) {
        return hasClassicVisDiscount()
                ? CULTIST_ROBE_VIS_DISCOUNT_PERCENT : 0;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (hasClassicVisDiscount()) {
            tooltip.add(Component.translatable("tc.visdiscount")
                    .append(": " + CULTIST_ROBE_VIS_DISCOUNT_PERCENT + "%")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private boolean hasClassicVisDiscount() {
        // TC4's ItemCultistRobeArmor and ItemCultistBoots return 1%; the
        // knight plate and Praetor armor do not implement IVisDiscountGear.
        return set == Set.CLERIC || set == Set.BOOTS;
    }

    /**
     * TC4 ItemCultistRobeArmor and ItemCultistBoots both contribute one point
     * of worn warp. Knight plate and Praetor armor contribute none.
     */
    public int classicWarp() {
        return hasClassicVisDiscount() ? 1 : 0;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity,
                                  EquipmentSlot slot, String type) {
        return ThaumcraftModern.MOD_ID + ":textures/entity/models/"
                + set.texture;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private CrimsonCultArmorModel model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                    HumanoidModel<?> defaultModel) {
                if (model == null) {
                    // Forge invokes initializeClient from Item's base
                    // constructor, before CultistArmorItem's `set` field is
                    // assigned. Resolve it only when the completed item is
                    // actually rendered; capturing it above permanently
                    // stores null and crashes on the first equipped piece.
                    Set armorSet = CultistArmorItem.this.set;
                    if (armorSet == null) {
                        return defaultModel;
                    }
                    model = new CrimsonCultArmorModel(
                            Minecraft.getInstance().getEntityModels()
                                    .bakeLayer(switch (armorSet) {
                                        case KNIGHT -> CrimsonCultArmorModel.KNIGHT_LAYER;
                                        case CLERIC -> CrimsonCultArmorModel.CLERIC_LAYER;
                                        case PRAETOR -> CrimsonCultArmorModel.PRAETOR_LAYER;
                                        case BOOTS -> CrimsonCultArmorModel.BOOTS_LAYER;
                                    }));
                }
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
                        model.rightLeg.visible = true;
                        model.leftLeg.visible = true;
                    }
                    case FEET -> {
                        model.rightLeg.visible = true;
                        model.leftLeg.visible = true;
                    }
                    default -> { }
                }
                model.suppressChestGeometryForLeggings(slot);
                return model;
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyPose(HumanoidModel<?> source,
                                 CrimsonCultArmorModel target) {
        ((HumanoidModel) source).copyPropertiesTo(target);
    }
}
