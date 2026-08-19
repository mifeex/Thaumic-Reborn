package com.thaumcraftmodern.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumicOverworldBiomeParametersTest {
    private static final ResourceLocation PLAINS = vanilla("plains");
    private static final ResourceLocation OCEAN = vanilla("ocean");
    private static final ResourceLocation BEACH = vanilla("beach");
    private static final ResourceLocation LUSH_CAVES = vanilla("lush_caves");
    private static final ResourceLocation DESERT = vanilla("desert");
    private static final ResourceLocation STONY_SHORE =
            vanilla("stony_shore");

    @Test
    void partitionsTheFormerOverlayThresholdsWithoutOverlap() {
        Climate.ParameterPoint source = point(
                Climate.Parameter.span(-0.11F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );
        List<Pair<Climate.ParameterPoint, ResourceLocation>> output =
                split(source, PLAINS, 5, 2);

        double magicalThreshold = ThaumicOverworldBiomeParameters
                .magicalThreshold(5);
        double taintThreshold = TaintedLandsGenerationPolicy
                .patchThreshold(2);
        int magicalSamples = 0;
        int taintSamples = 0;
        int vanillaSamples = 0;

        for (int weirdnessIndex = -100; weirdnessIndex <= 100;
             weirdnessIndex++) {
            float weirdness = weirdnessIndex / 100.0F;
            for (int erosionIndex = -100; erosionIndex <= 100;
                 erosionIndex++) {
                float erosion = erosionIndex / 100.0F;
                List<ResourceLocation> matches = output.stream()
                        .filter(pair -> contains(
                                pair.getFirst().weirdness(), weirdness
                        ))
                        .filter(pair -> contains(
                                pair.getFirst().erosion(), erosion
                        ))
                        .map(Pair::getSecond)
                        .toList();
                assertEquals(1, matches.size());

                ResourceLocation expected;
                if (Climate.quantizeCoord(erosion)
                        >= Climate.quantizeCoord((float) magicalThreshold)) {
                    expected = ThaumicOverworldBiomeParameters.MAGICAL_FOREST;
                    magicalSamples++;
                } else if (Climate.quantizeCoord(erosion)
                        <= Climate.quantizeCoord((float) taintThreshold)) {
                    expected = ThaumicOverworldBiomeParameters.TAINTED_LANDS;
                    taintSamples++;
                } else {
                    expected = PLAINS;
                    vanillaSamples++;
                }
                assertEquals(expected, matches.get(0));
            }
        }

        int total = magicalSamples + taintSamples + vanillaSamples;
        assertTrue(magicalSamples > 0);
        assertTrue(taintSamples > 0);
        assertTrue(vanillaSamples > total * 0.74D);
        assertTrue(magicalSamples > total * 0.13D);
        assertTrue(taintSamples > total * 0.04D);
    }

    @Test
    void defaultWeightsProduceBiomeScaleClimateWindows() {
        assertEquals(
                0.70D,
                ThaumicOverworldBiomeParameters.magicalThreshold(5),
                0.000001D
        );
        assertEquals(
                -0.91D,
                TaintedLandsGenerationPolicy.patchThreshold(2),
                0.000001D
        );
    }

    @Test
    void regionalMarkerCannotWinOrdinaryClimateLookup() {
        assertEquals(
                Climate.quantizeCoord(100.0F),
                ThaumicOverworldBiomeParameters.regionalMarker().offset()
        );
    }

    @Test
    void defaultMagicalWindowIsFiveThirdsWiderThanThePreviousWindow() {
        double previousWidth = 1.0D - 0.82D;
        double currentWidth = 1.0D
                - ThaumicOverworldBiomeParameters.magicalThreshold(5);

        assertEquals(5.0D / 3.0D, currentWidth / previousWidth, 0.000001D);
    }

    @Test
    void singleWideContourPreservesEveryRemainderWithoutOverlap() {
        Climate.ParameterPoint source = new Climate.ParameterPoint(
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-0.11F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.point(0.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                0L
        );
        List<Pair<Climate.ParameterPoint, ResourceLocation>> output =
                split(source, PLAINS, 5, 2);
        float[] temperatures = {-0.8F, -0.3F, 0.0F, 0.4F, 0.8F};
        float[] humidities = {-0.8F, -0.2F, 0.2F, 0.8F};
        float[] erosions = {-0.9F, -0.8F, -0.4F, 0.2F, 0.8F};
        float[] weirdnesses = {-0.8F, 0.0F, 0.69F, 0.71F, 0.9F};

        for (float temperature : temperatures) {
            for (float humidity : humidities) {
                for (float erosion : erosions) {
                    for (float weirdness : weirdnesses) {
                        List<ResourceLocation> matches = output.stream()
                                .filter(pair -> contains(
                                        pair.getFirst().temperature(),
                                        temperature
                                ))
                                .filter(pair -> contains(
                                        pair.getFirst().humidity(),
                                        humidity
                                ))
                                .filter(pair -> contains(
                                        pair.getFirst().continentalness(),
                                        0.3F
                                ))
                                .filter(pair -> contains(
                                        pair.getFirst().erosion(),
                                        erosion
                                ))
                                .filter(pair -> contains(
                                        pair.getFirst().weirdness(),
                                        weirdness
                                ))
                                .map(Pair::getSecond)
                                .toList();
                        assertEquals(1, matches.size());

                        boolean magical = erosion >= 0.70F;
                        ResourceLocation expected = magical
                                ? ThaumicOverworldBiomeParameters
                                        .MAGICAL_FOREST
                                : erosion <= -0.91F
                                        ? ThaumicOverworldBiomeParameters
                                                .TAINTED_LANDS
                                        : PLAINS;
                        assertEquals(expected, matches.get(0));
                    }
                }
            }
        }
    }

    @Test
    void leavesModOwnedMappingsUntouched() {
        ResourceLocation foreignBiome = new ResourceLocation(
                "regions_unexplored", "maple_forest"
        );
        Climate.ParameterPoint source = point(
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        List<Pair<Climate.ParameterPoint, ResourceLocation>> output =
                split(source, foreignBiome, 5, 2);

        assertEquals(1, output.size());
        assertSame(source, output.get(0).getFirst());
        assertEquals(foreignBiome, output.get(0).getSecond());
    }

    @Test
    void leavesOceanCoastAndUndergroundMappingsUntouched() {
        Climate.ParameterPoint ocean = point(
                Climate.Parameter.span(-1.0F, -0.20F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );
        Climate.ParameterPoint coast = point(
                Climate.Parameter.span(-0.19F, -0.11F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );
        Climate.ParameterPoint underground = new Climate.ParameterPoint(
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-0.11F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(0.20F, 0.90F),
                Climate.Parameter.span(-1.0F, 1.0F),
                0L
        );

        assertUnchanged(ocean, OCEAN);
        assertUnchanged(coast, BEACH);
        assertUnchanged(underground, LUSH_CAVES);
    }

    @Test
    void magicalForestContourDoesNotSplitOnTemperatureOrHumidity() {
        Climate.ParameterPoint hotAndDry = point(
                Climate.Parameter.span(0.56F, 1.0F),
                Climate.Parameter.span(-1.0F, -0.36F),
                Climate.Parameter.span(-0.11F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(0.91F, 1.0F)
        );

        List<Pair<Climate.ParameterPoint, ResourceLocation>> desert =
                split(hotAndDry, DESERT, 5, 2);

        assertTrue(desert.stream().anyMatch(pair -> pair.getSecond()
                .equals(ThaumicOverworldBiomeParameters.MAGICAL_FOREST)));
        assertTrue(desert.stream().anyMatch(pair -> pair.getSecond()
                .equals(ThaumicOverworldBiomeParameters.TAINTED_LANDS)));

        Climate.ParameterPoint frozen = point(
                Climate.Parameter.span(-1.0F, -0.46F),
                Climate.Parameter.span(-0.1F, 0.1F),
                Climate.Parameter.span(-0.11F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(0.70F, 1.0F)
        );
        List<Pair<Climate.ParameterPoint, ResourceLocation>> coldShore =
                split(frozen, STONY_SHORE, 5, 2);
        assertTrue(coldShore.stream().anyMatch(pair -> pair.getSecond()
                .equals(ThaumicOverworldBiomeParameters.MAGICAL_FOREST)));

        Climate.ParameterPoint eligible = point(
                Climate.Parameter.span(-0.15F, 0.20F),
                Climate.Parameter.span(0.10F, 0.30F),
                Climate.Parameter.span(-0.11F, 1.0F),
                Climate.Parameter.span(0.70F, 1.0F),
                Climate.Parameter.span(0.70F, 1.0F)
        );
        assertTrue(split(eligible, PLAINS, 5, 2).stream()
                .anyMatch(pair -> pair.getSecond()
                        .equals(ThaumicOverworldBiomeParameters
                                .MAGICAL_FOREST)));
    }

    @Test
    void neverClimateGeneratesEerie() {
        Climate.ParameterPoint source = point(
                Climate.Parameter.span(-0.11F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        assertTrue(split(source, PLAINS, 5, 2).stream()
                .noneMatch(pair -> pair.getSecond()
                        .getPath().equals("eerie")));
    }

    private static void assertUnchanged(
            Climate.ParameterPoint source,
            ResourceLocation biome
    ) {
        List<Pair<Climate.ParameterPoint, ResourceLocation>> output =
                split(source, biome, 5, 2);
        assertEquals(1, output.size());
        assertSame(source, output.get(0).getFirst());
        assertEquals(biome, output.get(0).getSecond());
    }

    private static List<Pair<Climate.ParameterPoint, ResourceLocation>>
    split(
            Climate.ParameterPoint point,
            ResourceLocation biome,
            int magicalWeight,
            int taintWeight
    ) {
        List<Pair<Climate.ParameterPoint, ResourceLocation>> output =
                new ArrayList<>();
        ThaumicOverworldBiomeParameters.splitLocationMapping(
                Pair.of(point, biome),
                output::add,
                magicalWeight,
                taintWeight
        );
        return output;
    }

    private static Climate.ParameterPoint point(
            Climate.Parameter continentalness,
            Climate.Parameter erosion,
            Climate.Parameter weirdness
    ) {
        return point(
                Climate.Parameter.span(-0.15F, 0.20F),
                Climate.Parameter.span(0.10F, 0.30F),
                continentalness,
                erosion,
                weirdness
        );
    }

    private static Climate.ParameterPoint point(
            Climate.Parameter temperature,
            Climate.Parameter humidity,
            Climate.Parameter continentalness,
            Climate.Parameter erosion,
            Climate.Parameter weirdness
    ) {
        return new Climate.ParameterPoint(
                temperature,
                humidity,
                continentalness,
                erosion,
                Climate.Parameter.point(0.0F),
                weirdness,
                0L
        );
    }

    private static boolean contains(
            Climate.Parameter parameter,
            float value
    ) {
        return parameter.distance(Climate.quantizeCoord(value)) == 0L;
    }

    private static ResourceLocation vanilla(String path) {
        return new ResourceLocation("minecraft", path);
    }
}
