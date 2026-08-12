package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModEntities;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Modern 1.20 Goal/TargetGoal scheduling for the classic animation-core roles. */
public final class GolemCoreGoals {
    private static final com.mojang.authlib.GameProfile GOLEM_PROFILE = new com.mojang.authlib.GameProfile(
            java.util.UUID.nameUUIDFromBytes("thaumcraft:golem_ai".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            "[TCGolem]");
    private GolemCoreGoals() {}

    public static void register(ClassicGolemEntity golem) {
        golem.goalSelector.addGoal(0, new FloatGoal(golem));
        golem.goalSelector.addGoal(0, new AvoidSwellGoal(golem));
        golem.goalSelector.addGoal(1, new CoreMeleeGoal(golem));
        golem.goalSelector.addGoal(2, new WorkGoal(golem));
        golem.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.OpenDoorGoal(golem, true));
        golem.goalSelector.addGoal(7, new LookAtPlayerGoal(golem, Player.class, 6F));
        golem.goalSelector.addGoal(8, new RandomLookAroundGoal(golem));
        golem.targetSelector.addGoal(1, new CoreHurtByTargetGoal(golem));
        golem.targetSelector.addGoal(2, new GuardTargetGoal(golem));
        golem.targetSelector.addGoal(3, new GuardAnimalTargetGoal(golem));
        golem.targetSelector.addGoal(3, new GuardPlayerTargetGoal(golem));
        golem.targetSelector.addGoal(2, new ButcherTargetGoal(golem));
    }

    private static final class CoreMeleeGoal extends MeleeAttackGoal {
        private final ClassicGolemEntity golem;
        CoreMeleeGoal(ClassicGolemEntity golem) { super(golem, 1D, true); this.golem = golem; }
        @Override public boolean canUse() { return combatCore(golem) && golem.isOperational() && super.canUse(); }
        @Override public boolean canContinueToUse() { return combatCore(golem) && golem.isOperational() && super.canContinueToUse(); }
    }

    private static final class CoreHurtByTargetGoal extends HurtByTargetGoal {
        private final ClassicGolemEntity golem;
        CoreHurtByTargetGoal(ClassicGolemEntity golem) { super(golem); this.golem = golem; }
        @Override public boolean canUse() { return golem.core() == GolemCoreType.GUARD && golem.isOperational() && super.canUse(); }
    }

    private static final class GuardTargetGoal extends NearestAttackableTargetGoal<Monster> {
        private final ClassicGolemEntity golem;
        GuardTargetGoal(ClassicGolemEntity golem) {
            super(golem, Monster.class, 10, true, false, candidate -> candidate.isAlive()
                    && candidate.distanceToSqr(golem.homePos().getX() + .5D, golem.homePos().getY() + .5D,
                            golem.homePos().getZ() + .5D) <= golem.workRange() * golem.workRange()
                    && (candidate instanceof Creeper ? golem.canAttackCreepers() : golem.canAttackHostiles()));
            this.golem = golem;
        }
        @Override public boolean canUse() { return golem.core() == GolemCoreType.GUARD && golem.isOperational() && super.canUse(); }
    }

    private static final class GuardAnimalTargetGoal extends NearestAttackableTargetGoal<Animal> {
        private final ClassicGolemEntity golem;
        GuardAnimalTargetGoal(ClassicGolemEntity golem) {
            super(golem, Animal.class, 10, true, false, candidate -> candidate.isAlive()
                    && (!(candidate instanceof net.minecraft.world.entity.TamableAnimal tame) || !tame.isTame())
                    && candidate.distanceToSqr(golem.homePos().getX() + .5D, golem.homePos().getY() + .5D,
                            golem.homePos().getZ() + .5D) <= golem.workRange() * golem.workRange());
            this.golem = golem;
        }
        @Override public boolean canUse() {
            return golem.core() == GolemCoreType.GUARD && golem.isOperational()
                    && golem.upgradeAmount(GolemUpgradeType.ORDO) > 0
                    && golem.canAttackAnimals() && super.canUse();
        }
    }

