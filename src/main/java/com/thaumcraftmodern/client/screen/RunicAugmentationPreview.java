package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.research.InfusionDisplayDefinition;
import com.thaumcraftmodern.research.ResearchPageDefinition;

import java.util.ArrayList;
import java.util.List;

/** Data-only adapter for TC4's five cycling runic augmentation previews. */
record RunicAugmentationPreview(
        int inputHardening,
        int outputHardening,
        List<InfusionDisplayDefinition.ComponentStack> components,
        List<AspectCost> costs,
        InfusionDisplayDefinition.Instability instability
) {
    static RunicAugmentationPreview atTime(ResearchPageDefinition page,
            InfusionDisplayDefinition display, long timeMillis) {
        if (!"thaumic_reborn:runic_augmentation".equals(page.recipeId())) {
            return null;
        }
        int charge = (int) Math.floorMod(timeMillis / 1_000L, 5L);
        List<InfusionDisplayDefinition.ComponentStack> components =
                new ArrayList<>(display.components());
        if (display.components().size() > 1) {
            for (int index = 0; index < charge; index++) {
                components.add(display.components().get(1));
            }
        }
        int amount = (int) (32.0D * Math.pow(2.0D, charge));
        List<AspectCost> costs = List.of(
                new AspectCost("tutamen", amount / 2),
                new AspectCost("praecantatio", amount / 2),
                new AspectCost("potentia", amount)
        );
        int instability = 5 + charge / 2;
        return new RunicAugmentationPreview(
                charge,
                charge + 1,
                List.copyOf(components),
                costs,
                instability < 6
                        ? InfusionDisplayDefinition.Instability.MODERATE
                        : InfusionDisplayDefinition.Instability.HIGH
        );
    }
}
