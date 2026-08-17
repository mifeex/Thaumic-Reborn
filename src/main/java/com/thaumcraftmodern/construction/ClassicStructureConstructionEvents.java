package com.thaumcraftmodern.construction;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.block.ClassicPartBlock;
import com.thaumcraftmodern.world.block.AdvancedAlchemicalFurnaceBlock;
import com.thaumcraftmodern.world.block.entity.AlchemicalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.entity.AdvancedAlchemicalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.InfusionPillarBlock;
import com.thaumcraftmodern.world.block.InfernalFurnaceBlock;
import com.thaumcraftmodern.world.block.RunicMatrixBlock;
import com.thaumcraftmodern.world.block.ThaumatoriumBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Faithful modern adapter for TC4 WandManager compound block triggers.
 *
 * <p>The old research recipe arrays describe what the Thaumonomicon renders,
 * while WandManager contains the authoritative fit/replace logic. These
 * handlers intentionally follow WandManager rather than treating the display
 * arrays as crafting recipes.</p>
 */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class ClassicStructureConstructionEvents {
    private ClassicStructureConstructionEvents() {
    }

    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !WandVisService.isWand(event.getItemStack())) {
            return;
        }

        ConstructionResult result = tryConstruct(
                level,
                player,
                event.getItemStack(),
                event.getPos(),
                event.getFace()
        );
        if (result == ConstructionResult.NO_MATCH) {
            return;
        }
        if (result == ConstructionResult.MISSING_RESEARCH) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.thaumic_reborn.structure.missing_research"
                    ),
                    true
            );
            return;
        }
        if (result == ConstructionResult.INSUFFICIENT_VIS) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.thaumic_reborn.structure.insufficient_vis"
                    ),
                    true
            );
            return;
        }
        if (result == ConstructionResult.FAILED) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.thaumic_reborn.structure.failed"
                    ),
                    true
            );
            return;
        }

        level.playSound(
                null,
                event.getPos(),
                ModSounds.WAND.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
        player.swing(event.getHand(), true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }

    public static ConstructionResult tryConstruct(
            ServerLevel level,
            ServerPlayer player,
            net.minecraft.world.item.ItemStack wand,
            BlockPos clicked,
        Direction clickedFace
    ) {
        ConstructionDefinition crucible = wandDefinition(
                ConstructionDefinition.Handler.CRUCIBLE
        );
        if (crucible != null
                && level.getBlockState(clicked).is(Blocks.CAULDRON)) {
            return construct(
                    ConstructionDefinition.Handler.CRUCIBLE,
                    level, player, wand,
                    List.of(new Change(
                            clicked,
                            ModBlocks.CRUCIBLE.get().defaultBlockState()
                    ))
            );
        }

        BlockPos infusionAnchor = findInfusionAnchor(level, clicked);
        if (infusionAnchor != null) {
            return construct(
                    ConstructionDefinition.Handler.INFUSION_ALTAR,
                    level, player, wand, infusionChanges(infusionAnchor)
            );
        }

        BlockPos infernalAnchor = findInfernalAnchor(level, clicked);
        if (infernalAnchor != null) {
            return construct(
                    ConstructionDefinition.Handler.INFERNAL_FURNACE,
                    level, player, wand,
                    infernalChanges(level, infernalAnchor)
            );
        }

        BlockPos thaumatoriumBase = findThaumatoriumBase(level, clicked);
        if (thaumatoriumBase != null) {
            Direction facing = clickedFace.getAxis().isHorizontal()
                    ? clickedFace
                    : player.getDirection().getOpposite();
            return construct(
                    ConstructionDefinition.Handler.THAUMATORIUM,
                    level, player, wand,
                    thaumatoriumChanges(thaumatoriumBase, facing)
            );
        }

        BlockPos advancedCenter = findAdvancedFurnaceCenter(level, clicked);
        if (advancedCenter != null) {
            Map<String, Integer> stored = level.getBlockEntity(advancedCenter)
                    instanceof AlchemicalFurnaceBlockEntity furnace
                    ? Map.copyOf(furnace.essentia()) : Map.of();
            ConstructionResult result = construct(
                    ConstructionDefinition.Handler
                            .ADVANCED_ALCHEMICAL_FURNACE,
                    level, player, wand,
                    advancedFurnaceChanges(advancedCenter)
            );
            if (result == ConstructionResult.CONSTRUCTED
                    && level.getBlockEntity(advancedCenter)
                    instanceof AdvancedAlchemicalFurnaceBlockEntity furnace) {
                furnace.importEssentia(stored);
            }
            return result;
        }
        return ConstructionResult.NO_MATCH;
    }

    private static ConstructionResult construct(
            ConstructionDefinition.Handler handler,
            ServerLevel level,
            ServerPlayer player,
            net.minecraft.world.item.ItemStack wand,
            List<Change> changes
    ) {
        ConstructionDefinition definition = wandDefinition(handler);
        if (definition == null) {
            return ConstructionResult.NO_MATCH;
        }
        if (!definition.research().isBlank()
                && !hasResearch(player, definition.research())) {
            return ConstructionResult.MISSING_RESEARCH;
        }
        if (definition.vis().isEmpty()) {
            return applyWithoutVis(level, changes);
        }
        return applyWithVis(
                level,
                player,
                wand,
                definition.vis(),
                changes
        );
    }

    private static ConstructionDefinition wandDefinition(
            ConstructionDefinition.Handler handler
    ) {
        return ConstructionRegistry.find(handler)
                .filter(definition -> definition.trigger().type()
                        == ConstructionDefinition.TriggerType.WAND)
                .orElse(null);
    }

    static BlockPos findInfusionAnchor(ServerLevel level, BlockPos clicked) {
        for (int x = clicked.getX() - 2; x <= clicked.getX(); x++) {
            for (int y = clicked.getY() - 2; y <= clicked.getY(); y++) {
                for (int z = clicked.getZ() - 2; z <= clicked.getZ(); z++) {
                    BlockPos anchor = new BlockPos(x, y, z);
                    if (fitsInfusionAltar(level, anchor)) {
                        return anchor;
                    }
                }
            }
        }
        return null;
    }

    static boolean fitsInfusionAltar(ServerLevel level, BlockPos anchor) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos position = anchor.offset(x, y, z);
                    if (!level.hasChunkAt(position)) {
                        return false;
                    }
                    boolean corner = x != 1 && z != 1;
                    BlockState state = level.getBlockState(position);
                    boolean matches;
                    if (y == 2 && x == 1 && z == 1) {
                        matches = state.is(ModBlocks.RUNIC_MATRIX.get())
                                && !state.getValue(RunicMatrixBlock.ACTIVE);
                    } else if (y == 1 && corner) {
                        matches = state.is(ModBlocks.ARCANE_STONE.get());
                    } else if (y == 0 && corner) {
                        matches = state.is(ModBlocks.ARCANE_STONE_BRICK.get());
                    } else if (y == 0 && x == 1 && z == 1) {
                        matches = state.is(ModBlocks.ARCANE_PEDESTAL.get());
                    } else {
                        matches = state.isAir();
                    }
                    if (!matches) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static List<Change> infusionChanges(BlockPos anchor) {
        List<Change> changes = new ArrayList<>(9);
        changes.add(new Change(
                anchor.offset(1, 2, 1),
                ModBlocks.RUNIC_MATRIX.get().defaultBlockState()
                        .setValue(RunicMatrixBlock.ACTIVE, true)
        ));
        for (int x : new int[]{0, 2}) {
            for (int z : new int[]{0, 2}) {
                Direction facing = facingTowardCenter(x, z);
                changes.add(new Change(
                        anchor.offset(x, 1, z),
                        ModBlocks.INFUSION_PILLAR.get().defaultBlockState()
                                .setValue(InfusionPillarBlock.FACING, facing)
                                .setValue(InfusionPillarBlock.CAP, true)
                ));
                changes.add(new Change(
                        anchor.offset(x, 0, z),
                        ModBlocks.INFUSION_PILLAR.get().defaultBlockState()
                                .setValue(InfusionPillarBlock.FACING, facing)
                                .setValue(InfusionPillarBlock.CAP, false)
                ));
            }
        }
        return changes;
    }

    static BlockPos findInfernalAnchor(ServerLevel level, BlockPos clicked) {
        for (int x = clicked.getX() - 2; x <= clicked.getX(); x++) {
            for (int y = clicked.getY() - 2; y <= clicked.getY(); y++) {
                for (int z = clicked.getZ() - 2; z <= clicked.getZ(); z++) {
                    BlockPos anchor = new BlockPos(x, y, z);
                    if (fitsInfernalFurnace(level, anchor)) {
                        return anchor;
                    }
                }
            }
        }
        return null;
    }

    static boolean fitsInfernalFurnace(ServerLevel level, BlockPos anchor) {
        int bars = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos position = anchor.offset(x, y, z);
                    if (!level.hasChunkAt(position)) {
                        return false;
                    }
                    BlockState state = level.getBlockState(position);
                    boolean corner = x != 1 && z != 1;
                    if (y == 2 && x == 1 && z == 1) {
                        if (!state.isAir()) {
                            return false;
                        }
                    } else if (y == 1 && x == 1 && z == 1) {
                        if (!state.is(Blocks.LAVA)) {
                            return false;
                        }
                    } else if (corner) {
                        if (!state.is(Blocks.NETHER_BRICKS)) {
                            return false;
                        }
                    } else if (y == 1 && isSideCenter(x, z)
                            && state.is(Blocks.IRON_BARS)) {
                        bars++;
                    } else if (!state.is(Blocks.OBSIDIAN)) {
                        return false;
                    }
                }
            }
        }
        return bars == 1;
    }

    private static List<Change> infernalChanges(
            ServerLevel level,
            BlockPos anchor
    ) {
        List<Change> changes = new ArrayList<>(26);
        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    BlockPos position = anchor.offset(x, y, z);
                    BlockState source = level.getBlockState(position);
                    if (source.isAir()) {
                        continue;
                    }
                    int part = source.is(Blocks.LAVA)
                            ? 0
                            : source.is(Blocks.IRON_BARS)
                            ? 10
                            : x + z * 3 + 1;
                    changes.add(new Change(
                            position,
                            ((InfernalFurnaceBlock) ModBlocks.INFERNAL_FURNACE.get())
                                    .stateForPart(part)
                    ));
                }
            }
        }
        return changes;
    }

    static BlockPos findThaumatoriumBase(
            ServerLevel level,
            BlockPos clicked
    ) {
        for (int dy = -2; dy <= 0; dy++) {
            BlockPos base = clicked.offset(0, dy, 0);
            if (level.hasChunkAt(base)
                    && level.hasChunkAt(base.above(2))
                    && level.getBlockState(base)
                    .is(ModBlocks.ALCHEMICAL_FURNACE.get())
                    && level.getBlockState(base.above())
                    .is(ModBlocks.ALCHEMICAL_CONSTRUCT.get())
                    && level.getBlockState(base.above(2))
                    .is(ModBlocks.ALCHEMICAL_CONSTRUCT.get())) {
                return base;
            }
        }
        return null;
    }

    private static List<Change> thaumatoriumChanges(
            BlockPos furnaceBase,
            Direction facing
    ) {
        BlockPos lower = furnaceBase.above();
        return List.of(
                new Change(
                        lower,
                        ModBlocks.THAUMATORIUM.get().defaultBlockState()
                                .setValue(ThaumatoriumBlock.FACING, facing)
                                .setValue(
                                        ThaumatoriumBlock.HALF,
                                        DoubleBlockHalf.LOWER
                                )
                ),
                new Change(
                        lower.above(),
                        ModBlocks.THAUMATORIUM.get().defaultBlockState()
                                .setValue(ThaumatoriumBlock.FACING, facing)
                                .setValue(
                                        ThaumatoriumBlock.HALF,
                                        DoubleBlockHalf.UPPER
                                )
                )
        );
    }

    static BlockPos findAdvancedFurnaceCenter(
            ServerLevel level,
            BlockPos clicked
    ) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos center = clicked.offset(x, y, z);
                    if (fitsAdvancedFurnace(level, center)) {
                        return center;
                    }
                }
            }
        }
        return null;
    }

    static boolean fitsAdvancedFurnace(
            ServerLevel level,
            BlockPos center
    ) {
        for (int y = 0; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos position = center.offset(x, y, z);
                    if (!level.hasChunkAt(position)) {
                        return false;
                    }
                    BlockState state = level.getBlockState(position);
                    boolean corner = x != 0 && z != 0;
                    boolean matches;
                    if (y == 0 && x == 0 && z == 0) {
                        matches = state.is(ModBlocks.ALCHEMICAL_FURNACE.get());
                    } else if (y == 0) {
                        matches = state.is(
                                ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()
                        );
                    } else if (x == 0 && z == 0) {
                        matches = state.isAir();
                    } else if (corner) {
                        matches = state.is(ModBlocks.ARCANE_ALEMBIC.get());
                    } else {
                        matches = state.is(
                                ModBlocks.ALCHEMICAL_CONSTRUCT.get()
                        );
                    }
                    if (!matches) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static List<Change> advancedFurnaceChanges(BlockPos center) {
        List<Change> changes = new ArrayList<>(17);
        AdvancedAlchemicalFurnaceBlock output =
                (AdvancedAlchemicalFurnaceBlock) ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get();
        for (int y = 0; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (y == 1 && x == 0 && z == 0) {
                        continue;
                    }
                    boolean corner = x != 0 && z != 0;
                    int part;
                    if (y == 0 && x == 0 && z == 0) {
                        part = 0;
                    } else if (y == 0) {
                        part = corner ? 4 : 1;
                    } else {
                        part = corner ? 2 : 3;
                    }
                    changes.add(new Change(
                            center.offset(x, y, z),
                            output.stateForPart(part)
                    ));
                }
            }
        }
        return changes;
    }

    private static ConstructionResult applyWithoutVis(
            ServerLevel level,
            List<Change> changes
    ) {
        return applyChanges(level, changes)
                ? ConstructionResult.CONSTRUCTED
                : ConstructionResult.FAILED;
    }

    private static ConstructionResult applyWithVis(
            ServerLevel level,
            ServerPlayer player,
            net.minecraft.world.item.ItemStack wand,
            Map<String, Integer> cost,
            List<Change> changes
    ) {
        if (!WandVisService.canConsume(player, wand, cost)) {
            return ConstructionResult.INSUFFICIENT_VIS;
        }
        CompoundTag originalTag = wand.getTag() == null
                ? null
                : wand.getTag().copy();
        if (!WandVisService.consume(player, wand, cost)) {
            return ConstructionResult.INSUFFICIENT_VIS;
        }
        if (applyChanges(level, changes)) {
            return ConstructionResult.CONSTRUCTED;
        }
        wand.setTag(originalTag == null ? null : originalTag.copy());
        player.getInventory().setChanged();
        return ConstructionResult.FAILED;
    }

    private static boolean applyChanges(
            ServerLevel level,
            List<Change> changes
    ) {
        LinkedHashMap<BlockPos, BlockState> original = new LinkedHashMap<>();
        for (Change change : changes) {
            if (!level.hasChunkAt(change.position())) {
                return false;
            }
            original.put(
                    change.position().immutable(),
                    level.getBlockState(change.position())
            );
        }
        List<BlockPos> applied = new ArrayList<>();
        for (Change change : changes) {
            if (!level.setBlock(change.position(), change.result(), 3)) {
                rollback(level, original, applied);
                return false;
            }
            applied.add(change.position());
            level.levelEvent(
                    null,
                    2001,
                    change.position(),
                    net.minecraft.world.level.block.Block.getId(
                            original.get(change.position())
                    )
            );
        }
        return true;
    }

    private static void rollback(
            ServerLevel level,
            Map<BlockPos, BlockState> original,
            List<BlockPos> applied
    ) {
        for (int index = applied.size() - 1; index >= 0; index--) {
            BlockPos position = applied.get(index);
            level.setBlock(position, original.get(position), 3);
        }
    }

    private static boolean hasResearch(
            ServerPlayer player,
            String researchId
    ) {
        return KnowledgeAccess.get(player)
                .map(knowledge -> knowledge.hasCompletedResearch(researchId))
                .orElse(false);
    }

    private static Direction facingTowardCenter(int x, int z) {
        if (x == 0) {
            return z == 0 ? Direction.SOUTH : Direction.EAST;
        }
        return z == 0 ? Direction.WEST : Direction.NORTH;
    }

    private static boolean isSideCenter(int x, int z) {
        return (x == 1) != (z == 1);
    }

    private record Change(BlockPos position, BlockState result) {
        private Change {
            position = position.immutable();
        }
    }

    public enum ConstructionResult {
        NO_MATCH,
        MISSING_RESEARCH,
        INSUFFICIENT_VIS,
        CONSTRUCTED,
        FAILED
    }
}