    private static final class GuardPlayerTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final ClassicGolemEntity golem;
        GuardPlayerTargetGoal(ClassicGolemEntity golem) {
            super(golem, Player.class, 10, true, false, candidate -> candidate.isAlive()
                    && golem.owner().map(owner -> !owner.equals(candidate.getUUID())).orElse(true)
                    && candidate.distanceToSqr(golem.homePos().getX() + .5D, golem.homePos().getY() + .5D,
                            golem.homePos().getZ() + .5D) <= golem.workRange() * golem.workRange());
            this.golem = golem;
        }
        @Override public boolean canUse() {
            return golem.core() == GolemCoreType.GUARD && golem.isOperational()
                    && golem.upgradeAmount(GolemUpgradeType.ORDO) > 0
                    && golem.canAttackPlayers() && super.canUse();
        }
    }

    private static final class ButcherTargetGoal extends Goal {
        private final ClassicGolemEntity golem;
        private Animal target;
        ButcherTargetGoal(ClassicGolemEntity golem) {
            this.golem = golem;
            setFlags(EnumSet.of(Flag.TARGET));
        }
        @Override public boolean canUse() {
            if (golem.core() != GolemCoreType.BUTCHER || !golem.isOperational()) return false;
            double range = golem.workRange();
            List<Animal> animals = golem.level().getEntitiesOfClass(Animal.class,
                    new net.minecraft.world.phys.AABB(golem.homePos()).inflate(range, 4D, range),
                    animal -> animal.isAlive() && !animal.isBaby() && golem.hasLineOfSight(animal)
                            && animal.distanceToSqr(golem.homePos().getX() + .5D, golem.homePos().getY() + .5D,
                                    golem.homePos().getZ() + .5D) <= range * range
                            && (!(animal instanceof net.minecraft.world.entity.TamableAnimal tame) || !tame.isTame()));
            animals.sort(java.util.Comparator.comparingInt((Animal animal) -> animal.tickCount).reversed());
            for (Animal candidate : animals) {
                long adults = animals.stream().filter(other -> other.getClass() == candidate.getClass()).count();
                if (adults > 2) { target = candidate; return true; }
            }
            return false;
        }
        @Override public void start() { golem.setTarget(target); }
        @Override public boolean canContinueToUse() {
            return target != null && target.isAlive() && golem.core() == GolemCoreType.BUTCHER
                    && golem.isOperational() && target.distanceToSqr(golem.homePos().getX() + .5D,
                            golem.homePos().getY() + .5D, golem.homePos().getZ() + .5D)
                            <= golem.workRange() * golem.workRange();
        }
        @Override public void stop() { target = null; }
    }

    private static final class AvoidSwellGoal extends Goal {
        private final ClassicGolemEntity golem;
        private Vec3 escape;
        AvoidSwellGoal(ClassicGolemEntity golem) {
            this.golem = golem;
            setFlags(EnumSet.of(Flag.MOVE));
        }
        @Override public boolean canUse() {
            if (!golem.isOperational()) return false;
            Creeper creeper = golem.level().getEntitiesOfClass(Creeper.class,
                    golem.getBoundingBox().inflate(5D, 3D, 5D), candidate -> candidate.getSwellDir() > 0)
                    .stream().filter(golem::hasLineOfSight)
                    .min(java.util.Comparator.comparingDouble(golem::distanceToSqr)).orElse(null);
            if (creeper == null) return false;
            escape = DefaultRandomPos.getPosAway(golem, 16, 7, creeper.position());
            return escape != null && creeper.distanceToSqr(escape) > creeper.distanceToSqr(golem);
        }
        @Override public void start() {
            golem.getNavigation().moveTo(escape.x, escape.y, escape.z, 1.25D);
        }
        @Override public boolean canContinueToUse() { return !golem.getNavigation().isDone(); }
    }

    private static final class WorkGoal extends Goal {
        private static final int[][] APPROACH_OFFSETS = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {0, 0}
        };
        private final ClassicGolemEntity golem;
        private final GolemRouteWatchdog routeWatchdog = new GolemRouteWatchdog();
        private final java.util.Map<Long, Integer> unreachableUntil = new java.util.HashMap<>();
        private int cooldown;
        private int lastWorkTick;
        private BlockPos breakTarget;
        private int breakTicks = -1;
        private int maxBreakTicks = 1;
        private boolean breakingLogs;
        private int nextUseTick;
        private BlockPos fishingTarget;
        private int fishingTicks;
        private float fishingQuality;
        private int nextFishingSearchTick;
        private GolemFishingBobberEntity fishingBobber;
        private BlockEntity animatedContainerEntity;
        private Container animatedContainer;
        private net.minecraftforge.common.util.FakePlayer containerActor;
        private int containerOpenTicks;

        WorkGoal(ClassicGolemEntity golem) {
            this.golem = golem;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override public boolean canUse() {
            GolemCoreType core = golem.core();
            return core != null && !combatCore(golem) && golem.isOperational();
        }

        @Override public boolean canContinueToUse() { return canUse(); }

        @Override public void stop() {
            clearFishing();
            closeAnimatedContainer();
            routeWatchdog.arrived();
        }

        @Override public void tick() {
            tickContainerAnimation();
            if (breakTarget != null) {
                tickBreaking();
                return;
            }
            if (fishingTarget != null) {
                tickFishing();
                return;
            }
            if (cooldown-- > 0) return;
            cooldown = 5;
            switch (golem.core()) {
                case GATHER -> {
                    if (hasCarriedItems()) transferItems(homeContainer(), true, false);
                    else gatherItems();
                }
                case FILL -> {
                    if (hasCarriedItems()) transferItems(homeContainer(), true, false);
                    else fillFromMarkedInventory();
                }
                case EMPTY -> {
                    if (hasCarriedItems()) emptyCorePlace();
                    else transferItems(homeContainer(), false, false);
                }
                case SORTING -> {
                    if (hasCarriedItems()) placeSortedItem();
                    else takeSortableItem();
                }
                case HARVEST -> harvest(false);
                case LUMBER -> harvest(true);
                case LIQUID -> liquidBuckets();
                case ALCHEMY -> transferEssentia();
                case USE -> useCore();
                case FISHING -> fish();
                default -> { }
            }
            if (golem.tickCount - lastWorkTick > golem.workDelay() && golem.hasRestriction()) {
                BlockPos home = golem.getRestrictCenter();
                if (golem.distanceToSqr(home.getX() + .5D, home.getY() + .5D, home.getZ() + .5D) >= 3D) {
                    navigateNear(home, 3D, .9D, false);
                }
            }
        }

        private void markWork() { lastWorkTick = golem.tickCount; }

        private void gatherItems() {
            List<ItemEntity> items = golem.level().getEntitiesOfClass(ItemEntity.class,
                    new net.minecraft.world.phys.AABB(golem.homePos()).inflate(golem.workRange()),
                    item -> item.isAlive() && !item.hasPickUpDelay() && golem.acceptsFilter(item.getItem())
                            && targetAvailable(item.blockPosition())
                            && item.distanceToSqr(golem.homePos().getX() + .5D, golem.homePos().getY() + .5D,
                                    golem.homePos().getZ() + .5D) <= golem.workRange() * golem.workRange()
                            && (golem.material().light() || !item.isInWater()));
            if (items.isEmpty()) return;
            markWork();
            ItemEntity closest = items.stream().min(java.util.Comparator.comparingDouble(golem::distanceToSqr)).orElseThrow();
            if (!moveNear(closest)) return;
            ItemStack existing = golem.inventory().getItem(0);
            ItemStack dropped = closest.getItem();
            if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, dropped)) return;
            int room = Math.min(golem.carryLimit() - existing.getCount(), dropped.getMaxStackSize() - existing.getCount());
            if (room <= 0) return;
            int moved = Math.min(room, dropped.getCount());
            if (existing.isEmpty()) golem.inventory().setItem(0, dropped.copyWithCount(moved));
            else existing.grow(moved);
            dropped.shrink(moved);
            if (dropped.isEmpty()) closest.discard(); else closest.setItem(dropped);
            golem.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, .2F, 2F);
            golem.startWorkAnimation();
        }

        private void transferItems(BlockEntity containerEntity, boolean intoContainer, boolean matchingOnly) {
            if (!(containerEntity instanceof Container container)) return;
            markWork();
            if (!moveNear(containerEntity.getBlockPos())) return;
            if (intoContainer) {
                for (int own = 0; own < golem.inventory().getContainerSize(); own++) {
                    ItemStack stack = golem.inventory().getItem(own);
                    if (stack.isEmpty() || matchingOnly && !containsMatching(container, stack)) continue;
                    animateContainer(containerEntity, container);
                    moveOne(stack, container);
                    if (stack.isEmpty()) golem.inventory().setItem(own, ItemStack.EMPTY);
                    golem.startWorkAnimation();
                    return;
                }
            } else {
                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                    ItemStack stack = container.getItem(slot);
                    if (stack.isEmpty() || !golem.acceptsFilter(stack)) continue;
                    animateContainer(containerEntity, container);
                    ItemStack one = stack.copyWithCount(Math.min(stack.getCount(), golem.carryLimit()));
                    ItemStack remainder = golem.inventory().addItem(one);
                    stack.shrink(one.getCount() - remainder.getCount());
                    container.setChanged();
                    golem.startWorkAnimation();
                    return;
                }
            }
        }

        private void fillFromMarkedInventory() {
            if (!golem.hasConfiguredFilters() || !(homeContainer() instanceof Container home)) return;
            ItemStack wanted = ItemStack.EMPTY;
            int missing = Integer.MAX_VALUE;
            for (int filterSlot = 0; filterSlot < golem.filters().getContainerSize(); filterSlot++) {
                ItemStack filter = golem.filters().getItem(filterSlot);
                if (filter.isEmpty()) continue;
                int have = countMatching(home, filter);
                int need = !golem.toggle(0) ? golem.configuredAmount(filter) - have : Integer.MAX_VALUE;
                if (need > 0) { wanted = filter; missing = need; break; }
            }
            if (wanted.isEmpty()) return;
            final ItemStack match = wanted;
            BlockEntity sourceEntity = markedBlockEntitiesFor(match, be -> be instanceof Container container
                    && containsMatching(container, match)).stream().findFirst().orElse(null);
            if (!(sourceEntity instanceof Container source)) return;
            if (!moveNear(sourceEntity.getBlockPos())) return;
            for (int slot = 0; slot < source.getContainerSize(); slot++) {
                ItemStack stack = source.getItem(slot);
                if (!golem.filterMatches(match, stack)) continue;
                animateContainer(sourceEntity, source);
                int moved = Math.min(stack.getCount(), Math.min(golem.carryLimit(), missing));
                golem.inventory().setItem(0, stack.copyWithCount(moved));
                stack.shrink(moved);
                source.setChanged();
                golem.startWorkAnimation();
                markWork();
                return;
            }
        }

        private void emptyCorePlace() {
            ItemStack carried = golem.inventory().getItem(0);
            if (carried.isEmpty()) return;
            BlockEntity destination = markedBlockEntitiesFor(carried,
                    entity -> entity instanceof Container container && containerHasRoom(container, carried))
                    .stream().findFirst().orElse(null);
            if (destination != null) {
                transferItems(destination, true, false);
                return;
            }
            var marker = golem.markers().stream()
                    .filter(candidate -> candidate.pos().distSqr(golem.homePos()) <= golem.workRange() * golem.workRange())
                    .filter(candidate -> targetAvailable(candidate.pos()))
                    .filter(candidate -> golem.markerAccepts(carried, candidate))
                    .filter(candidate -> !(golem.level().getBlockEntity(candidate.pos()) instanceof Container))
                    .min(java.util.Comparator.comparingDouble(candidate -> candidate.pos().distSqr(golem.blockPosition())))
                    .orElse(null);
            if (marker == null) {
                transferItems(homeContainer(), true, false);
                return;
            }
            if (!moveNear(marker.pos())) return;
            ItemEntity dropped = new ItemEntity(golem.level(), golem.getX(), golem.getY() + golem.getBbHeight() * .5D,
                    golem.getZ(), carried.copy());
            Vec3 target = Vec3.atCenterOf(marker.pos()).subtract(dropped.position());
            double length = Math.max(.25D, target.length());
            dropped.setDeltaMovement(target.scale(Math.min(.35D, 1D / length)).add(0D, .1D, 0D));
            dropped.setPickUpDelay(10);
            golem.level().addFreshEntity(dropped);
            golem.inventory().setItem(0, ItemStack.EMPTY);
            golem.startWorkAnimation();
            markWork();
        }

        private void takeSortableItem() {
            BlockEntity homeEntity = homeContainer();
            if (!(homeEntity instanceof Container home) || !moveNear(homeEntity.getBlockPos())) return;
            for (int slot = 0; slot < home.getContainerSize(); slot++) {
                ItemStack stack = home.getItem(slot);
                if (stack.isEmpty()) continue;
                boolean destinationExists = markedBlockEntities(be -> be instanceof Container container
                        && containsMatching(container, stack)).stream().findAny().isPresent();
                if (!destinationExists) continue;
                animateContainer(homeEntity, home);
                int moved = Math.min(stack.getCount(), golem.carryLimit());
                golem.inventory().setItem(0, stack.copyWithCount(moved));
                stack.shrink(moved);
                home.setChanged();
                golem.startWorkAnimation();
                markWork();
                return;
            }
        }

        private void placeSortedItem() {
            ItemStack carried = golem.inventory().getItem(0);
            if (carried.isEmpty()) return;
            BlockEntity destination = markedBlockEntities(be -> be instanceof Container container
                    && containsMatching(container, carried)).stream().findFirst().orElse(null);
            transferItems(destination, true, false);
        }

        private boolean moveNear(BlockPos pos) {
            return navigateNear(pos, 4D, 1D, true);
        }

        /** TC4's AIItemPickup follows the entity and picks it up at distance squared 2, without a LOS gate. */
        private boolean moveNear(ItemEntity item) {
            double distanceSqr = golem.distanceToSqr(item);
            if (distanceSqr <= 2D) {
                golem.getNavigation().stop();
                routeWatchdog.arrived();
                return true;
            }
            BlockPos pos = item.blockPosition();
            if (!targetAvailable(pos)) return false;
            if (routeWatchdog.shouldRebuild(pos.asLong(), golem.tickCount, golem.getX(), golem.getY(), golem.getZ(),
                    distanceSqr, golem.getNavigation().isDone())) {
                if (routeWatchdog.shouldReleaseTarget() || !golem.getNavigation().moveTo(item, 1D)) {
                    unreachableUntil.put(pos.asLong(), golem.tickCount + 100);
                    golem.getNavigation().stop();
                    routeWatchdog.arrived();
                    return false;
                }
            }
            markWork();
            return false;
        }

        private void animateContainer(BlockEntity entity, Container container) {
            if (!(golem.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
            if (animatedContainerEntity == entity && animatedContainer == container) {
                containerOpenTicks = 10;
                return;
            }
            closeAnimatedContainer();
            containerActor = net.minecraftforge.common.util.FakePlayerFactory.get(level, GOLEM_PROFILE);
            containerActor.moveTo(golem.getX(), golem.getY(), golem.getZ(), golem.getYRot(), golem.getXRot());
            animatedContainerEntity = entity;
            animatedContainer = container;
            animatedContainer.startOpen(containerActor);
            containerOpenTicks = 10;
        }

        private void tickContainerAnimation() {
            if (containerOpenTicks > 0 && --containerOpenTicks == 0) closeAnimatedContainer();
        }

        private void closeAnimatedContainer() {
            if (animatedContainer != null && containerActor != null) animatedContainer.stopOpen(containerActor);
            animatedContainerEntity = null;
            animatedContainer = null;
            containerActor = null;
            containerOpenTicks = 0;
        }

        private boolean navigateNear(BlockPos pos, double reachSqr, double speed, boolean requireInteractionAccess) {
            double distanceSqr = golem.distanceToSqr(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
            if (distanceSqr <= reachSqr && (!requireInteractionAccess || hasInteractionAccess(pos))) {
                golem.getNavigation().stop();
                routeWatchdog.arrived();
                return true;
            }
            if (!targetAvailable(pos)) return false;
            if (routeWatchdog.shouldRebuild(pos.asLong(), golem.tickCount, golem.getX(), golem.getY(), golem.getZ(),
                    distanceSqr, golem.getNavigation().isDone())) {
                if (routeWatchdog.shouldReleaseTarget() || !rebuildPath(pos, reachSqr, speed)) {
                    unreachableUntil.put(pos.asLong(), golem.tickCount + 100);
                    golem.getNavigation().stop();
                    routeWatchdog.arrived();
                    return false;
                }
            }
            markWork();
            return false;
        }

        private boolean hasInteractionAccess(BlockPos pos) {
            Vec3 target = Vec3.atCenterOf(pos);
            BlockHitResult hit = golem.level().clip(new ClipContext(golem.getEyePosition(), target,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, golem));
            return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
        }

        private boolean rebuildPath(BlockPos interactionPos, double reachSqr, double speed) {
            golem.getNavigation().stop();
            int start = Math.floorMod(routeWatchdog.approachIndex(), APPROACH_OFFSETS.length);
            for (int vertical : new int[]{0, -1, 1}) {
                for (int attempt = 0; attempt < APPROACH_OFFSETS.length; attempt++) {
                    int[] offset = APPROACH_OFFSETS[(start + attempt) % APPROACH_OFFSETS.length];
                    BlockPos approach = interactionPos.offset(offset[0], vertical, offset[1]);
                    if (approach.distSqr(interactionPos) > reachSqr) continue;
                    net.minecraft.world.level.pathfinder.Path path = golem.getNavigation().createPath(approach, 0);
                    if (path == null || !path.canReach() || path.getEndNode() == null
                            || path.getEndNode().distanceToSqr(approach) > .01F) continue;
                    if (golem.getNavigation().moveTo(path, speed)) return true;
                }
            }
            return false;
        }

        private boolean targetAvailable(BlockPos pos) {
            int until = unreachableUntil.getOrDefault(pos.asLong(), 0);
            if (until <= golem.tickCount) {
                unreachableUntil.remove(pos.asLong());
                return true;
            }
            return false;
        }

        private int countMatching(Container container, ItemStack wanted) {
            int amount = 0;
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (golem.filterMatches(wanted, stack)) amount += stack.getCount();
            }
            return amount;
        }

        private boolean hasCarriedItems() {
            for (int slot = 0; slot < golem.inventory().getContainerSize(); slot++) {
                if (!golem.inventory().getItem(slot).isEmpty()) return true;
            }
            return false;
        }

        private BlockEntity homeContainer() {
            return homeBlockEntity(entity -> entity instanceof Container);
        }

        private BlockEntity remoteContainer() {
            return remoteBlockEntity(entity -> entity instanceof Container);
        }

        private BlockEntity remoteContainerFor(ItemStack stack) {
            return markedBlockEntitiesFor(stack, entity -> entity instanceof Container).stream().findFirst().orElse(null);
        }

        private BlockEntity homeBlockEntity(java.util.function.Predicate<BlockEntity> predicate) {
            BlockEntity entity = golem.level().getBlockEntity(golem.attachedPos());
            if (entity != null && predicate.test(entity)) return entity;
            // Versions before 1.5.70 persisted HomeFacing but lost Mob.restrictTo() on reload.
            // Repair those already-loaded golems once by binding the nearest valid attachment.
            return recoverLegacyHome(predicate);
        }

        private BlockEntity recoverLegacyHome(java.util.function.Predicate<BlockEntity> predicate) {
            if (golem.hasRestriction()) return null;
            BlockEntity recovered = nearestBlockEntity(predicate);
            if (recovered != null) {
                golem.restrictTo(recovered.getBlockPos().relative(golem.homeFacing()), 32);
            }
            return recovered;
        }

        private BlockEntity remoteBlockEntity(java.util.function.Predicate<BlockEntity> predicate) {
            return markedBlockEntities(predicate).stream().findFirst().orElse(null);
        }

        private List<BlockEntity> markedBlockEntities(java.util.function.Predicate<BlockEntity> predicate) {
            double rangeSq = golem.workRange() * golem.workRange();
            return golem.markers().stream().filter(marker -> marker.pos().distSqr(golem.homePos()) <= rangeSq)
                    .filter(marker -> targetAvailable(marker.pos()))
                    .map(marker -> golem.level().getBlockEntity(marker.pos()))
                    .filter(java.util.Objects::nonNull).filter(predicate)
                    .sorted(java.util.Comparator.comparingDouble(entity -> entity.getBlockPos().distSqr(golem.blockPosition())))
                    .toList();
        }

        private List<BlockEntity> markedBlockEntitiesFor(ItemStack stack,
                java.util.function.Predicate<BlockEntity> predicate) {
            double rangeSq = golem.workRange() * golem.workRange();
            return golem.markers().stream()
                    .filter(marker -> marker.pos().distSqr(golem.homePos()) <= rangeSq)
                    .filter(marker -> targetAvailable(marker.pos()))
                    .filter(marker -> golem.markerAccepts(stack, marker))
                    .map(marker -> golem.level().getBlockEntity(marker.pos()))
                    .filter(java.util.Objects::nonNull).filter(predicate)
                    .sorted(java.util.Comparator.comparingDouble(entity -> entity.getBlockPos().distSqr(golem.blockPosition())))
                    .toList();
        }

        private void harvest(boolean logs) {
            BlockPos target = logs ? lumberTarget() : findMatureCrop();
            if (target == null) return;
            markWork();
            if (logs && !moveNearRememberedTree()) {
                return;
            }
            if (!logs && !moveNear(target)) return;
            BlockState state = golem.level().getBlockState(target);
            float hardness = state.getDestroySpeed(golem.level(), target);
            if (hardness < 0F) {
                if (logs) golem.forgetLumberLog(target);
                return;
            }
            breakingLogs = logs;
            breakTarget = target;
            breakTicks = Math.max(logs ? 5 : 10,
                    (int) ((golem.workDelay() - golem.effectiveStrength() * (logs ? 3 : 2)) * hardness));
            maxBreakTicks = Math.max(1, breakTicks);
        }

        private void tickBreaking() {
            BlockState state = golem.level().getBlockState(breakTarget);
            if (state.isAir() || breakingLogs && !state.is(BlockTags.LOGS)
                    || !breakingLogs && !isMatureCrop(breakTarget, state)) {
                if (breakingLogs) golem.forgetLumberLog(breakTarget);
                clearBreaking();
                return;
            }
            if (breakingLogs && golem.core() != GolemCoreType.LUMBER) {
                clearBreaking();
                return;
            }
            if (breakingLogs && !moveNearRememberedTree()) {
                return;
            }
            if (!breakingLogs && !moveNear(breakTarget)) return;
            if (breakTicks % Math.max(1, maxBreakTicks / 6) == 0) {
                golem.startWorkAnimation();
                var sound = state.getSoundType(golem.level(), breakTarget, golem);
                golem.level().playSound(null, breakTarget, sound.getHitSound(),
                        net.minecraft.sounds.SoundSource.BLOCKS, (sound.getVolume() + 1F) / 8F, sound.getPitch() * .5F);
            }
            int progress = Math.min(9, (int) (9F * (1F - (float) breakTicks / maxBreakTicks)));
            golem.level().destroyBlockProgress(golem.getId(), breakTarget, progress);
            if (--breakTicks > 0) return;
            golem.level().destroyBlockProgress(golem.getId(), breakTarget, -1);
            if (!breakingLogs && golem.upgradeAmount(GolemUpgradeType.ORDO) > 0
                    && state.getBlock() instanceof CropBlock crop) {
                var drops = net.minecraft.world.level.block.Block.getDrops(state,
                        (net.minecraft.server.level.ServerLevel) golem.level(), breakTarget,
                        golem.level().getBlockEntity(breakTarget), golem, ItemStack.EMPTY);
                boolean replanted = false;
                for (ItemStack drop : drops) {
                    if (!replanted && drop.is(state.getBlock().asItem())) {
                        drop.shrink(1);
                        replanted = true;
                    }
                    if (!drop.isEmpty()) net.minecraft.world.level.block.Block.popResource(golem.level(), breakTarget, drop);
                }
                if (replanted) golem.level().setBlock(breakTarget, crop.getStateForAge(0), 3);
                else golem.level().destroyBlock(breakTarget, false, golem);
            } else {
                golem.level().destroyBlock(breakTarget, true, golem);
            }
            if (breakingLogs) golem.forgetLumberLog(breakTarget);
            golem.startWorkAnimation();
            clearBreaking();
        }

        private BlockPos lumberTarget() {
            if (golem.lumberTreeBase() == null || golem.lumberTreeLogs().isEmpty()) {
                GolemLumberTask.Tree tree = GolemLumberTask.discover(
                        golem.level(), golem.homePos(), golem.workRange());
                if (tree == null) {
                    golem.clearLumberTree();
                    return null;
                }
                golem.rememberLumberTree(tree.base(), tree.logs());
            }
            for (BlockPos remembered : golem.lumberTreeLogs()) {
                if (targetAvailable(remembered) && golem.level().getBlockState(remembered).is(BlockTags.LOGS)) {
                    return remembered;
                }
                golem.forgetLumberLog(remembered);
            }
            golem.clearLumberTree();
            return null;
        }

        private boolean moveNearRememberedTree() {
            BlockPos base = golem.lumberTreeBase();
            if (base == null) return false;
            return navigateNear(base, 6.25D, 1D, true);
        }

        private void clearBreaking() {
            if (breakTarget != null) golem.level().destroyBlockProgress(golem.getId(), breakTarget, -1);
            breakTarget = null;
            breakTicks = -1;
        }

        private BlockPos findMatureCrop() {
            int radius = golem.workRange();
            BlockPos center = golem.homePos();
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            for (BlockPos mutable : BlockPos.betweenClosed(center.offset(-radius, -2, -radius),
                    center.offset(radius, 2, radius))) {
                double distance = mutable.distSqr(center);
                if (distance > radius * radius || !targetAvailable(mutable)
                        || !isMatureCrop(mutable, golem.level().getBlockState(mutable))) continue;
                if (distance < bestDistance) { bestDistance = distance; best = mutable.immutable(); }
            }
            return best;
        }

        private boolean isMatureCrop(BlockPos pos, BlockState state) {
            if (state.getBlock() instanceof CropBlock crop) return crop.isMaxAge(state);
            if (state.getBlock() instanceof net.minecraft.world.level.block.NetherWartBlock) {
                return state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE) >= 3;
            }
            if (state.getBlock() instanceof net.minecraft.world.level.block.CocoaBlock) {
                return state.getValue(net.minecraft.world.level.block.CocoaBlock.AGE) >= 2;
            }
            if (state.getBlock() instanceof net.minecraft.world.level.block.SweetBerryBushBlock) {
                return state.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE) >= 3;
            }
            if (state.is(net.minecraft.world.level.block.Blocks.SUGAR_CANE)
                    || state.is(net.minecraft.world.level.block.Blocks.CACTUS)
                    || state.is(net.minecraft.world.level.block.Blocks.BAMBOO)) {
                return golem.level().getBlockState(pos.below()).is(state.getBlock())
                        && golem.level().getBlockState(pos.above()).isAir();
            }
            return false;
        }

        private void liquidBuckets() {
            java.util.function.Predicate<BlockEntity> fluidHandler = be -> be.getCapability(
                    net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, golem.homeFacing()).isPresent();
            if (!golem.fluidCarried().isEmpty()) {
                BlockEntity home = homeBlockEntity(fluidHandler);
                if (home == null || !moveNear(home.getBlockPos())) return;
                home.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, golem.homeFacing())
                        .ifPresent(handler -> {
                            net.minecraftforge.fluids.FluidStack offered = golem.fluidCarried().copy();
                            int accepted = handler.fill(offered,
                                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                            if (accepted > 0) {
                                offered = golem.fluidCarried().copy();
                                offered.shrink(accepted);
                                golem.setFluidCarried(offered);
                                golem.startWorkAnimation();
                                markWork();
                            }
                        });
                return;
            }
            BlockEntity source = markedFluidHandlerSource();
            if (source != null) {
                if (!moveNear(source.getBlockPos())) return;
                Direction sourceSide = golem.markers().stream().filter(marker -> marker.pos().equals(source.getBlockPos()))
                        .map(marker -> marker.side().getOpposite()).findFirst().orElse(null);
                source.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, sourceSide)
                        .ifPresent(handler -> {
                            net.minecraftforge.fluids.FluidStack simulated = handler.drain(golem.fluidCarryLimit(),
                                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                            if (simulated.isEmpty() || !fluidAllowed(simulated)) return;
                            net.minecraftforge.fluids.FluidStack drained = handler.drain(
                                    Math.min(simulated.getAmount(), golem.fluidCarryLimit()),
                                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                            if (!drained.isEmpty()) {
                                golem.setFluidCarried(drained);
                                golem.startWorkAnimation();
                                markWork();
                            }
                        });
                return;
            }
            BlockPos sourcePos = markedWorldFluidSource();
            if (sourcePos == null || !moveNear(sourcePos)) return;
            var fluidState = golem.level().getFluidState(sourcePos);
            if (!fluidState.isSource()) return;
            net.minecraftforge.fluids.FluidStack stack = new net.minecraftforge.fluids.FluidStack(fluidState.getType(), 1000);
            if (!fluidAllowed(stack) || golem.fluidCarryLimit() < 1000) return;
            golem.level().destroyBlock(sourcePos, false, golem);
            golem.setFluidCarried(stack);
            golem.startWorkAnimation();
            markWork();
        }

        private boolean fluidAllowed(net.minecraftforge.fluids.FluidStack fluid) {
            if (!golem.hasConfiguredFilters()) return true;
            for (int slot = 0; slot < golem.filters().getContainerSize(); slot++) {
                var contained = net.minecraftforge.fluids.FluidUtil.getFluidContained(golem.filters().getItem(slot));
                if (contained.isPresent() && contained.get().isFluidEqual(fluid)) return true;
            }
            return false;
        }

        private boolean fluidMarkerAllowed(net.minecraftforge.fluids.FluidStack fluid, GolemMarker marker) {
            if (golem.upgradeAmount(GolemUpgradeType.ORDO) == 0 || !golem.hasConfiguredFilters()) return marker.color() == -1;
            for (int slot = 0; slot < golem.filters().getContainerSize(); slot++) {
                var contained = net.minecraftforge.fluids.FluidUtil.getFluidContained(golem.filters().getItem(slot));
                if (contained.isPresent() && contained.get().isFluidEqual(fluid)) {
                    int color = golem.filterColor(slot);
                    if (color == -1 || marker.color() == color) return true;
                }
            }
            return false;
        }

        private BlockEntity markedFluidHandlerSource() {
            double rangeSq = golem.workRange() * golem.workRange();
            return golem.markers().stream().filter(marker -> marker.pos().distSqr(golem.homePos()) <= rangeSq)
                    .filter(marker -> targetAvailable(marker.pos()))
                    .sorted(java.util.Comparator.comparingDouble(marker -> marker.pos().distSqr(golem.blockPosition())))
                    .filter(marker -> {
                        BlockEntity entity = golem.level().getBlockEntity(marker.pos());
                        if (entity == null) return false;
                        return entity.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER,
                                        marker.side().getOpposite()).map(handler -> {
                            var simulated = handler.drain(golem.fluidCarryLimit(),
                                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                            return !simulated.isEmpty() && fluidAllowed(simulated) && fluidMarkerAllowed(simulated, marker);
                        }).orElse(false);
                    }).map(marker -> golem.level().getBlockEntity(marker.pos())).findFirst().orElse(null);
        }

        private BlockPos markedWorldFluidSource() {
            BlockPos best = null;
            double bestDistance = golem.upgradeAmount(GolemUpgradeType.PERDITIO) > 0 ? -1D : Double.MAX_VALUE;
            for (var marker : golem.markers()) {
                BlockPos candidate = marker.pos();
                if (!targetAvailable(candidate)) continue;
                var fluid = golem.level().getFluidState(candidate);
                var fluidStack = new net.minecraftforge.fluids.FluidStack(fluid.getType(), 1000);
                if (!fluid.isSource() || !fluidAllowed(fluidStack) || !fluidMarkerAllowed(fluidStack, marker)) continue;
                if (golem.upgradeAmount(GolemUpgradeType.PERDITIO) > 0) candidate = furthestConnectedSource(candidate, fluid.getType());
                double distance = candidate.distSqr(golem.homePos());
                if (distance > golem.workRange() * golem.workRange()) continue;
                if (best == null || (golem.upgradeAmount(GolemUpgradeType.PERDITIO) > 0
                        ? distance > bestDistance : distance < bestDistance)) {
                    best = candidate;
                    bestDistance = distance;
                }
            }
            return best;
        }

        private BlockPos furthestConnectedSource(BlockPos origin, net.minecraft.world.level.material.Fluid fluid) {
            java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
            java.util.HashSet<BlockPos> seen = new java.util.HashSet<>();
            queue.add(origin);
            BlockPos furthest = origin;
            double farthest = 0D;
            while (!queue.isEmpty() && seen.size() < 4096) {
                BlockPos current = queue.removeFirst();
                if (!seen.add(current)) continue;
                var state = golem.level().getFluidState(current);
                if (!state.isSource() || state.getType() != fluid) continue;
                double distance = current.distSqr(origin);
                if (distance <= golem.workRange() * golem.workRange() && distance > farthest) {
                    farthest = distance;
                    furthest = current;
                }
                if (distance > golem.workRange() * golem.workRange()) continue;
                for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) queue.add(current.offset(dx, dy, dz));
                }
            }
            return furthest;
        }

        private void transferEssentia() {
            if (golem.essentiaAmount() > 0 && golem.essentiaCarried() != null) {
                BlockEntity destinationEntity = essentiaDestinations(golem.essentiaCarried()).stream().findFirst().orElse(null);
                if (!(destinationEntity instanceof EssentiaTransport destination)
                        || !moveNear(destinationEntity.getBlockPos())) return;
                Direction input = essentiaInputSide(destination, golem.essentiaCarried());
                if (input == null) return;
                int accepted = destination.addEssentia(golem.essentiaCarried(), golem.essentiaAmount(), input);
                if (accepted > 0) {
                    golem.setEssentiaCarried(golem.essentiaCarried(), golem.essentiaAmount() - accepted);
                    golem.startWorkAnimation();
                    markWork();
                }
                return;
            }
            BlockEntity sourceEntity = essentiaSources().stream().findFirst().orElse(null);
            if (!(sourceEntity instanceof EssentiaTransport source) || !moveNear(sourceEntity.getBlockPos())) return;
            Direction output = essentiaOutputSide(source);
            if (output == null) return;
            String aspect = source.essentiaType(output);
            int available = source.essentiaAmount(output);
            if (aspect == null || available <= 0) return;
            int requested = Math.min(available, golem.carryLimit());
            int taken = source.takeEssentia(aspect, requested, output);
            if (taken > 0) {
                golem.setEssentiaCarried(aspect, taken);
                golem.startWorkAnimation();
                markWork();
            }
        }

        private List<BlockEntity> essentiaSources() {
            java.util.ArrayList<BlockEntity> sources = new java.util.ArrayList<>();
            if (!golem.hasRestriction()) {
                recoverLegacyHome(entity -> entity instanceof EssentiaTransport transport
                        && essentiaOutputSide(transport) != null);
            }
            BlockPos attached = golem.attachedPos();
            for (int up = 0; up <= 5; up++) {
                BlockEntity entity = golem.level().getBlockEntity(attached.above(up));
                if (entity instanceof EssentiaTransport transport && essentiaOutputSide(transport) != null) {
                    if (targetAvailable(entity.getBlockPos())) sources.add(entity);
                }
            }
            sources.sort(java.util.Comparator.comparingInt((BlockEntity entity) -> {
                EssentiaTransport transport = (EssentiaTransport) entity;
                Direction side = essentiaOutputSide(transport);
                return side == null ? 0 : transport.essentiaAmount(side);
            }).reversed());
            return sources;
        }

        private List<BlockEntity> essentiaDestinations(String aspect) {
            java.util.ArrayList<BlockEntity> found = new java.util.ArrayList<>();
            java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
            java.util.HashSet<BlockPos> seen = new java.util.HashSet<>();
            for (var marker : golem.markers()) queue.add(marker.pos());
            while (!queue.isEmpty() && seen.size() < 512) {
                BlockPos pos = queue.removeFirst();
                if (!seen.add(pos) || pos.distSqr(golem.homePos()) > golem.workRange() * golem.workRange()) continue;
                BlockEntity entity = golem.level().getBlockEntity(pos);
                if (targetAvailable(pos) && canReceiveEssentia(entity, aspect)) found.add(entity);
                if (entity instanceof com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity
                        || entity instanceof com.thaumcraftmodern.world.block.entity.VoidJarBlockEntity) {
                    for (Direction direction : Direction.values()) queue.add(pos.relative(direction));
                }
            }
            found.sort(java.util.Comparator
                    .comparingInt((BlockEntity entity) -> essentiaPriority(entity, aspect))
                    .thenComparingDouble(entity -> entity.getBlockPos().distSqr(golem.blockPosition())));
            return found;
        }

        private boolean canReceiveEssentia(BlockEntity entity, String aspect) {
            if (entity instanceof com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity jar) {
                return jar.acceptsAspect(aspect, 1);
            }
            if (entity instanceof com.thaumcraftmodern.world.block.entity.VoidJarBlockEntity jar) {
                return jar.acceptsAspect(aspect, 1);
            }
            return entity instanceof EssentiaTransport transport && essentiaInputSide(transport, aspect) != null;
        }

        private Direction essentiaOutputSide(EssentiaTransport transport) {
            for (Direction side : new Direction[]{Direction.UP, golem.homeFacing(), Direction.DOWN,
                    Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
                if (transport.canOutputTo(side) && transport.essentiaAmount(side) > 0
                        && transport.essentiaType(side) != null) return side;
            }
            return null;
        }

        private Direction essentiaInputSide(EssentiaTransport transport, String aspect) {
            for (Direction side : new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH,
                    Direction.SOUTH, Direction.WEST, Direction.EAST}) {
                if (transport.canInputFrom(side)
                        && (transport.suctionType(side) == null || aspect.equals(transport.suctionType(side)))) return side;
            }
            return null;
        }

        private int essentiaPriority(BlockEntity entity, String aspect) {
            if (entity instanceof com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity jar) {
                if (aspect.equals(jar.filter())) return 0;
                if (aspect.equals(jar.aspect()) && jar.amount() > 0) return 1;
                return 2;
            }
            if (entity instanceof com.thaumcraftmodern.world.block.entity.VoidJarBlockEntity jar) {
                if (aspect.equals(jar.filter())) return 3;
                if (aspect.equals(jar.aspect()) && jar.amount() > 0) return 4;
                return 5;
            }
            return 6;
        }

        private void useCore() {
            if (!(golem.level() instanceof net.minecraft.server.level.ServerLevel level) || golem.tickCount < nextUseTick) return;
            boolean attachedInventory = homeContainer() instanceof Container;
            if (golem.inventory().getItem(0).isEmpty() && attachedInventory) {
                transferItems(homeContainer(), false, false);
                return;
            }
            var marker = golem.markers().stream()
                    .filter(candidate -> candidate.pos().distSqr(golem.homePos())
                            <= Math.pow(Math.max(1, golem.workRange() / 3), 2))
                    .filter(candidate -> golem.toggle(0) == golem.level().getBlockState(candidate.pos()).isAir())
                    .filter(candidate -> targetAvailable(candidate.pos()))
                    .filter(candidate -> golem.inventory().getItem(0).isEmpty()
                            || golem.markerAccepts(golem.inventory().getItem(0), candidate))
                    .min(java.util.Comparator.comparingDouble(candidate -> candidate.pos().distSqr(golem.blockPosition())))
                    .orElse(null);
            if (marker == null) {
                if (attachedInventory && !golem.inventory().getItem(0).isEmpty()) transferItems(homeContainer(), true, false);
                return;
            }
            if (attachedInventory && golem.inventory().getItem(0).isEmpty()) return;
            if (!moveNear(marker.pos())) return;
            net.minecraftforge.common.util.FakePlayer fake = net.minecraftforge.common.util.FakePlayerFactory.get(level, GOLEM_PROFILE);
            fake.moveTo(golem.getX(), golem.getY(), golem.getZ(), golem.getYRot(), golem.getXRot());
            fake.setShiftKeyDown(golem.toggle(2));
            ItemStack carried = golem.inventory().getItem(0);
            fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, carried.copy());
            if (golem.toggle(1)) {
                if (!golem.level().getBlockState(marker.pos()).isAir()) fake.gameMode.destroyBlock(marker.pos());
                golem.startLeftArmAnimation();
            } else {
                net.minecraft.world.phys.Vec3 hit = net.minecraft.world.phys.Vec3.atCenterOf(marker.pos());
                net.minecraft.world.phys.BlockHitResult result = new net.minecraft.world.phys.BlockHitResult(
                        hit, marker.side().getOpposite(), marker.pos(), false);
                net.minecraft.world.InteractionResult interaction = fake.gameMode.useItemOn(fake, level,
                        fake.getMainHandItem(), net.minecraft.world.InteractionHand.MAIN_HAND, result);
                if (interaction == net.minecraft.world.InteractionResult.PASS) {
                    fake.gameMode.useItem(fake, level, fake.getMainHandItem(), net.minecraft.world.InteractionHand.MAIN_HAND);
                }
                golem.startRightArmAnimation();
            }
            golem.inventory().setItem(0, fake.getMainHandItem().copy());
            for (int slot = 1; slot < fake.getInventory().getContainerSize(); slot++) {
                ItemStack extra = fake.getInventory().getItem(slot);
                if (!extra.isEmpty()) {
                    golem.level().addFreshEntity(new ItemEntity(golem.level(), golem.getX(),
                            golem.getY() + golem.getBbHeight() * .5D, golem.getZ(), extra.copy()));
                    fake.getInventory().setItem(slot, ItemStack.EMPTY);
                }
            }
            nextUseTick = golem.tickCount + Math.max(3, 12 - golem.upgradeAmount(GolemUpgradeType.AER) * 3);
            markWork();
        }

        private void fish() {
            if (golem.tickCount < nextFishingSearchTick) return;
            nextFishingSearchTick = golem.tickCount + 40;
            int distance = Math.max(1, golem.workRange() / 2);
            BlockPos home = golem.homePos();
            java.util.ArrayList<BlockPos> waterTargets = new java.util.ArrayList<>();
            int vertical = Math.min(4, Math.max(2, distance / 2));
            for (BlockPos candidate : BlockPos.betweenClosed(home.offset(-distance, -vertical, -distance),
                    home.offset(distance, vertical, distance))) {
                if (candidate.distSqr(home) <= distance * distance
                        && golem.level().getFluidState(candidate).is(net.minecraft.tags.FluidTags.WATER)
                        && golem.level().getBlockState(candidate.above()).isAir()) {
                    waterTargets.add(candidate.immutable());
                }
            }
            if (waterTargets.isEmpty()) return;
            BlockPos water = waterTargets.get(golem.getRandom().nextInt(waterTargets.size()));
            GolemFishingBobberEntity bobber = new GolemFishingBobberEntity(
                    ModEntities.GOLEM_FISHING_BOBBER.get(), golem.level());
            bobber.castFrom(golem, water);
            if (!golem.level().addFreshEntity(bobber)) return;
            fishingBobber = bobber;
            fishingTarget = water;
            fishingTicks = 300 + golem.getRandom().nextInt(200);
            fishingQuality = fishingQuality(water);
            golem.startRightArmAnimation();
            golem.level().playSound(null, golem.blockPosition(), net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW,
                    net.minecraft.sounds.SoundSource.NEUTRAL, .5F,
                    .4F / (golem.getRandom().nextFloat() * .4F + .8F));
            markWork();
        }

        private void tickFishing() {
            if (fishingBobber == null || !fishingBobber.isAlive()
                    || !golem.level().getFluidState(fishingTarget).is(net.minecraft.tags.FluidTags.WATER)) {
                clearFishing();
                return;
            }
            fishingTicks--;
            golem.getLookControl().setLookAt(fishingTarget.getX() + .5D, fishingTarget.getY() + 1D,
                    fishingTarget.getZ() + .5D, 30F, 30F);
            float chance = fishingQuality + golem.effectiveStrength() * 1.5E-4F;
            // TC4's straw fisherman has zero base strength and otherwise silently fails most
            // 300-500 tick casts. Preserve the original chance, but guarantee the final bite.
            if (fishingTicks > 20 && golem.getRandom().nextFloat() >= chance) return;
            golem.startRightArmAnimation();
            int catches = 1;
            if (golem.upgradeAmount(GolemUpgradeType.AER) > 0
                    && golem.getRandom().nextInt(10) < golem.upgradeAmount(GolemUpgradeType.AER)) catches++;
            Vec3 landing = dryFishingCatchLanding();
            for (int index = 0; index < catches; index++) {
                ItemStack caught = fishingLoot();
                if (golem.upgradeAmount(GolemUpgradeType.IGNIS) > 0) caught = smelt(caught);
                // Do not rely on ItemEntity collision to complete the throw: an item
                // spawned over water can hit the bank's side and fall straight back.
                // Create it at the validated dry landing and retain a short upward pop
                // so the catch still visibly flies out after the bobber splash.
                ItemEntity item = new ItemEntity(golem.level(), landing.x, landing.y, landing.z, caught);
                item.setPickUpDelay(20);
                if (golem.upgradeAmount(GolemUpgradeType.IGNIS) > 0) item.setUnlimitedLifetime();
                Vec3 awayFromWater = landing.subtract(Vec3.atCenterOf(fishingTarget));
                Vec3 horizontal = new Vec3(awayFromWater.x, 0D, awayFromWater.z);
                if (horizontal.lengthSqr() > 1.0E-4D) horizontal = horizontal.normalize().scale(.08D);
                item.setDeltaMovement(horizontal.x, .22D + index * .025D, horizontal.z);
                golem.level().addFreshEntity(item);
            }
            golem.level().playSound(null, fishingTarget, net.minecraft.sounds.SoundEvents.FISHING_BOBBER_SPLASH,
                    net.minecraft.sounds.SoundSource.NEUTRAL, .15F, 1F + (golem.getRandom().nextFloat() - .5F) * .4F);
            fishingBobber.catchSplash();
            fishingBobber = null;
            fishingTarget = null;
            fishingTicks = 0;
        }

        /** Chooses an empty block over a dry, sturdy surface near the fisher. */
        private Vec3 dryFishingCatchLanding() {
            BlockPos origin = golem.blockPosition();
            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;
            for (BlockPos candidate : BlockPos.betweenClosed(
                    origin.offset(-4, -2, -4), origin.offset(4, 2, 4))) {
                BlockPos floor = candidate.below();
                if (!golem.level().getFluidState(candidate).isEmpty()
                        || !golem.level().getFluidState(floor).isEmpty()
                        || !golem.level().getBlockState(candidate)
                                .getCollisionShape(golem.level(), candidate).isEmpty()
                        || !golem.level().getBlockState(floor)
                                .isFaceSturdy(golem.level(), floor, Direction.UP)) {
                    continue;
                }
                double score = candidate.distSqr(origin) * 2D
                        + candidate.distSqr(fishingTarget);
                if (score < bestScore) {
                    best = candidate.immutable();
                    bestScore = score;
                }
            }
            return best == null
                    ? golem.position().add(0D, .2D, 0D)
                    : Vec3.atBottomCenterOf(best).add(0D, .2D, 0D);
        }

        private void clearFishing() {
            if (fishingBobber != null && fishingBobber.isAlive()) fishingBobber.discard();
            fishingBobber = null;
            fishingTarget = null;
            fishingTicks = 0;
        }

        private float fishingQuality(BlockPos water) {
            float quality = 0F;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos side = water.relative(direction);
                if (!golem.level().getFluidState(side).is(net.minecraft.tags.FluidTags.WATER)
                        || !golem.level().getBlockState(side.above()).isAir()) continue;
                quality += 3E-5F;
                if (golem.level().canSeeSky(side.above())) quality += 3E-5F;
                for (int depth = 1; depth <= 3; depth++) {
                    if (golem.level().getFluidState(side.below(depth)).is(net.minecraft.tags.FluidTags.WATER)) quality += 1.5E-5F;
                }
            }
            return quality;
        }

        private ItemStack fishingLoot() {
            float junk = Math.max(0F, .1F - golem.upgradeAmount(GolemUpgradeType.PERDITIO) * .025F);
            float treasure = .05F + golem.upgradeAmount(GolemUpgradeType.ORDO) * .0125F;
            float roll = golem.getRandom().nextFloat();
            if (roll < junk) {
                int weighted = golem.getRandom().nextInt(87);
                ItemStack result;
                float enchantChance = 0F;
                if (weighted < 10) { result = new ItemStack(Items.LEATHER_BOOTS); enchantChance = .9F; }
                else if (weighted < 20) result = new ItemStack(Items.ROTTEN_FLESH);
                else if (weighted < 35) result = new ItemStack(Items.BONE);
                else if (weighted < 45) result = new ItemStack(Items.STRING);
                else if (weighted < 50) result = new ItemStack(Items.BOWL);
                else if (weighted < 52) { result = new ItemStack(Items.FISHING_ROD); enchantChance = .9F; }
                else if (weighted < 62) result = new ItemStack(Items.STICK);
                else if (weighted < 67) result = new ItemStack(Items.ARROW, 10);
                else if (weighted < 77) result = new ItemStack(Items.TRIPWIRE_HOOK);
                else result = new ItemStack(Items.GUNPOWDER);
                return enchantChance > 0F && golem.getRandom().nextFloat() < enchantChance
                        ? net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
                                golem.getRandom(), result, 30, false) : result;
            }
            if (roll < junk + treasure) {
                ItemStack[] items = {new ItemStack(Items.LILY_PAD), new ItemStack(Items.NAME_TAG),
                        new ItemStack(Items.SADDLE), new ItemStack(Items.BOW), new ItemStack(Items.FISHING_ROD),
                        new ItemStack(Items.BOOK)};
                int selected = golem.getRandom().nextInt(items.length);
                ItemStack result = items[selected];
                float enchantChance = selected == 5 ? 1F : selected == 3 || selected == 4 ? .25F : 0F;
                if (enchantChance > 0F && golem.getRandom().nextFloat() < enchantChance) {
                    result = net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
                            golem.getRandom(), result, 30, false);
                }
                return result;
            }
            int fish = golem.getRandom().nextInt(100);
            return new ItemStack(fish < 60 ? Items.COD : fish < 85 ? Items.SALMON
                    : fish < 87 ? Items.TROPICAL_FISH : Items.PUFFERFISH);
        }

        private ItemStack smelt(ItemStack stack) {
            if (!(golem.level() instanceof net.minecraft.server.level.ServerLevel level)) return stack;
            return level.getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING,
                            new net.minecraft.world.SimpleContainer(stack), level)
                    .map(recipe -> recipe.getResultItem(level.registryAccess()).copyWithCount(stack.getCount()))
                    .filter(result -> !result.isEmpty()).orElse(stack);
        }

        private BlockEntity nearestBlockEntity(java.util.function.Predicate<BlockEntity> predicate) {
            return nearbyBlockEntities(predicate).stream().min(java.util.Comparator.comparingDouble(
                    be -> be.getBlockPos().distSqr(golem.blockPosition()))).orElse(null);
        }

        private List<BlockEntity> nearbyBlockEntities(java.util.function.Predicate<BlockEntity> predicate) {
            java.util.ArrayList<BlockEntity> found = new java.util.ArrayList<>();
            int radius = golem.workRange();
            BlockPos center = golem.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -3, -radius), center.offset(radius, 3, radius))) {
                BlockEntity be = golem.level().getBlockEntity(pos);
                if (be != null && predicate.test(be)) found.add(be);
            }
            return found;
        }

        private BlockPos findBlock(java.util.function.Predicate<BlockState> predicate) {
            return findBlock(golem.workRange(), predicate);
        }

        private BlockPos findBlock(int radius, java.util.function.Predicate<BlockState> predicate) {
            BlockPos center = golem.homePos();
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            for (BlockPos mutable : BlockPos.betweenClosed(center.offset(-radius, -3, -radius), center.offset(radius, 3, radius))) {
                if (!predicate.test(golem.level().getBlockState(mutable))) continue;
                double distance = mutable.distSqr(center);
                if (distance > radius * radius) continue;
                if (distance < bestDistance) { bestDistance = distance; best = mutable.immutable(); }
            }
            return best;
        }

        private static boolean containsMatching(Container container, ItemStack wanted) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                if (ItemStack.isSameItemSameTags(container.getItem(slot), wanted)) return true;
            }
            return false;
        }

        private static boolean containerHasRoom(Container container, ItemStack stack) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack present = container.getItem(slot);
                if (present.isEmpty() || ItemStack.isSameItemSameTags(present, stack)
                        && present.getCount() < Math.min(present.getMaxStackSize(), container.getMaxStackSize())) return true;
            }
            return false;
        }

        private static void moveOne(ItemStack source, Container destination) {
            for (int slot = 0; slot < destination.getContainerSize(); slot++) {
                ItemStack present = destination.getItem(slot);
                if (present.isEmpty()) {
                    destination.setItem(slot, source.split(Math.min(source.getCount(), source.getMaxStackSize())));
                    destination.setChanged();
                    return;
                }
                if (ItemStack.isSameItemSameTags(present, source) && present.getCount() < present.getMaxStackSize()) {
                    int moved = Math.min(source.getCount(), present.getMaxStackSize() - present.getCount());
                    present.grow(moved); source.shrink(moved); destination.setChanged(); return;
                }
            }
        }
    }

    private static boolean combatCore(ClassicGolemEntity golem) {
        return golem.core() == GolemCoreType.GUARD || golem.core() == GolemCoreType.BUTCHER;
    }
}
