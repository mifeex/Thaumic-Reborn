package com.thaumcraftmodern.worldgen;

import com.mojang.datafixers.util.Pair;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.config.ThaumicOverworldConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Splits vanilla Overworld climate mappings to insert the two naturally
 * generated TC4 biomes without replacing the normal world preset or owning a
 * biome source. Mod-owned mappings are forwarded untouched.
 *
 * <p>Magical Forest and Tainted Lands use opposite tails of the same
 * low-frequency erosion climate axis. This produces coherent regions without
 * changing temperature or humidity and keeps Magical Forest away from the
 * low-erosion mountain terrain. Every remainder of the original mapping is
 * emitted, while oceans, coasts, rivers, underground and mod-owned mappings
 * remain unchanged.</p>
 */
public final class ThaumicOverworldBiomeParameters {
    static final float INLAND_CONTINENTALNESS = -0.11F;
    static final double MAGICAL_WEIGHT_THRESHOLD_STEP = 0.06D;
    static final double MAXIMUM_THRESHOLD_OFFSET = 0.45D;
    static final ResourceLocation MAGICAL_FOREST = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "magical_forest"
    );
    static final ResourceLocation TAINTED_LANDS = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "tainted_lands"
    );
    private static final AtomicBoolean INSTALL_LOGGED = new AtomicBoolean();

    private ThaumicOverworldBiomeParameters() {
    }

    /**
     * Wraps the vanilla mapper instead of replacing its source. TerraBlender
     * 3.0.x also calls {@code OverworldBiomeBuilder.addBiomes} for its default
     * Overworld region, so the same additive split is used on that route.
     */
    public static Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>>
    wrapVanillaMapper(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> target
    ) {
        if (!ThaumicOverworldConfig.generateTc4Biomes()) {
            return target;
        }
        int magicalWeight = ThaumicOverworldConfig.magicalForestWeight();
        int taintWeight = ThaumicOverworldConfig.taintedLandsWeight();
        if (INSTALL_LOGGED.compareAndSet(false, true)) {
            ThaumcraftModern.LOGGER.info(
                    "Installing TC4 Overworld regional markers "
                            + "(Magical Forest weight {}, Tainted Lands "
                            + "weight {}, region sizes {}/{} quarts); existing "
                            + "mod mappings are forwarded unchanged",
                    magicalWeight,
                    taintWeight,
                    ThaumicBiomeRegionMask.MAGICAL_REGION_SIZE_QUARTS,
                    ThaumicBiomeRegionMask.TAINTED_REGION_SIZE_QUARTS
            );
        }
        AtomicBoolean markersAdded = new AtomicBoolean();
        return pair -> {
            if (markersAdded.compareAndSet(false, true)) {
                target.accept(Pair.of(
                        regionalMarker(),
                        ResourceKey.create(Registries.BIOME, MAGICAL_FOREST)
                ));
                target.accept(Pair.of(
                        regionalMarker(),
                        ResourceKey.create(Registries.BIOME, TAINTED_LANDS)
                ));
            }
            target.accept(pair);
        };
    }

    /**
     * Makes the dynamic biome holders available to MultiNoiseBiomeSource.
     * The large offset keeps these markers from winning normal climate
     * lookup; the regional mixin selects them explicitly by X/Z region.
     */
    static Climate.ParameterPoint regionalMarker() {
        Climate.Parameter full = Climate.Parameter.span(-1.0F, 1.0F);
        return new Climate.ParameterPoint(
                full,
                full,
                full,
                full,
                full,
                full,
                Climate.quantizeCoord(100.0F)
        );
    }

    static void splitMapping(
            Pair<Climate.ParameterPoint, ResourceKey<Biome>> mapping,
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> target,
            int magicalWeight,
            int taintWeight
    ) {
        ResourceKey<Biome> original = mapping.getSecond();
        splitLocationMapping(
                Pair.of(mapping.getFirst(), original.location()),
                split -> target.accept(Pair.of(
                        split.getFirst(),
                        split.getSecond().equals(original.location())
                                ? original
                                : ResourceKey.create(
                                        Registries.BIOME,
                                        split.getSecond()
                                )
                )),
                magicalWeight,
                taintWeight
        );
    }

    static void splitLocationMapping(
            Pair<Climate.ParameterPoint, ResourceLocation> mapping,
            Consumer<Pair<Climate.ParameterPoint, ResourceLocation>> target,
            int magicalWeight,
            int taintWeight
    ) {
        Climate.ParameterPoint point = mapping.getFirst();
        ResourceLocation biome = mapping.getSecond();
        if (!isVanillaSurfaceInland(point, biome)) {
            target.accept(mapping);
            return;
        }

        List<Climate.ParameterPoint> remainders = List.of(point);
        if (magicalWeight > 0) {
            ClimatePartition partition = partitionMagicalForest(
                    point,
                    magicalWeight
            );
            if (partition.magicalForest() != null) {
                target.accept(Pair.of(
                        partition.magicalForest(),
                        MAGICAL_FOREST
                ));
            }
            remainders = partition.remainders();
        }

        for (Climate.ParameterPoint remainder : remainders) {
            emitTaintedRemainder(
                    remainder,
                    biome,
                    target,
                    taintWeight
            );
        }
    }

    private static void emitTaintedRemainder(
            Climate.ParameterPoint remainder,
            ResourceLocation biome,
            Consumer<Pair<Climate.ParameterPoint, ResourceLocation>> target,
            int taintWeight
    ) {
        if (taintWeight > 0) {
            long threshold = Climate.quantizeCoord((float)
                    TaintedLandsGenerationPolicy.patchThreshold(taintWeight));
            Climate.Parameter low = intersectLow(
                    remainder.erosion(),
                    threshold
            );
            if (low != null) {
                target.accept(Pair.of(
                        withErosion(remainder, low),
                        TAINTED_LANDS
                ));
            }
            Climate.Parameter high = above(
                    remainder.erosion(),
                    threshold
            );
            if (high == null) {
                return;
            }
            remainder = withErosion(remainder, high);
        }

        target.accept(Pair.of(remainder, biome));
    }

    private static ClimatePartition partitionMagicalForest(
            Climate.ParameterPoint source,
            int magicalWeight
    ) {
        Climate.Parameter[] mask = {
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(INLAND_CONTINENTALNESS, 1.0F),
                Climate.Parameter.span(
                        (float) magicalThreshold(magicalWeight),
                        1.0F
                ),
                Climate.Parameter.span(-1.0F, 1.0F)
        };
        Climate.ParameterPoint intersection = source;
        List<Climate.ParameterPoint> remainders = new ArrayList<>();
        for (int axis = 0; axis < mask.length; axis++) {
            Climate.Parameter current = parameter(intersection, axis);
            Climate.Parameter clipped = intersect(current, mask[axis]);
            if (clipped == null) {
                return new ClimatePartition(null, List.of(source));
            }
            if (current.min() < clipped.min()) {
                remainders.add(withParameter(
                        intersection,
                        axis,
                        new Climate.Parameter(
                                current.min(),
                                clipped.min() - 1L
                        )
                ));
                intersection = withParameter(
                        intersection,
                        axis,
                        new Climate.Parameter(
                                clipped.min(),
                                current.max()
                        )
                );
            }
            current = parameter(intersection, axis);
            if (clipped.max() < current.max()) {
                remainders.add(withParameter(
                        intersection,
                        axis,
                        new Climate.Parameter(
                                clipped.max() + 1L,
                                current.max()
                        )
                ));
                intersection = withParameter(
                        intersection,
                        axis,
                        new Climate.Parameter(
                                current.min(),
                                clipped.max()
                        )
                );
            }
        }
        return new ClimatePartition(intersection, List.copyOf(remainders));
    }

    static double magicalThreshold(int configuredWeight) {
        int weight = Math.max(0, configuredWeight);
        return 1.0D - Math.min(
                MAXIMUM_THRESHOLD_OFFSET,
                weight * MAGICAL_WEIGHT_THRESHOLD_STEP
        );
    }

    private static boolean isVanillaSurfaceInland(
            Climate.ParameterPoint point,
            ResourceLocation biome
    ) {
        if (!biome.getNamespace().equals("minecraft")
                || biome.getPath().equals("river")
                || biome.getPath().equals("frozen_river")) {
            return false;
        }
        long surface = Climate.quantizeCoord(0.0F);
        long inland = Climate.quantizeCoord(INLAND_CONTINENTALNESS);
        return point.depth().min() == surface
                && point.depth().max() == surface
                && point.continentalness().min() >= inland;
    }

    private static Climate.Parameter intersect(
            Climate.Parameter first,
            Climate.Parameter second
    ) {
        long minimum = Math.max(first.min(), second.min());
        long maximum = Math.min(first.max(), second.max());
        return minimum <= maximum
                ? new Climate.Parameter(minimum, maximum)
                : null;
    }

    private static Climate.Parameter parameter(
            Climate.ParameterPoint point,
            int axis
    ) {
        return switch (axis) {
            case 0 -> point.temperature();
            case 1 -> point.humidity();
            case 2 -> point.continentalness();
            case 3 -> point.erosion();
            case 4 -> point.weirdness();
            default -> throw new IllegalArgumentException(
                    "Unknown climate axis " + axis
            );
        };
    }

    private static Climate.ParameterPoint withParameter(
            Climate.ParameterPoint point,
            int axis,
            Climate.Parameter parameter
    ) {
        return new Climate.ParameterPoint(
                axis == 0 ? parameter : point.temperature(),
                axis == 1 ? parameter : point.humidity(),
                axis == 2 ? parameter : point.continentalness(),
                axis == 3 ? parameter : point.erosion(),
                point.depth(),
                axis == 4 ? parameter : point.weirdness(),
                point.offset()
        );
    }

    private static Climate.Parameter below(
            Climate.Parameter parameter,
            long threshold
    ) {
        if (threshold == Long.MIN_VALUE) {
            return null;
        }
        long max = Math.min(parameter.max(), threshold - 1L);
        return parameter.min() <= max
                ? new Climate.Parameter(parameter.min(), max)
                : null;
    }

    private static Climate.Parameter intersectLow(
            Climate.Parameter parameter,
            long threshold
    ) {
        long max = Math.min(parameter.max(), threshold);
        return parameter.min() <= max
                ? new Climate.Parameter(parameter.min(), max)
                : null;
    }

    private static Climate.Parameter above(
            Climate.Parameter parameter,
            long threshold
    ) {
        if (threshold == Long.MAX_VALUE) {
            return null;
        }
        long min = Math.max(parameter.min(), threshold + 1L);
        return min <= parameter.max()
                ? new Climate.Parameter(min, parameter.max())
                : null;
    }

    private static Climate.ParameterPoint withErosion(
            Climate.ParameterPoint point,
            Climate.Parameter erosion
    ) {
        return new Climate.ParameterPoint(
                point.temperature(),
                point.humidity(),
                point.continentalness(),
                erosion,
                point.depth(),
                point.weirdness(),
                point.offset()
        );
    }

    private record ClimatePartition(
            Climate.ParameterPoint magicalForest,
            List<Climate.ParameterPoint> remainders
    ) {
    }

}
