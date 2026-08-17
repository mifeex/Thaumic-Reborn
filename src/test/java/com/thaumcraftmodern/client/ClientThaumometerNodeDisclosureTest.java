package com.thaumcraftmodern.client;

import com.thaumcraftmodern.aura.AuraNodeFactory;
import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.AuraNodeType;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClientThaumometerNodeDisclosureTest {
    @Test
    void thaumometerDoesNotDiscloseAnUnstudiedNode() {
        assertFalse(ClientThaumometerTarget.discloseNodeAspects(false));
        assertTrue(ClientThaumometerTarget.discloseNodeAspects(true));
    }

    @Test
    void nodeTypeAndModifierFollowTheClassicStudiedOnlyRule() {
        AuraNodeState.Snapshot node = AuraNodeFactory.typed(
                UUID.fromString("19357725-f98c-49c2-9bcf-eb3c4230e030"),
                AuraNodeType.DARK,
                AuraNodeModifier.PALE,
                50
        ).snapshot();

        assertEquals(
                "",
                ClientThaumometerTarget.nodeDescription(node, false).getString()
        );
        Component description =
                ClientThaumometerTarget.nodeDescription(node, true);
        assertEquals(
                "node_type.thaumic_reborn.dark, "
                        + "node_modifier.thaumic_reborn.pale",
                description.getString()
        );
    }

    @Test
    void normalModifierIsNotRepeatedLikeAClassicNullModifier() {
        AuraNodeState.Snapshot node = AuraNodeFactory.ordinary(
                UUID.fromString("560539cd-7971-44ec-b459-3567ef57c43e")
        ).snapshot();

        assertEquals(
                "node_type.thaumic_reborn.normal",
                ClientThaumometerTarget.nodeDescription(node, true).getString()
        );
    }
}
