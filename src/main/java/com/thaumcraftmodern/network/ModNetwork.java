package com.thaumcraftmodern.network;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.network.packet.KnowledgeSyncPacket;
import com.thaumcraftmodern.network.packet.NodeZapPacket;
import com.thaumcraftmodern.network.packet.OpenThaumonomiconPacket;
import com.thaumcraftmodern.network.packet.PurchaseResearchPacket;
import com.thaumcraftmodern.network.packet.RequestResearchNotesPacket;
import com.thaumcraftmodern.network.packet.ResearchTableFeedbackPacket;
import com.thaumcraftmodern.network.packet.ScanFeedbackPacket;
import com.thaumcraftmodern.network.packet.ThaumatoriumEssentiaSyncPacket;
import com.thaumcraftmodern.network.packet.ThaumatoriumRecipeSyncPacket;
import com.thaumcraftmodern.network.packet.GolemBellSyncPacket;
import com.thaumcraftmodern.network.packet.WarpFeedbackPacket;
import com.thaumcraftmodern.network.packet.WispZapPacket;
import com.thaumcraftmodern.network.packet.CycleShovelOrientationPacket;
import com.thaumcraftmodern.network.packet.ElementalDowsingPacket;
import com.thaumcraftmodern.network.packet.ChangeWandFocusPacket;
import com.thaumcraftmodern.network.packet.RunicShieldSyncPacket;
import com.thaumcraftmodern.network.packet.RunicShieldFxPacket;
import com.thaumcraftmodern.network.packet.InventoryScanPacket;
import com.thaumcraftmodern.network.packet.AuraNodeStateSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "22";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );
    private static boolean registered;

    private ModNetwork() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        int id = 0;
        CHANNEL.messageBuilder(KnowledgeSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(KnowledgeSyncPacket::encode)
                .decoder(KnowledgeSyncPacket::decode)
                .consumerMainThread(KnowledgeSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(ScanFeedbackPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ScanFeedbackPacket::encode)
                .decoder(ScanFeedbackPacket::decode)
                .consumerMainThread(ScanFeedbackPacket::handle)
                .add();
        CHANNEL.messageBuilder(WarpFeedbackPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(WarpFeedbackPacket::encode)
                .decoder(WarpFeedbackPacket::decode)
                .consumerMainThread(WarpFeedbackPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenThaumonomiconPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenThaumonomiconPacket::encode)
                .decoder(OpenThaumonomiconPacket::decode)
                .consumerMainThread(OpenThaumonomiconPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        WispZapPacket.class,
                        id++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(WispZapPacket::encode)
                .decoder(WispZapPacket::decode)
                .consumerMainThread(WispZapPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        NodeZapPacket.class,
                        id++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(NodeZapPacket::encode)
                .decoder(NodeZapPacket::decode)
                .consumerMainThread(NodeZapPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        RequestResearchNotesPacket.class,
                        id++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(RequestResearchNotesPacket::encode)
                .decoder(RequestResearchNotesPacket::decode)
                .consumerMainThread(RequestResearchNotesPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        PurchaseResearchPacket.class,
                        id++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(PurchaseResearchPacket::encode)
                .decoder(PurchaseResearchPacket::decode)
                .consumerMainThread(PurchaseResearchPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        ThaumatoriumEssentiaSyncPacket.class,
                        id++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(ThaumatoriumEssentiaSyncPacket::encode)
                .decoder(ThaumatoriumEssentiaSyncPacket::decode)
                .consumerMainThread(ThaumatoriumEssentiaSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        CycleShovelOrientationPacket.class,
                        id++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(CycleShovelOrientationPacket::encode)
                .decoder(CycleShovelOrientationPacket::decode)
                .consumerMainThread(CycleShovelOrientationPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        ElementalDowsingPacket.class,
                        id++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(ElementalDowsingPacket::encode)
                .decoder(ElementalDowsingPacket::decode)
                .consumerMainThread(ElementalDowsingPacket::handle)
                .add();
        CHANNEL.messageBuilder(ChangeWandFocusPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChangeWandFocusPacket::encode)
                .decoder(ChangeWandFocusPacket::decode)
                .consumerMainThread(ChangeWandFocusPacket::handle)
                .add();
        CHANNEL.messageBuilder(RunicShieldSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RunicShieldSyncPacket::encode).decoder(RunicShieldSyncPacket::decode)
                .consumerMainThread(RunicShieldSyncPacket::handle).add();
        CHANNEL.messageBuilder(RunicShieldFxPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RunicShieldFxPacket::encode).decoder(RunicShieldFxPacket::decode)
                .consumerMainThread(RunicShieldFxPacket::handle).add();
        CHANNEL.messageBuilder(InventoryScanPacket.class, id++,
                        NetworkDirection.PLAY_TO_SERVER)
                .encoder(InventoryScanPacket::encode)
                .decoder(InventoryScanPacket::decode)
                .consumerMainThread(InventoryScanPacket::handle).add();
        CHANNEL.messageBuilder(ThaumatoriumRecipeSyncPacket.class, id++,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ThaumatoriumRecipeSyncPacket::encode)
                .decoder(ThaumatoriumRecipeSyncPacket::decode)
                .consumerMainThread(ThaumatoriumRecipeSyncPacket::handle).add();
        CHANNEL.messageBuilder(GolemBellSyncPacket.class, id++,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(GolemBellSyncPacket::encode)
                .decoder(GolemBellSyncPacket::decode)
                .consumerMainThread(GolemBellSyncPacket::handle).add();
        CHANNEL.messageBuilder(AuraNodeStateSyncPacket.class, id++,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AuraNodeStateSyncPacket::encode)
                .decoder(AuraNodeStateSyncPacket::decode)
                .consumerMainThread(AuraNodeStateSyncPacket::handle).add();
        CHANNEL.messageBuilder(ResearchTableFeedbackPacket.class, id,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ResearchTableFeedbackPacket::encode)
                .decoder(ResearchTableFeedbackPacket::decode)
                .consumerMainThread(ResearchTableFeedbackPacket::handle).add();
    }

    public static void sendTo(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToTracking(Entity entity, Object packet) {
        CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                packet
        );
    }

    public static void sendToTrackingChunk(
            ServerLevel level,
            BlockPos position,
            Object packet
    ) {
        CHANNEL.send(
                PacketDistributor.TRACKING_CHUNK.with(
                        () -> level.getChunkAt(position)
                ),
                packet
        );
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
