package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ResearchRegistry {
    private static volatile Snapshot snapshot = new Snapshot(
            Map.of(),
            List.of(),
            0L
    );

    private ResearchRegistry() {
    }

    public static synchronized void replace(Collection<ResearchDefinition> values) {
        Map<String, ResearchDefinition> next = new LinkedHashMap<>();
        values.stream()
                .sorted(Comparator.comparing(ResearchDefinition::id))
                .forEach(definition -> {
                    if (next.put(definition.id(), definition) != null) {
                        throw new IllegalArgumentException("Duplicate research definition: " + definition.id());
                    }
                });
        Snapshot previous = snapshot;
        snapshot = new Snapshot(
                Map.copyOf(next),
                List.copyOf(next.values()),
                previous.revision() + 1L
        );
    }

    public static Optional<ResearchDefinition> find(String id) {
        return Optional.ofNullable(snapshot.definitions().get(id));
    }

    public static List<ResearchDefinition> all() {
        return snapshot.orderedDefinitions();
    }

    public static long revision() {
        return snapshot.revision();
    }

    public static CompoundTag serialize() {
        CompoundTag root = new CompoundTag();
        ListTag entries = new ListTag();
        for (ResearchDefinition definition : all()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", definition.id());
            entry.putString("category", definition.categoryId());
            entry.putString("icon", definition.iconItem());
            entry.putString("iconResource", definition.iconResource());
            entry.putString("title", definition.titleKey());
            entry.putString("subtitle", definition.subtitleKey());
            entry.putBoolean("concealed", definition.concealed());
            entry.putBoolean("autoUnlock", definition.autoUnlock());
            entry.putBoolean("inactive", definition.inactive());
            entry.putBoolean("virtual", definition.virtual());
            entry.putString("nodeFrame", definition.nodeFrame().name());
            entry.putBoolean("specialFrame", definition.specialFrame());
            ListTag researchCost = new ListTag();
            for (AspectCost cost : definition.researchCost()) {
                CompoundTag costTag = new CompoundTag();
                costTag.putString("id", cost.aspectId());
                costTag.putInt("amount", cost.amount());
                researchCost.add(costTag);
            }
            entry.put("researchCost", researchCost);
            ListTag purchaseCost = new ListTag();
            for (AspectCost cost : definition.purchaseCost()) {
                CompoundTag costTag = new CompoundTag();
                costTag.putString("id", cost.aspectId());
                costTag.putInt("amount", cost.amount());
                purchaseCost.add(costTag);
            }
            entry.put("purchaseCost", purchaseCost);
            entry.putString("revealedBy", definition.revealedBy());
            ListTag parents = new ListTag();
            definition.parents().stream()
                    .map(StringTag::valueOf)
                    .forEach(parents::add);
            entry.put("parents", parents);
            ListTag hiddenParents = new ListTag();
            definition.hiddenParents().stream()
                    .map(StringTag::valueOf)
                    .forEach(hiddenParents::add);
            entry.put("hiddenParents", hiddenParents);
            ListTag siblings = new ListTag();
            definition.siblings().stream()
                    .map(StringTag::valueOf)
                    .forEach(siblings::add);
            entry.put("siblings", siblings);
            entry.put(
                    "revealWhen",
                    ResearchConditionCodec.toNbt(definition.revealWhen())
            );
            entry.put(
                    "unlockWhen",
                    ResearchConditionCodec.toNbt(definition.unlockWhen())
            );
            entry.putInt("x", definition.x());
            entry.putInt("y", definition.y());
            entry.putInt("completionWarp", definition.completionWarp());

            ListTag pages = new ListTag();
            for (ResearchPageDefinition page : definition.pages()) {
                CompoundTag pageTag = new CompoundTag();
                pageTag.putString("type", page.type().name());
                pageTag.putString("title", page.titleKey());
                pageTag.putString("body", page.bodyKey());
                pageTag.putString("recipe", page.recipeId());
                ListTag recipeIds = new ListTag();
                page.recipeIds().forEach(recipe -> recipeIds.add(
                        net.minecraft.nbt.StringTag.valueOf(recipe)
                ));
                pageTag.put("recipes", recipeIds);
                ListTag aspectCosts = new ListTag();
                for (AspectCost cost : page.aspectCosts()) {
                    CompoundTag costTag = new CompoundTag();
                    costTag.putString("id", cost.aspectId());
                    costTag.putInt("amount", cost.amount());
                    aspectCosts.add(costTag);
                }
                pageTag.put("aspectCosts", aspectCosts);
                if (page.infusionDisplay() != null) {
                    InfusionDisplayDefinition display =
                            page.infusionDisplay();
                    CompoundTag infusion = new CompoundTag();
                    infusion.putString("output", display.outputItem());
                    infusion.putString("central", display.centralItem());
                    infusion.putString(
                            "instability",
                            display.instability().name()
                    );
                    infusion.putString("detail", display.detailKey());
                    ListTag components = new ListTag();
                    for (InfusionDisplayDefinition.ComponentStack component
                            : display.components()) {
                        CompoundTag componentTag = new CompoundTag();
                        if (component.isTag()) {
                            componentTag.putString("tag", component.tag());
                        } else {
                            componentTag.putString("item", component.item());
                        }
                        componentTag.putInt("count", component.count());
                        if (!component.potion().isBlank()) {
                            componentTag.putString("potion", component.potion());
                        }
                        components.add(componentTag);
                    }
                    infusion.put("components", components);
                    pageTag.put("infusion", infusion);
                }
                pages.add(pageTag);
            }
            entry.put("pages", pages);
            entries.add(entry);
        }
        root.put("entries", entries);
        return root;
    }

    public static List<ResearchDefinition> deserialize(CompoundTag root) {
        List<ResearchDefinition> result = new ArrayList<>();
        ListTag entries = root.getList("entries", Tag.TAG_COMPOUND);
        for (Tag raw : entries) {
            CompoundTag entry = (CompoundTag) raw;
            List<String> parents = new ArrayList<>();
            ListTag parentTags = entry.getList("parents", Tag.TAG_STRING);
            for (int index = 0; index < parentTags.size(); index++) {
                parents.add(parentTags.getString(index));
            }
            List<String> hiddenParents = new ArrayList<>();
            ListTag hiddenParentTags = entry.getList("hiddenParents", Tag.TAG_STRING);
            for (int index = 0; index < hiddenParentTags.size(); index++) {
                hiddenParents.add(hiddenParentTags.getString(index));
            }
            List<String> siblings = new ArrayList<>();
            ListTag siblingTags = entry.getList("siblings", Tag.TAG_STRING);
            for (int index = 0; index < siblingTags.size(); index++) {
                siblings.add(siblingTags.getString(index));
            }
            List<ResearchPageDefinition> pages = new ArrayList<>();
            ListTag pageTags = entry.getList("pages", Tag.TAG_COMPOUND);
            for (Tag rawPage : pageTags) {
                CompoundTag page = (CompoundTag) rawPage;
                ResearchPageDefinition.Type type;
                try {
                    type = ResearchPageDefinition.Type.valueOf(page.getString("type"));
                } catch (IllegalArgumentException ex) {
                    type = ResearchPageDefinition.Type.TEXT;
                }
                List<AspectCost> aspectCosts = new ArrayList<>();
                for (Tag rawCost : page.getList(
                        "aspectCosts",
                        Tag.TAG_COMPOUND
                )) {
                    CompoundTag cost = (CompoundTag) rawCost;
                    aspectCosts.add(new AspectCost(
                            cost.getString("id"),
                            cost.getInt("amount")
                    ));
                }
                InfusionDisplayDefinition infusionDisplay = null;
                if (page.contains("infusion", Tag.TAG_COMPOUND)) {
                    CompoundTag infusion = page.getCompound("infusion");
                    List<InfusionDisplayDefinition.ComponentStack> components =
                            new ArrayList<>();
                    for (Tag rawComponent : infusion.getList(
                            "components",
                            Tag.TAG_COMPOUND
                    )) {
                        CompoundTag component = (CompoundTag) rawComponent;
                        int count = component.getInt("count");
                        components.add(component.contains("tag", Tag.TAG_STRING)
                                ? InfusionDisplayDefinition.ComponentStack.tagged(
                                        component.getString("tag"), count
                                )
                                : new InfusionDisplayDefinition.ComponentStack(
                                        component.getString("item"), count,
                                        component.getString("potion")
                                ));
                    }
                    infusionDisplay = new InfusionDisplayDefinition(
                            infusion.getString("output"),
                            infusion.getString("central"),
                            components,
                            InfusionDisplayDefinition.Instability.valueOf(
                                    infusion.getString("instability")
                            ),
                            infusion.getString("detail")
                    );
                }
                pages.add(new ResearchPageDefinition(
                        type,
                        page.getString("title"),
                        page.getString("body"),
                        page.getString("recipe"),
                        aspectCosts,
                        infusionDisplay,
                        page.getList("recipes", Tag.TAG_STRING).stream()
                                .map(Tag::getAsString)
                                .toList()
                ));
            }
            result.add(new ResearchDefinition(
                    entry.getString("id"),
                    entry.getString("category"),
                    entry.getString("icon"),
                    entry.getString("iconResource"),
                    entry.getString("title"),
                    entry.getString("subtitle"),
                    entry.getBoolean("concealed"),
                    entry.getBoolean("autoUnlock"),
                    entry.getBoolean("inactive"),
                    entry.getBoolean("virtual"),
                    entry.getString("revealedBy"),
                    parents,
                    hiddenParents,
                    ResearchConditionCodec.fromNbt(entry.getCompound("revealWhen")),
                    ResearchConditionCodec.fromNbt(entry.getCompound("unlockWhen")),
                    entry.getInt("x"),
                    entry.getInt("y"),
                    pages,
                    entry.getInt("completionWarp"),
                    entry.contains("nodeFrame")
                            ? ResearchDefinition.NodeFrame.valueOf(
                                    entry.getString("nodeFrame")
                            )
                            : ResearchDefinition.NodeFrame.PRIMARY,
                    entry.getBoolean("specialFrame"),
                    readAspectCosts(entry.getList("researchCost", Tag.TAG_COMPOUND)),
                    readAspectCosts(entry.getList("purchaseCost", Tag.TAG_COMPOUND)),
                    siblings
            ));
        }
        return result;
    }

    private static List<AspectCost> readAspectCosts(ListTag tags) {
        List<AspectCost> result = new ArrayList<>();
        for (Tag raw : tags) {
            CompoundTag cost = (CompoundTag) raw;
            result.add(new AspectCost(
                    cost.getString("id"),
                    cost.getInt("amount")
            ));
        }
        return List.copyOf(result);
    }

    private record Snapshot(
            Map<String, ResearchDefinition> definitions,
            List<ResearchDefinition> orderedDefinitions,
            long revision
    ) {
    }
}
