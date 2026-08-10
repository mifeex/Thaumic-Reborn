package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

public record KnowledgeSyncPacket(
        CompoundTag knowledge,
        CompoundTag aspects,
        CompoundTag research,
        CompoundTag researchCategories,
        CompoundTag scans,
        CompoundTag wands
) {
    private static final int MAX_COMPRESSED_BYTES = 2 * 1024 * 1024;

    public static void encode(KnowledgeSyncPacket packet, FriendlyByteBuf buffer) {
        CompoundTag root = new CompoundTag();
        root.put("knowledge", packet.knowledge);
        root.put("aspects", packet.aspects);
        root.put("research", packet.research);
        root.put("research_categories", packet.researchCategories);
        root.put("scans", packet.scans);
        root.put("wands", packet.wands);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NbtIo.writeCompressed(root, output);
            byte[] compressed = output.toByteArray();
            if (compressed.length > MAX_COMPRESSED_BYTES) {
                throw new IllegalStateException(
                        "Compressed knowledge sync exceeds "
                                + MAX_COMPRESSED_BYTES + " bytes: "
                                + compressed.length
                );
            }
            buffer.writeByteArray(compressed);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to compress knowledge sync", exception
            );
        }
    }

    public static KnowledgeSyncPacket decode(FriendlyByteBuf buffer) {
        byte[] compressed = buffer.readByteArray(MAX_COMPRESSED_BYTES);
        try {
            CompoundTag root = NbtIo.readCompressed(
                    new ByteArrayInputStream(compressed)
            );
            return new KnowledgeSyncPacket(
                    root.getCompound("knowledge"),
                    root.getCompound("aspects"),
                    root.getCompound("research"),
                    root.getCompound("research_categories"),
                    root.getCompound("scans"),
                    root.getCompound("wands")
            );
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Unable to decompress knowledge sync", exception
            );
        }
    }

    public static void handle(KnowledgeSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleKnowledgeSync(packet));
        context.get().setPacketHandled(true);
    }
}
