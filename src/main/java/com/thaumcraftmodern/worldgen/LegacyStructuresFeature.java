package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeType;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModVillagers;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.world.block.LootVesselBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Procedural modern equivalents of TC4's overworld structures. The Outer
 * Lands dimension and its generated labyrinth are intentionally absent.
 */
public final class LegacyStructuresFeature
        extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation TOWER_LOOT =
            new ResourceLocation(
                    "thaumic_reborn",
                    "chests/wizard_tower"
            );
    private static final ResourceLocation HILLTOP_STONES_LOOT =
            new ResourceLocation(
                    "thaumic_reborn",
                    "chests/hilltop_stones"
            );

    public LegacyStructuresFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!ThaumcraftModernServerConfig.generateStructures()) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int scale = ThaumcraftModernServerConfig.structureRarityScale();
        int chunkMinX = Math.floorDiv(context.origin().getX(), 16) * 16;
        int chunkMinZ = Math.floorDiv(context.origin().getZ(), 16) * 16;
        int x = chunkMinX + random.nextInt(16);
        int z = chunkMinZ + random.nextInt(16);
        BlockPos surface = new BlockPos(
                x,
                level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z),
                z
        );

        if (random.nextInt(150 * scale) == 0) {
            return placeRegistered(
                    LegacyStructureKind.ANCIENT_MOUND,
                    level,
                    surface,
                    random
            );
        }
        if (random.nextInt(66 * scale) == 0) {
            return placeRegistered(
                    LegacyStructureKind.ELDRITCH_RING,
                    level,
                    surface,
                    random
            );
        }
        if (random.nextInt(40 * scale) == 0) {
            return placeRegistered(
                    LegacyStructureKind.HILLTOP_STONES,
                    level,
                    surface,
                    random
            );
        }
        if (random.nextInt(360 * scale) == 0) {
            return placeRegistered(
                    LegacyStructureKind.AURA_TOTEM,
                    level,
                    surface,
                    random
            );
        }
        return false;
    }

    /**
     * Places one registry-selected site. Used by real Structure starts so the
     * sites participate in vanilla locating and map structure bookkeeping.
     */
    static boolean placeRegistered(
            LegacyStructureKind kind,
            WorldGenLevel level,
            BlockPos center,
            RandomSource random
    ) {
        return switch (kind) {
            case ANCIENT_MOUND -> mound(level, center, random);
            case ELDRITCH_RING -> eldritchRing(level, center, random);
            case HILLTOP_STONES -> hilltopStones(level, center, random);
            case AURA_TOTEM -> auraTotem(level, center, random);
            case WIZARD_TOWER, BANKER_HOME -> placeVillageBuilding(
                    kind,
                    level,
                    footprintOrigin(kind, center),
                    Rotation.NONE,
                    random
            );
        };
    }

    static boolean placeVillageBuilding(
            LegacyStructureKind kind,
            WorldGenLevel level,
            BlockPos origin,
            Rotation rotation,
            RandomSource random
    ) {
        int width = kind == LegacyStructureKind.WIZARD_TOWER ? 7 : 4;
        int depth = kind == LegacyStructureKind.WIZARD_TOWER ? 6 : 5;
        if (!StructureSitePolicy.hasDrySupportedFloor(
                level,
                origin,
                width,
                depth,
                rotation
        )) {
            return false;
        }
        int height = kind == LegacyStructureKind.WIZARD_TOWER ? 12 : 6;
        if (!StructureSitePolicy.hasDryReplaceableClearance(
                level,
                origin,
                width,
                depth,
                height,
                rotation
        )) {
            return false;
        }
        boolean placed = switch (kind) {
            case WIZARD_TOWER ->
                    wizardTowerAtOrigin(level, origin, rotation, random);
            case BANKER_HOME ->
                    bankerHomeAtOrigin(level, origin, rotation, random);
            default -> false;
        };
        if (placed) {
            ServerLevel serverLevel = level instanceof ServerLevel server
                    ? server
                    : level instanceof net.minecraft.server.level.WorldGenRegion region
                            ? region.getLevel()
                            : null;
            if (serverLevel != null) {
                LegacyStructureMarkerIndex.get(serverLevel).record(
                        kind,
                        origin
                );
            }
        }
        return placed;
    }

    private static BlockPos footprintOrigin(
            LegacyStructureKind kind,
            BlockPos center
    ) {
        return switch (kind) {
            case ANCIENT_MOUND -> center.offset(-9, -9, -9);
            case ELDRITCH_RING -> center.offset(-3, 0, -3);
            case HILLTOP_STONES -> center.offset(-3, 0, -3);
            case AURA_TOTEM -> center.offset(-1, 0, -1);
            case WIZARD_TOWER -> center.offset(-3, 0, -3);
            case BANKER_HOME -> center.offset(-1, 0, -2);
        };
    }

    private static int footprintWidth(LegacyStructureKind kind) {
        return switch (kind) {
            case ANCIENT_MOUND -> 19;
            case ELDRITCH_RING, HILLTOP_STONES, WIZARD_TOWER -> 7;
            case AURA_TOTEM -> 3;
            case BANKER_HOME -> 4;
        };
    }

    private static int footprintDepth(LegacyStructureKind kind) {
        return switch (kind) {
            case ANCIENT_MOUND -> 19;
            case ELDRITCH_RING, HILLTOP_STONES -> 7;
            case AURA_TOTEM -> 3;
            case WIZARD_TOWER -> 6;
            case BANKER_HOME -> 5;
        };
    }

    private static boolean mound(
            WorldGenLevel level,
            BlockPos center,
            RandomSource random
    ) {
        BlockPos moundCenter = findValidMoundCenter(level, center);
        if (moundCenter == null) {
            return false;
        }
        BlockPos origin = moundCenter.offset(-9, -9, -9);
        if (!hasDrySurfaceColumns(level, origin, 19, 19)) {
            return false;
        }
        AncientMoundBlueprint.place(level, origin);

        placeMoundLootContainer(level, origin.offset(9, 1, 7), random);
        placeMoundLootContainer(level, origin.offset(9, 1, 11), random);

        BlockPos chest = origin.offset(10, 1, 9);
        if (random.nextInt(3) == 0) {
            level.setBlock(
                    chest,
                    Blocks.TRAPPED_CHEST.defaultBlockState(),
                    2
            );
            level.setBlock(
                    origin.offset(10, -1, 9),
                    Blocks.TNT.defaultBlockState(),
                    2
            );
        } else {
            level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2);
        }
        if (level.getBlockEntity(chest)
                instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(
                    new ResourceLocation("minecraft", "chests/simple_dungeon"),
                    random.nextLong()
            );
        }

        placeSpawner(
                level,
                origin.offset(4, 5, 4),
                EntityType.SKELETON,
                random
        );
        placeSpawner(
                level,
                origin.offset(4, 5, 14),
                EntityType.ZOMBIE,
                random
        );
        placeMoundNode(
                level,
                origin.offset(9, 8, 9),
                random
        );
        return true;
    }

    /**
     * Terrain probing belongs to actual chunk generation, never /locate.
     * TC4 tested the selected position only. Its five-point surface check is
     * intentionally not expanded into a modern full-footprint support gate.
     */
    private static BlockPos findValidMoundCenter(
            WorldGenLevel level,
            BlockPos requestedCenter
    ) {
        return hasValidMoundSurface(level, requestedCenter)
                ? requestedCenter
                : null;
    }

    private static void placeMoundLootContainer(
            WorldGenLevel level,
            BlockPos position,
            RandomSource random
    ) {
        float rarity = random.nextFloat();
        int tier = rarity < 0.1F ? 2 : rarity < 0.33F ? 1 : 0;
        LootVesselBlock vessel = (LootVesselBlock) (
                random.nextFloat() < 0.3F
                        ? ModBlocks.LOOT_CRATE.get()
                        : ModBlocks.LOOT_URN.get()
        );
        level.setBlock(position, vessel.stateForTier(tier), 2);
    }

    private static void placeSpawner(
            WorldGenLevel level,
            BlockPos position,
            EntityType<?> entityType,
            RandomSource random
    ) {
        level.setBlock(position, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(position)
                instanceof SpawnerBlockEntity spawner) {
            /*
             * Structure decoration runs on a chunk worker. Supplying the
             * ServerLevel here makes SpawnerBlockEntity broadcast an update,
             * which synchronously asks ServerChunkCache for this not-yet-full
             * chunk. The worker then waits for the server thread while the
             * server thread is waiting for decoration: a hard worldgen
             * deadlock. A null level intentionally suppresses that update;
             * the configured block entity is serialized with the chunk and
             * becomes fully active after the chunk reaches FULL status.
             */
            spawner.getSpawner().setEntityId(
                    entityType,
                    null,
                    random,
                    position
            );
            spawner.setChanged();
        }
    }

    private static void placeMoundNode(
            WorldGenLevel level,
            BlockPos position,
            RandomSource random
    ) {
        placeNode(level, position, AuraNodeType.DARK, random);
        if (level.getBlockEntity(position) instanceof AuraNodeBlockEntity node) {
            node.enableMoundGuardianSpawner();
        }
    }

    private static boolean hasValidMoundSurface(
            WorldGenLevel level,
            BlockPos center
    ) {
        int[][] samples = {
                {0, 0},
                {-9, -9},
                {9, -9},
                {9, 9},
                {-9, 9}
        };
        for (int[] sample : samples) {
            if (!isClassicSpawnSurface(
                    level,
                    center.offset(sample[0], 0, sample[1]),
                    false
            )) {
                return false;
            }
        }
        return true;
    }

    /** Exact TC4 upward search: at most two solid/cover blocks before air. */
    private static boolean isClassicSpawnSurface(
            WorldGenLevel level,
            BlockPos start,
            boolean eldritchRing
    ) {
        int distanceToAir = 0;
        BlockPos cursor = start;
        while (!level.getBlockState(cursor).isAir()) {
            if (!level.getFluidState(cursor).isEmpty()) {
                return false;
            }
            distanceToAir++;
            if (distanceToAir
                    > ClassicStructureSurfacePolicy.MAX_UPWARD_SEARCH
                    || start.getY() + distanceToAir
                            >= level.getMaxBuildHeight()) {
                return false;
            }
            cursor = start.above(distanceToAir);
        }

        BlockPos surface = start.above(distanceToAir - 1);
        BlockPos above = surface.above();
        if (!level.getFluidState(surface).isEmpty()
                || !level.getFluidState(above).isEmpty()
                || !level.getBlockState(above).isAir()) {
            return false;
        }

        BlockState state = level.getBlockState(surface);
        BlockState below = level.getBlockState(surface.below());
        boolean cover = isClassicSurfaceCover(state);
        if (eldritchRing) {
            return ClassicStructureSurfacePolicy.acceptsEldritchRing(
                    state.is(Blocks.STONE),
                    state.is(Blocks.SAND) || state.is(Blocks.RED_SAND),
                    state.is(Blocks.TERRACOTTA),
                    state.is(Blocks.GRASS_BLOCK),
                    state.is(Blocks.GRAVEL),
                    isClassicDirt(state),
                    cover,
                    isClassicEldritchGround(below)
            );
        }
        return ClassicStructureSurfacePolicy.acceptsMoundOrHilltop(
                state.is(Blocks.STONE),
                state.is(Blocks.GRASS_BLOCK),
                isClassicDirt(state),
                cover,
                isClassicMoundOrHilltopGround(below)
        );
    }

    private static boolean isClassicSurfaceCover(BlockState state) {
        return state.is(Blocks.SNOW)
                || state.is(Blocks.GRASS)
                || state.is(Blocks.FERN);
    }

    private static boolean isClassicDirt(BlockState state) {
        return state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL);
    }

    private static boolean isClassicMoundOrHilltopGround(BlockState state) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.GRASS_BLOCK)
                || isClassicDirt(state);
    }

    private static boolean isClassicEldritchGround(BlockState state) {
        return isClassicMoundOrHilltopGround(state)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.TERRACOTTA)
                || state.is(Blocks.GRAVEL);
    }

    /** User-requested addition to TC4: never cover a water surface. */
    private static boolean hasDrySurfaceColumns(
            WorldGenLevel level,
            BlockPos origin,
            int width,
            int depth
    ) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                int worldX = origin.getX() + x;
                int worldZ = origin.getZ() + z;
                int surfaceAirY = level.getHeight(
                        Heightmap.Types.OCEAN_FLOOR_WG,
                        worldX,
                        worldZ
                );
                BlockPos surfaceAir = new BlockPos(
                        worldX,
                        surfaceAirY,
                        worldZ
                );
                if (!level.getFluidState(surfaceAir).isEmpty()
                        || !level.getFluidState(surfaceAir.below()).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean eldritchRing(
            WorldGenLevel level,
            BlockPos requestedPosition,
            RandomSource random
    ) {
        /*
         * Heightmaps return the first air block. TC4 receives a ground block
         * and builds the ring at that Y, so the modern floor belongs one block
         * below the requested air position.
         */
        BlockPos center = requestedPosition.below();
        int[][] samples = {
                {-3, -3},
                {0, 0},
                {3, 0},
                {3, 3},
                {0, 3}
        };
        for (int[] sample : samples) {
            if (!isClassicSpawnSurface(
                    level,
                    center.offset(sample[0], 0, sample[1]),
                    true
            )) {
                return false;
            }
        }
        if (!hasDrySurfaceColumns(level, center.offset(-3, 0, -3), 7, 7)) {
            return false;
        }
        /*
         * TC4 WorldGenEldritchRing is a fixed 7 x 7 altar, not a loose
         * circular ruin. Its foundation extends from y - 4 through y and the
         * four extreme corners are absent.
         */
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (Math.abs(x) == 3 && Math.abs(z) == 3) {
                    continue;
                }
                BlockPos column = center.offset(x, 0, z);
                for (int depth = -4; depth < 0; depth++) {
                    level.setBlock(
                            column.offset(0, depth, 0),
                            random.nextInt(4) == 0
                                    ? Blocks.OBSIDIAN.defaultBlockState()
                                    : ModBlocks.OBSIDIAN_TILE.get()
                                            .defaultBlockState(),
                            2
                    );
                }
                level.setBlock(
                        column,
                        ModBlocks.OBSIDIAN_TILE.get().defaultBlockState(),
                        2
                );
                for (int height = 1; height <= 7; height++) {
                    level.setBlock(
                            column.above(height),
                            Blocks.AIR.defaultBlockState(),
                            2
                    );
                }
                if (isEldritchPedestalPosition(x, z)) {
                    level.setBlock(
                            column.above(),
                            ((com.thaumcraftmodern.world.block.EldritchAltarPartBlock)
                                    ModBlocks.ELDRITCH_ALTAR_PART.get())
                                    .stateForPart(4),
                            2
                    );
                }
            }
        }

        var parts = (com.thaumcraftmodern.world.block.EldritchAltarPartBlock)
                ModBlocks.ELDRITCH_ALTAR_PART.get();
        level.setBlock(center.above(), parts.stateForPart(0), 2);
        placeNode(level, center.above(2), AuraNodeType.DARK, random);
        level.setBlock(center.above(3), parts.stateForPart(1), 2);
        for (int height = 4; height <= 6; height++) {
            level.setBlock(center.above(height), parts.stateForPart(2), 2);
        }
        level.setBlock(center.above(7), parts.stateForPart(3), 2);

        /*
         * TC4 WorldGenEldritchRing assigns variants 1..4 of nextInt(10) to
         * the Crimson Cult spawner: exactly 40% of generated rings. The
         * altar then establishes four diagonal ritualist clerics and grows
         * the local cultist population to eight with knights. This modern
         * structure has no perpetual altar spawner, so materialize the
         * stable original 4 cleric + 4 knight guard composition at generation.
         */
        if (CrimsonCultStructureSpawn.isCultVariant(
                random.nextInt(CrimsonCultStructureSpawn.VARIANT_COUNT)
        )) {
            spawnCrimsonCult(level, center);
        }
        return true;
    }

    private static void spawnCrimsonCult(
            WorldGenLevel level,
            BlockPos center
    ) {
        BlockPos altarPosition = center.above();
        for (CrimsonCultStructureSpawn.Offset offset
                : CrimsonCultStructureSpawn.clericOffsets()) {
            spawnLegacyMob(
                    level,
                    center.offset(offset.x(), 1, offset.z()),
                    LegacyMobKind.CRIMSON_CLERIC,
                    altarPosition,
                    true
            );
        }
        for (CrimsonCultStructureSpawn.Offset offset
                : CrimsonCultStructureSpawn.knightOffsets()) {
            spawnLegacyMob(
                    level,
                    center.offset(offset.x(), 1, offset.z()),
                    LegacyMobKind.CRIMSON_KNIGHT,
                    altarPosition,
                    false
            );
        }
    }

    private static boolean isEldritchPedestalPosition(int x, int z) {
        return ((Math.abs(x) == 3 && Math.abs(z % 2) == 1)
                || (Math.abs(z) == 3 && Math.abs(x % 2) == 1))
                && Math.abs(x) != Math.abs(z);
    }

    private static boolean hilltopStones(
            WorldGenLevel level,
            BlockPos requestedPosition,
            RandomSource random
    ) {
        if (!isHilltopBiome(level, requestedPosition)
                || !hasValidHilltopSurface(level, requestedPosition)) {
            return false;
        }

        BlockPos floorCenter = requestedPosition.offset(
                0,
                HilltopStonesGeneration.FLOOR_OFFSET,
                0
        );
        if (!hasDrySurfaceColumns(
                level,
                floorCenter.offset(-3, 0, -3),
                7,
                7
        )) {
            return false;
        }
        BlockState backfill = hilltopBackfillState(level, floorCenter);
        for (int x = -HilltopStonesGeneration.FOUNDATION_RADIUS;
                x <= HilltopStonesGeneration.FOUNDATION_RADIUS;
                x++) {
            for (int z = -HilltopStonesGeneration.FOUNDATION_RADIUS;
                    z <= HilltopStonesGeneration.FOUNDATION_RADIUS;
                    z++) {
                if (!HilltopStonesGeneration.isFoundationPosition(x, z)) {
                    continue;
                }
                BlockPos floor = floorCenter.offset(x, 0, z);
                level.setBlock(floor, hilltopFoundation(random), 2);
                for (int depth = 1;
                        depth <= HilltopStonesGeneration.BACKFILL_DEPTH;
                        depth++) {
                    BlockPos target = floor.below(depth);
                    if (isHilltopBackfillTarget(level.getBlockState(target))) {
                        level.setBlock(target, backfill, 2);
                    }
                }

                if (!HilltopStonesGeneration.isPillarPosition(x, z)) {
                    continue;
                }
                HilltopStonesGeneration.PillarPlan pillar =
                        HilltopStonesGeneration.planPillar(
                                ignored -> random.nextBoolean()
                        );
                for (int height = 1; height <= pillar.height(); height++) {
                    level.setBlock(
                            floor.above(height),
                            ModBlocks.OBSIDIAN_TOTEM.get()
                                    .defaultBlockState(),
                            2
                    );
                }
            }
        }

        BlockPos centralTile = floorCenter.above();
        level.setBlock(
                centralTile,
                ModBlocks.OBSIDIAN_TILE.get().defaultBlockState(),
                2
        );
        BlockPos chest = floorCenter.above(2);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chest)
                instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(HILLTOP_STONES_LOOT, random.nextLong());
        }
        placeSpawner(level, floorCenter, ModEntities.WISP.get(), random);
        placeNode(
                level,
                requestedPosition.above(
                        HilltopStonesGeneration.NODE_HEIGHT
                ),
                AuraNodeType.DARK,
                random
        );
        return true;
    }

    private static BlockState hilltopFoundation(RandomSource random) {
        if (random.nextBoolean()) {
            return ModBlocks.OBSIDIAN_TILE.get().defaultBlockState();
        }
        return random.nextBoolean()
                ? Blocks.OBSIDIAN.defaultBlockState()
                : Blocks.CRYING_OBSIDIAN.defaultBlockState();
    }

    private static boolean isHilltopBiome(
            WorldGenLevel level,
            BlockPos position
    ) {
        var biome = level.getBiome(position);
        return biome.is(ModWorldgenKeys.HAS_HILLTOP_STONES);
    }

    private static boolean hasValidHilltopSurface(
            WorldGenLevel level,
            BlockPos center
    ) {
        if (center.getY() < HilltopStonesGeneration.MINIMUM_Y) {
            return false;
        }
        int[][] samples = {
                {-2, -2},
                {0, 0},
                {2, 0},
                {2, 2},
                {0, 2}
        };
        for (int[] sample : samples) {
            if (!isClassicSpawnSurface(
                    level,
                    center.offset(sample[0], 0, sample[1]),
                    false
            )) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHilltopSurface(
            WorldGenLevel level,
            BlockPos position
    ) {
        BlockState surface = level.getBlockState(position);
        if (surface.is(Blocks.SNOW)) {
            surface = level.getBlockState(position.below());
        }
        return surface.is(Blocks.STONE)
                || surface.is(Blocks.GRASS_BLOCK)
                || surface.is(Blocks.DIRT)
                || surface.is(Blocks.COARSE_DIRT)
                || surface.is(Blocks.PODZOL)
                || surface.is(Blocks.SNOW_BLOCK);
    }

    private static BlockState hilltopBackfillState(
            WorldGenLevel level,
            BlockPos center
    ) {
        BlockPos surface = center.below();
        BlockState state = level.getBlockState(surface);
        if (state.is(Blocks.SNOW)) {
            state = level.getBlockState(surface.below());
        }
        return state;
    }

    private static boolean isHilltopBackfillTarget(BlockState state) {
        return state.isAir()
                || state.is(Blocks.GRASS)
                || state.is(Blocks.SNOW)
                || state.is(BlockTags.FLOWERS);
    }

    private static boolean wizardTower(
            WorldGenLevel level,
            BlockPos base,
            RandomSource random
    ) {
        return wizardTowerAtOrigin(
                level,
                base.offset(-3, 0, -3),
                Rotation.NONE,
                random
        );
    }

    private static boolean wizardTowerAtOrigin(
            WorldGenLevel level,
            BlockPos origin,
            Rotation rotation,
            RandomSource random
    ) {
        /*
         * Direct port of TC4 4.2.3.5 ComponentWizardTower. The original
         * village component uses local coordinates 1..5 inside a 5 x 12 x 5
         * tower, with its approach at local z=0. Keep the otherwise unused
         * local coordinate zero so every placement below matches the source.
         */
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();

        fillRotated(level, origin, 2, 1, 2, 4, 11, 4, air, rotation);
        fillRotated(level, origin, 2, 0, 2, 4, 0, 4, planks, rotation);
        fillRotated(level, origin, 2, 5, 2, 4, 5, 4, planks, rotation);
        fillRotated(level, origin, 2, 10, 2, 4, 10, 4, planks, rotation);

        fillRotated(level, origin, 1, 0, 2, 1, 11, 4, cobblestone, rotation);
        fillRotated(level, origin, 2, 0, 1, 4, 11, 1, cobblestone, rotation);
        fillRotated(level, origin, 5, 0, 2, 5, 11, 4, cobblestone, rotation);
        fillRotated(level, origin, 2, 0, 5, 4, 11, 5, cobblestone, rotation);
        for (int y : new int[]{0, 5, 10}) {
            setRotatedLocal(level, origin, 1, y, 1, cobblestone, rotation);
            setRotatedLocal(level, origin, 1, y, 5, cobblestone, rotation);
            setRotatedLocal(level, origin, 5, y, 1, cobblestone, rotation);
            setRotatedLocal(level, origin, 5, y, 5, cobblestone, rotation);
        }

        /*
         * Worldgen uses update flag 2, so pane neighbour shapes are not
         * recalculated immediately. Persist the wall connection explicitly;
         * otherwise these windows initially appear as thin isolated posts.
         */
        BlockState glassPane = Blocks.GLASS_PANE.defaultBlockState()
                .setValue(BlockStateProperties.EAST, true)
                .setValue(BlockStateProperties.WEST, true);
        setRotatedLocal(level, origin, 3, 7, 1, glassPane, rotation);
        setRotatedLocal(level, origin, 3, 8, 1, glassPane, rotation);
        setRotatedLocal(level, origin, 3, 7, 5, glassPane, rotation);
        setRotatedLocal(level, origin, 3, 8, 5, glassPane, rotation);
        setRotatedLocal(level, origin, 3, 2, 5, glassPane, rotation);
        setRotatedLocal(level, origin, 3, 3, 5, glassPane, rotation);

        BlockState ladder = Blocks.LADDER.defaultBlockState()
                .setValue(LadderBlock.FACING, Direction.WEST);
        for (int y = 1; y <= 9; y++) {
            setRotatedLocal(level, origin, 4, y, 3, ladder, rotation);
        }
        setRotatedLocal(
                level,
                origin,
                4,
                10,
                3,
                Blocks.OAK_TRAPDOOR.defaultBlockState()
                        .setValue(TrapDoorBlock.FACING, Direction.WEST),
                rotation
        );
        setRotatedLocal(
                level,
                origin,
                3,
                5,
                3,
                Blocks.GLOWSTONE.defaultBlockState(),
                rotation
        );

        BlockPos chest = rotatedLocal(origin, 2, 6, 2, rotation);
        level.setBlock(
                chest,
                Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.FACING, Direction.EAST)
                        .rotate(rotation),
                2
        );
        if (level.getBlockEntity(chest)
                instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(TOWER_LOOT, random.nextLong());
        }

        placeDoor(
                level,
                rotatedLocal(origin, 3, 1, 1, rotation),
                rotation.rotate(Direction.NORTH)
        );
        BlockPos step = rotatedLocal(origin, 3, 0, 0, rotation);
        if (canPlaceDryEntranceStep(level, step)) {
            level.setBlock(
                    step,
                    Blocks.COBBLESTONE_STAIRS.defaultBlockState()
                            .setValue(
                                    StairBlock.FACING,
                                    rotation.rotate(Direction.SOUTH)
                            ),
                    2
            );
        }

        spawnVillager(
                level,
                rotatedLocal(origin, 3, 1, 3, rotation),
                ModVillagers.THAUMATURGE.get()
        );
        return true;
    }

    private static boolean bankerHome(
            WorldGenLevel level,
            BlockPos base,
            RandomSource random
    ) {
        return bankerHomeAtOrigin(
                level,
                base.offset(-1, 0, -2),
                Rotation.NONE,
                random
        );
    }

    private static boolean bankerHomeAtOrigin(
            WorldGenLevel level,
            BlockPos origin,
            Rotation rotation,
            RandomSource random
    ) {
        /*
         * Direct port of ComponentBankerHome. It has no chest and no Arcane
         * Workbench in TC4: the only table is optionally an oak fence with a
         * wooden pressure plate.
         */
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();

        fillRotated(level, origin, 1, 1, 1, 2, 5, 3, air, rotation);
        fillRotated(level, origin, 0, 0, 0, 3, 0, 4, cobblestone, rotation);
        fillRotated(
                level,
                origin,
                1,
                0,
                1,
                2,
                0,
                3,
                Blocks.DIRT.defaultBlockState(),
                rotation
        );

        boolean tallHouse = random.nextBoolean();
        int tablePosition = random.nextInt(3);
        int roofCenterY = tallHouse ? 4 : 5;
        fillRotated(
                level,
                origin,
                1,
                roofCenterY,
                1,
                2,
                roofCenterY,
                3,
                log,
                rotation
        );
        for (int x = 1; x <= 2; x++) {
            setRotatedLocal(level, origin, x, 4, 0, log, rotation);
            setRotatedLocal(level, origin, x, 4, 4, log, rotation);
        }
        for (int z = 1; z <= 3; z++) {
            setRotatedLocal(level, origin, 0, 4, z, log, rotation);
            setRotatedLocal(level, origin, 3, 4, z, log, rotation);
        }

        fillRotated(level, origin, 0, 1, 0, 0, 3, 0, log, rotation);
        fillRotated(level, origin, 3, 1, 0, 3, 3, 0, log, rotation);
        fillRotated(level, origin, 0, 1, 4, 0, 3, 4, log, rotation);
        fillRotated(level, origin, 3, 1, 4, 3, 3, 4, log, rotation);
        fillRotated(level, origin, 0, 1, 1, 0, 3, 3, planks, rotation);
        fillRotated(level, origin, 3, 1, 1, 3, 3, 3, planks, rotation);
        fillRotated(level, origin, 1, 1, 0, 2, 3, 0, planks, rotation);
        fillRotated(level, origin, 1, 1, 4, 2, 3, 4, planks, rotation);

        BlockState bars = Blocks.IRON_BARS.defaultBlockState()
                .setValue(BlockStateProperties.NORTH, true)
                .setValue(BlockStateProperties.SOUTH, true);
        setRotatedLocal(level, origin, 0, 2, 2, bars, rotation);
        setRotatedLocal(level, origin, 3, 2, 2, bars, rotation);
        if (tablePosition > 0) {
            setRotatedLocal(
                    level,
                    origin,
                    tablePosition,
                    1,
                    3,
                    Blocks.OAK_FENCE.defaultBlockState(),
                    rotation
            );
            setRotatedLocal(
                    level,
                    origin,
                    tablePosition,
                    2,
                    3,
                    Blocks.OAK_PRESSURE_PLATE.defaultBlockState(),
                    rotation
            );
        }

        placeDoor(
                level,
                rotatedLocal(origin, 1, 1, 0, rotation),
                rotation.rotate(Direction.NORTH)
        );
        BlockPos step = rotatedLocal(origin, 1, 0, -1, rotation);
        if (canPlaceDryEntranceStep(level, step)) {
            level.setBlock(
                    step,
                    Blocks.COBBLESTONE_STAIRS.defaultBlockState()
                            .setValue(
                                    StairBlock.FACING,
                                    rotation.rotate(Direction.SOUTH)
                            ),
                    2
            );
        }
        spawnVillager(
                level,
                rotatedLocal(origin, 1, 1, 2, rotation),
                ModVillagers.THAUMIC_BANKER.get()
        );
        return true;
    }

    private static boolean canPlaceDryEntranceStep(
            WorldGenLevel level,
            BlockPos step
    ) {
        BlockPos support = step.below();
        return level.getFluidState(step).isEmpty()
                && level.getFluidState(support).isEmpty()
                && level.getBlockState(support).isFaceSturdy(
                        level,
                        support,
                        Direction.UP
                );
    }

    private static void fill(
            WorldGenLevel level,
            BlockPos origin,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ,
            BlockState state
    ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    setLocal(level, origin, x, y, z, state);
                }
            }
        }
    }

    private static void fillRotated(
            WorldGenLevel level,
            BlockPos origin,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ,
            BlockState state,
            Rotation rotation
    ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    setRotatedLocal(
                            level,
                            origin,
                            x,
                            y,
                            z,
                            state,
                            rotation
                    );
                }
            }
        }
    }

    private static void setRotatedLocal(
            WorldGenLevel level,
            BlockPos origin,
            int x,
            int y,
            int z,
            BlockState state,
            Rotation rotation
    ) {
        level.setBlock(
                rotatedLocal(origin, x, y, z, rotation),
                state.rotate(rotation),
                2
        );
    }

    private static BlockPos rotatedLocal(
            BlockPos origin,
            int x,
            int y,
            int z,
            Rotation rotation
    ) {
        return StructureSitePolicy.rotated(origin, x, y, z, rotation);
    }

    private static void placeDoor(
            WorldGenLevel level,
            BlockPos lowerPosition,
            Direction facing
    ) {
        BlockState door = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing);
        level.setBlock(
                lowerPosition,
                door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER),
                2
        );
        level.setBlock(
                lowerPosition.above(),
                door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                2
        );
    }

    private static void setLocal(
            WorldGenLevel level,
            BlockPos origin,
            int x,
            int y,
            int z,
            BlockState state
    ) {
        level.setBlock(local(origin, x, y, z), state, 2);
    }

    private static BlockPos local(
            BlockPos origin,
            int x,
            int y,
            int z
    ) {
        return origin.offset(x, y, z);
    }

    private static boolean auraTotem(
            WorldGenLevel level,
            BlockPos surfaceAir,
            RandomSource random
    ) {
        BlockPos base = resolveAuraTotemBase(level, surfaceAir);
        if (base == null) {
            return false;
        }
        if (!level.getFluidState(base).isEmpty()
                || !isDryTotemReplaceable(level, base.above())
                || !isDryTotemReplaceable(level, base.above(2))) {
            return false;
        }
        level.setBlock(
                base,
                ModBlocks.OBSIDIAN_TILE.get().defaultBlockState(),
                2
        );
        for (int y = 1; y < AuraTotemGeneration.MAX_NODE_HEIGHT; y++) {
            BlockPos position = base.above(y);
            if (!isDryTotemReplaceable(level, position)) {
                return false;
            }
            if (y > 1 && AuraTotemGeneration.isNodeLevel(
                    y,
                    random.nextInt(4)
            )) {
                placeTotemNode(level, position, random);
                return true;
            }
            level.setBlock(
                    position,
                    ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState(),
                    2
            );
        }
        placeTotemNode(
                level,
                base.above(AuraTotemGeneration.MAX_NODE_HEIGHT),
                random
        );
        return true;
    }

    private static boolean isDryTotemReplaceable(
            WorldGenLevel level,
            BlockPos position
    ) {
        return level.getFluidState(position).isEmpty()
                && AuraTotemGeneration.isReplaceable(
                        level.getBlockState(position)
                );
    }

    private static BlockPos resolveAuraTotemBase(
            WorldGenLevel level,
            BlockPos surfaceAir
    ) {
        BlockPos base = surfaceAir.below();
        BlockState state = level.getBlockState(base);
        if (state.is(BlockTags.LEAVES)) {
            while (base.getY() > AuraTotemGeneration.MIN_LEAF_SEARCH_Y) {
                base = base.below();
                state = level.getBlockState(base);
                if (state.is(Blocks.GRASS_BLOCK)) {
                    break;
                }
            }
        }
        if (AuraTotemGeneration.isSurfaceCover(state)) {
            base = base.below();
            state = level.getBlockState(base);
        }
        return AuraTotemGeneration.isValidBase(state) ? base : null;
    }

    private static void placeTotemNode(
            WorldGenLevel level,
            BlockPos position,
            RandomSource random
    ) {
        level.setBlock(
                position,
                ModBlocks.OBSIDIAN_TOTEM_NODE.get().defaultBlockState(),
                2
        );
        if (level.getBlockEntity(position) instanceof AuraNodeBlockEntity node) {
            node.initializeOnce(
                    ClassicAuraNodeWorldFactory.createEerie(
                            level,
                            position,
                            random
                    )
            );
        }
    }

    private static void placeNode(
            WorldGenLevel level,
            BlockPos position,
            AuraNodeType type,
            RandomSource random
    ) {
        if (type != AuraNodeType.DARK) {
            throw new IllegalArgumentException(
                    "legacy structure nodes are eerie DARK nodes"
            );
        }
        level.setBlock(
                position,
                ModBlocks.AURA_NODE.get().defaultBlockState(),
                2
        );
        if (level.getBlockEntity(position) instanceof AuraNodeBlockEntity node) {
            node.initializeOnce(
                    ClassicAuraNodeWorldFactory.createEerie(
                            level,
                            position,
                            random
                    )
            );
        }
    }

    private static void spawnVillager(
            WorldGenLevel level,
            BlockPos position,
            net.minecraft.world.entity.npc.VillagerProfession profession
    ) {
        Villager villager = EntityType.VILLAGER.create(level.getLevel());
        if (villager == null) {
            return;
        }
        villager.setVillagerData(
                villager.getVillagerData().setProfession(profession)
        );
        villager.moveTo(
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D,
                0.0F,
                0.0F
        );
        villager.setPersistenceRequired();
        level.addFreshEntity(villager);
    }

    private static void spawnLegacyMob(
            WorldGenLevel level,
            BlockPos position,
            LegacyMobKind kind
    ) {
        spawnLegacyMob(level, position, kind, null, false);
    }

    private static void spawnLegacyMob(
            WorldGenLevel level,
            BlockPos position,
            LegacyMobKind kind,
            BlockPos crimsonAltar,
            boolean ritualist
    ) {
        var mob = ModEntities.forKind(kind).get().create(level.getLevel());
        if (mob == null) {
            return;
        }
        mob.moveTo(
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D,
                0.0F,
                0.0F
        );
        mob.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(position),
                MobSpawnType.STRUCTURE,
                null,
                null
        );
        if (crimsonAltar != null) {
            mob.configureCrimsonAltar(crimsonAltar, ritualist);
        }
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
    }

    private static boolean isGround(WorldGenLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.isSolidRender(level, position)
                && !state.hasProperty(BlockStateProperties.WATERLOGGED);
    }
}
