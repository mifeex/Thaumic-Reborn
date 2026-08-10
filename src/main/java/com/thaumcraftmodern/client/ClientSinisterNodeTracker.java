package com.thaumcraftmodern.client;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeClientLifecycle;
import com.thaumcraftmodern.aura.AuraNodeType;
import com.thaumcraftmodern.item.SinisterLodestoneVisibility;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Event-maintained client index used by the sinister lodestone property. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class ClientSinisterNodeTracker {
    private static final Map<Long, Set<BlockPos>> NODES_BY_CHUNK =
            new HashMap<>();
    private static ClientLevel indexedLevel;

    private ClientSinisterNodeTracker() {
    }

    /** Connects common aura-node lifecycle notifications to the client index. */
    public static void installLifecycleListener() {
        AuraNodeClientLifecycle.install(new AuraNodeClientLifecycle.Listener() {
            @Override
            public void changed(AuraNodeBlockEntity node) {
                track(node);
            }

            @Override
            public void removed(AuraNodeBlockEntity node) {
                untrack(node);
            }
        });
    }

    /** Hot item-property path: inspect only already indexed dark nodes. */
    public static boolean pointsAt(ClientLevel level, LivingEntity holder) {
        ensureLevel(level);
        Vec3 eye = holder.getEyePosition();
        Vec3 look = holder.getLookAngle().normalize();
        for (Set<BlockPos> chunkNodes : NODES_BY_CHUNK.values()) {
            for (BlockPos position : chunkNodes) {
                if (SinisterLodestoneVisibility.isVisibleTo(
                        eye,
                        look,
                        position
                )) {
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ClientLevel level)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        ensureLevel(level);
        Set<BlockPos> darkNodes = new HashSet<>();
        for (var blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof AuraNodeBlockEntity node
                    && node.snapshotState().type() == AuraNodeType.DARK) {
                darkNodes.add(node.getBlockPos().immutable());
            }
        }
        replaceChunk(chunk.getPos(), darkNodes);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() == indexedLevel) {
            NODES_BY_CHUNK.remove(event.getChunk().getPos().toLong());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() == indexedLevel) {
            clear();
        }
    }

    private static void track(AuraNodeBlockEntity node) {
        if (!(node.getLevel() instanceof ClientLevel level)) {
            return;
        }
        ensureLevel(level);
        BlockPos position = node.getBlockPos().immutable();
        long chunkKey = ChunkPos.asLong(position);
        if (node.snapshotState().type() == AuraNodeType.DARK) {
            NODES_BY_CHUNK.computeIfAbsent(
                    chunkKey,
                    ignored -> new HashSet<>()
            ).add(position);
        } else {
            remove(chunkKey, position);
        }
    }

    private static void untrack(AuraNodeBlockEntity node) {
        if (node.getLevel() != indexedLevel) {
            return;
        }
        BlockPos position = node.getBlockPos();
        remove(ChunkPos.asLong(position), position);
    }

    private static void replaceChunk(ChunkPos chunk, Set<BlockPos> nodes) {
        if (nodes.isEmpty()) {
            NODES_BY_CHUNK.remove(chunk.toLong());
        } else {
            NODES_BY_CHUNK.put(chunk.toLong(), nodes);
        }
    }

    private static void remove(long chunkKey, BlockPos position) {
        Set<BlockPos> nodes = NODES_BY_CHUNK.get(chunkKey);
        if (nodes == null) {
            return;
        }
        nodes.remove(position);
        if (nodes.isEmpty()) {
            NODES_BY_CHUNK.remove(chunkKey);
        }
    }

    private static void ensureLevel(ClientLevel level) {
        if (indexedLevel != level) {
            clear();
            indexedLevel = level;
        }
    }

    private static void clear() {
        NODES_BY_CHUNK.clear();
        indexedLevel = null;
    }
}
