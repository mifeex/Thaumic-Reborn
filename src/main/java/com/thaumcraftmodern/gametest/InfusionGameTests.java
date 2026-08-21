package com.thaumcraftmodern.gametest;

import com.mojang.authlib.GameProfile;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.api.wand.WandApi;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.RunicMatrixBlock;
import com.thaumcraftmodern.world.block.entity.ArcanePedestalBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.RunicMatrixBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InfusionGameTests {
    private static final BlockPos MATRIX = new BlockPos(14, 4, 14);
    private static final BlockPos CENTER = MATRIX.below(2);
    private static final BlockPos BALANCED = CENTER.west();
    private static final BlockPos AIR = CENTER.east();

    private InfusionGameTests() {
    }

    @GameTest(template = "infusion_empty", batch = "infusionSuccess", timeoutTicks = 80)
    public static void originalReedRodRecipeCompletesServerSide(GameTestHelper helper) {
        Rig rig = rig(helper, true);
        startWithWand(helper, rig, "infusion-success");
        tick(helper, rig.matrix(), 500);
        helper.assertTrue(!rig.matrix().crafting(), "Infusion did not complete");
        helper.assertTrue(rig.center().item().is(ModItems.REED_WAND_ROD.get()),
                "Confirmed TC4 Reed Wand Rod recipe produced the wrong output");
        helper.assertTrue(rig.balanced().item().isEmpty() && rig.air().item().isEmpty(),
                "Infusion did not sequentially consume both components");
        helper.assertTrue(rig.aer().amount() == 0 && rig.magic().amount() == 0
                        && rig.motion().amount() == 0,
                "Infusion did not consume exactly 12 Aer, 6 Praecantatio, 6 Motus");
        helper.succeed();
    }

    @GameTest(template = "infusion_empty", batch = "infusionSilverwood", timeoutTicks = 80)
    public static void originalSilverwoodRodRecipeCompletesServerSide(GameTestHelper helper) {
        SilverwoodRig rig = silverwoodRig(helper);
        startWithWand(helper, rig.matrix(), "infusion-silverwood", "rod_silverwood");
        tick(helper, rig.matrix(), 1_400);
        helper.assertTrue(!rig.matrix().crafting(), "Silverwood infusion did not complete");
        helper.assertTrue(rig.center().item().is(ModItems.SILVERWOOD_WAND_ROD.get()),
                "Original TC4 Silverwood Wand Rod recipe produced the wrong output");
        helper.assertTrue(rig.components().stream().allMatch(ArcanePedestalBlockEntity::isEmpty),
                "Silverwood infusion did not consume all seven original components");
        helper.assertTrue(rig.jars().stream().allMatch(jar -> jar.amount() == 0),
                "Silverwood infusion did not consume exactly 9 of all seven aspects");
        helper.succeed();
    }

    @GameTest(template = "infusion_empty", batch = "infusionRestored", timeoutTicks = 80)
    public static void advancedNodeStabilizerRecipeMatchesServerSide(GameTestHelper helper) {
        BaseRig base = baseAltar(helper);
        base.center().setItem(0, new ItemStack(ModItems.NODE_STABILIZER.get()));
        List<BlockPos> positions = List.of(
                MATRIX.offset(-3, -2, -3), MATRIX.offset(0, -2, -3),
                MATRIX.offset(3, -2, -3), MATRIX.offset(-3, -2, 0),
                MATRIX.offset(3, -2, 0), MATRIX.offset(-3, -2, 3),
                MATRIX.offset(0, -2, 3), MATRIX.offset(3, -2, 3));
        List<Item> components = List.of(
                ModItems.NITOR.get(), Blocks.REDSTONE_BLOCK.asItem(),
                ModItems.ALUMENTUM.get(), Blocks.REDSTONE_BLOCK.asItem(),
                Blocks.REDSTONE_BLOCK.asItem(), ModItems.ALUMENTUM.get(),
                Blocks.REDSTONE_BLOCK.asItem(), ModItems.NITOR.get());
        for (int index = 0; index < positions.size(); index++) {
            helper.setBlock(positions.get(index), ModBlocks.ARCANE_PEDESTAL.get());
            ArcanePedestalBlockEntity pedestal =
                    (ArcanePedestalBlockEntity) helper.getBlockEntity(positions.get(index));
            pedestal.setItem(0, new ItemStack(components.get(index)));
        }

        startWithWand(helper, base.matrix(), "infusion-advanced-stabilizer",
                "nodestabilizeradv");
        helper.assertTrue(base.matrix().remainingComponentCount() == 8,
                "Advanced stabilizer did not match all eight original components");
        helper.assertTrue(base.matrix().remainingEssentia().equals(Map.of(
                        "auram", 32,
                        "praecantatio", 16,
                        "ordo", 16,
                        "potentia", 16)),
                "Advanced stabilizer loaded the wrong original essentia costs");
        helper.succeed();
    }

    @GameTest(template = "infusion_empty", batch = "infusionNoEssentia", timeoutTicks = 80)
    public static void missingEssentiaStallsBeforeComponents(GameTestHelper helper) {
        Rig rig = rig(helper, false);
        startWithWand(helper, rig, "infusion-no-essentia");
        tick(helper, rig.matrix(), 160);
        helper.assertTrue(rig.matrix().crafting(),
                "A recipe without essentia incorrectly stopped instead of waiting");
        helper.assertTrue(rig.matrix().remainingEssentia().getOrDefault("aer", 0) == 12,
                "Missing essentia was silently fabricated or consumed");
        helper.assertTrue(rig.balanced().item().is(ModItems.BALANCED_SHARD.get())
                        && rig.air().item().is(ModItems.AIR_SHARD.get()),
                "Components were consumed before the essentia stage completed");
        helper.succeed();
    }

    @GameTest(template = "infusion_empty", batch = "infusionMissingComponent", timeoutTicks = 80)
    public static void vanishedComponentLeavesCraftWaiting(GameTestHelper helper) {
        Rig rig = rig(helper, true);
        startWithWand(helper, rig, "infusion-missing-component");
        rig.air().clearContent();
        tick(helper, rig.matrix(), 600);
        helper.assertTrue(rig.matrix().crafting(),
                "Infusion completed after a required component vanished");
        helper.assertTrue(rig.center().item().is(Items.SUGAR_CANE),
                "Missing component transformed the central input");
        helper.assertTrue(rig.matrix().remainingComponentCount() == 1,
                "Infusion did not retain the vanished component as pending");
        helper.succeed();
    }

    @GameTest(template = "infusion_empty", batch = "infusionReload", timeoutTicks = 80)
    public static void activeInfusionSurvivesNbtReload(GameTestHelper helper) {
        Rig rig = rig(helper, true);
        startWithWand(helper, rig, "infusion-reload");
        tick(helper, rig.matrix(), 70);
        CompoundTag saved = rig.matrix().saveWithFullMetadata();
        int aerBefore = rig.matrix().remainingEssentia().getOrDefault("aer", 0);

        helper.setBlock(MATRIX, Blocks.AIR);
        helper.setBlock(MATRIX, ModBlocks.RUNIC_MATRIX.get().defaultBlockState()
                .setValue(RunicMatrixBlock.ACTIVE, true));
        RunicMatrixBlockEntity loaded = (RunicMatrixBlockEntity) helper.getBlockEntity(MATRIX);
        loaded.load(saved);
        helper.assertTrue(loaded.crafting()
                        && loaded.remainingEssentia().getOrDefault("aer", 0) == aerBefore,
                "Runic Matrix lost its active recipe state across NBT reload");
        tick(helper, loaded, 500);
        helper.assertTrue(!loaded.crafting()
                        && rig.center().item().is(ModItems.REED_WAND_ROD.get()),
                "Reloaded infusion did not resume and finish");
        helper.succeed();
    }

    @GameTest(template = "infusion_empty", batch = "infusionMultiplayer", timeoutTicks = 80)
    public static void secondPlayerCannotHijackActiveInfusion(GameTestHelper helper) {
        Rig rig = rig(helper, true);
        ServerPlayer first = player(helper, "infusion-owner");
        ServerPlayer second = player(helper, "infusion-intruder");
        unlock(first);
        unlock(second);
        helper.assertTrue(rig.matrix().startCrafting(first)
                        == RunicMatrixBlockEntity.StartResult.STARTED,
                "First player could not start infusion");
        helper.assertTrue(rig.matrix().startCrafting(second)
                        == RunicMatrixBlockEntity.StartResult.ALREADY_CRAFTING,
                "Second player was allowed to replace an active infusion");
        helper.assertTrue(first.getUUID().equals(rig.matrix().ownerId()),
                "Runic Matrix changed its server-owned recipe owner");
        tick(helper, rig.matrix(), 500);
        helper.assertTrue(rig.center().item().is(ModItems.REED_WAND_ROD.get()),
                "Two-player contention prevented the original craft from completing");
        helper.succeed();
    }

    @GameTest(template = "infusion_empty", batch = "infusionStability", timeoutTicks = 80)
    public static void stabilizationUsesExactMirrorArithmetic(GameTestHelper helper) {
        Rig rig = rig(helper, false);
        rig.matrix().refreshSymmetry();
        float neutralBaseline = rig.matrix().symmetry();

        rig.air().clearContent();
        helper.setBlock(AIR, Blocks.AIR);
        rig.matrix().refreshSymmetry();
        helper.assertTrue(rig.matrix().symmetry() == neutralBaseline + 3,
                "Unpaired occupied pedestal must add +2 block and +1 item; baseline="
                        + neutralBaseline + ", got " + rig.matrix().symmetry());

        helper.setBlock(AIR, ModBlocks.ARCANE_PEDESTAL.get());
        ArcanePedestalBlockEntity restored = (ArcanePedestalBlockEntity) helper.getBlockEntity(AIR);
        restored.setItem(0, new ItemStack(ModItems.AIR_SHARD.get()));
        for (int y : new int[]{0, 1, 3}) {
            helper.setBlock(MATRIX.offset(-4, y - MATRIX.getY(), 0),
                    ModBlocks.AIR_CRYSTAL_CLUSTER.get());
            helper.setBlock(MATRIX.offset(4, y - MATRIX.getY(), 0),
                    ModBlocks.AIR_CRYSTAL_CLUSTER.get());
            helper.setBlock(MATRIX.offset(0, y - MATRIX.getY(), -4),
                    ModBlocks.AIR_CRYSTAL_CLUSTER.get());
            helper.setBlock(MATRIX.offset(0, y - MATRIX.getY(), 4),
                    ModBlocks.AIR_CRYSTAL_CLUSTER.get());
        }
        rig.matrix().refreshSymmetry();
        helper.assertTrue(Math.abs(rig.matrix().symmetry() - (neutralBaseline - 1.2F)) < 0.0001F,
                "Six mirrored stabilizer pairs must improve symmetry by exactly 1.2; baseline="
                        + neutralBaseline + ", got " + rig.matrix().symmetry());
        helper.succeed();
    }

    private static Rig rig(GameTestHelper helper, boolean withEssentia) {
        BaseRig base = baseAltar(helper);
        helper.setBlock(BALANCED, ModBlocks.ARCANE_PEDESTAL.get());
        helper.setBlock(AIR, ModBlocks.ARCANE_PEDESTAL.get());
        ArcanePedestalBlockEntity balanced = (ArcanePedestalBlockEntity) helper.getBlockEntity(BALANCED);
        ArcanePedestalBlockEntity air = (ArcanePedestalBlockEntity) helper.getBlockEntity(AIR);
        base.center().setItem(0, new ItemStack(Items.SUGAR_CANE));
        balanced.setItem(0, new ItemStack(ModItems.BALANCED_SHARD.get()));
        air.setItem(0, new ItemStack(ModItems.AIR_SHARD.get()));

        EssentiaJarBlockEntity aer = jar(helper, MATRIX.offset(-4, -4, 0),
                "aer", withEssentia ? 12 : 0);
        EssentiaJarBlockEntity magic = jar(helper, MATRIX.offset(0, -4, -4),
                "praecantatio", withEssentia ? 6 : 0);
        EssentiaJarBlockEntity motion = jar(helper, MATRIX.offset(4, -4, 0),
                "motus", withEssentia ? 6 : 0);
        return new Rig(base.matrix(), base.center(), balanced, air, aer, magic, motion);
    }

    private static SilverwoodRig silverwoodRig(GameTestHelper helper) {
        BaseRig base = baseAltar(helper);
        base.center().setItem(0, new ItemStack(ModItems.SILVERWOOD_LOG.get()));
        List<BlockPos> positions = List.of(
                MATRIX.offset(-3, -2, -3), MATRIX.offset(0, -2, -3),
                MATRIX.offset(3, -2, -3), MATRIX.offset(-3, -2, 0),
                MATRIX.offset(3, -2, 0), MATRIX.offset(-3, -2, 3),
                MATRIX.offset(3, -2, 3));
        List<Item> items = List.of(
                ModItems.BALANCED_SHARD.get(), ModItems.AIR_SHARD.get(),
                ModItems.FIRE_SHARD.get(), ModItems.WATER_SHARD.get(),
                ModItems.EARTH_SHARD.get(), ModItems.ORDER_SHARD.get(),
                ModItems.ENTROPY_SHARD.get());
        List<ArcanePedestalBlockEntity> components = new ArrayList<>();
        for (int index = 0; index < positions.size(); index++) {
            helper.setBlock(positions.get(index), ModBlocks.ARCANE_PEDESTAL.get());
            ArcanePedestalBlockEntity pedestal =
                    (ArcanePedestalBlockEntity) helper.getBlockEntity(positions.get(index));
            pedestal.setItem(0, new ItemStack(items.get(index)));
            components.add(pedestal);
        }
        List<String> aspects = List.of(
                "aer", "ignis", "aqua", "terra", "ordo", "perditio", "praecantatio");
        List<BlockPos> jarPositions = List.of(
                MATRIX.offset(-8, -4, 0), MATRIX.offset(8, -4, 0),
                MATRIX.offset(0, -4, -8), MATRIX.offset(0, -4, 8),
                MATRIX.offset(-8, -4, -8), MATRIX.offset(8, -4, -8),
                MATRIX.offset(-8, -4, 8));
        List<EssentiaJarBlockEntity> jars = new ArrayList<>();
        for (int index = 0; index < aspects.size(); index++) {
            jars.add(jar(helper, jarPositions.get(index), aspects.get(index), 9));
        }
        return new SilverwoodRig(base.matrix(), base.center(),
                List.copyOf(components), List.copyOf(jars));
    }

    private static BaseRig baseAltar(GameTestHelper helper) {
        clearRig(helper);
        helper.setBlock(MATRIX, ModBlocks.RUNIC_MATRIX.get().defaultBlockState()
                .setValue(RunicMatrixBlock.ACTIVE, true));
        helper.setBlock(CENTER, ModBlocks.ARCANE_PEDESTAL.get());
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                helper.setBlock(MATRIX.offset(x, -2, z), ModBlocks.INFUSION_PILLAR.get());
            }
        }
        return new BaseRig(
                (RunicMatrixBlockEntity) helper.getBlockEntity(MATRIX),
                (ArcanePedestalBlockEntity) helper.getBlockEntity(CENTER));
    }

    private static void clearRig(GameTestHelper helper) {
        for (int x = MATRIX.getX() - 12; x <= MATRIX.getX() + 12; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = MATRIX.getZ() - 12; z <= MATRIX.getZ() + 12; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    private static EssentiaJarBlockEntity jar(GameTestHelper helper, BlockPos pos,
            String aspect, int amount) {
        helper.setBlock(pos, ModBlocks.WARDED_JAR.get());
        EssentiaJarBlockEntity jar = (EssentiaJarBlockEntity) helper.getBlockEntity(pos);
        if (amount > 0) {
            helper.assertTrue(jar.addEssentia(aspect, amount, Direction.UP) == amount,
                    "Test jar rejected " + aspect);
        }
        return jar;
    }

    private static void startWithWand(GameTestHelper helper, Rig rig, String name) {
        startWithWand(helper, rig.matrix(), name, "rod_reed");
    }

    private static void startWithWand(GameTestHelper helper,
            RunicMatrixBlockEntity matrix, String name, String research) {
        ServerPlayer player = player(helper, name);
        KnowledgeAccess.get(player).orElseThrow().completeResearch(research);
        player.setItemInHand(InteractionHand.MAIN_HAND, WandApi.createCodexWand());
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(helper.absolutePos(MATRIX)), Direction.UP,
                helper.absolutePos(MATRIX), false);
        InteractionResult result = ModBlocks.RUNIC_MATRIX.get().use(
                helper.getBlockState(MATRIX), helper.getLevel(),
                helper.absolutePos(MATRIX), player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result.consumesAction() && matrix.crafting(),
                "Server wand interaction did not start infusion; result=" + result);
    }

    private static void unlock(ServerPlayer player) {
        KnowledgeAccess.get(player).orElseThrow().completeResearch("rod_reed");
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        UUID id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(id, name));
    }

    private static void tick(GameTestHelper helper, RunicMatrixBlockEntity matrix, int ticks) {
        for (int tick = 0; tick < ticks && matrix.crafting(); tick++) {
            RunicMatrixBlockEntity.serverTick(helper.getLevel(), matrix.getBlockPos(),
                    matrix.getBlockState(), matrix);
        }
    }

    private record Rig(RunicMatrixBlockEntity matrix,
                       ArcanePedestalBlockEntity center,
                       ArcanePedestalBlockEntity balanced,
                       ArcanePedestalBlockEntity air,
                       EssentiaJarBlockEntity aer,
                       EssentiaJarBlockEntity magic,
                       EssentiaJarBlockEntity motion) {
    }

    private record BaseRig(RunicMatrixBlockEntity matrix,
                           ArcanePedestalBlockEntity center) {
    }

    private record SilverwoodRig(RunicMatrixBlockEntity matrix,
                                 ArcanePedestalBlockEntity center,
                                 List<ArcanePedestalBlockEntity> components,
                                 List<EssentiaJarBlockEntity> jars) {
    }
}
