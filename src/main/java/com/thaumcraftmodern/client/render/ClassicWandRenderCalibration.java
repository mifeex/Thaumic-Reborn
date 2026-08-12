package com.thaumcraftmodern.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.wand.WandForm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

import java.io.BufferedReader;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Reloadable hand placement, casting pivot and cap endpoint for every tool.
 */
public final class ClassicWandRenderCalibration {
    private static final ResourceLocation RESOURCE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "config/wand_casting_render.json"
            );
    private static final Map<WandForm, Form> DEFAULT_FORMS = defaults();
    private static volatile Configuration current = new Configuration(
            DEFAULT_FORMS,
            DrainAnimationMode.MODERN
    );

    private ClassicWandRenderCalibration() {
    }

    public static Form form(WandForm form) {
        return current.forms().getOrDefault(
                form,
                DEFAULT_FORMS.get(form)
        );
    }

    public static DrainAnimationMode drainAnimationMode() {
        return current.drainAnimationMode();
    }

    public static void registerReloadListener(
            RegisterClientReloadListenersEvent event
    ) {
        event.registerReloadListener(
                new SimplePreparableReloadListener<Configuration>() {
                    @Override
                    protected Configuration prepare(
                            ResourceManager resourceManager,
                            ProfilerFiller profiler
                    ) {
                        try {
                            Resource resource = resourceManager
                                    .getResource(RESOURCE)
                                    .orElseThrow(() ->
                                            new IllegalStateException(
                                                    "Missing client resource "
                                                            + RESOURCE
                                            )
                                    );
                            try (BufferedReader reader =
                                         resource.openAsReader()) {
                                return parseConfiguration(
                                        JsonParser.parseReader(reader)
                                                .getAsJsonObject()
                                );
                            }
                        } catch (Exception exception) {
                            ThaumcraftModern.LOGGER.error(
                                    "Rejected wand render calibration reload; "
                                            + "keeping previous calibration",
                                    exception
                            );
                            return current;
                        }
                    }

                    @Override
                    protected void apply(
                            Configuration prepared,
                            ResourceManager resourceManager,
                            ProfilerFiller profiler
                    ) {
                        current = prepared;
                        ThaumcraftModern.LOGGER.info(
                                "Loaded wand render calibration for {} forms "
                                        + "with {} drain animation",
                                prepared.forms().size(),
                                prepared.drainAnimationMode()
                        );
                    }
                }
        );
    }

    static Map<WandForm, Form> parse(JsonObject root) {
        return parseConfiguration(root).forms();
    }

    static Configuration parseConfiguration(JsonObject root) {
        EnumMap<WandForm, Form> parsed = new EnumMap<>(WandForm.class);
        for (WandForm wandForm : WandForm.values()) {
            String key = wandForm.name().toLowerCase(Locale.ROOT);
            JsonObject json = root.getAsJsonObject(key);
            if (json == null) {
                throw new IllegalArgumentException(
                        "Missing wand calibration form: " + key
                );
            }
            parsed.put(
                    wandForm,
                    new Form(
                            vector(json, "hand_pre_offset"),
                            vector(json, "hand_offset"),
                            vector(json, "first_person_scale"),
                            vector(json, "third_person_scale"),
                            vector(json, "primary_cap_tip"),
                            vector(json, "casting_pivot"),
                            gui(json)
                )
            );
        }
        DrainAnimationMode mode = root.has("drain_animation_mode")
                ? DrainAnimationMode.parse(
                        root.get("drain_animation_mode").getAsString()
                )
                : DrainAnimationMode.MODERN;
        return new Configuration(Map.copyOf(parsed), mode);
    }

    private static Vector vector(JsonObject owner, String key) {
        JsonArray array = owner.getAsJsonArray(key);
        if (array == null || array.size() != 3) {
            throw new IllegalArgumentException(
                    key + " must contain exactly three numbers"
            );
        }
        Vector value = new Vector(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat()
        );
        if (!Float.isFinite(value.x())
                || !Float.isFinite(value.y())
                || !Float.isFinite(value.z())) {
            throw new IllegalArgumentException(key + " must be finite");
        }
        return value;
    }

    private static Gui gui(JsonObject owner) {
        JsonObject json = owner.getAsJsonObject("gui");
        if (json == null) {
            return Gui.NONE;
        }
        boolean override = json.has("override")
                && json.get("override").getAsBoolean();
        return new Gui(
                override,
                vector(json, "translation_pixels"),
                vector(json, "rotation_degrees"),
                vector(json, "scale")
        );
    }

    private static Map<WandForm, Form> defaults() {
        EnumMap<WandForm, Form> defaults = new EnumMap<>(WandForm.class);
        defaults.put(
                WandForm.WAND,
                form(0.0F, -0.0625F, 1.0F, Gui.NONE)
        );
        defaults.put(
                WandForm.STAFF,
                form(
                        0.5F,
                        0.13125F,
                        1.8F,
                        Gui.NONE
                )
        );
        defaults.put(
                WandForm.SCEPTRE,
                form(0.0F, -0.08125F, 1.0F, Gui.NONE)
        );
        return Map.copyOf(defaults);
    }

    private static Form form(
            float preOffsetY,
            float primaryCapTipY,
            float castingPivotY,
            Gui gui
    ) {
        return new Form(
                new Vector(0.0F, preOffsetY, 0.0F),
                new Vector(0.5F, 1.0F, 0.5F),
                new Vector(0.5F, 0.55F, 0.5F),
                new Vector(0.5F, 0.5F, 0.5F),
                new Vector(0.0F, primaryCapTipY, 0.0F),
                new Vector(0.0F, castingPivotY, 0.0F),
                gui
        );
    }

    public record Form(
            Vector handPreOffset,
            Vector handOffset,
            Vector firstPersonScale,
            Vector thirdPersonScale,
            Vector primaryCapTip,
            Vector castingPivot,
            Gui gui
    ) {
    }

    public record Gui(
            boolean override,
            Vector translationPixels,
            Vector rotationDegrees,
            Vector scale
    ) {
        private static final Gui NONE = new Gui(
                false,
                new Vector(0.0F, 0.0F, 0.0F),
                new Vector(0.0F, 0.0F, 0.0F),
                new Vector(1.0F, 1.0F, 1.0F)
        );
    }

    public record Vector(float x, float y, float z) {
    }

    record Configuration(
            Map<WandForm, Form> forms,
            DrainAnimationMode drainAnimationMode
    ) {
    }

    public enum DrainAnimationMode {
        MODERN,
        CLASSIC;

        private static DrainAnimationMode parse(String value) {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "modern" -> MODERN;
                case "classic", "tc4" -> CLASSIC;
                default -> throw new IllegalArgumentException(
                        "Unknown drain_animation_mode: " + value
                );
            };
        }
    }
}
