package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.ClassicGolemEntity;
import com.thaumcraftmodern.entity.GolemCoreType;
import com.thaumcraftmodern.entity.GolemMaterial;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Modern registry counterpart of the eight metadata variants of TC4 ItemGolemPlacer. */
public class ClassicGolemItem extends Item {
    private final GolemMaterial material;
    private final Supplier<? extends EntityType<? extends ClassicGolemEntity>> entityType;

    public ClassicGolemItem(Properties properties, GolemMaterial material,
            Supplier<? extends EntityType<? extends ClassicGolemEntity>> entityType) {
        super(properties);
        this.material = material;
        this.entityType = entityType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level raw = context.getLevel();
        if (raw.isClientSide) return InteractionResult.SUCCESS;
        if (!(raw instanceof ServerLevel level) || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        var target = context.getClickedPos().relative(context.getClickedFace());
        ClassicGolemEntity golem = entityType.get().create(level);
        if (golem == null) return InteractionResult.FAIL;
        golem.moveTo(target.getX() + .5D, target.getY(), target.getZ() + .5D,
                level.random.nextFloat() * 360F, 0F);
        golem.restrictTo(target, 32);
        golem.setHomeFacing(context.getClickedFace());
        golem.setOwner(context.getPlayer().getUUID());
        if (context.getItemInHand().hasTag()
                && context.getItemInHand().getTag().contains("GolemData", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            golem.loadPortableData(context.getItemInHand().getTag().getCompound("GolemData"));
        }
        if (context.getItemInHand().hasTag()
                && context.getItemInHand().getTag().getBoolean("Advanced")) {
            golem.setAdvanced(true);
        }
        if (context.getItemInHand().hasCustomHoverName()) golem.setCustomName(context.getItemInHand().getHoverName());
        if (!level.noCollision(golem) || !level.addFreshEntity(golem)) return InteractionResult.FAIL;
        if (!context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        boolean advanced = stack.hasTag() && (stack.getTag().getBoolean("Advanced")
                || stack.getTag().getCompound("GolemData").getBoolean("advanced"));
        lines.add(Component.translatable("tooltip.thaumic_reborn.golem.stats", material.health(),
                material.carry(), material.strength(), material.armor(),
                material.speed() * (advanced ? 1.1D : 1D), material.upgradeSlots() + (advanced ? 1 : 0))
                .withStyle(ChatFormatting.DARK_PURPLE));
        GolemCoreType core = stack.hasTag()
                ? PortableGolemCore.read(stack.getTag())
                : null;
        if (core != null) {
            lines.add(Component.translatable(
                    "tooltip.thaumic_reborn.golem.core",
                    Component.translatable(
                            "tooltip.thaumic_reborn.golem.core_type." + core.id())
                            .withStyle(ChatFormatting.GOLD)));
        }
    }

    public GolemMaterial material() { return material; }
}
