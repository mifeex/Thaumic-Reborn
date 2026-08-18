package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.construction.CraftingStructureDisassembly;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.essentia.EssentiaAirHandler;
import com.thaumcraftmodern.infusion.InfusionRecipeDefinition;
import com.thaumcraftmodern.infusion.InfusionRecipeRegistry;
import com.thaumcraftmodern.infusion.InfusionInstability;
import com.thaumcraftmodern.infusion.InfusionStability;
import com.thaumcraftmodern.item.PrimordialPearlItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.NodeZapPacket;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEffects;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.research.ResearchProgressService;
import com.thaumcraftmodern.world.block.CrystalClusterBlock;
import com.thaumcraftmodern.world.block.EldritchCrystalBlock;
import com.thaumcraftmodern.world.block.RunicMatrixBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Server-authoritative first TC4 infusion vertical. */
public final class RunicMatrixBlockEntity extends BlockEntity {
    public static final int ESSENTIA_RANGE = 12;
    public static final int CYCLE_TICKS = 10;
    public static final int COMPONENT_CHARGE_CYCLES = 5;

    public enum StartResult {
        STARTED,
        ALREADY_CRAFTING,
        INVALID_ALTAR,
        NO_CENTRAL_ITEM,
        NO_RECIPE
    }

    /** Explicit client FX channel matching TC4's two separate source packets. */
    public enum EffectType {
        NONE,
        ESSENTIA,
        COMPONENT,
        COMPLETE
    }

    private boolean crafting;
    private int counter;
    private int componentCharge;
    private int symmetry;
    private int instability;
    private int recipeInstability;
    private int remainingExperienceLevels;
    private @Nullable ResourceLocation recipeId;
    private @Nullable UUID ownerId;
    private ItemStack centralSnapshot = ItemStack.EMPTY;
    private final LinkedHashMap<String, Integer> remainingEssentia = new LinkedHashMap<>();
    private final List<Integer> remainingComponents = new ArrayList<>();
    private @Nullable BlockPos effectSource;
    private int effectColor = 0xAA33FF;
    private EffectType effectType = EffectType.NONE;
    private long effectUntil;
    private int clientSoundTicks;
    private float clientStartUp;

