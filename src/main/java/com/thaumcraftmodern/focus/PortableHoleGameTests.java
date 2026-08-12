package com.thaumcraftmodern.focus;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.CrystalClusterBlock;
import com.thaumcraftmodern.world.block.entity.TemporaryHoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortableHoleGameTests {
    private static final BlockPos CRYSTAL_SUPPORT = new BlockPos(2, 1, 2);
    private static final BlockPos CRYSTAL = CRYSTAL_SUPPORT.above();
    private static final BlockPos PLANT_SUPPORT = new BlockPos(4, 1, 2);
    private static final BlockPos PLANT = PLANT_SUPPORT.above();

    private PortableHoleGameTests() {
    }

    @GameTest(template = "empty", batch = "portableHole")
    public static void temporaryOpeningPreservesAttachedBlocks(
            GameTestHelper helper
    ) {
        helper.setBlock(CRYSTAL_SUPPORT, Blocks.STONE);
        helper.setBlock(
                CRYSTAL,
                ModBlocks.AIR_CRYSTAL_CLUSTER.get()
                        .defaultBlockState()
                        .setValue(CrystalClusterBlock.FACING, Direction.UP)
        );
        helper.setBlock(PLANT_SUPPORT, Blocks.DIRT);
        helper.setBlock(PLANT, Blocks.GRASS);

        TemporaryHoleBlockEntity.setBlockWithoutNeighborUpdates(
                helper.getLevel(),
                helper.absolutePos(CRYSTAL_SUPPORT),
                ModBlocks.TEMPORARY_HOLE.get().defaultBlockState()
        );
        TemporaryHoleBlockEntity.setBlockWithoutNeighborUpdates(
                helper.getLevel(),
                helper.absolutePos(PLANT_SUPPORT),
                ModBlocks.TEMPORARY_HOLE.get().defaultBlockState()
        );
        helper.getLevel().updateNeighborsAt(
                helper.absolutePos(CRYSTAL_SUPPORT),
                ModBlocks.TEMPORARY_HOLE.get()
        );
        helper.getLevel().updateNeighborsAt(
                helper.absolutePos(PLANT_SUPPORT),
                ModBlocks.TEMPORARY_HOLE.get()
        );

        helper.runAfterDelay(2, () -> {
            helper.assertBlockPresent(
                    ModBlocks.AIR_CRYSTAL_CLUSTER.get(),
                    CRYSTAL
            );
            helper.assertBlockPresent(Blocks.GRASS, PLANT);

            TemporaryHoleBlockEntity.setBlockWithoutNeighborUpdates(
                    helper.getLevel(),
                    helper.absolutePos(CRYSTAL_SUPPORT),
                    Blocks.STONE.defaultBlockState()
            );
            TemporaryHoleBlockEntity.setBlockWithoutNeighborUpdates(
                    helper.getLevel(),
                    helper.absolutePos(PLANT_SUPPORT),
                    Blocks.DIRT.defaultBlockState()
            );
            helper.assertBlockPresent(Blocks.STONE, CRYSTAL_SUPPORT);
            helper.assertBlockPresent(Blocks.DIRT, PLANT_SUPPORT);
            helper.succeed();
        });
    }
}
