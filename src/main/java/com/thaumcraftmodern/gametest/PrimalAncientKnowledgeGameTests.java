package com.thaumcraftmodern.gametest;

import com.mojang.authlib.GameProfile;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeFactory;
import com.thaumcraftmodern.aura.AuraNodeType;
import com.thaumcraftmodern.item.PrimalCrusherItem;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PrimalAncientKnowledgeGameTests {
    private PrimalAncientKnowledgeGameTests() {
    }

    @GameTest(template = "empty", batch = "primalAncientKnowledge")
    public static void pearlTransformsNodeExplodesAndScattersFlux(
            GameTestHelper helper
    ) {
        BlockPos relative = new BlockPos(5, 3, 5);
        BlockPos absolute = helper.absolutePos(relative);
        helper.setBlock(relative, ModBlocks.AURA_NODE.get());
        AuraNodeBlockEntity node = (AuraNodeBlockEntity)
                helper.getBlockEntity(relative);
        node.initializeOnce(AuraNodeFactory.structureNode(
                absolute,
                AuraNodeType.NORMAL
        ));
        long revision = node.snapshotState().revision();

        ServerPlayer player = fakePlayer(helper, "primnode");
        player.setPos(
                absolute.getX() + 0.5D,
                absolute.getY() + 1.0D,
                absolute.getZ() + 2.5D
        );
        player.setItemInHand(
                InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.PRIMORDIAL_PEARL.get())
        );
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolute),
                Direction.UP,
                absolute,
                false
        );
        InteractionResult interaction = player.getMainHandItem().useOn(
                new UseOnContext(player, InteractionHand.MAIN_HAND, hit)
        );

        helper.assertTrue(interaction.consumesAction(),
                "Primordial Pearl did not consume the node interaction");
        helper.assertTrue(player.getMainHandItem().isEmpty(),
                "Primordial Pearl was not consumed");
        helper.assertTrue(helper.getLevel().getBlockEntity(absolute)
                        instanceof AuraNodeBlockEntity transformed
                        && transformed.snapshotState().revision() == revision + 1,
                "Aura node state was not atomically transformed and synced");
        int flux = 0;
        for (BlockPos target : BlockPos.betweenClosed(
                absolute.offset(-5, -5, -5),
                absolute.offset(5, 5, 5))) {
            if (helper.getLevel().getBlockState(target)
                    .is(ModBlocks.FLUX_GOO.get())
                    || helper.getLevel().getBlockState(target)
                    .is(ModBlocks.FLUX_GAS.get())) {
                flux++;
            }
        }
        helper.assertTrue(flux > 0,
                "Primordial Pearl explosion did not scatter TC4 flux");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "primalAncientKnowledge")
    public static void crusherMinesTheFacingThreeByThreePlane(
            GameTestHelper helper
    ) {
        BlockPos center = new BlockPos(5, 3, 5);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                helper.setBlock(center.offset(x, y, 0), Blocks.STONE);
            }
        }
        ServerPlayer player = fakePlayer(helper, "primalcrusher");
        BlockPos absoluteCenter = helper.absolutePos(center);
        player.setPos(
                absoluteCenter.getX() + 0.5D,
                absoluteCenter.getY() + 0.5D,
                absoluteCenter.getZ() + 2.5D
        );
        ItemStack crusher = new ItemStack(ModItems.PRIMAL_CRUSHER.get());
        crusher.getOrCreateTag().putByte(
                "tc4BreakFace",
                (byte) Direction.NORTH.get3DDataValue()
        );
        player.setItemInHand(InteractionHand.MAIN_HAND, crusher);
        helper.setBlock(center, Blocks.AIR);
        ((PrimalCrusherItem) crusher.getItem()).mineBlock(
                crusher,
                helper.getLevel(),
                Blocks.STONE.defaultBlockState(),
                absoluteCenter,
                player
        );

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                helper.assertBlockPresent(
                        Blocks.AIR,
                        center.offset(x, y, 0)
                );
            }
        }
        helper.assertTrue(crusher.getDamageValue() >= 9,
                "Primal Crusher did not spend durability for the 3x3 break");
        helper.succeed();
    }

    private static ServerPlayer fakePlayer(
            GameTestHelper helper,
            String testId
    ) {
        UUID id = UUID.nameUUIDFromBytes(
                ("thaumcraftmodern:gametest:" + testId)
                        .getBytes(StandardCharsets.UTF_8)
        );
        return FakePlayerFactory.get(
                helper.getLevel(),
                new GameProfile(id, "tcm-" + testId)
        );
    }
}
