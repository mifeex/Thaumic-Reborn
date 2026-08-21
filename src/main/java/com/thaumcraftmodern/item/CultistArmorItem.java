package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.api.wand.VisDiscountGear;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.client.render.CultistArmorClientExtensions;
import com.thaumcraftmodern.compat.OptiFinePresence;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
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
        super(materialFor(set), type, properties);
        this.set = set;
    }

    private static ArmorMaterial materialFor(Set set) {
        return set == Set.PRAETOR
                ? PraetorArmorMaterial.INSTANCE
                : CultistArmorMaterial.INSTANCE;
    }

    public Set set() {
        return set;
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
        if (OptiFinePresence.loaded()) {
            return ThaumcraftModern.MOD_ID
                    + ":textures/entity/models/transparent_armor.png";
        }
        return ThaumcraftModern.MOD_ID + ":textures/entity/models/"
                + set.texture;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CultistArmorClientExtensions.create(() -> set));
    }

}
