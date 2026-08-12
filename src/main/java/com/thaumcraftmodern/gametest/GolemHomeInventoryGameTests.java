package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.GolemCoreType;
import com.thaumcraftmodern.entity.GolemFishingBobberEntity;
import com.thaumcraftmodern.entity.StrawGolemEntity;
import com.thaumcraftmodern.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Runtime contract for TC4 golems attached directly to an inventory. */
@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GolemHomeInventoryGameTests {
    private static final BlockPos CHEST = new BlockPos(2, 1, 2);
    private static final BlockPos GOLEM = CHEST.above();
    private static final BlockPos DROP = GOLEM.east(2);

    private GolemHomeInventoryGameTests() {}

    @GameTest(template = "empty", batch = "golemHomeInventory", timeoutTicks = 160)
    public static void gatherCoreUsesChestItWasPlacedOn(GameTestHelper helper) {
        helper.setBlock(CHEST, Blocks.CHEST);
        helper.setBlock(GOLEM, Blocks.AIR);
        helper.setBlock(DROP, Blocks.AIR);

        StrawGolemEntity golem = ModEntities.STRAW_GOLEM.get().create(helper.getLevel());
        helper.assertTrue(golem != null, "Could not create straw golem");
        BlockPos absoluteGolem = helper.absolutePos(GOLEM);
        golem.moveTo(absoluteGolem.getX() + .5D, absoluteGolem.getY(),
                absoluteGolem.getZ() + .5D, 0F, 0F);
        golem.restrictTo(absoluteGolem, 32);
        golem.setHomeFacing(Direction.UP);
        golem.setCore(GolemCoreType.GATHER);
        helper.getLevel().addFreshEntity(golem);

        BlockPos absoluteDrop = helper.absolutePos(DROP);
        ItemEntity dropped = new ItemEntity(helper.getLevel(),
                absoluteDrop.getX() + .5D, absoluteDrop.getY() + .2D,
                absoluteDrop.getZ() + .5D, new ItemStack(Items.DIAMOND));
        dropped.setPickUpDelay(0);
        helper.getLevel().addFreshEntity(dropped);

        helper.succeedWhen(() -> {
            helper.assertTrue(golem.attachedPos().equals(helper.absolutePos(CHEST)),
                    "Placed golem is not attached to the chest below it");
            Container chest = (Container) helper.getBlockEntity(CHEST);
            helper.assertTrue(chest.countItem(Items.DIAMOND) == 1,
                    "Gather core did not deposit the collected item into its attached chest");
            helper.assertTrue(ChestBlockEntity.getOpenCount(helper.getLevel(), helper.absolutePos(CHEST)) > 0,
                    "Gather core moved the item without opening its chest");
        });
    }

    @GameTest(template = "empty", batch = "golemHomeInventory", timeoutTicks = 200)
    public static void filteredGatherSurvivesEntityNbtReload(GameTestHelper helper) {
        helper.setBlock(CHEST, Blocks.CHEST);
        helper.setBlock(GOLEM, Blocks.AIR);
        helper.setBlock(DROP, Blocks.AIR);

        StrawGolemEntity original = ModEntities.STRAW_GOLEM.get().create(helper.getLevel());
        helper.assertTrue(original != null, "Could not create original straw golem");
        BlockPos absoluteGolem = helper.absolutePos(GOLEM);
        original.moveTo(absoluteGolem.getX() + .5D, absoluteGolem.getY(),
                absoluteGolem.getZ() + .5D, 0F, 0F);
        original.restrictTo(absoluteGolem, 32);
        original.setHomeFacing(Direction.UP);
        original.setCore(GolemCoreType.GATHER);
        original.filters().setItem(1, new ItemStack(Items.GOLD_INGOT));

        CompoundTag saved = new CompoundTag();
        helper.assertTrue(original.save(saved), "Could not serialize configured gather golem");
        StrawGolemEntity restored = ModEntities.STRAW_GOLEM.get().create(helper.getLevel());
        helper.assertTrue(restored != null, "Could not create restored straw golem");
        restored.load(saved);
        helper.assertTrue(restored.core() == GolemCoreType.GATHER,
                "Entity reload lost the gather core");
        helper.assertTrue(restored.homePos().equals(absoluteGolem)
                        && restored.attachedPos().equals(helper.absolutePos(CHEST)),
                "Entity reload lost the attached home inventory");
        helper.assertTrue(restored.filters().getItem(1).is(Items.GOLD_INGOT),
                "Entity reload lost the configured gold-ingot filter");
        helper.assertTrue(helper.getLevel().addFreshEntity(restored),
                "Could not spawn restored gather golem");

        BlockPos absoluteDrop = helper.absolutePos(DROP);
        ItemEntity rejected = new ItemEntity(helper.getLevel(), absoluteDrop.getX() + .5D,
                absoluteDrop.getY() + .2D, absoluteDrop.getZ() + .5D,
                new ItemStack(Items.IRON_INGOT));
        rejected.setPickUpDelay(0);
        helper.getLevel().addFreshEntity(rejected);
        ItemEntity accepted = new ItemEntity(helper.getLevel(), absoluteDrop.getX() + .25D,
                absoluteDrop.getY() + .2D, absoluteDrop.getZ() + .25D,
                new ItemStack(Items.GOLD_INGOT, 2));
        accepted.setPickUpDelay(0);
        helper.getLevel().addFreshEntity(accepted);

        helper.succeedWhen(() -> {
            Container chest = (Container) helper.getBlockEntity(CHEST);
            helper.assertTrue(chest.countItem(Items.GOLD_INGOT) == 2,
                    "Reloaded gather golem did not collect and deposit its filtered item");
            helper.assertTrue(chest.countItem(Items.IRON_INGOT) == 0 && rejected.isAlive(),
                    "Reloaded gather golem ignored its restored item filter");
        });
    }

    @GameTest(template = "empty", batch = "golemFishing", timeoutTicks = 620)
    public static void fishingCoreCastsBobberAndProducesCatch(GameTestHelper helper) {
        BlockPos golemPos = new BlockPos(2, 2, 2);
        for (int x = 4; x <= 7; x++) for (int z = 1; z <= 4; z++) {
            helper.setBlock(new BlockPos(x, 1, z), Blocks.WATER);
            helper.setBlock(new BlockPos(x, 2, z), Blocks.AIR);
        }
        StrawGolemEntity golem = ModEntities.STRAW_GOLEM.get().create(helper.getLevel());
        helper.assertTrue(golem != null, "Could not create fishing golem");
        BlockPos absoluteGolem = helper.absolutePos(golemPos);
        golem.moveTo(absoluteGolem.getX() + .5D, absoluteGolem.getY(), absoluteGolem.getZ() + .5D, 0F, 0F);
        golem.restrictTo(absoluteGolem, 32);
        golem.setCore(GolemCoreType.FISHING);
        helper.getLevel().addFreshEntity(golem);
        boolean[] sawBobber = {false};

        helper.succeedWhen(() -> {
            if (!helper.getLevel().getEntitiesOfClass(GolemFishingBobberEntity.class,
                    golem.getBoundingBox().inflate(16D)).isEmpty()) sawBobber[0] = true;
            helper.assertTrue(sawBobber[0], "Fishing core never spawned its bobber");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    golem.getBoundingBox().inflate(16D), item -> item.isAlive()
                            && item.onGround()
                            && item.level().getFluidState(item.blockPosition()).isEmpty())
                    .stream().findAny().isPresent(),
                    "Fishing core did not land its catch on a dry block");
        });
    }
}
