package com.thaumcraftmodern.aura;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuraNodeRegenerationPolicyTest {
    @Test
    void everyNodeRunsClassicEmptyAspectDecay() throws Exception {
        String ticker = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/aura/AuraNodeServerTicker.java"
        ));
        String gate = ticker.substring(
                ticker.indexOf("if (ticks % 1200 == 0"),
                ticker.indexOf("regenerate(")
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                gate.contains("ticks % 1200 == 0")
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                gate.contains("decayEmptyAspects(level, position, node)")
        );
        org.junit.jupiter.api.Assertions.assertFalse(
                gate.contains("AuraNodeModifier.FADING")
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                ticker.contains("ModSounds.CRAFT_FAIL.get()")
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                ticker.contains("level.removeBlock(position, false)")
        );
    }
    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void stabilizersUseTheExactTc4RechargeIntervals() {
        assertEquals(600, interval(AuraNodeModifier.NORMAL, 0));
        assertEquals(1_200, interval(AuraNodeModifier.NORMAL, 1));
        assertEquals(12_000, interval(AuraNodeModifier.NORMAL, 2));
        assertEquals(400, interval(AuraNodeModifier.BRIGHT, 0));
        assertEquals(800, interval(AuraNodeModifier.BRIGHT, 1));
        assertEquals(8_000, interval(AuraNodeModifier.BRIGHT, 2));
        assertEquals(900, interval(AuraNodeModifier.PALE, 0));
        assertEquals(1_800, interval(AuraNodeModifier.PALE, 1));
        assertEquals(18_000, interval(AuraNodeModifier.PALE, 2));
        assertEquals(0, interval(AuraNodeModifier.FADING, 0));
        assertEquals(0, interval(AuraNodeModifier.FADING, 1));
        assertEquals(0, interval(AuraNodeModifier.FADING, 2));
    }

    @Test
    void unloadedNodeAccumulatesAndConsumesBoundedCatchUpCycles() {
        long lastActive = 1_000L;
        long normalCycle = 600L * 75L;
        assertEquals(0, AuraNodeRegenerationPolicy.missedCycles(
                lastActive + normalCycle - 1L,
                lastActive,
                600,
                100
        ));
        assertEquals(3, AuraNodeRegenerationPolicy.missedCycles(
                lastActive + normalCycle * 3L,
                lastActive,
                600,
                100
        ));
        assertEquals(2, AuraNodeRegenerationPolicy.missedCycles(
                lastActive + normalCycle * 30L,
                lastActive,
                600,
                2
        ));
        assertEquals(lastActive + normalCycle * 3L,
                AuraNodeRegenerationPolicy.advanceLastActive(
                        lastActive,
                        600,
                        3
                ));
    }

    @Test
    void stabilizationChanceImprovesForTheAdvancedDevice() {
        assertEquals(0,
                AuraNodeRegenerationPolicy.unstableImprovementBound(0));
        assertEquals(10_000,
                AuraNodeRegenerationPolicy.unstableImprovementBound(1));
        assertEquals(5_000,
                AuraNodeRegenerationPolicy.unstableImprovementBound(2));
        assertEquals(0,
                AuraNodeRegenerationPolicy.fadingImprovementBound(0));
        assertEquals(12_500,
                AuraNodeRegenerationPolicy.fadingImprovementBound(1));
        assertEquals(6_250,
                AuraNodeRegenerationPolicy.fadingImprovementBound(2));
    }

    @Test
    void blockEntityPersistsRechargeClockAndTickerRestoresClassicHealing()
            throws Exception {
        String blockEntity = Files.readString(ROOT.resolve(
                "src/main/java/com/thaumcraftmodern/aura/"
                        + "AuraNodeBlockEntity.java"));
        String ticker = Files.readString(ROOT.resolve(
                "src/main/java/com/thaumcraftmodern/aura/"
                        + "AuraNodeServerTicker.java"));
        assertTrue(blockEntity.contains(
                "tag.putLong(LAST_ACTIVE_KEY, lastActiveMillis)"));
        assertTrue(blockEntity.contains(
                "lastActiveMillis = Math.max(0L, tag.getLong(LAST_ACTIVE_KEY))"));
        assertTrue(blockEntity.contains(
                "tag.putInt(REGENERATION_WAIT_KEY, regenerationWait)"));
        assertTrue(ticker.contains("catchUpRegeneration("));
        assertTrue(ticker.contains("node.replaceModifier(AuraNodeModifier.PALE)"));
        assertTrue(ticker.contains("node.replaceType(AuraNodeType.NORMAL)"));
        assertTrue(blockEntity.contains("new AuraNodeStateSyncPacket("));
        assertTrue(blockEntity.contains("ModNetwork.sendToTrackingChunk("));
    }

    private static int interval(AuraNodeModifier modifier, int lock) {
        return AuraNodeRegenerationPolicy.interval(modifier, lock);
    }
}
