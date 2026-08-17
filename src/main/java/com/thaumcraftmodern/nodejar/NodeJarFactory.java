package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeFactory;
import com.thaumcraftmodern.aura.AuraNodeState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * Shared construction path for survival and ready-to-test creative jars.
 */
public final class NodeJarFactory {
    private static final UUID CREATIVE_PAYLOAD_ID = UUID.nameUUIDFromBytes(
            "thaumic_reborn:creative_node_jar_payload:v1"
                    .getBytes(StandardCharsets.UTF_8)
    );

    private NodeJarFactory() {
    }

    public static NodeJarData captured(UUID payloadId, AuraNodeState node) {
        return new NodeJarData(
                Objects.requireNonNull(payloadId, "payloadId"),
                NodeJarData.Origin.SURVIVAL,
                node
        );
    }

    public static NodeJarData deterministicCreativeData() {
        return new NodeJarData(
                CREATIVE_PAYLOAD_ID,
                NodeJarData.Origin.CREATIVE_TEMPLATE,
                AuraNodeFactory.deterministicCreativeNode()
        );
    }

    public static ItemStack deterministicCreativeStack(Item jarItem) {
        ItemStack stack = new ItemStack(Objects.requireNonNull(jarItem, "jarItem"));
        NodeJarCodec.write(stack, deterministicCreativeData());
        return stack;
    }
}
