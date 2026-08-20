package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.compat.OptiFinePresence;
import com.thaumcraftmodern.item.CultistArmorItem;
import com.thaumcraftmodern.item.FortressArmorItem;
import com.thaumcraftmodern.item.ThaumaturgeRobeItem;
import com.thaumcraftmodern.item.VoidArmorItem;
import com.thaumcraftmodern.item.VoidRobeArmorItem;
import com.thaumcraftmodern.item.WingedMantleArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Bypasses OptiFine 1.20.1's replacement of {@code HumanoidArmorLayer}.
 *
 * <p>The I6 builds patch that vanilla/Forge layer and corrupt the child-part
 * transforms of Forge custom armor models. The items give that patched pass a
 * transparent texture and a no-op model, while a normal Forge render layer
 * draws the real models without passing them back through OptiFine's armor
 * hook.</p>
 */
public final class OptiFineArmorCompatibility {
    private static final boolean ACTIVE = OptiFinePresence.loaded();
    private static final ResourceLocation WINGED_MANTLE_TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/entity/models/winged_mantle_armor_optifine.png"
            );
    private static HumanoidModel<LivingEntity> invisibleModel;

    private OptiFineArmorCompatibility() {
    }

    public static boolean active() {
        return ACTIVE;
    }

    public static HumanoidModel<?> invisibleModel() {
        if (invisibleModel == null) {
            invisibleModel = new InvisibleArmorModel();
        }
        return invisibleModel;
    }

    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        if (!ACTIVE) {
            return;
        }
        Models models = new Models();
        for (String skin : event.getSkins()) {
            Object renderer = event.getPlayerSkin(skin);
            addLayer(renderer, models);
        }
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            addLayer(event.getEntityRenderer(type), models);
        }
        ThaumcraftModern.LOGGER.info(
                "Enabled OptiFine-safe custom armor render layer");
    }

    private static void addLayer(Object candidate, Models models) {
        // LegacyMobRenderer already owns CrimsonCultArmorLayer and does not
        // use HumanoidArmorLayer for its rank armor. Adding this equipment
        // fallback there would draw the cult set twice.
        if (candidate instanceof LegacyMobRenderer
                || !(candidate instanceof LivingEntityRenderer<?, ?> renderer)
                || !(renderer.getModel() instanceof HumanoidModel<?>)) {
            return;
        }
        addLayerUnchecked(renderer, models);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerUnchecked(
            LivingEntityRenderer<?, ?> renderer,
            Models models
    ) {
        renderer.addLayer(new ArmorLayer(renderer, models));
    }

    private static final class InvisibleArmorModel
            extends HumanoidModel<LivingEntity> {
        private InvisibleArmorModel() {
            super(Minecraft.getInstance().getEntityModels()
                    .bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        }

        @Override
        public void renderToBuffer(
                PoseStack pose,
                VertexConsumer vertices,
                int light,
                int overlay,
                float red,
                float green,
                float blue,
                float alpha
        ) {
            // OptiFine may freely change this model's pose and visibility;
            // its render is deliberately empty.
        }
    }

    private static final class Models {
        private final CrimsonCultArmorModel knight = cult(
                CrimsonCultArmorModel.KNIGHT_LAYER);
        private final CrimsonCultArmorModel cleric = cult(
                CrimsonCultArmorModel.CLERIC_LAYER);
        private final CrimsonCultArmorModel praetor = cult(
                CrimsonCultArmorModel.PRAETOR_LAYER);
        private final CrimsonCultArmorModel knightLeggings = cult(
                CrimsonCultArmorModel.KNIGHT_LEGGINGS_LAYER);
        private final CrimsonCultArmorModel clericLeggings = cult(
                CrimsonCultArmorModel.CLERIC_LEGGINGS_LAYER);
        private final CrimsonCultArmorModel praetorLeggings = cult(
                CrimsonCultArmorModel.PRAETOR_LEGGINGS_LAYER);
        private final CrimsonCultArmorModel cultBoots = cult(
                CrimsonCultArmorModel.BOOTS_LAYER);
        private final FortressArmorModel fortress = new FortressArmorModel(
                bake(FortressArmorModel.LAYER));
        private final FortressArmorModel fortressLeggings =
                new FortressArmorModel(
                        bake(FortressArmorModel.LEGGINGS_LAYER));
        private final ThaumaturgeRobeArmorModel robe =
                new ThaumaturgeRobeArmorModel(
                        bake(ThaumaturgeRobeArmorModel.OUTER_LAYER));
        private final HumanoidModel<LivingEntity> robeBoots =
                new HumanoidModel<>(bake(ModelLayers.PLAYER_OUTER_ARMOR));
        private final VoidRobeArmorModel voidRobeOuter =
                new VoidRobeArmorModel(
                        bake(VoidRobeArmorModel.OUTER_LAYER), false);
        private final VoidRobeArmorModel voidRobeInner =
                new VoidRobeArmorModel(
                        bake(VoidRobeArmorModel.INNER_LAYER), true);
        private final VoidArmorChestModel voidChest = new VoidArmorChestModel(
                bake(VoidArmorChestModel.LAYER));
        private final WingedMantleArmorModel mantle =
                new WingedMantleArmorModel(
                        bake(WingedMantleArmorModel.OPTIFINE_LAYER));

        private static CrimsonCultArmorModel cult(
                net.minecraft.client.model.geom.ModelLayerLocation layer
        ) {
            return new CrimsonCultArmorModel(bake(layer));
        }

        private static net.minecraft.client.model.geom.ModelPart bake(
                net.minecraft.client.model.geom.ModelLayerLocation layer
        ) {
            return Minecraft.getInstance().getEntityModels().bakeLayer(layer);
        }
    }

    private static final class ArmorLayer<
            T extends LivingEntity,
            M extends HumanoidModel<T>> extends RenderLayer<T, M> {
        private final Models models;

        private ArmorLayer(RenderLayerParent<T, M> parent, Models models) {
            super(parent);
            this.models = models;
        }

        @Override
        public void render(
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                T entity,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch
        ) {
            renderSlot(pose, buffers, light, entity, EquipmentSlot.CHEST,
                    limbSwing, limbSwingAmount, ageInTicks,
                    netHeadYaw, headPitch);
            renderSlot(pose, buffers, light, entity, EquipmentSlot.LEGS,
                    limbSwing, limbSwingAmount, ageInTicks,
                    netHeadYaw, headPitch);
            renderSlot(pose, buffers, light, entity, EquipmentSlot.FEET,
                    limbSwing, limbSwingAmount, ageInTicks,
                    netHeadYaw, headPitch);
            renderSlot(pose, buffers, light, entity, EquipmentSlot.HEAD,
                    limbSwing, limbSwingAmount, ageInTicks,
                    netHeadYaw, headPitch);
        }

        private void renderSlot(
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                T entity,
                EquipmentSlot slot,
                float limbSwing,
                float limbSwingAmount,
                float ageInTicks,
                float netHeadYaw,
                float headPitch
        ) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!(stack.getItem() instanceof ArmorItem armor)
                    || armor.getEquipmentSlot() != slot) {
                return;
            }
            HumanoidModel<?> model = model(entity, stack, slot);
            if (model == null) {
                return;
            }
            prepareModel(model, entity, slot, limbSwing, limbSwingAmount,
                    ageInTicks, netHeadYaw, headPitch);
            if (armor instanceof DyeableLeatherItem dyeable) {
                int color = dyeable.getColor(stack);
                renderModel(model, armorTexture(armor, stack, entity, slot, null),
                        pose, buffers, light, stack,
                        (float) (color >> 16 & 255) / 255.0F,
                        (float) (color >> 8 & 255) / 255.0F,
                        (float) (color & 255) / 255.0F);
                renderModel(model, armorTexture(
                                armor, stack, entity, slot, "overlay"),
                        pose, buffers, light, stack, 1.0F, 1.0F, 1.0F);
            } else {
                renderModel(model, armorTexture(armor, stack, entity, slot, null),
                        pose, buffers, light, stack, 1.0F, 1.0F, 1.0F);
            }
        }

        private HumanoidModel<?> model(
                T entity,
                ItemStack stack,
                EquipmentSlot slot
        ) {
            if (stack.getItem() instanceof CultistArmorItem cultist) {
                if (slot == EquipmentSlot.LEGS) {
                    return switch (cultist.set()) {
                        case KNIGHT -> models.knightLeggings;
                        case CLERIC -> models.clericLeggings;
                        case PRAETOR -> models.praetorLeggings;
                        case BOOTS -> models.cultBoots;
                    };
                }
                return switch (cultist.set()) {
                    case KNIGHT -> models.knight;
                    case CLERIC -> models.cleric;
                    case PRAETOR -> models.praetor;
                    case BOOTS -> models.cultBoots;
                };
            }
            if (stack.getItem() instanceof FortressArmorItem) {
                // Fortress boots stay on the original humanoid armor pass;
                // there are no boot-only parts in FortressArmorModel.
                if (slot == EquipmentSlot.FEET) {
                    return null;
                }
                FortressArmorModel fortress = slot == EquipmentSlot.LEGS
                        ? models.fortressLeggings : models.fortress;
                fortress.prepare(entity, stack, slot);
                return fortress;
            }
            if (stack.getItem() instanceof ThaumaturgeRobeItem) {
                return slot == EquipmentSlot.FEET
                        ? models.robeBoots : models.robe;
            }
            if (stack.getItem() instanceof VoidRobeArmorItem) {
                return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.LEGS
                        ? models.voidRobeInner : models.voidRobeOuter;
            }
            if (stack.getItem() instanceof VoidArmorItem
                    && slot == EquipmentSlot.CHEST) {
                models.voidChest.narrowSleevesHorizontally();
                return models.voidChest;
            }
            if (stack.getItem() instanceof WingedMantleArmorItem) {
                if (slot == EquipmentSlot.LEGS) {
                    return models.voidRobeInner;
                }
                models.mantle.configureForSlot(slot);
                return models.mantle;
            }
            return null;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private void prepareModel(
                HumanoidModel model,
                T entity,
                EquipmentSlot slot,
                float limbSwing,
                float limbSwingAmount,
                float ageInTicks,
                float netHeadYaw,
                float headPitch
        ) {
            setTreeVisible(model.head, true);
            setTreeVisible(model.hat, true);
            setTreeVisible(model.body, true);
            setTreeVisible(model.rightArm, true);
            setTreeVisible(model.leftArm, true);
            setTreeVisible(model.rightLeg, true);
            setTreeVisible(model.leftLeg, true);
            getParentModel().copyPropertiesTo(model);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks,
                    netHeadYaw, headPitch);
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
            if (model instanceof CrimsonCultArmorModel cultist) {
                cultist.configureForSlot(slot);
            }
            if (model instanceof FortressArmorModel fortress) {
                fortress.prepare(entity, entity.getItemBySlot(slot), slot);
            }
            if (model instanceof WingedMantleArmorModel mantle) {
                mantle.configureForSlot(slot);
            }
        }

        private static void setTreeVisible(
                net.minecraft.client.model.geom.ModelPart root,
                boolean visible
        ) {
            root.getAllParts().forEach(part -> part.visible = visible);
        }

        private static ResourceLocation armorTexture(
                ArmorItem armor,
                ItemStack stack,
                Entity entity,
                EquipmentSlot slot,
                String type
        ) {
            if (armor instanceof WingedMantleArmorItem
                    && slot != EquipmentSlot.LEGS) {
                return WINGED_MANTLE_TEXTURE;
            }
            String path;
            if (armor instanceof CultistArmorItem cultist) {
                path = "textures/entity/models/" + switch (cultist.set()) {
                    case KNIGHT -> "cultist_plate_armor.png";
                    case CLERIC -> "cultist_robe_armor.png";
                    case PRAETOR -> "cultist_leader_armor.png";
                    case BOOTS -> "cultistboots.png";
                };
            } else if (armor instanceof FortressArmorItem) {
                path = "textures/entity/models/fortress_armor.png";
            } else if (armor instanceof ThaumaturgeRobeItem) {
                String layer = slot == EquipmentSlot.LEGS
                        ? "robes_2" : "robes_1";
                path = "textures/models/" + layer
                        + (type == null ? "" : "_overlay") + ".png";
            } else if (armor instanceof VoidRobeArmorItem) {
                path = "textures/models/void_robe_armor"
                        + (type == null ? "_overlay" : "") + ".png";
            } else if (armor instanceof VoidArmorItem) {
                path = "textures/models/void_"
                        + (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png";
            } else if (armor instanceof WingedMantleArmorItem) {
                path = "textures/entity/models/winged_mantle_leggings.png";
            } else {
                throw new IllegalArgumentException(
                        "Unsupported OptiFine armor " + armor);
            }
            return new ResourceLocation(ThaumcraftModern.MOD_ID, path);
        }

        private static void renderModel(
                EntityModel<?> model,
                ResourceLocation texture,
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                ItemStack stack,
                float red,
                float green,
                float blue
        ) {
            VertexConsumer vertices = ItemRenderer.getArmorFoilBuffer(
                    buffers,
                    RenderType.armorCutoutNoCull(texture),
                    false,
                    stack.hasFoil());
            model.renderToBuffer(pose, vertices, light,
                    OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
        }
    }
}
