package com.thaumcraftmodern.scan;

import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.entity.PechBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Set;

public final class EntityScanIdentity {
    private static final String PECH_PREFIX = "thaumic_reborn:pech/";
    private static final Set<String> PECH_VARIANTS = Set.of(
            PECH_PREFIX + "forager",
            PECH_PREFIX + "mage",
            PECH_PREFIX + "stalker"
    );

    private EntityScanIdentity() {
    }

    public static String targetId(Entity entity) {
        String base = BuiltInRegistries.ENTITY_TYPE
                .getKey(entity.getType())
                .toString();
        if (entity instanceof LegacyThaumcraftMob mob
                && mob.kind() == LegacyMobKind.PECH) {
            return base + "/" + switch (mob.pechType()) {
                case PechBehavior.MAGE -> "mage";
                case PechBehavior.STALKER -> "stalker";
                default -> "forager";
            };
        }
        return base;
    }

    public static boolean isRegisteredTarget(String targetId) {
        if (PECH_VARIANTS.contains(targetId)) {
            return BuiltInRegistries.ENTITY_TYPE.containsKey(
                    new ResourceLocation("thaumic_reborn", "pech")
            );
        }
        ResourceLocation id = ResourceLocation.tryParse(targetId);
        return id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id);
    }
}
