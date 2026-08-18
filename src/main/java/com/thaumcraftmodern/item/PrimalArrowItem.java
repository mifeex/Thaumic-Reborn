package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.PrimalArrowEntity;
import com.thaumcraftmodern.entity.PrimalArrowType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A real bow projectile, replacing the recipe-placeholder primal arrow item. */
public final class PrimalArrowItem extends ArrowItem {
    private final PrimalArrowType type;

    public PrimalArrowItem(PrimalArrowType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public PrimalArrowType type() {
        return type;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new PrimalArrowEntity(level, shooter, type);
    }
}
