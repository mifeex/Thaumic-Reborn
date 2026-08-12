package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.AdvancedAlchemicalFurnaceBlock;
import com.thaumcraftmodern.world.block.ArcaneBellowsBlock;
import com.thaumcraftmodern.world.block.entity.AdvancedAlchemicalFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdvancedAlchemicalFurnaceBellowsGameTests {
    private AdvancedAlchemicalFurnaceBellowsGameTests() {
    }

    @GameTest(template = "infusion_empty", batch = "advancedFurnaceBellows",
            timeoutTicks = 20)
    public static void inwardUnpoweredBellowsAccelerateTheFurnace(
            GameTestHelper helper
    ) {
        BlockPos center = new BlockPos(5, 2, 5);
        BlockPos lowerBellows = center.offset(2, 0, 0);
        BlockPos upperBellows = center.offset(2, 1, 0);
        AdvancedAlchemicalFurnaceBlock furnaceBlock =
                (AdvancedAlchemicalFurnaceBlock) ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get();
        BlockState inwardBellows = ModBlocks.ARCANE_BELLOWS.get()
                .defaultBlockState().setValue(ArcaneBellowsBlock.FACING, Direction.WEST);

        helper.setBlock(center, furnaceBlock.stateForPart(AdvancedAlchemicalFurnaceBlock.CENTER));
        helper.setBlock(lowerBellows, inwardBellows);
        helper.setBlock(upperBellows, inwardBellows);

        AdvancedAlchemicalFurnaceBlockEntity furnace =
                (AdvancedAlchemicalFurnaceBlockEntity) helper.getBlockEntity(center);
        helper.assertTrue(furnace.attachedBellows() == 2,
                "Two inward-facing bellows were not detected");

        helper.setBlock(upperBellows.above(), Blocks.REDSTONE_BLOCK);
        helper.assertTrue(furnace.attachedBellows() == 1,
                "A redstone-powered bellow still accelerated the furnace");
        helper.succeed();
    }
}