    public RunicMatrixBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.RUNIC_MATRIX.get(), position, state);
    }

    public static void serverTick(Level rawLevel, BlockPos position,
            BlockState state, RunicMatrixBlockEntity matrix) {
        if (rawLevel.isClientSide) {
            matrix.clientSoundTick(rawLevel);
            return;
        }
        if (!(rawLevel instanceof ServerLevel level)) return;
        matrix.counter++;
        if (state.getValue(RunicMatrixBlock.ACTIVE)
                && matrix.counter % 20 == 0 && !matrix.validLocation()) {
            if (matrix.crafting) matrix.fail(level);
            CraftingStructureDisassembly.invalidInfusionMatrix(level, position);
            return;
        }
        if (!matrix.crafting) return;
        if (matrix.counter % CYCLE_TICKS == 0) {
            matrix.craftCycle(level);
        }
    }

    private void clientSoundTick(Level level) {
        boolean active = getBlockState().getValue(RunicMatrixBlock.ACTIVE);
        if (active && clientStartUp < 1.0F) {
            clientStartUp += Math.max(clientStartUp / 10.0F, 0.001F);
            if (clientStartUp > 0.999F) clientStartUp = 1.0F;
        } else if (!active && clientStartUp > 0.0F) {
            // Preserve the smooth TC4 exponential fold, but finish it twice
            // as quickly so a deactivated matrix does not linger unfolded.
            clientStartUp -= clientStartUp / 5.0F;
            if (clientStartUp < 0.001F) clientStartUp = 0.0F;
        }
        if (!crafting) {
            clientSoundTicks = Math.max(0, clientSoundTicks - 2);
            return;
        }
        if (clientSoundTicks == 0) {
            playLocalInfusionSound(level, ModSounds.INFUSER_START.get());
        } else if (clientSoundTicks % 65 == 0) {
            playLocalInfusionSound(level, ModSounds.INFUSER.get());
        }
        clientSoundTicks++;
    }

    private void playLocalInfusionSound(Level level,
            net.minecraft.sounds.SoundEvent sound) {
        level.playLocalSound(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D,
                sound,
                SoundSource.BLOCKS,
                0.5F,
                1.0F,
                false
        );
    }

    public StartResult startCrafting(ServerPlayer player) {
        if (crafting) return StartResult.ALREADY_CRAFTING;
        if (!(level instanceof ServerLevel server) || !validLocation()) {
            return StartResult.INVALID_ALTAR;
        }
        ArcanePedestalBlockEntity center = centerPedestal();
        if (center == null || center.item().isEmpty()) {
            return StartResult.NO_CENTRAL_ITEM;
        }
        List<ArcanePedestalBlockEntity> pedestals = surroundingPedestals();
        List<ItemStack> components = pedestals.stream()
                .map(ArcanePedestalBlockEntity::item)
                .filter(stack -> !stack.isEmpty())
                .map(stack -> {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    return copy;
                }).toList();
        Optional<InfusionRecipeDefinition> match = InfusionRecipeRegistry.findMatching(
                center.item(), components,
                research -> KnowledgeAccess.get(player)
                        .map(knowledge -> knowledge.hasCompletedResearch(research))
                        .orElse(false));
        if (match.isEmpty()) return StartResult.NO_RECIPE;

        InfusionRecipeDefinition recipe = match.get();
        recipeId = recipe.id();
        ownerId = player.getUUID();
        centralSnapshot = center.item().copy();
        centralSnapshot.setCount(1);
        remainingEssentia.clear();
        remainingEssentia.putAll(recipe.effectiveEssentia(centralSnapshot));
        remainingComponents.clear();
        for (int index = 0;
                index < recipe.effectiveComponents(centralSnapshot).size(); index++) {
            remainingComponents.add(index);
        }
        recipeInstability = recipe.effectiveInstability(centralSnapshot);
        remainingExperienceLevels = recipe.experienceLevels(centralSnapshot);
        componentCharge = 0;
        refreshSymmetry();
        instability = symmetry + recipeInstability;
        crafting = true;
        server.playSound(null, worldPosition, ModSounds.CRAFT_SUCCESS.get(),
                SoundSource.BLOCKS, 0.5F, 1.0F);
        sync();
        return StartResult.STARTED;
    }

    private void craftCycle(ServerLevel level) {
        ArcanePedestalBlockEntity center = centerPedestal();
        boolean validInput = center != null && !center.item().isEmpty()
                && ItemStack.isSameItemSameTags(center.item(), centralSnapshot);
        if (!validInput || InfusionInstability.triggers(
                instability,
                level.random.nextInt(500)
        )) {
            runInstabilityEvent(level);
            if (validInput) {
                sync();
                return;
            }
        }
        if (!validInput) {
            fail(level);
            return;
        }
        if (remainingExperienceLevels > 0) {
            List<net.minecraft.world.entity.player.Player> players =
                    level.getEntitiesOfClass(
                            net.minecraft.world.entity.player.Player.class,
                            effectBounds());
            for (net.minecraft.world.entity.player.Player player : players) {
                if (player.experienceLevel <= 0) continue;
                player.giveExperienceLevels(-1);
                remainingExperienceLevels--;
                int damage = level.random.nextInt(2);
                if (damage > 0) player.hurt(level.damageSources().magic(), damage);
                level.playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.PLAYERS, 1.0F,
                        2.0F + level.random.nextFloat() * 0.4F);
                setEffect(player.blockPosition(), 0xCC66FF, 20,
                        EffectType.ESSENTIA);
                sync();
                return;
            }
            if (!players.isEmpty()) addMissingIngredientInstability(level, 3);
            sync();
            return;
        }
        boolean needsEssentia = false;
        for (Map.Entry<String, Integer> entry : remainingEssentia.entrySet()) {
            if (entry.getValue() <= 0) continue;
            needsEssentia = true;
            BlockPos source = drainOneEssentia(level, entry.getKey());
            if (source != null) {
                entry.setValue(entry.getValue() - 1);
                setEffect(source, aspectColor(entry.getKey()), 15,
                        EffectType.ESSENTIA);
                sync();
                return;
            }
            increaseInstabilityRandom(level, Math.max(
                    1,
                    100 - recipeInstability * 3
            ));
        }
        if (needsEssentia) {
            sync();
            return;
        }

        InfusionRecipeDefinition recipe = recipe().orElse(null);
        if (recipe == null) {
            fail(level);
            return;
        }
        if (!remainingComponents.isEmpty()) {
            for (int pendingIndex = 0;
                 pendingIndex < remainingComponents.size();
                 pendingIndex++) {
                int componentIndex = remainingComponents.get(pendingIndex);
                Ingredient ingredient = recipe.effectiveComponents(centralSnapshot)
                        .get(componentIndex);
                ArcanePedestalBlockEntity pedestal = findPedestalWith(ingredient);
                if (pedestal == null) {
                    addMissingIngredientInstability(level, 1 + pendingIndex);
                    continue;
                }
                if (componentCharge == 0) {
                    componentCharge = COMPONENT_CHARGE_CYCLES;
                    setEffect(pedestal.getBlockPos(), 0, 60,
                            EffectType.COMPONENT);
                    sync();
                    return;
                }
                componentCharge--;
                if (componentCharge <= 0) {
                    consumeOne(pedestal);
                    remainingComponents.remove(pendingIndex);
                    componentCharge = 0;
                }
                sync();
                return;
            }
            componentCharge = 0;
            sync();
            return;
        }
        finish(level, center, recipe);
    }

    private @Nullable BlockPos drainOneEssentia(ServerLevel level, String aspect) {
        return EssentiaAirHandler.drain(
                level,
                worldPosition,
                aspect,
                null,
                ESSENTIA_RANGE,
                false
        );
    }

    private void increaseInstabilityRandom(ServerLevel level, int bound) {
        if (level.random.nextInt(Math.max(1, bound)) == 0) {
            instability = InfusionInstability.increaseCapped(instability);
        }
    }

    private void addMissingIngredientInstability(
            ServerLevel level,
            int aspectChanceBound
    ) {
        if (remainingEssentia.isEmpty()
                || level.random.nextInt(Math.max(1, aspectChanceBound)) != 0) {
            return;
        }
        List<String> aspects = List.copyOf(remainingEssentia.keySet());
        String aspect = aspects.get(level.random.nextInt(aspects.size()));
        remainingEssentia.merge(aspect, 1, Integer::sum);
        increaseInstabilityRandom(
                level,
                Math.max(1, 50 - recipeInstability * 2)
        );
    }

    private void runInstabilityEvent(ServerLevel level) {
        switch (InfusionInstability.eventForRoll(level.random.nextInt(21))) {
            case EJECT -> instabilityEject(level, 0);
            case EJECT_GOO -> instabilityEject(level, 1);
            case EJECT_GAS -> instabilityEject(level, 2);
            case DESTROY_GOO -> instabilityEject(level, 3);
            case DESTROY_GAS -> instabilityEject(level, 4);
            case EJECT_EXPLODE -> instabilityEject(level, 5);
            case ZAP_ONE -> instabilityZap(level, false);
            case ZAP_ALL -> instabilityZap(level, true);
            case HARM_ONE -> instabilityHarm(level, false);
            case HARM_ALL -> instabilityHarm(level, true);
            case MATRIX_EXPLOSION -> level.explode(
                    null,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D,
                    1.5F + level.random.nextFloat(),
                    Level.ExplosionInteraction.NONE
            );
            case WARP -> instabilityWarp(level);
        }
    }

    private void instabilityZap(ServerLevel level, boolean all) {
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                effectBounds()
        );
        for (LivingEntity target : targets) {
            sendInstabilityZap(level, target.blockPosition());
            DamageSource magic = level.damageSources().magic();
            target.hurt(magic, 4.0F + level.random.nextInt(4));
            if (!all) {
                break;
            }
        }
    }

    private void instabilityHarm(ServerLevel level, boolean all) {
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                effectBounds()
        );
        for (LivingEntity target : targets) {
            if (level.random.nextBoolean()) {
                target.addEffect(new MobEffectInstance(
                        ModEffects.FLUX_TAINT.get(),
                        120,
                        0,
                        false,
                        true
                ));
            } else {
                MobEffectInstance visExhaust = new MobEffectInstance(
                        ModEffects.VIS_EXHAUST.get(),
                        2400,
                        0,
                        false,
                        true
                );
                visExhaust.setCurativeItems(List.of());
                target.addEffect(visExhaust);
            }
            if (!all) {
                break;
            }
        }
    }

    private void instabilityWarp(ServerLevel level) {
        List<ServerPlayer> targets = level.getEntitiesOfClass(
                ServerPlayer.class,
                effectBounds()
        );
        if (targets.isEmpty()) {
            return;
        }
        ServerPlayer target = targets.get(level.random.nextInt(targets.size()));
        if (level.random.nextFloat() < 0.25F) {
            ResearchProgressService.addWarp(
                    target,
                    WarpType.PERMANENT,
                    1,
                    "infusion_instability"
            );
        } else {
            ResearchProgressService.addWarp(
                    target,
                    WarpType.TEMPORARY,
                    1 + level.random.nextInt(5),
                    "infusion_instability"
            );
        }
    }

    private void instabilityEject(ServerLevel level, int type) {
        List<ArcanePedestalBlockEntity> pedestals = surroundingPedestals();
        for (int tries = 0; tries < 50 && !pedestals.isEmpty(); tries++) {
            ArcanePedestalBlockEntity pedestal = pedestals.get(
                    level.random.nextInt(pedestals.size())
            );
            ItemStack stack = pedestal.item();
            if (stack.isEmpty()) {
                continue;
            }
            BlockPos pedestalPosition = pedestal.getBlockPos();
            if (type < 3 || type == 5) {
                Containers.dropItemStack(
                        level,
                        pedestalPosition.getX() + 0.5D,
                        pedestalPosition.getY() + 0.5D,
                        pedestalPosition.getZ() + 0.5D,
                        stack.copy()
                );
            }
            pedestal.setItem(0, ItemStack.EMPTY);

            BlockPos above = pedestalPosition.above();
            if (type == 1 || type == 3) {
                level.setBlock(
                        above,
                        ModBlocks.FLUX_GOO.get().defaultBlockState()
                                .setValue(
                                        com.thaumcraftmodern.world.block.FluxGooBlock.LEVEL,
                                        7
                                ),
                        3
                );
                level.playSound(
                        null,
                        pedestalPosition,
                        net.minecraft.sounds.SoundEvents.GENERIC_SWIM,
                        SoundSource.BLOCKS,
                        0.3F,
                        1.0F
                );
            } else if (type == 2 || type == 4) {
                level.setBlock(
                        above,
                        ModBlocks.FLUX_GAS.get().defaultBlockState()
                                .setValue(
                                        com.thaumcraftmodern.world.block.FluxGasBlock.LEVEL,
                                        7
                                ),
                        3
                );
                level.playSound(
                        null,
                        pedestalPosition,
                        net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.3F,
                        1.0F
                );
            } else if (type == 5) {
                level.explode(
                        null,
                        pedestalPosition.getX() + 0.5D,
                        pedestalPosition.getY() + 0.5D,
                        pedestalPosition.getZ() + 0.5D,
                        1.0F,
                        Level.ExplosionInteraction.BLOCK
                );
            }
            level.blockEvent(
                    pedestalPosition,
                    pedestal.getBlockState().getBlock(),
                    11,
                    0
            );
            sendInstabilityZap(level, pedestalPosition.above());
            return;
        }
    }

    private void sendInstabilityZap(ServerLevel level, BlockPos target) {
        ModNetwork.sendToTrackingChunk(
                level,
                worldPosition,
                new NodeZapPacket(
                        worldPosition,
                        target,
                        level.random.nextLong()
                )
        );
    }

    private AABB effectBounds() {
        return new AABB(worldPosition).inflate(10.0D);
    }

    private void consumeOne(ArcanePedestalBlockEntity pedestal) {
        ItemStack stack = pedestal.item();
        if (stack.isEmpty()) return;
        ItemStack remainder = infusionRemainder(stack);
        pedestal.setInfusionItem(remainder);
        if (level != null) level.blockEvent(pedestal.getBlockPos(),
                pedestal.getBlockState().getBlock(), 11, 0);
    }

    static ItemStack infusionRemainder(ItemStack stack) {
        // A Primordial Pearl survives grid crafting, but infusion consumes it.
        if (stack.getItem() instanceof PrimordialPearlItem) return ItemStack.EMPTY;
        return stack.getCraftingRemainingItem();
    }

    private void finish(ServerLevel level, ArcanePedestalBlockEntity center,
            InfusionRecipeDefinition recipe) {
        center.setInfusionItem(recipe.createResult(center.item()));
        crafting = false;
        instability = 0;
        recipeInstability = 0;
        remainingExperienceLevels = 0;
        componentCharge = 0;
        remainingEssentia.clear();
        remainingComponents.clear();
        effectSource = center.getBlockPos();
        effectColor = 0xCC66FF;
        effectType = EffectType.COMPLETE;
        effectUntil = level.getGameTime() + 20;
        recipeId = null;
        ownerId = null;
        centralSnapshot = ItemStack.EMPTY;
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                worldPosition.getX() + 0.5D, worldPosition.getY() - 1.0D,
                worldPosition.getZ() + 0.5D, 24, 0.5D, 0.7D, 0.5D, 0.05D);
        level.playSound(null, worldPosition, ModSounds.CRAFT_SUCCESS.get(),
                SoundSource.BLOCKS, 1.0F, 1.0F);
        sync();
    }

    private void fail(ServerLevel level) {
        crafting = false;
        recipeId = null;
        ownerId = null;
        centralSnapshot = ItemStack.EMPTY;
        remainingEssentia.clear();
        remainingComponents.clear();
        componentCharge = 0;
        instability = 0;
        recipeInstability = 0;
        remainingExperienceLevels = 0;
        effectType = EffectType.NONE;
        level.playSound(null, worldPosition, ModSounds.CRAFT_FAIL.get(),
                SoundSource.BLOCKS, 1.0F, 0.6F);
        sync();
    }

    public boolean validLocation() {
        if (level == null || !getBlockState().getValue(RunicMatrixBlock.ACTIVE)) return false;
        if (!(level.getBlockEntity(worldPosition.below(2)) instanceof ArcanePedestalBlockEntity)) {
            return false;
        }
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                if (!level.getBlockState(worldPosition.offset(x, -2, z))
                        .is(ModBlocks.INFUSION_PILLAR.get())) return false;
            }
        }
        return true;
    }

    public void refreshSymmetry() {
        if (level == null) {
            symmetry = 0;
            return;
        }
        List<InfusionStability.Pedestal> pedestals = new ArrayList<>();
        List<BlockPos> stabilizers = new ArrayList<>();
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                boolean foundPedestal = false;
                for (int yy = -5; yy <= 10; yy++) {
                    if (x == 0 && z == 0) continue;
                    BlockPos scan = worldPosition.offset(x, -yy, z);
                    if (!level.hasChunkAt(scan)) continue;
                    if (!foundPedestal && yy > 0 && Math.abs(x) <= 8 && Math.abs(z) <= 8
                            && level.getBlockEntity(scan) instanceof ArcanePedestalBlockEntity pedestal) {
                        pedestals.add(new InfusionStability.Pedestal(scan, !pedestal.item().isEmpty()));
                        foundPedestal = true;
                    }
                    if (isStabilizer(scan)) stabilizers.add(scan.immutable());
                }
            }
        }
        symmetry = InfusionStability.symmetry(worldPosition, pedestals, stabilizers,
                pos -> level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity,
                pos -> level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal
                        && !pedestal.item().isEmpty(),
                this::isStabilizer);
    }

    private boolean isStabilizer(BlockPos position) {
        if (level == null || !level.hasChunkAt(position)) return false;
        BlockState state = level.getBlockState(position);
        return state.is(BlockTags.CANDLES)
                || state.getBlock() instanceof AbstractSkullBlock
                || state.getBlock() instanceof CrystalClusterBlock
                || state.getBlock() instanceof EldritchCrystalBlock;
    }

    private List<ArcanePedestalBlockEntity> surroundingPedestals() {
        if (level == null) return List.of();
        List<ArcanePedestalBlockEntity> pedestals = new ArrayList<>();
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                if (x == 0 && z == 0) continue;
                for (int yy = 1; yy <= 10; yy++) {
                    BlockPos scan = worldPosition.offset(x, -yy, z);
                    if (level.getBlockEntity(scan) instanceof ArcanePedestalBlockEntity pedestal) {
                        pedestals.add(pedestal);
                        break;
                    }
                }
            }
        }
        return List.copyOf(pedestals);
    }

    private @Nullable ArcanePedestalBlockEntity findPedestalWith(Ingredient ingredient) {
        for (ArcanePedestalBlockEntity pedestal : surroundingPedestals()) {
            if (ingredient.test(pedestal.item())) return pedestal;
        }
        return null;
    }

    private @Nullable ArcanePedestalBlockEntity centerPedestal() {
        return level != null && level.getBlockEntity(worldPosition.below(2))
                instanceof ArcanePedestalBlockEntity pedestal ? pedestal : null;
    }

    private Optional<InfusionRecipeDefinition> recipe() {
        return recipeId == null ? Optional.empty() : InfusionRecipeRegistry.find(recipeId);
    }

    private int aspectColor(String aspect) {
        return AspectRegistryRuntime.find(aspect).map(AspectDefinition::color).orElse(0xFFFFFF);
    }

    private void setEffect(BlockPos source, int color, int ticks,
            EffectType type) {
        effectSource = source.immutable();
        effectColor = color;
        effectType = type;
        effectUntil = level == null ? ticks : level.getGameTime() + ticks;
    }

    public boolean crafting() { return crafting; }
    public int symmetry() { return symmetry; }
    public int instability() { return Mth.clamp(instability, 0, 25); }
    public @Nullable UUID ownerId() { return ownerId; }
    public Map<String, Integer> remainingEssentia() { return Map.copyOf(remainingEssentia); }
    public int remainingComponentCount() { return remainingComponents.size(); }
    public @Nullable BlockPos effectSource() { return effectSource; }
    public int effectColor() { return effectColor; }
    public EffectType effectType() { return effectType; }
    public long effectUntil() { return effectUntil; }
    public int clientCraftTicks() { return clientSoundTicks; }
    public float clientStartUp() { return clientStartUp; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Crafting", crafting);
        tag.putInt("Counter", counter);
        tag.putInt("ComponentCharge", componentCharge);
        tag.putInt("Symmetry", symmetry);
        tag.putInt("Instability", instability);
        tag.putInt("RecipeInstability", recipeInstability);
        tag.putInt("RecipeExperience", remainingExperienceLevels);
        if (recipeId != null) tag.putString("Recipe", recipeId.toString());
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        if (!centralSnapshot.isEmpty()) tag.put("Central", centralSnapshot.save(new CompoundTag()));
        ListTag essentia = new ListTag();
        remainingEssentia.forEach((aspect, amount) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Aspect", aspect);
            entry.putInt("Amount", amount);
            essentia.add(entry);
        });
        tag.put("Essentia", essentia);
        tag.putIntArray("Components", remainingComponents);
        if (effectSource != null) tag.putLong("EffectSource", effectSource.asLong());
        tag.putInt("EffectColor", effectColor);
        tag.putString("EffectType", effectType.name());
        tag.putLong("EffectUntil", effectUntil);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        crafting = tag.getBoolean("Crafting");
        counter = tag.getInt("Counter");
        componentCharge = Math.max(0, tag.getInt("ComponentCharge"));
        symmetry = tag.getInt("Symmetry");
        recipeInstability = Math.max(0, tag.getInt("RecipeInstability"));
        remainingExperienceLevels = Math.max(0, tag.getInt("RecipeExperience"));
        instability = tag.contains("Instability", Tag.TAG_INT)
                ? tag.getInt("Instability")
                : symmetry + recipeInstability;
        recipeId = tag.contains("Recipe", Tag.TAG_STRING)
                ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        centralSnapshot = tag.contains("Central", Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound("Central")) : ItemStack.EMPTY;
        remainingEssentia.clear();
        ListTag essentia = tag.getList("Essentia", Tag.TAG_COMPOUND);
        for (int index = 0; index < essentia.size(); index++) {
            CompoundTag entry = essentia.getCompound(index);
            String aspect = entry.getString("Aspect");
            int amount = Math.max(0, entry.getInt("Amount"));
            if (!aspect.isBlank() && amount > 0) remainingEssentia.put(aspect, amount);
        }
        remainingComponents.clear();
        for (int index : tag.getIntArray("Components")) {
            if (index >= 0) remainingComponents.add(index);
        }
        effectSource = tag.contains("EffectSource", Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong("EffectSource")) : null;
        effectColor = tag.getInt("EffectColor");
        try {
            effectType = EffectType.valueOf(tag.getString("EffectType"));
        } catch (IllegalArgumentException ignored) {
            effectType = EffectType.NONE;
        }
        effectUntil = tag.getLong("EffectUntil");
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
