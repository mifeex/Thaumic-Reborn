package com.thaumcraftmodern.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thaumcraftmodern.aura.AuraNodeFactory;
import com.thaumcraftmodern.aura.AuraNodeScanResult;
import com.thaumcraftmodern.network.packet.ScanFeedbackPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class ScanFeedbackPacketTest {
    @Test
    void nodeParametersRoundTripThroughThaumometerResultPacket() {
        AuraNodeScanResult scan = AuraNodeScanResult.from(
                AuraNodeFactory.deterministicCreativeNode().snapshot()
        );
        ScanFeedbackPacket original = new ScanFeedbackPacket(
                true,
                "message.thaumic_reborn.scan.success",
                "block.thaumic_reborn.aura_node",
                List.of(new ScanFeedbackPacket.AspectGain("aer", 10, 10, false)),
                Optional.of(ScanFeedbackPacket.NodeData.from(scan))
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        ScanFeedbackPacket.encode(original, buffer);
        ScanFeedbackPacket decoded = ScanFeedbackPacket.decode(buffer);

        assertEquals(original, decoded);
        assertTrue(decoded.node().isPresent());
        assertEquals(6, decoded.node().orElseThrow().aspects().size());
        assertEquals(100, decoded.node().orElseThrow().aspects().get(0).current());
        assertEquals(100, decoded.node().orElseThrow().aspects().get(0).maximum());
    }
}
