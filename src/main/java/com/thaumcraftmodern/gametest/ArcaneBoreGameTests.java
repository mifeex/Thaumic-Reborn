package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.ArcaneBoreBaseBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneBoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ArcaneBoreGameTests {
    private static final BlockPos BASE = new BlockPos(2, 1, 3);
    private static final BlockPos BORE = BASE.above();
    private ArcaneBoreGameTests() { }

    @GameTest(template = "empty", batch = "arcaneBore", timeoutTicks = 160)
    public static void poweredBoreMinesAndEjectsThroughBase(GameTestHelper helper) {
        Rig rig = rig(helper, true);
        helper.setBlock(BASE.east(), Blocks.CHEST);
        for (int x = 5; x <= 7; x++) for (int y = 0; y <= 4; y++)
            for (int z = 0; z <= 6; z++) helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
        tick(helper, rig.bore(), 100);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(BASE.east());
        helper.assertTrue(!chest.isEmpty(), "Arcane Bore did not eject into the base nozzle inventory");
        helper.assertTrue(rig.bore().getItem(1).getDamageValue() > 0,
                "Arcane Bore did not damage the installed pickaxe");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "arcaneBore", timeoutTicks = 80)
    public static void boreRequiresRedstone(GameTestHelper helper) {
        Rig rig = rig(helper, false);
        BlockPos target = BORE.east(3);
        helper.setBlock(target, Blocks.STONE);
        tick(helper, rig.bore(), 70);
        helper.assertBlockPresent(Blocks.STONE, target);
        helper.assertTrue(rig.bore().getItem(1).getDamageValue() == 0,
                "Unpowered Arcane Bore used its pickaxe");
        helper.succeed();
    }

    private static Rig rig(GameTestHelper helper, boolean powered) {
        helper.setBlock(BASE, ModBlocks.ARCANE_BORE_BASE.get());
        helper.setBlock(BORE, ModBlocks.ARCANE_BORE.get());
        ArcaneBoreBaseBlockEntity base = (ArcaneBoreBaseBlockEntity)
                helper.getBlockEntity(BASE);
        ArcaneBoreBlockEntity bore = (ArcaneBoreBlockEntity) helper.getBlockEntity(BORE);
        base.setOutput(Direction.EAST);
        bore.configurePlacement(Direction.UP, Direction.EAST);
        bore.setItem(0, new ItemStack(ModItems.ARCANE_RECIPE_COMPONENTS
                .get("focus_excavation").get()));
        bore.setItem(1, new ItemStack(Items.IRON_PICKAXE));
        if (powered) helper.setBlock(BASE.west(), Blocks.REDSTONE_BLOCK);
        return new Rig(base, bore);
    }

    private static void tick(GameTestHelper helper, ArcaneBoreBlockEntity bore, int ticks) {
        for (int tick = 0; tick < ticks; tick++) ArcaneBoreBlockEntity.serverTick(
                helper.getLevel(), helper.absolutePos(BORE), bore.getBlockState(), bore);
    }

    private record Rig(ArcaneBoreBaseBlockEntity base, ArcaneBoreBlockEntity bore) { }
}
