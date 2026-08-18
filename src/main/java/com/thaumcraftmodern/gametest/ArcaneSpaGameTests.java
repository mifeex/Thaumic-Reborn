package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.ArcaneSpaBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ArcaneSpaGameTests {
    private static final BlockPos SPA = new BlockPos(3, 1, 3);

    private ArcaneSpaGameTests() {
    }

    @GameTest(template = "empty", batch = "arcaneSpa", timeoutTicks = 40)
    public static void mixesWaterAndSaltIntoPurifyingFluid(GameTestHelper helper) {
        prepareFloor(helper);
        ArcaneSpaBlockEntity spa = spa(helper);
        spa.fluidTank().fill(new FluidStack(Fluids.WATER, 1000),
                IFluidHandler.FluidAction.EXECUTE);
        spa.setItem(0, new ItemStack(ModItems.BATH_SALTS.get()));

        tickForty(helper, spa);

        helper.assertBlockPresent(ModBlocks.PURIFYING_FLUID.get(), SPA.above());
        helper.assertTrue(spa.fluidTank().isEmpty(),
                "Arcane Spa did not consume exactly one bucket of water");
        helper.assertTrue(spa.getItem(0).isEmpty(),
                "Arcane Spa did not consume one bath salt");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "arcaneSpa", timeoutTicks = 40)
    public static void redstoneDisablesDispensing(GameTestHelper helper) {
        prepareFloor(helper);
        ArcaneSpaBlockEntity spa = spa(helper);
        spa.fluidTank().fill(new FluidStack(Fluids.WATER, 1000),
                IFluidHandler.FluidAction.EXECUTE);
        spa.setItem(0, new ItemStack(ModItems.BATH_SALTS.get()));
        helper.setBlock(SPA.east(), Blocks.REDSTONE_BLOCK);

        tickForty(helper, spa);

        helper.assertBlockNotPresent(ModBlocks.PURIFYING_FLUID.get(), SPA.above());
        helper.assertTrue(spa.fluidTank().getFluidAmount() == 1000,
                "A powered Arcane Spa consumed its tank");
        helper.assertTrue(spa.getItem(0).getCount() == 1,
                "A powered Arcane Spa consumed bath salts");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "arcaneSpa", timeoutTicks = 40)
    public static void rawModeBuildsConnectedFiveByFiveSurface(GameTestHelper helper) {
        prepareFloor(helper);
        ArcaneSpaBlockEntity spa = spa(helper);
        spa.toggleMix();
        for (int placement = 0; placement < 25; placement++) {
            spa.fluidTank().fill(new FluidStack(Fluids.WATER, 1000),
                    IFluidHandler.FluidAction.EXECUTE);
            tickForty(helper, spa);
        }

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                helper.assertTrue(helper.getBlockState(SPA.above().offset(x, 0, z))
                                .getFluidState().isSourceOfType(Fluids.WATER),
                        "Arcane Spa left a hole in its original 5x5 surface");
            }
        }
        helper.assertTrue(spa.fluidTank().isEmpty(),
                "Raw mode did not consume one bucket per placed source");
        helper.succeed();
    }

    private static void prepareFloor(GameTestHelper helper) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                helper.setBlock(SPA.offset(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(SPA, ModBlocks.ARCANE_SPA.get());
    }

    private static ArcaneSpaBlockEntity spa(GameTestHelper helper) {
        if (helper.getBlockEntity(SPA) instanceof ArcaneSpaBlockEntity spa) return spa;
        throw new IllegalStateException("Arcane Spa block entity was not created");
    }

    private static void tickForty(GameTestHelper helper, ArcaneSpaBlockEntity spa) {
        for (int tick = 0; tick < ArcaneSpaBlockEntity.DISPENSE_INTERVAL; tick++) {
            ArcaneSpaBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(SPA),
                    spa.getBlockState(), spa);
        }
    }
}
