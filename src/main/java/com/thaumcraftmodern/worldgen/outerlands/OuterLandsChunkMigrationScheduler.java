package com.thaumcraftmodern.worldgen.outerlands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;

/** Defers load migrations until the chunk's FULL future has completed. */
final class OuterLandsChunkMigrationScheduler {
    private OuterLandsChunkMigrationScheduler() {
    }

    static void nextTick(MinecraftServer server, Runnable migration) {
        /*
         * Minecraft fires ChunkEvent.Load while protoChunkToFullChunk is still
         * completing. MinecraftServer#execute may run re-entrantly from
         * ServerChunkCache#getChunk's managedBlock loop, so a migration that
         * reads through ServerLevel can wait on the very FULL future currently
         * invoking it. A future-tick TickTask cannot run in that loop.
         */
        server.tell(new TickTask(server.getTickCount() + 1, migration));
    }
}
