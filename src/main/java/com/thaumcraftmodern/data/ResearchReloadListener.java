package com.thaumcraftmodern.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.research.ResearchDefinition;
import com.thaumcraftmodern.research.InfusionDisplayDefinition;
import com.thaumcraftmodern.research.ResearchCategoryRegistry;
import com.thaumcraftmodern.research.ResearchPageDefinition;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.research.ResearchPuzzleRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ResearchReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public ResearchReloadListener() {
        super(GSON, "thaumcraft/research");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<ResearchDefinition> definitions = new ArrayList<>();
        Map<String, Integer> puzzleComplexities = new java.util.LinkedHashMap<>();
        int inactiveCount = 0;
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(file.getValue(), "research definition");
                boolean inactive = DefinitionActivation.isInactive(json);
                if (inactive) {
                    inactiveCount++;
                }
                String id = GsonHelper.getAsString(json, "id");
                JsonObject legacyData = json.has("legacy")
                        ? GsonHelper.getAsJsonObject(json, "legacy")
                        : null;
                puzzleComplexities.put(
                        id,
                        legacyData == null
                                ? 1
                                : GsonHelper.getAsInt(legacyData, "complexity", 1)
                );
                List<String> parents = new ArrayList<>();
                if (json.has("parents")) {
                    for (JsonElement parent : GsonHelper.getAsJsonArray(json, "parents")) {
                        parents.add(GsonHelper.convertToString(parent, "research parent"));
                    }
                }
                List<String> hiddenParents = new ArrayList<>();
                if (json.has("hidden_parents")) {
                    for (JsonElement parent : GsonHelper.getAsJsonArray(
                            json,
                            "hidden_parents"
                    )) {
                        hiddenParents.add(
                                GsonHelper.convertToString(parent, "hidden research parent")
                        );
                    }
                }
                List<String> siblings = new ArrayList<>();
                JsonObject legacy = json.has("legacy")
                        ? GsonHelper.getAsJsonObject(json, "legacy")
                        : null;
                JsonElement siblingsElement = json.has("siblings")
                        ? json.get("siblings")
                        : legacy != null ? legacy.get("siblings") : null;
                if (siblingsElement != null && siblingsElement.isJsonArray()) {
                    for (JsonElement sibling : siblingsElement.getAsJsonArray()) {
                        siblings.add(GsonHelper.convertToString(
                                sibling,
                                "research sibling"
                        ).toLowerCase(Locale.ROOT));
                    }
                }
                List<ResearchPageDefinition> pages = new ArrayList<>();
                for (JsonElement rawPage : GsonHelper.getAsJsonArray(json, "pages")) {
                    JsonObject page = GsonHelper.convertToJsonObject(rawPage, "research page");
                    ResearchPageDefinition.Type type = ResearchPageDefinition.Type.valueOf(
                            GsonHelper.getAsString(page, "type").toUpperCase(Locale.ROOT)
                    );
                    List<AspectCost> aspectCosts = new ArrayList<>();
                    if (page.has("aspect_costs")) {
                        for (JsonElement rawCost : GsonHelper.getAsJsonArray(
                                page,
                                "aspect_costs"
                        )) {
                            JsonObject cost = GsonHelper.convertToJsonObject(
                                    rawCost,
                                    "research page aspect cost"
                            );
                            aspectCosts.add(new AspectCost(
                                    GsonHelper.getAsString(cost, "id"),
                                    GsonHelper.getAsInt(cost, "amount")
                            ));
                        }
                    }
                    pages.add(new ResearchPageDefinition(
                            type,
                            GsonHelper.getAsString(page, "title", ""),
                            GsonHelper.getAsString(page, "body", ""),
                            GsonHelper.getAsString(page, "recipe", ""),
                            aspectCosts,
                            type == ResearchPageDefinition.Type.INFUSION
                                    ? readInfusionDisplay(page)
                                    : null,
                            readRecipeIds(page)
                    ));
                }
                String categoryId = GsonHelper.getAsString(json, "category", "basics");
                if (ResearchCategoryRegistry.find(categoryId).isEmpty()) {
                    throw new IllegalArgumentException(
                            "research " + id + " requests missing category " + categoryId
                                    + "; registered categories: "
                                    + ResearchCategoryRegistry.all().stream()
                                    .map(category -> category.id())
                                    .toList()
                    );
                }
                String iconResource = GsonHelper.getAsString(
                        json,
                        "icon_resource",
                        ""
                );
                String iconItem = iconResource.isBlank()
                        ? GsonHelper.getAsString(
                                json,
                                "icon",
                                "thaumic_reborn:thaumonomicon"
                        )
                        : "";
                NodeStyle nodeStyle = readNodeStyle(json);
                List<AspectCost> purchaseCost = readAspectCosts(
                        json,
                        "purchase_cost",
                        "research " + id + " purchase cost"
                );
                List<AspectCost> researchCost = json.has("research_cost")
                        ? readAspectCosts(
                                json,
                                "research_cost",
                                "research " + id + " aspect cost"
                        )
                        : legacy == null ? List.of() : readLegacyResearchCosts(
                                legacy,
                                "research_aspects",
                                "research " + id + " aspect cost"
                        );
                definitions.add(new ResearchDefinition(
                        id,
                        categoryId,
                        iconItem,
                        iconResource,
                        GsonHelper.getAsString(json, "title"),
                        GsonHelper.getAsString(json, "subtitle", ""),
                        GsonHelper.getAsBoolean(json, "concealed", false),
                        GsonHelper.getAsBoolean(json, "auto_unlock", false),
                        inactive,
                        GsonHelper.getAsBoolean(json, "virtual", false),
                        GsonHelper.getAsString(json, "revealed_by", ""),
                        parents,
                        hiddenParents,
                        com.thaumcraftmodern.research.ResearchConditionCodec.fromJson(
                                json.get("reveal_when"),
                                "research " + id + ".reveal_when"
                        ),
                        com.thaumcraftmodern.research.ResearchConditionCodec.fromJson(
                                json.get("unlock_when"),
                                "research " + id + ".unlock_when"
                        ),
                        GsonHelper.getAsInt(json, "x", 0),
                        GsonHelper.getAsInt(json, "y", 0),
                        pages,
                        readCompletionWarp(json),
                        nodeStyle.frame(),
                        nodeStyle.special(),
                        researchCost,
                        purchaseCost,
                        siblings
                ));
            } catch (RuntimeException ex) {
                ThaumcraftModern.LOGGER.error("Invalid research definition {}", file.getKey(), ex);
            }
        }
        ResearchRegistry.replace(definitions);
        ResearchPuzzleRegistry.replace(puzzleComplexities);
        ThaumcraftModern.LOGGER.info(
                "Loaded {} research definitions; {} have inactive gameplay content",
                definitions.size(),
                inactiveCount
        );
    }

    private static List<String> readRecipeIds(JsonObject page) {
        if (!page.has("recipes")) {
            return List.of();
        }
        List<String> recipes = new ArrayList<>();
        for (JsonElement rawRecipe : GsonHelper.getAsJsonArray(
                page,
                "recipes"
        )) {
            recipes.add(GsonHelper.convertToString(rawRecipe, "recipe"));
        }
        return List.copyOf(recipes);
    }

    private static List<AspectCost> readAspectCosts(
            JsonObject json,
            String member,
            String label
    ) {
        if (!json.has(member)) {
            return List.of();
        }
        List<AspectCost> result = new ArrayList<>();
        for (JsonElement rawCost : GsonHelper.getAsJsonArray(json, member)) {
            JsonObject cost = GsonHelper.convertToJsonObject(rawCost, label);
            result.add(new AspectCost(
                    GsonHelper.getAsString(cost, "id"),
                    GsonHelper.getAsInt(cost, "amount")
            ));
        }
        return List.copyOf(result);
    }

    private static List<AspectCost> readLegacyResearchCosts(
            JsonObject json,
            String member,
            String label
    ) {
        java.util.LinkedHashMap<String, Integer> merged = new java.util.LinkedHashMap<>();
        for (AspectCost cost : readAspectCosts(json, member, label)) {
            merged.merge(cost.aspectId(), cost.amount(), Math::addExact);
        }
        return merged.entrySet().stream()
                .map(entry -> new AspectCost(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static int readCompletionWarp(JsonObject json) {
        if (json.has("completion_warp")) {
            return GsonHelper.getAsInt(json, "completion_warp");
        }
        if (!json.has("legacy")) {
            return 0;
        }
        JsonObject legacy = GsonHelper.getAsJsonObject(json, "legacy");
        if (!legacy.has("warp")
                || GsonHelper.getAsJsonArray(legacy, "warp").isEmpty()) {
            return 0;
        }
        return GsonHelper.convertToInt(
                GsonHelper.getAsJsonArray(legacy, "warp").get(0),
                "legacy research completion warp"
        );
    }

    private static NodeStyle readNodeStyle(JsonObject json) {
        JsonObject style = json.has("node_style")
                ? GsonHelper.getAsJsonObject(json, "node_style")
                : null;
        if (style != null) {
            return new NodeStyle(
                    parseNodeFrame(
                            GsonHelper.getAsString(style, "frame", "primary")
                    ),
                    GsonHelper.getAsBoolean(style, "special", false)
            );
        }

        /*
         * Imported TC4 entries already preserve their original flags. Use
         * them as the compatibility fallback so the complete legacy tree
         * immediately regains its classic mixture of frames. An explicit
         * node_style object always wins and is the editable modern contract.
         */
        if (json.has("legacy")) {
            JsonObject legacy = GsonHelper.getAsJsonObject(json, "legacy");
            if (legacy.has("flags")) {
                JsonObject flags = GsonHelper.getAsJsonObject(legacy, "flags");
                ResearchDefinition.NodeFrame frame =
                        GsonHelper.getAsBoolean(flags, "round", false)
                                ? ResearchDefinition.NodeFrame.ROUND
                                : GsonHelper.getAsBoolean(
                                        flags,
                                        "hidden",
                                        false
                                )
                                ? ResearchDefinition.NodeFrame.HIDDEN
                                : GsonHelper.getAsBoolean(
                                        flags,
                                        "secondary",
                                        false
                                )
                                ? ResearchDefinition.NodeFrame.SECONDARY
                                : ResearchDefinition.NodeFrame.PRIMARY;
                return new NodeStyle(
                        frame,
                        GsonHelper.getAsBoolean(flags, "special", false)
                );
            }
        }
        return new NodeStyle(ResearchDefinition.NodeFrame.PRIMARY, false);
    }

    private static ResearchDefinition.NodeFrame parseNodeFrame(String raw) {
        try {
            return ResearchDefinition.NodeFrame.valueOf(
                    raw.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "unknown research node frame '" + raw
                            + "'; expected primary, round, secondary, or hidden",
                    ex
            );
        }
    }

    private record NodeStyle(
            ResearchDefinition.NodeFrame frame,
            boolean special
    ) {
    }

    private static InfusionDisplayDefinition readInfusionDisplay(
            JsonObject page
    ) {
        List<InfusionDisplayDefinition.ComponentStack> components =
                new ArrayList<>();
        for (JsonElement rawComponent : GsonHelper.getAsJsonArray(
                page,
                "components"
        )) {
            JsonObject component = GsonHelper.convertToJsonObject(
                    rawComponent,
                    "infusion display component"
            );
            int count = GsonHelper.getAsInt(component, "count", 1);
            if (component.has("tag")) {
                components.add(InfusionDisplayDefinition.ComponentStack.tagged(
                        GsonHelper.getAsString(component, "tag"), count
                ));
            } else {
                components.add(new InfusionDisplayDefinition.ComponentStack(
                        GsonHelper.getAsString(component, "item"), count,
                        GsonHelper.getAsString(component, "potion", "")
                ));
            }
        }
        return new InfusionDisplayDefinition(
                GsonHelper.getAsString(page, "output"),
                GsonHelper.getAsString(page, "central"),
                components,
                InfusionDisplayDefinition.Instability.parse(
                        GsonHelper.getAsString(page, "instability")
                ),
                GsonHelper.getAsString(page, "detail", "")
        );
    }
}
