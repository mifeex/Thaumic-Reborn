package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

/** Server-authoritative research-table text routed away from chat/action bar. */
public record ResearchTableFeedbackPacket(Component message, boolean success) {
    public ResearchTableFeedbackPacket {
        message = Objects.requireNonNull(message, "message");
    }

    public static void encode(
            ResearchTableFeedbackPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeUtf(Component.Serializer.toJson(packet.message));
        buffer.writeBoolean(packet.success);
    }

    public static ResearchTableFeedbackPacket decode(FriendlyByteBuf buffer) {
        Component message = Component.Serializer.fromJson(buffer.readUtf());
        if (message == null) {
            message = Component.empty();
        }
        return new ResearchTableFeedbackPacket(message, buffer.readBoolean());
    }

    public static void handle(
            ResearchTableFeedbackPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleResearchTableFeedback(packet)
        );
        context.get().setPacketHandled(true);
    }
}
