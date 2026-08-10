package com.thaumcraftmodern.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KnowledgeSyncPacketCompressionTest {
    @Test
    void largeRegistryPayloadRoundTripsBelowVanillaNbtReadLimit() {
        CompoundTag scans = new CompoundTag();
        String repeated = "thaumcraftmodern:generated_recipe_scan/"
                + "abcdefghijklmnopqrstuvwxyz0123456789".repeat(32);
        for (int index = 0; index < 20_000; index++) {
            scans.putString("scan_" + index, repeated);
        }
        assertTrue(scans.toString().length() > 2 * 1024 * 1024);

        KnowledgeSyncPacket packet = new KnowledgeSyncPacket(
                new CompoundTag(), new CompoundTag(), new CompoundTag(),
                new CompoundTag(), scans, new CompoundTag()
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        KnowledgeSyncPacket.encode(packet, buffer);
        assertTrue(buffer.readableBytes() < 2 * 1024 * 1024);

        KnowledgeSyncPacket decoded = KnowledgeSyncPacket.decode(buffer);
        assertEquals(scans, decoded.scans());
    }
}
