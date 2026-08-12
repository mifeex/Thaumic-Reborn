package com.thaumcraftmodern.client.render;

import com.google.gson.JsonParser;
import com.thaumcraftmodern.wand.WandForm;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class ClassicWandRenderCalibrationTest {
    @Test
    void resourceDefinesIndependentCapTipsForEveryForm()
            throws Exception {
        try (var stream = getClass().getResourceAsStream(
                "/assets/thaumcraftmodern/config/"
                        + "wand_casting_render.json"
        )) {
            assertNotNull(stream);
            var configuration =
                    ClassicWandRenderCalibration.parseConfiguration(
                            JsonParser.parseReader(
                                    new InputStreamReader(
                                            stream,
                                            StandardCharsets.UTF_8
                                    )
                            ).getAsJsonObject()
                    );
            Map<WandForm, ClassicWandRenderCalibration.Form> forms =
                    configuration.forms();

            assertEquals(
                    -0.0625F,
                    forms.get(WandForm.WAND).primaryCapTip().y()
            );
            assertEquals(
                    0.13125F,
                    forms.get(WandForm.STAFF).primaryCapTip().y()
            );
            assertEquals(
                    -0.08125F,
                    forms.get(WandForm.SCEPTRE).primaryCapTip().y()
            );
            assertEquals(
                    1.0F,
                    forms.get(WandForm.WAND).castingPivot().y()
            );
            assertEquals(
                    1.8F,
                    forms.get(WandForm.STAFF).castingPivot().y()
            );
            assertEquals(
                    1.0F,
                    forms.get(WandForm.SCEPTRE).castingPivot().y()
            );
            assertEquals(
                    0.5F,
                    forms.get(WandForm.STAFF).handPreOffset().y()
            );
            assertEquals(false, forms.get(WandForm.STAFF).gui().override());
            assertEquals(
                    0.0F,
                    forms.get(WandForm.STAFF)
                            .gui()
                            .translationPixels()
                            .y()
            );
            assertEquals(
                    0.0F,
                    forms.get(WandForm.STAFF)
                            .gui()
                            .rotationDegrees()
                            .z()
            );
            assertEquals(
                    1.0F,
                    forms.get(WandForm.STAFF).gui().scale().x()
            );
        }
    }

    @Test
    void resourceSwitchAcceptsClassicComparisonMode()
            throws Exception {
        try (var stream = getClass().getResourceAsStream(
                "/assets/thaumcraftmodern/config/"
                        + "wand_casting_render.json"
        )) {
            assertNotNull(stream);
            var root = JsonParser.parseReader(
                    new InputStreamReader(
                            stream,
                            StandardCharsets.UTF_8
                    )
            ).getAsJsonObject();
            root.addProperty("drain_animation_mode", "classic");

            assertEquals(
                    ClassicWandRenderCalibration.DrainAnimationMode.CLASSIC,
                    ClassicWandRenderCalibration
                            .parseConfiguration(root)
                            .drainAnimationMode()
            );
        }
    }
}
