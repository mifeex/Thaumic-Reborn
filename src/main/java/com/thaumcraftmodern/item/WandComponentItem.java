package com.thaumcraftmodern.item;

import com.thaumcraftmodern.wand.WandComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Inventory representation of one registered rod or cap definition.
 */
public final class WandComponentItem extends Item {
    private final Kind kind;
    private final String componentId;

    public WandComponentItem(
            Kind kind,
            String componentId,
            Properties properties
    ) {
        super(properties);
        this.kind = Objects.requireNonNull(kind, "kind");
        this.componentId = Objects.requireNonNull(componentId, "componentId");
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        switch (kind) {
            case ROD -> WandComponentRegistry.rod(componentId).ifPresent(rod ->
                    tooltip.add(Component.translatable(
                                    "tooltip.thaumic_reborn.wand_rod.capacity",
                                    rod.capacityVis()
                            )
                            .withStyle(ChatFormatting.DARK_PURPLE))
            );
            case CAP -> WandComponentRegistry.cap(componentId).ifPresent(cap ->
                    tooltip.add(Component.translatable(
                                    "tooltip.thaumic_reborn.wand_cap.cost",
                                    Math.round(cap.costModifier() * 100.0F)
                            )
                            .withStyle(ChatFormatting.DARK_PURPLE))
            );
        }
    }

    public enum Kind {
        ROD,
        CAP
    }

    public Kind kind() {
        return kind;
    }

    public String componentId() {
        return componentId;
    }
}
