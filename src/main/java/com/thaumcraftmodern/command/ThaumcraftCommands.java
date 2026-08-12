package com.thaumcraftmodern.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.research.ResearchCategoryRegistry;
import com.thaumcraftmodern.research.ResearchDefinition;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.worldgen.LegacyStructureKind;
import com.thaumcraftmodern.worldgen.LegacyStructureMarkerIndex;
import com.thaumcraftmodern.worldgen.LegacyStructureMarkerSearch;
import com.thaumcraftmodern.worldgen.LegacyVillageBuildingSearch;
import com.thaumcraftmodern.warp.WarpGearService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class ThaumcraftCommands {
    private static final int REQUIRED_PERMISSION_LEVEL = 2;

    private ThaumcraftCommands() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("thaumcraft")
                .requires(source -> source.hasPermission(REQUIRED_PERMISSION_LEVEL))
                .then(researchCommands())
                .then(aspectCommands())
                .then(warpCommands()));
        registerMarkerLocateOverrides(dispatcher);
    }

    /**
     * Literal children win over vanilla's generic structure argument. This
     * preserves the normal command spelling while replacing TC4 sites with
     * real-placement-backed results.
     */
    private static void registerMarkerLocateOverrides(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        LiteralArgumentBuilder<CommandSourceStack> structure =
                Commands.literal("structure");
        for (LegacyStructureKind kind : LegacyStructureKind.values()) {
            String id = ThaumcraftModern.MOD_ID + ":" + kind.serializedName();
            structure.then(Commands.literal(id)
                    .executes(context -> locateRealStructure(
                            context.getSource(),
                            kind
                    )));
            if (kind == LegacyStructureKind.BANKER_HOME) {
                structure.then(Commands.literal(
                                ThaumcraftModern.MOD_ID + ":banker_house"
                        )
                        .executes(context -> locateRealStructure(
                                context.getSource(),
                                kind
                        )));
            }
        }
        dispatcher.register(Commands.literal("locate")
                .requires(source -> source.hasPermission(
                        REQUIRED_PERMISSION_LEVEL
                ))
                .then(structure));
    }

    private static int locateRealStructure(
            CommandSourceStack source,
            LegacyStructureKind kind
    ) {
        BlockPos origin = BlockPos.containing(source.getPosition());
        var indexed = LegacyStructureMarkerIndex.get(source.getLevel())
                .nearest(kind, origin);
        if (indexed.isPresent()) {
            sendLocateSuccess(source, origin, kind, indexed.get());
            return 1;
        }
        source.sendSuccess(
                () -> Component.literal(
                        "Checking real " + kind.serializedName()
                                + (kind.isVillageBuilding()
                                        ? " village pieces..."
                                        : " block markers...")
                ),
                false
        );
        var search = kind.isVillageBuilding()
                ? LegacyVillageBuildingSearch.find(
                        source.getLevel(), origin, kind)
                : LegacyStructureMarkerSearch.find(
                        source.getLevel(), origin, kind);
        search
                .whenCompleteAsync((position, exception) -> {
                    if (exception != null) {
                        source.sendFailure(Component.literal(
                                "Failed to search real "
                                        + kind.serializedName() + ": "
                                        + exception.getMessage()
                        ));
                    } else if (position.isPresent()) {
                        sendLocateSuccess(
                                source,
                                origin,
                                kind,
                                position.get()
                        );
                    } else {
                        source.sendFailure(Component.literal(
                                "No generated " + kind.serializedName()
                                        + " passed its real-placement check"
                        ));
                    }
                }, source.getServer());
        return 1;
    }

    private static void sendLocateSuccess(
            CommandSourceStack source,
            BlockPos origin,
            LegacyStructureKind kind,
            BlockPos position
    ) {
        long dx = (long) position.getX() - origin.getX();
        long dz = (long) position.getZ() - origin.getZ();
        int distance = (int) Math.floor(Math.sqrt(dx * dx + dz * dz));
        Component coordinates = Component.literal(
                "[" + position.getX() + ", ~, " + position.getZ() + "]"
        ).withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/tp @s " + position.getX() + " ~ " + position.getZ()
                ))
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.literal("Click to prepare teleport")
                )));
        source.sendSuccess(
                () -> Component.literal(
                        "Nearest real " + kind.serializedName() + " is at "
                ).append(coordinates).append(
                        " (" + distance + " blocks away)"
                ),
                false
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> researchCommands() {
        return Commands.literal("research")
                .then(Commands.literal("all")
                        .executes(context -> grantAllResearch(
                                context.getSource(),
                                context.getSource().getPlayerOrException()
                        ))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> grantAllResearch(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")
                                ))))
                .then(Commands.literal("category")
                        .then(Commands.argument("category", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        ResearchCategoryRegistry.all().stream()
                                                .map(category -> category.id()),
                                        builder
                                ))
                                .executes(context -> grantCategoryResearch(
                                        context.getSource(),
                                        context.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(context, "category")
                                ))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> grantCategoryResearch(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "category")
                                        )))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> aspectCommands() {
        return Commands.literal("aspects")
                .then(Commands.literal("all")
                        .executes(context -> grantAllAspects(
                                context.getSource(),
                                context.getSource().getPlayerOrException()
                        ))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> grantAllAspects(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")
                                ))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> warpCommands() {
        return Commands.literal("warp")
                .executes(context -> showWarp(
                        context.getSource(),
                        context.getSource().getPlayerOrException()
                ))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> showWarp(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player")
                        )))
                .then(Commands.literal("add")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggest(
                                                new String[]{
                                                        "permanent",
                                                        "normal",
                                                        "temporary"
                                                },
                                                builder
                                        ))
                                .then(Commands.argument(
                                                "amount",
                                                IntegerArgumentType.integer(-10000, 10000)
                                        )
                                        .executes(context -> changeWarp(
                                                context.getSource(),
                                                context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "type"),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        ))
                                        .then(Commands.argument(
                                                        "player",
                                                        EntityArgument.player()
                                                )
                                                .executes(context -> changeWarp(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        StringArgumentType.getString(context, "type"),
                                                        IntegerArgumentType.getInteger(context, "amount")
                                                ))))));
    }

    private static int changeWarp(
            CommandSourceStack source,
            ServerPlayer player,
            String typeName,
            int amount
    ) {
        WarpType type;
        try {
            type = WarpType.valueOf(typeName.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal(
                    "Unknown warp type: " + typeName
                            + " (expected permanent, normal, or temporary)"
            ));
            return 0;
        }
        if (amount == 0) {
            source.sendFailure(Component.literal("Warp change cannot be zero"));
            return 0;
        }

        return KnowledgeAccess.get(player).map(knowledge -> {
            int before = knowledge.warp(type);
            int after;
            if (amount > 0) {
                after = knowledge.addWarp(type, amount);
            } else {
                after = knowledge.setWarp(type, Math.max(0, before + amount));
            }
            knowledge.setWarpCounter(knowledge.totalWarp());
            KnowledgeSync.send(player, "command:change_warp");
            source.sendSuccess(
                    () -> Component.literal(
                            "Changed " + typeName.toLowerCase(java.util.Locale.ROOT)
                                    + " warp for " + player.getGameProfile().getName()
                                    + ": " + before + " -> " + after
                                    + " (effective total="
                                    + (knowledge.totalWarp()
                                            + WarpGearService.equippedWarp(player))
                                    + ")"
                    ),
                    true
            );
            return 1;
        }).orElseGet(() -> {
            source.sendFailure(Component.literal(
                    "Thaumcraft knowledge is unavailable for "
                            + player.getGameProfile().getName()
            ));
            return 0;
        });
    }

    private static int showWarp(
            CommandSourceStack source,
            ServerPlayer player
    ) {
        return KnowledgeAccess.get(player).map(knowledge -> {
            int permanent = knowledge.warp(WarpType.PERMANENT);
            int normal = knowledge.warp(WarpType.NORMAL);
            int temporary = knowledge.warp(WarpType.TEMPORARY);
            int personal = knowledge.totalWarp();
            int equipment = WarpGearService.equippedWarp(player);
            int effective = personal + equipment;
            source.sendSuccess(
                    () -> Component.literal(
                            "Warp for " + player.getGameProfile().getName()
                                    + ": permanent=" + permanent
                                    + ", normal=" + normal
                                    + ", temporary=" + temporary
                                    + ", non-temporary=" + knowledge.nonTemporaryWarp()
                                    + ", personal total=" + personal
                                    + ", equipment=" + equipment
                                    + ", effective total=" + effective
                    ),
                    false
            );
            return 1;
        }).orElseGet(() -> {
            source.sendFailure(Component.literal(
                    "Thaumcraft knowledge is unavailable for "
                            + player.getGameProfile().getName()
            ));
            return 0;
        });
    }

    private static int grantAllResearch(CommandSourceStack source, ServerPlayer player) {
        return grantResearch(
                source,
                player,
                definition -> true,
                "all active research"
        );
    }

    private static int grantCategoryResearch(
            CommandSourceStack source,
            ServerPlayer player,
            String categoryId
    ) {
        if (ResearchCategoryRegistry.find(categoryId).isEmpty()) {
            source.sendFailure(Component.literal(
                    "Unknown Thaumcraft research category: " + categoryId
            ));
            return 0;
        }
        return grantResearch(
                source,
                player,
                definition -> definition.categoryId().equals(categoryId),
                "active research in category " + categoryId
        );
    }

    private static int grantResearch(
            CommandSourceStack source,
            ServerPlayer player,
            Predicate<ResearchDefinition> filter,
            String description
    ) {
        int changed = KnowledgeAccess.get(player).map(knowledge -> {
            int granted = 0;
            for (ResearchDefinition definition : ResearchRegistry.all()) {
                if (!definition.inactive()
                        && filter.test(definition)
                        && knowledge.completeResearch(definition.id())) {
                    granted++;
                }
            }
            return granted;
        }).orElse(0);

        KnowledgeSync.send(player, "command:grant_research");
        source.sendSuccess(
                () -> Component.literal(
                        "Granted " + changed + " new entries from " + description
                                + " to " + player.getGameProfile().getName()
                ),
                true
        );
        return changed;
    }

    private static int grantAllAspects(CommandSourceStack source, ServerPlayer player) {
        int changed = KnowledgeAccess.get(player).map(knowledge -> {
            int granted = 0;
            for (AspectDefinition aspect : AspectRegistryRuntime.catalog().definitions()) {
                if (knowledge.learnAspect(aspect.id())) {
                    granted++;
                }
            }
            return granted;
        }).orElse(0);

        KnowledgeSync.send(player, "command:grant_aspects");
        source.sendSuccess(
                () -> Component.literal(
                        "Discovered " + changed + " new aspects for "
                                + player.getGameProfile().getName()
                ),
                true
        );
        return changed;
    }
}
