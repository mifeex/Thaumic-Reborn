package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.registry.ModBlocks;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Detects actual TC4 structure blocks whenever a full chunk is loaded. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class LegacyStructureMarkerDetector {
    private static final String WISP_ENTITY_ID = "thaumic_reborn:wisp";

    private LegacyStructureMarkerDetector() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        refresh(event.getLevel(), event.getChunk());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        refresh(event.getLevel(), event.getChunk());
    }

    private static void refresh(
            LevelAccessor accessor,
            net.minecraft.world.level.chunk.ChunkAccess chunkAccess
    ) {
        if (!(accessor instanceof ServerLevel level)
                || !(chunkAccess instanceof LevelChunk chunk)) {
            return;
        }
        if (!level.getServer().isSameThread()) {
            level.getServer().execute(() -> refresh(level, chunk));
            return;
        }
        Map<LegacyStructureKind, List<BlockPos>> found =
                new EnumMap<>(LegacyStructureKind.class);
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            LegacyStructureKind kind = classify(chunk, blockEntity);
            if (kind != null) {
                found.computeIfAbsent(kind, ignored -> new ArrayList<>())
                        .add(blockEntity.getBlockPos().immutable());
            }
        }
        LegacyStructureMarkerIndex.get(level).replaceChunk(
                chunk.getPos(),
                found
        );
    }

    private static LegacyStructureKind classify(
            LevelChunk chunk,
            BlockEntity blockEntity
    ) {
        if (!(blockEntity instanceof AuraNodeBlockEntity node)) {
            return null;
        }
        BlockPos position = node.getBlockPos();

        // The mound node owns the persistent guardian-spawn flag; ordinary
        // dark nodes can never acquire it.
        if (node.isMoundGuardianSpawner()) {
            return LegacyStructureKind.ANCIENT_MOUND;
        }

        // A totem uses its own solid node block and a vertical run of at least
        // one obsidian-totem block ending on an obsidian tile.
        if (node.getBlockState().is(ModBlocks.OBSIDIAN_TOTEM_NODE.get())
                && hasTotemColumn(chunk, position)) {
            return LegacyStructureKind.AURA_TOTEM;
        }

        // The eldritch obelisk sandwiches its dark node between altar parts.
        if (chunk.getBlockState(position.below())
                        .is(ModBlocks.ELDRITCH_ALTAR_PART.get())
                && chunk.getBlockState(position.above())
                        .is(ModBlocks.ELDRITCH_ALTAR_PART.get())) {
            return LegacyStructureKind.ELDRITCH_RING;
        }

        // Hilltop stones have the dark node exactly six blocks above their
        // central wisp spawner.
        BlockEntity below = chunk.getBlockEntity(position.below(6));
        if (below instanceof SpawnerBlockEntity spawner
                && isWispSpawner(spawner)) {
            return LegacyStructureKind.HILLTOP_STONES;
        }
        return null;
    }

    /** Returns this chunk's first marker of the requested structure kind. */
    public static Optional<BlockPos> findMarker(
            ServerLevel level,
            LevelChunk chunk,
            LegacyStructureKind requestedKind
    ) {
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (classify(chunk, blockEntity) == requestedKind) {
                return Optional.of(blockEntity.getBlockPos().immutable());
            }
        }
        return Optional.empty();
    }

    private static boolean hasTotemColumn(
            LevelChunk chunk,
            BlockPos nodePosition
    ) {
        boolean foundTotem = false;
        for (int depth = 1; depth <= AuraTotemGeneration.MAX_NODE_HEIGHT;
                depth++) {
            var state = chunk.getBlockState(nodePosition.below(depth));
            if (state.is(ModBlocks.OBSIDIAN_TOTEM.get())) {
                foundTotem = true;
                continue;
            }
            return foundTotem && state.is(ModBlocks.OBSIDIAN_TILE.get());
        }
        return false;
    }

    private static boolean isWispSpawner(SpawnerBlockEntity spawner) {
        CompoundTag spawnData = spawner.saveWithoutMetadata()
                .getCompound("SpawnData");
        return WISP_ENTITY_ID.equals(
                spawnData.getCompound("entity").getString("id")
        );
    }
}
