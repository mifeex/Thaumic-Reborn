package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.essentia.AdvancedBufferSideRole;
import com.thaumcraftmodern.essentia.tube.TubeEssentiaReleaseRisk;
import com.thaumcraftmodern.essentia.tube.TubeEssentiaReleaseRules;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.AdvancedEssentiaBufferBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaBufferBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import com.thaumcraftmodern.world.block.EssentiaTubeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdvancedEssentiaAutomationGameTests {
    private AdvancedEssentiaAutomationGameTests() {
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void verticalBufferReconnectsToHorizontalTubeImmediately(
            GameTestHelper helper) {
        BlockPos bufferPos = helper.absolutePos(new BlockPos(8, 3, 8));
        BlockPos tubePos = bufferPos.above();
        helper.getLevel().setBlock(bufferPos, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(tubePos, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(bufferPos,
                ModBlocks.ESSENTIA_BUFFER.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(tubePos,
                ModBlocks.ESSENTIA_TUBE.get().defaultBlockState(), 3);
        EssentiaBufferBlockEntity buffer = (EssentiaBufferBlockEntity)
                helper.getLevel().getBlockEntity(bufferPos);
        EssentiaTubeBlock.refreshConnections(helper.getLevel(), tubePos);
        helper.assertTrue(helper.getLevel().getBlockState(tubePos)
                        .getValue(EssentiaTubeBlock.DOWN),
                "Horizontal tube did not connect down to the vertical buffer");

        buffer.toggleSide(Direction.UP);
        helper.assertTrue(!helper.getLevel().getBlockState(tubePos)
                        .getValue(EssentiaTubeBlock.DOWN),
                "Closing the buffer side did not retract the tube connection");
        buffer.toggleSide(Direction.UP);
        helper.assertTrue(helper.getLevel().getBlockState(tubePos)
                        .getValue(EssentiaTubeBlock.DOWN),
                "Reopening the buffer side did not restore the tube connection");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void improvedBufferCapsEachAspectAtFourAndPersistsRoles(
            GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8));
        helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(pos,
                ModBlocks.ADVANCED_ESSENTIA_BUFFER.get().defaultBlockState(), 3);
        AdvancedEssentiaBufferBlockEntity buffer =
                (AdvancedEssentiaBufferBlockEntity) helper.getLevel()
                        .getBlockEntity(pos);
        for (int index = 0; index < 4; index++) {
            helper.assertTrue(buffer.addEssentia("aer", 1, Direction.DOWN) == 1,
                    "Improved buffer rejected one of its four Aer points");
        }
        helper.assertTrue(buffer.addEssentia("aer", 1, Direction.DOWN) == 0,
                "Improved buffer accepted a fifth Aer point");
        buffer.cycleRole(Direction.EAST);
        helper.assertTrue(buffer.role(Direction.EAST)
                        == AdvancedBufferSideRole.INPUT,
                "Wand role cycle did not move the unique input to east");
        helper.assertTrue(buffer.role(Direction.DOWN)
                        == AdvancedBufferSideRole.BLOCKED,
                "Old unique input side remained open");

        CompoundTag saved = buffer.saveWithFullMetadata();
        AdvancedEssentiaBufferBlockEntity loaded =
                new AdvancedEssentiaBufferBlockEntity(pos,
                        helper.getLevel().getBlockState(pos));
        loaded.load(saved);
        helper.assertTrue(loaded.supplyContents().getOrDefault("aer", 0) == 4,
                "Improved buffer lost essentia across NBT reload");
        helper.assertTrue(loaded.role(Direction.EAST)
                        == AdvancedBufferSideRole.INPUT,
                "Improved buffer lost side roles across NBT reload");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void improvedBufferMovesMatchingEssentiaFromAlembicToJar(
            GameTestHelper helper) {
        BlockPos bufferPos = helper.absolutePos(new BlockPos(8, 5, 8));
        BlockPos inputTubePos = bufferPos.below();
        BlockPos alembicPos = inputTubePos.below();
        BlockPos outputTubePos = bufferPos.north();
        BlockPos jarPos = outputTubePos.below();
        for (BlockPos pos : new BlockPos[] {
                bufferPos, inputTubePos, alembicPos, outputTubePos, jarPos
        }) {
            helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        helper.getLevel().setBlock(alembicPos,
                ModBlocks.ARCANE_ALEMBIC.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(inputTubePos,
                ModBlocks.ESSENTIA_TUBE.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(bufferPos,
                ModBlocks.ADVANCED_ESSENTIA_BUFFER.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(outputTubePos,
                ModBlocks.ESSENTIA_TUBE.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(jarPos,
                ModBlocks.WARDED_JAR.get().defaultBlockState(), 3);

        ArcaneAlembicBlockEntity alembic = (ArcaneAlembicBlockEntity)
                helper.getLevel().getBlockEntity(alembicPos);
        EssentiaTubeBlockEntity inputTube = (EssentiaTubeBlockEntity)
                helper.getLevel().getBlockEntity(inputTubePos);
        AdvancedEssentiaBufferBlockEntity buffer =
                (AdvancedEssentiaBufferBlockEntity) helper.getLevel()
                        .getBlockEntity(bufferPos);
        EssentiaTubeBlockEntity outputTube = (EssentiaTubeBlockEntity)
                helper.getLevel().getBlockEntity(outputTubePos);
        EssentiaJarBlockEntity jar = (EssentiaJarBlockEntity)
                helper.getLevel().getBlockEntity(jarPos);
        helper.assertTrue(alembic.acceptFromFurnace("aqua", 8) == 8
                        && jar.addEssentia("aqua", 14, Direction.UP) == 14,
                "Could not prepare matching Aqua source and destination");

        for (int tick = 0; tick < 160; tick++) {
            EssentiaTubeBlockEntity.serverTick(helper.getLevel(), inputTubePos,
                    helper.getLevel().getBlockState(inputTubePos), inputTube);
            EssentiaTubeBlockEntity.serverTick(helper.getLevel(), outputTubePos,
                    helper.getLevel().getBlockState(outputTubePos), outputTube);
            AdvancedEssentiaBufferBlockEntity.serverTick(helper.getLevel(),
                    bufferPos, helper.getLevel().getBlockState(bufferPos), buffer);
            EssentiaJarBlockEntity.serverTick(helper.getLevel(), jarPos,
                    helper.getLevel().getBlockState(jarPos), jar);
        }

        helper.assertTrue(alembic.storedAmount() < 8 && jar.amount() > 14,
                "Improved buffer did not move Aqua from alembic to matching jar");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void improvedBufferServesBothRequestingOutputs(
            GameTestHelper helper) {
        BufferRoutingRig rig = bufferRoutingRig(helper);
        helper.assertTrue(rig.mainJar().addEssentia(
                        "aer", 1, Direction.UP) == 1
                        && rig.buffer().addEssentia(
                                "ignis", 1, Direction.DOWN) == 1
                        && rig.buffer().addEssentia(
                                "aer", 1, Direction.DOWN) == 1,
                "Could not prepare the matching-main-jar routing case");

        tickRoutingRig(helper, rig, 160);

        helper.assertTrue(rig.mainJar().amount() == 2
                        && "ignis".equals(rig.reserveJar().aspect())
                        && rig.reserveJar().amount() == 1
                        && rig.buffer().totalAmount() == 0,
                "Main and reserve outputs did not independently serve suction");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void reserveFaceFeedsJarDirectlyBelowInNormalFlow(
            GameTestHelper helper) {
        BlockPos bufferPos = helper.absolutePos(new BlockPos(8, 4, 8));
        BlockPos jarPos = bufferPos.below();
        helper.getLevel().setBlock(bufferPos, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(jarPos, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(bufferPos,
                ModBlocks.ADVANCED_ESSENTIA_BUFFER.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(jarPos,
                ModBlocks.WARDED_JAR.get().defaultBlockState(), 3);
        AdvancedEssentiaBufferBlockEntity buffer =
                (AdvancedEssentiaBufferBlockEntity) helper.getLevel()
                        .getBlockEntity(bufferPos);
        EssentiaJarBlockEntity jar = (EssentiaJarBlockEntity)
                helper.getLevel().getBlockEntity(jarPos);

        buffer.cycleRole(Direction.EAST);
        buffer.cycleRole(Direction.DOWN);
        buffer.cycleRole(Direction.DOWN);
        helper.assertTrue(buffer.role(Direction.DOWN)
                        == AdvancedBufferSideRole.RESERVE_OUTPUT,
                "Could not configure the lower face as reserve output");
        helper.assertTrue(buffer.addEssentia("aer", 1, Direction.EAST) == 1,
                "Could not load the improved buffer through its input face");

        for (int tick = 0; tick < 40; tick++) {
            AdvancedEssentiaBufferBlockEntity.serverTick(helper.getLevel(),
                    bufferPos, helper.getLevel().getBlockState(bufferPos), buffer);
            EssentiaJarBlockEntity.serverTick(helper.getLevel(), jarPos,
                    helper.getLevel().getBlockState(jarPos), jar);
        }

        helper.assertTrue("aer".equals(jar.aspect()) && jar.amount() == 1
                        && buffer.totalAmount() == 0,
                "Reserve face did not feed the requesting jar directly below");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void reserveOutputWorksWhenMainJarIsFull(
            GameTestHelper helper) {
        BufferRoutingRig rig = bufferRoutingRig(helper);
        boolean filledBufferSlot = true;
        for (int amount = 0;
                amount < AdvancedEssentiaBufferBlockEntity.CAPACITY_PER_ASPECT;
                amount++) {
            filledBufferSlot &= rig.buffer().addEssentia(
                    "aer", 1, Direction.DOWN) == 1;
        }
        helper.assertTrue(rig.mainJar().addEssentia(
                        "aer", EssentiaJarBlockEntity.CAPACITY,
                        Direction.UP) == EssentiaJarBlockEntity.CAPACITY
                        && filledBufferSlot
                        && rig.buffer().addEssentia(
                                "aer", 1, Direction.DOWN) == 0,
                "Could not prepare the full-main-jar routing case");

        tickRoutingRig(helper, rig, 200);

        helper.assertTrue(
                rig.mainJar().amount() == EssentiaJarBlockEntity.CAPACITY
                        && "aer".equals(rig.reserveJar().aspect())
                        && rig.reserveJar().amount()
                                == AdvancedEssentiaBufferBlockEntity
                                        .CAPACITY_PER_ASPECT
                        && rig.buffer().totalAmount() == 0,
                "Reserve output ignored its requesting jar");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void reserveOutputAcceptsAspectRejectedByMainJar(
            GameTestHelper helper) {
        BufferRoutingRig rig = bufferRoutingRig(helper);
        helper.assertTrue(rig.mainJar().addEssentia(
                        "ignis", 1, Direction.UP) == 1
                        && rig.buffer().addEssentia(
                                "aer", 1, Direction.DOWN) == 1,
                "Could not prepare the mismatched-main-jar routing case");

        tickRoutingRig(helper, rig, 200);

        helper.assertTrue("ignis".equals(rig.mainJar().aspect())
                        && rig.mainJar().amount() == 1
                        && "aer".equals(rig.reserveJar().aspect())
                        && rig.reserveJar().amount() == 1
                        && rig.buffer().totalAmount() == 0,
                "Reserve output did not serve its own suction path");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void bothOutputsArePassiveSourcesWithZeroSuction(
            GameTestHelper helper) {
        BufferRoutingRig rig = bufferRoutingRig(helper);
        helper.assertTrue(rig.buffer().addEssentia(
                        "ignis", 1, Direction.DOWN) == 1,
                "Could not load the improved buffer");
        helper.assertTrue(rig.buffer().suctionAmount(Direction.NORTH) == 0
                        && rig.buffer().suctionAmount(Direction.SOUTH) == 0
                        && rig.buffer().canOutputTo(Direction.NORTH)
                        && rig.buffer().canOutputTo(Direction.SOUTH),
                "An output created suction or was controller-blocked");
        helper.assertTrue(rig.buffer().takeEssentia(
                        "ignis", 1, Direction.NORTH) == 1
                        && rig.buffer().addEssentia(
                                "ignis", 1, Direction.DOWN) == 1
                        && rig.buffer().takeEssentia(
                                "ignis", 1, Direction.SOUTH) == 1,
                "Main and reserve outputs did not expose the same storage");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void reversibleTubeAppliesItsBoundedSwitchDelay(
            GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8));
        helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(pos,
                ModBlocks.REVERSIBLE_ESSENTIA_TUBE.get().defaultBlockState(), 3);
        EssentiaTubeBlockEntity tube = (EssentiaTubeBlockEntity)
                helper.getLevel().getBlockEntity(pos);
        Direction initialFacing = tube.facing();
        tube.toggleManualReturn();
        int delay = EssentiaTubeBlockEntity.switchDelayTicks(pos);
        for (int tick = 0; tick < delay - 1; tick++) {
            EssentiaTubeBlockEntity.serverTick(helper.getLevel(), pos,
                    helper.getLevel().getBlockState(pos), tube);
        }
        helper.assertTrue(!tube.returnEnabled(),
                "Reversible tube switched before its delay elapsed");
        helper.assertTrue(tube.facing() == initialFacing,
                "Reversible tube rotated before its delay elapsed");
        EssentiaTubeBlockEntity.serverTick(helper.getLevel(), pos,
                helper.getLevel().getBlockState(pos), tube);
        helper.assertTrue(tube.returnEnabled(),
                "Reversible tube did not switch after its delay elapsed");
        helper.assertTrue(tube.facing() == initialFacing.getOpposite(),
                "Reversible tube arrow did not rotate by 180 degrees");
        helper.assertTrue(helper.getLevel().getBlockState(pos).getValue(
                        com.thaumcraftmodern.world.block.EssentiaTubeBlock.FACING)
                        == initialFacing.getOpposite(),
                "Reversible tube did not sync its arrow direction to clients");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void reversibleTubeIgnoresLegacyAutomaticBufferControl(
            GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(8, 3, 8));
        helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(pos,
                ModBlocks.REVERSIBLE_ESSENTIA_TUBE.get().defaultBlockState(), 3);
        EssentiaTubeBlockEntity tube = (EssentiaTubeBlockEntity)
                helper.getLevel().getBlockEntity(pos);
        CompoundTag legacy = tube.saveWithFullMetadata();
        legacy.putBoolean("AutomaticReturn", true);
        legacy.putBoolean("ManualReturn", false);
        legacy.putBoolean("ReverseTarget", true);
        legacy.putBoolean("ReturnEnabled", true);
        legacy.putInt("ReverseSwitch", 0);
        legacy.putInt("Side", Direction.UP.ordinal());
        tube.load(legacy);

        tube.toggleManualReturnFromWand();
        int delay = EssentiaTubeBlockEntity.switchDelayTicks(pos);
        for (int tick = 0; tick < delay; tick++) {
            EssentiaTubeBlockEntity.serverTick(helper.getLevel(), pos,
                    helper.getLevel().getBlockState(pos), tube);
        }

        helper.assertTrue(!tube.returnEnabled()
                        && tube.facing() == Direction.DOWN,
                "Legacy buffer-control flag prevented the tube from switching");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void reversibleTubeShowsOnlyTheLastWandSelectedHead(
            GameTestHelper helper) {
        BlockPos lowerPos = helper.absolutePos(new BlockPos(8, 3, 8));
        BlockPos upperPos = lowerPos.above();
        helper.getLevel().setBlock(lowerPos,
                ModBlocks.REVERSIBLE_ESSENTIA_TUBE.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(upperPos,
                ModBlocks.REVERSIBLE_ESSENTIA_TUBE.get().defaultBlockState(), 3);
        EssentiaTubeBlockEntity lower = (EssentiaTubeBlockEntity)
                helper.getLevel().getBlockEntity(lowerPos);
        EssentiaTubeBlockEntity upper = (EssentiaTubeBlockEntity)
                helper.getLevel().getBlockEntity(upperPos);

        upper.toggleManualReturnFromWand();
        helper.assertTrue(upper.reversibleArrowVisible()
                        && !lower.reversibleArrowVisible()
                        && lower.facing() == Direction.DOWN,
                "Selecting the upper head did not hide and reset the lower arrow");
        int upperDelay = EssentiaTubeBlockEntity.switchDelayTicks(upperPos);
        for (int tick = 0; tick < upperDelay; tick++) {
            EssentiaTubeBlockEntity.serverTick(helper.getLevel(), upperPos,
                    helper.getLevel().getBlockState(upperPos), upper);
        }
        helper.assertTrue(upper.facing() == Direction.UP,
                "The selected upper arrow did not rotate by 180 degrees");

        lower.toggleManualReturnFromWand();
        helper.assertTrue(lower.reversibleArrowVisible()
                        && !upper.reversibleArrowVisible()
                        && upper.facing() == Direction.DOWN,
                "Selecting the lower head did not hide and reset the upper arrow");
        int lowerDelay = EssentiaTubeBlockEntity.switchDelayTicks(lowerPos);
        for (int tick = 0; tick < lowerDelay; tick++) {
            EssentiaTubeBlockEntity.serverTick(helper.getLevel(), lowerPos,
                    helper.getLevel().getBlockState(lowerPos), lower);
        }
        helper.assertTrue(lower.facing() == Direction.UP,
                "The selected lower arrow did not rotate by 180 degrees");
        CompoundTag hiddenUpper = upper.saveWithFullMetadata();
        EssentiaTubeBlockEntity restoredUpper = new EssentiaTubeBlockEntity(
                upperPos, helper.getLevel().getBlockState(upperPos));
        restoredUpper.load(hiddenUpper);
        helper.assertTrue(!restoredUpper.reversibleArrowVisible()
                        && restoredUpper.facing() == Direction.DOWN,
                "The hidden default-down head did not survive NBT reload");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancedEssentia", timeoutTicks = 80)
    public static void differentTubesShareReleaseRiskOnThePlayer(
            GameTestHelper helper) {
        BlockPos[] positions = {
                helper.absolutePos(new BlockPos(5, 3, 8)),
                helper.absolutePos(new BlockPos(8, 3, 8)),
                helper.absolutePos(new BlockPos(11, 3, 8))
        };
        for (BlockPos pos : positions) {
            helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            for (Direction direction : Direction.values()) {
                helper.getLevel().setBlock(pos.relative(direction),
                        Blocks.AIR.defaultBlockState(), 3);
            }
            helper.getLevel().setBlock(pos,
                    ModBlocks.ESSENTIA_TUBE.get().defaultBlockState(), 3);
        }
        UUID id = UUID.nameUUIDFromBytes(
                "thaumcraftmodern:gametest:tube-release-risk"
                        .getBytes(StandardCharsets.UTF_8));
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(id, "tcm-tube-release-risk"));
        TubeEssentiaReleaseRisk.clear(player);

        for (int index = 0; index < positions.length; index++) {
            EssentiaTubeBlockEntity tube = (EssentiaTubeBlockEntity)
                    helper.getLevel().getBlockEntity(positions[index]);
            helper.assertTrue(tube.addEssentia(
                            "alienis", 1, Direction.NORTH) == 1,
                    "Tube " + index + " could not accept clogged essentia");
            TubeEssentiaReleaseRules.Release release =
                    TubeEssentiaReleaseRisk.preview(player,
                            TubeEssentiaReleaseRules.Complexity.COMPLEX_COMPOUND);
            helper.assertTrue(release.createsFlux() == (index == 2),
                    "Shared player risk crossed the threshold on the wrong tube");
            helper.assertTrue(tube.releaseCloggedEssentia(
                            helper.getLevel(), release.createsFlux()),
                    "Tube " + index + " did not release its essentia");
            TubeEssentiaReleaseRisk.commit(player, release);
            helper.assertTrue(tube.essentiaAmount(Direction.NORTH) == 0,
                    "Tube " + index + " retained released essentia");
            helper.assertTrue(!tube.saveWithFullMetadata().contains("ReleaseRisk"),
                    "Release risk was still serialized on a tube");
        }
        helper.assertTrue(TubeEssentiaReleaseRisk.get(player) == 0,
                "Player risk did not reset after forming flux");
        boolean visibleFlux = false;
        for (Direction direction : Direction.values()) {
            visibleFlux |= helper.getLevel()
                    .getBlockState(positions[2].relative(direction))
                    .is(ModBlocks.FLUX_GAS.get());
        }
        helper.assertTrue(visibleFlux,
                "The third tube did not create visible Flux Gas");
        helper.succeed();
    }

    private static BufferRoutingRig bufferRoutingRig(GameTestHelper helper) {
        BlockPos bufferPos = helper.absolutePos(new BlockPos(8, 4, 8));
        BlockPos mainTubePos = bufferPos.north();
        BlockPos reserveTubePos = bufferPos.south();
        BlockPos mainJarPos = mainTubePos.below();
        BlockPos reserveJarPos = reserveTubePos.below();
        for (BlockPos pos : new BlockPos[] {
                bufferPos, mainTubePos, reserveTubePos,
                mainJarPos, reserveJarPos
        }) {
            helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        helper.getLevel().setBlock(bufferPos,
                ModBlocks.ADVANCED_ESSENTIA_BUFFER.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(mainTubePos,
                ModBlocks.ESSENTIA_TUBE.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(reserveTubePos,
                ModBlocks.ESSENTIA_TUBE.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(mainJarPos,
                ModBlocks.WARDED_JAR.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(reserveJarPos,
                ModBlocks.WARDED_JAR.get().defaultBlockState(), 3);
        return new BufferRoutingRig(
                bufferPos,
                mainTubePos,
                reserveTubePos,
                mainJarPos,
                reserveJarPos,
                (AdvancedEssentiaBufferBlockEntity) helper.getLevel()
                        .getBlockEntity(bufferPos),
                (EssentiaTubeBlockEntity) helper.getLevel()
                        .getBlockEntity(mainTubePos),
                (EssentiaTubeBlockEntity) helper.getLevel()
                        .getBlockEntity(reserveTubePos),
                (EssentiaJarBlockEntity) helper.getLevel()
                        .getBlockEntity(mainJarPos),
                (EssentiaJarBlockEntity) helper.getLevel()
                        .getBlockEntity(reserveJarPos));
    }

    private static void tickRoutingRig(GameTestHelper helper,
            BufferRoutingRig rig, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            EssentiaTubeBlockEntity.serverTick(helper.getLevel(),
                    rig.mainTubePos(), helper.getLevel().getBlockState(
                            rig.mainTubePos()), rig.mainTube());
            EssentiaTubeBlockEntity.serverTick(helper.getLevel(),
                    rig.reserveTubePos(), helper.getLevel().getBlockState(
                            rig.reserveTubePos()), rig.reserveTube());
            AdvancedEssentiaBufferBlockEntity.serverTick(helper.getLevel(),
                    rig.bufferPos(), helper.getLevel().getBlockState(
                            rig.bufferPos()), rig.buffer());
            EssentiaJarBlockEntity.serverTick(helper.getLevel(),
                    rig.mainJarPos(), helper.getLevel().getBlockState(
                            rig.mainJarPos()), rig.mainJar());
            EssentiaJarBlockEntity.serverTick(helper.getLevel(),
                    rig.reserveJarPos(), helper.getLevel().getBlockState(
                            rig.reserveJarPos()), rig.reserveJar());
        }
    }

    private record BufferRoutingRig(
            BlockPos bufferPos,
            BlockPos mainTubePos,
            BlockPos reserveTubePos,
            BlockPos mainJarPos,
            BlockPos reserveJarPos,
            AdvancedEssentiaBufferBlockEntity buffer,
            EssentiaTubeBlockEntity mainTube,
            EssentiaTubeBlockEntity reserveTube,
            EssentiaJarBlockEntity mainJar,
            EssentiaJarBlockEntity reserveJar
    ) {
    }
}
