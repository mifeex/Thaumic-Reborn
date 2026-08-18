package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;

/**
 * Living TC4 entities that can occur through biome spawning or restored
 * overworld structures. Crafted golems and projectile-only entities are not
 * world-generated and therefore are outside this registry.
 */
public enum LegacyMobKind {
    ANGRY_ZOMBIE("angry_zombie", 25.0D, 5.0D, 0.23D, 0.6F, 1.95F, "models/bzombie.png"),
    FURIOUS_ZOMBIE("furious_zombie", 60.0D, 7.0D, 0.25D, 0.6F, 1.95F, "models/bzombie.png"),
    WISP("wisp", 22.0D, 3.0D, 0.20D, 0.9F, 0.9F, "misc/wisp.png", true),
    FIREBAT("firebat", 5.0D, 1.0D, 0.35D, 0.5F, 0.9F, "models/firebat.png", true),
    PECH("pech", 30.0D, 3.0D, 0.25D, 0.7F, 1.5F, "models/pech_forage.png"),
    MIND_SPIDER("mind_spider", 1.0D, 1.0D, 0.30D, 0.3F, 0.3F, "models/taint_spider.png"),
    ELDRITCH_GUARDIAN("eldritch_guardian", 50.0D, 7.0D, 0.28D, 0.8F, 2.25F, "models/eldritch_guardian.png"),
    ELDRITCH_WARDEN("eldritch_warden", 180.0D, 10.0D, 0.25D, 1.5F, 3.5F, "models/eldritch_warden.png"),
    CRIMSON_KNIGHT("crimson_knight", 40.0D, 7.0D, 0.30D, 0.7F, 1.95F, "models/cultist.png"),
    CRIMSON_INQUISITOR("crimson_inquisitor", 55.0D, 8.0D, 0.31D, 0.75F, 2.05F, "models/cultist.png"),
    CONVERTED_VILLAGER("converted_villager", 20.0D, 0.0D, 0.30D, 0.6F, 1.95F, "models/villager.png"),
    CRIMSON_CLERIC("crimson_cleric", 32.0D, 5.0D, 0.30D, 0.7F, 1.95F, "models/cultist.png"),
    CRIMSON_PRAETOR("crimson_praetor", 120.0D, 9.0D, 0.30D, 0.8F, 2.1F, "models/cultist.png"),
    ELDRITCH_CONSTRUCT("eldritch_construct", 250.0D, 10.0D, 0.30D, 1.75F, 3.5F, "models/eldritch_golem.png"),
    ELDRITCH_CRAB("eldritch_crab", 20.0D, 4.0D, 0.30D, 0.8F, 0.6F, "models/crab.png"),
    INHABITED_ZOMBIE("inhabited_zombie", 35.0D, 6.0D, 0.23D, 0.6F, 1.95F, "models/czombie.png"),
    THAUMIC_SLIME("thaumic_slime", 1.0D, 1.0D, 0.24D, 1.0F, 1.0F, "models/tslime.png"),
    TAINTED_CRAWLER("tainted_crawler", 5.0D, 2.0D, 0.31D, 0.4F, 0.3F, "models/taint_spider.png"),
    TAINTACLE("taintacle", 50.0D, 7.0D, 0.0D, 0.66F, 3.0F, "models/taintacle.png"),
    TAINT_TENDRIL("taint_tendril", 12.0D, 3.0D, 0.0D, 0.22F, 1.0F, "models/taintacle.png"),
    TAINT_SPORE("taint_spore", 1.0D, 1.0D, 0.12D, 0.5F, 0.5F, "models/taint_spore.png"),
    TAINT_SPORE_SWARMER("taint_spore_swarmer", 75.0D, 1.0D, 0.33D, 1.0F, 1.0F, "models/taint_spore.png"),
    TAINT_SWARM("taint_swarm", 30.0D, 2.0D, 0.35D, 2.0F, 2.0F, "misc/particles.png", true),
    TAINTED_CHICKEN("tainted_chicken", 8.0D, 2.0D, 0.26D, 0.4F, 0.7F, "models/chicken.png"),
    TAINTED_COW("tainted_cow", 22.0D, 4.0D, 0.24D, 0.9F, 1.4F, "models/cow.png"),
    TAINTED_CREEPER("tainted_creeper", 24.0D, 5.0D, 0.25D, 0.6F, 1.7F, "models/creeper.png"),
    TAINTED_PIG("tainted_pig", 18.0D, 3.0D, 0.25D, 0.9F, 0.9F, "models/pig.png"),
    TAINTED_SHEEP("tainted_sheep", 18.0D, 3.0D, 0.25D, 0.9F, 1.3F, "models/sheep.png"),
    TAINTED_VILLAGER("tainted_villager", 30.0D, 5.0D, 0.24D, 0.6F, 1.95F, "models/villager.png"),
    GIANT_TAINTACLE("giant_taintacle", 250.0D, 12.0D, 0.0D, 1.1F, 6.0F, "models/taintacle.png");

    private final String id;
    private final double health;
    private final double damage;
    private final double speed;
    private final float width;
    private final float height;
    private final String texture;
    private final boolean flying;

    LegacyMobKind(
            String id,
            double health,
            double damage,
            double speed,
            float width,
            float height,
            String texture
    ) {
        this(id, health, damage, speed, width, height, texture, false);
    }

    LegacyMobKind(
            String id,
            double health,
            double damage,
            double speed,
            float width,
            float height,
            String texture,
            boolean flying
    ) {
        this.id = id;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.width = width;
        this.height = height;
        this.texture = texture;
        this.flying = flying;
    }

    public String id() {
        return id;
    }

    public double health() {
        return health;
    }

    public double damage() {
        return damage;
    }

    public double speed() {
        return speed;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public boolean flying() {
        return flying;
    }

    public ResourceLocation texture() {
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                "textures/entity/" + texture
        );
    }

    public boolean tainted() {
        return name().startsWith("TAINT")
                || this == THAUMIC_SLIME
                || this == GIANT_TAINTACLE;
    }

    public boolean eldritch() {
        return name().startsWith("ELDRITCH")
                || name().startsWith("CRIMSON")
                || this == MIND_SPIDER
                || this == INHABITED_ZOMBIE;
    }

    public boolean taintacle() {
        return this == TAINTACLE
                || this == TAINT_TENDRIL
                || this == GIANT_TAINTACLE;
    }

    boolean allowsClassicBiomePopulation(int nearbySameKind) {
        return switch (this) {
            case PECH -> nearbySameKind < 4;
            case WISP -> nearbySameKind < 8;
            case TAINTACLE -> nearbySameKind < 1;
            default -> true;
        };
    }
}
