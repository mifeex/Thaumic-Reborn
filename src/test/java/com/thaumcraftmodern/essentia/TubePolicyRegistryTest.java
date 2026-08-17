package com.thaumcraftmodern.essentia;

import com.thaumcraftmodern.essentia.tube.TubePolicy;
import com.thaumcraftmodern.essentia.tube.TubePolicyRegistry;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TubePolicyRegistryTest {
    @Test
    void standardTubeRulesRemainIsolatedAndSourceFaithful() {
        TubePolicy plain = TubePolicyRegistry.require(TubePolicyRegistry.PLAIN);
        TubePolicy filtered = TubePolicyRegistry.require(TubePolicyRegistry.FILTERED);
        TubePolicy restricted = TubePolicyRegistry.require(TubePolicyRegistry.RESTRICTED);
        TubePolicy oneWay = TubePolicyRegistry.require(TubePolicyRegistry.ONE_WAY);
        TubePolicy valve = TubePolicyRegistry.require(TubePolicyRegistry.VALVE);

        assertEquals(new TubePolicy(false, false, false, false), plain);
        assertTrue(filtered.filtered());
        assertTrue(restricted.restrictedSuction());
        assertTrue(oneWay.directional());
        assertTrue(valve.redstoneValve());
        assertFalse(filtered.redstoneValve());
    }

    @Test
    void customTubePolicyCanBeAddedWithoutChangingNetworkCode() {
        ResourceLocation id = new ResourceLocation(
                "thaumic_reborn", "test_custom_policy");
        TubePolicy policy = new TubePolicy(true, true, true, true);
        TubePolicyRegistry.register(id, policy);
        assertEquals(policy, TubePolicyRegistry.require(id));
    }
}
