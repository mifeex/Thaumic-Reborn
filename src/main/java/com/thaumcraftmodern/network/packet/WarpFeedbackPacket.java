package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WarpFeedbackPacket(
        byte type,
        int change,
        byte visual,
        String messageKey
) {
    public static final byte PERMANENT = 0;
    public static final byte NORMAL = 1;
    public static final byte TEMPORARY = 2;
    public static final byte VISUAL_NONE = 0;
    public static final byte VISUAL_EVENT = 1;
    public static final byte VISUAL_MIST = 2;

    public WarpFeedbackPacket(byte type, int change, byte visual) {
        this(type, change, visual, "");
    }

    public static void encode(WarpFeedbackPacket packet, FriendlyByteBuf buffer) {
        buffer.writeByte(packet.type);
        buffer.writeVarInt(packet.change);
        buffer.writeByte(packet.visual);
        buffer.writeUtf(packet.messageKey);
    }

    public static WarpFeedbackPacket decode(FriendlyByteBuf buffer) {
        return new WarpFeedbackPacket(
                buffer.readByte(),
                buffer.readVarInt(),
                buffer.readByte(),
                buffer.readUtf()
        );
    }

    public static void handle(
            WarpFeedbackPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleWarpFeedback(packet)
        );
        context.get().setPacketHandled(true);
    }
}
