package com.thaumcraftmodern.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.research.ResearchCategoryDefinition;
import com.thaumcraftmodern.research.ResearchCategoryRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ResearchCategoryReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_BACKGROUND =
            "thaumic_reborn:textures/gui/gui_researchback.png";

    public ResearchCategoryReloadListener() {
        super(GSON, "thaumcraft/categories");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> objects,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        List<ResearchCategoryDefinition> definitions = new ArrayList<>();
        int inactiveCount = 0;
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(
                        file.getValue(),
                        "research category"
                );
                if (DefinitionActivation.isInactive(json)) {
                    inactiveCount++;
                    continue;
                }
                String iconResource = GsonHelper.getAsString(
                        json,
                        "icon_resource",
                        ""
                );
                String iconItem = iconResource.isBlank()
                        ? GsonHelper.getAsString(json, "icon")
                        : "";
                definitions.add(new ResearchCategoryDefinition(
                        GsonHelper.getAsString(json, "id"),
                        GsonHelper.getAsString(json, "title"),
                        iconItem,
                        iconResource,
                        GsonHelper.getAsString(json, "background", DEFAULT_BACKGROUND),
                        GsonHelper.getAsInt(json, "order", 0)
                ));
            } catch (RuntimeException ex) {
                ThaumcraftModern.LOGGER.error(
                        "Invalid research category definition {}",
                        file.getKey(),
                        ex
                );
            }
        }
        ResearchCategoryRegistry.replace(definitions);
        ThaumcraftModern.LOGGER.info(
                "Loaded {} research category definitions; skipped {} inactive definitions",
                definitions.size(),
                inactiveCount
        );
    }
}
