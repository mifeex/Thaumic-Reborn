package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.NodeChargingService;
import com.thaumcraftmodern.focus.WandFocusService;
import com.thaumcraftmodern.client.WandClientItemExtensions;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandStateCodec;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.wand.WandComponentRegistry;
import com.thaumcraftmodern.wand.WandForm;
import com.thaumcraftmodern.wand.WandInteractable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A configured casting wand. Composition and vis are always read from its
 * versioned stack NBT; constructor defaults only create new stacks.
 */
public final class WandItem extends Item {
    private static final int THAUMONOMICON_SPARKLES = 7;

    private final String defaultRodId;
    private final String defaultCapId;
    private final WandForm form;
    private final boolean filledByDefault;
    private final int fallbackFilledCapacityVis;

    public WandItem(
            String defaultRodId,
            String defaultCapId,
            Properties properties
    ) {
        this(
                defaultRodId,
                defaultCapId,
                WandForm.WAND,
                false,
                0,
                properties
        );
    }

    public WandItem(
            String defaultRodId,
            String defaultCapId,
            WandForm form,
            boolean filledByDefault,
            Properties properties
    ) {
        this(
                defaultRodId,
                defaultCapId,
                form,
                filledByDefault,
                0,
                properties
        );
    }

    public WandItem(
            String defaultRodId,
            String defaultCapId,
            WandForm form,
            boolean filledByDefault,
            int fallbackFilledCapacityVis,
            Properties properties
    ) {
        super(properties);
        this.defaultRodId = Objects.requireNonNull(defaultRodId, "defaultRodId");
        this.defaultCapId = Objects.requireNonNull(defaultCapId, "defaultCapId");
        this.form = Objects.requireNonNull(form, "form");
        this.filledByDefault = filledByDefault;
        if (fallbackFilledCapacityVis < 0) {
            throw new IllegalArgumentException(
                    "fallback filled capacity cannot be negative"
            );
        }
        this.fallbackFilledCapacityVis = fallbackFilledCapacityVis;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return filledByDefault
                ? createFilled(defaultRodId, defaultCapId)
                : create(defaultRodId, defaultCapId);
    }

    public ItemStack create(String rodId, String capId) {
        return WandStateCodec.create(new ItemStack(this), rodId, capId);
    }

    public ItemStack createFilled(String rodId, String capId) {
        ItemStack stack = create(rodId, capId);
        int capacityVis = WandComponentRegistry.rod(rodId)
                .map(rod -> form.applyCapacity(rod.capacityVis()))
                .orElseGet(() -> {
                    if (rodId.equals(defaultRodId)
                            && fallbackFilledCapacityVis > 0) {
                        return fallbackFilledCapacityVis;
                    }
                    throw new IllegalArgumentException(
                            "unknown wand rod id: " + rodId
                    );
                });
        int capacityCentivis = Math.multiplyExact(
                capacityVis,
                WandVisService.CENTIVIS_PER_VIS
        );
        java.util.EnumMap<PrimalAspect, Integer> full =
                new java.util.EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            full.put(aspect, capacityCentivis);
        }
        WandStateCodec.write(
                stack,
                new WandState(
                        WandStateCodec.SERIAL_VERSION,
                        rodId,
                        capId,
                        full
                )
        );
        return stack;
    }

    public WandForm form() {
        return form;
    }

    @Override
    public Component getName(ItemStack stack) {
        WandState state = WandVisService.state(stack).orElse(null);
        if (state == null) {
            return super.getName(stack);
        }
        Component rod = WandComponentRegistry.rod(state.rodId())
                .map(definition -> Component.translatable(
                        definition.translationKey()
                ))
                .orElse(Component.literal(state.rodId()));
        Component cap = WandComponentRegistry.cap(state.capId())
                .map(definition -> Component.translatable(
                        definition.translationKey()
                ))
                .orElse(Component.literal(state.capId()));
        return Component.translatable(
                "item.thaumic_reborn.assembled_wand",
                cap,
                rod,
                Component.translatable(form.translationKey())
        );
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity entity,
            int slotId,
            boolean isSelected
    ) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        WandState state = WandVisService.state(stack).orElse(null);
        if (state == null) {
            return;
        }
        WandComponentRegistry.rod(state.rodId()).ifPresent(rod -> {
            if (rod.rechargeAspects().isEmpty()
                    || player.tickCount % rod.rechargeIntervalTicks() != 0) {
                return;
            }
            int threshold = WandVisService.capacityCentivis(stack) / 10;
            List<String> candidates = rod.rechargeAspects().stream()
                    .filter(aspect -> WandVisService.visCentivis(stack, aspect)
                            < threshold)
                    .toList();
            if (candidates.isEmpty()) {
                return;
            }
            String selected = candidates.get(
                    level.random.nextInt(candidates.size())
            );
            WandVisService.addCentivis(
                    player,
                    stack,
                    selected,
                    rod.rechargeCentivis()
            );
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos position = context.getClickedPos();
        var state = level.getBlockState(position);
        if (context.getPlayer() != null
                && state.getBlock() instanceof WandInteractable interactable) {
            InteractionResult result = interactable.onWandRightClick(
                    state,
                    level,
                    position,
                    context.getPlayer(),
                    context.getHand(),
                    new net.minecraft.world.phys.BlockHitResult(
                            context.getClickLocation(),
                            context.getClickedFace(),
                            position,
                            context.isInside()
                    )
            );
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        if (!state.is(Blocks.BOOKSHELF)) {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                return WandFocusService.cast(context.getItemInHand(), level,
                        serverPlayer, context.getHand(), new net.minecraft.world.phys.BlockHitResult(
                                context.getClickLocation(), context.getClickedFace(), position,
                                context.isInside()));
            }
            return WandFocusService.hasFocus(context.getItemInHand())
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (WandVisService.state(context.getItemInHand()).isEmpty()) {
            return InteractionResult.PASS;
        }

        /*
         * TC4's createThaumonomicon path has no vis check and no vis
         * consumption. Do not route this through WandVisService.consume.
         */
        if (!level.setBlock(position, Blocks.AIR.defaultBlockState(), 3)) {
            return InteractionResult.FAIL;
        }
        ItemEntity book = new ItemEntity(
                level,
                position.getX() + 0.5D,
                position.getY() + 0.3D,
                position.getZ() + 0.5D,
                new ItemStack(ModItems.THAUMONOMICON.get())
        );
        book.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(book);
        level.playSound(
                null,
                position,
                ModSounds.WAND.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
        spawnThaumonomiconSparkles((ServerLevel) level, position);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!WandFocusService.hasFocus(stack)) return InteractionResultHolder.pass(stack);
        if (level.isClientSide) {
            if (WandFocusService.continuous(stack)) player.startUsingItem(hand);
            return InteractionResultHolder.success(stack);
        }
        net.minecraft.world.phys.HitResult aimed = player.pick(20.0D, 1.0F, false);
        WandFocusService.cast(stack, level, (ServerPlayer) player, hand,
                aimed instanceof net.minecraft.world.phys.BlockHitResult blockHit
                        ? blockHit : null);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    /**
     * Vis and the active node session live in stack NBT and change while the
     * wand remains in the same physical hand. Those updates must not restart
     * Minecraft's first-person equip animation.
     */
    @Override
    public boolean shouldCauseReequipAnimation(
            ItemStack oldStack,
            ItemStack newStack,
            boolean slotChanged
    ) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(WandClientItemExtensions.INSTANCE);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public void onUseTick(
            Level level,
            LivingEntity entity,
            ItemStack stack,
            int remainingUseDuration
    ) {
        NodeChargingService.tick(level, entity, stack, remainingUseDuration);
        WandFocusService.tick(stack, level, entity);
    }

    @Override
    public void releaseUsing(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            int remainingUseDuration
    ) {
        NodeChargingService.clear(entity, stack);
        WandFocusService.stopped(entity, stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        WandState state = WandVisService.state(stack).orElse(null);
        if (state == null) {
            tooltip.add(Component.translatable(
                            "tooltip.thaumic_reborn.wand.invalid"
                    )
                    .withStyle(ChatFormatting.RED));
            return;
        }
        int capacity = WandVisService.capacity(stack);
        tooltip.add(Component.translatable(
                        "tooltip.thaumic_reborn.wand.capacity",
                        capacity
                )
                .withStyle(ChatFormatting.DARK_PURPLE));
        if (form == WandForm.SCEPTRE) {
            tooltip.add(Component.translatable(
                            "tooltip.thaumic_reborn.wand.sceptre.crafting"
                    )
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable(
                            "tooltip.thaumic_reborn.wand.sceptre.no_focus"
                    )
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else if (form == WandForm.STAFF) {
            tooltip.add(Component.translatable(
                            "tooltip.thaumic_reborn.wand.staff.no_workbench"
                    )
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable(
                            "tooltip.thaumic_reborn.wand.staff.focus"
                    )
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        WandFocusService.focusStack(stack).ifPresent(focus -> tooltip.add(
                Component.translatable("tooltip.thaumic_reborn.wand.focus", focus.getHoverName())
                        .withStyle(ChatFormatting.GOLD)));
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            tooltip.add(Component.translatable(
                            "tooltip.thaumic_reborn.wand.vis",
                            Component.translatable(
                                    "aspect.thaumic_reborn." + aspect.id()
                            ),
                            formatCentivis(state.visCentivis(aspect)),
                            capacity
                    )
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    static String formatCentivis(int centivis) {
        if (centivis % WandVisService.CENTIVIS_PER_VIS == 0) {
            return Integer.toString(
                    centivis / WandVisService.CENTIVIS_PER_VIS
            );
        }
        return String.format(
                Locale.ROOT,
                "%.2f",
                centivis / (double) WandVisService.CENTIVIS_PER_VIS
        );
    }

    /**
     * The legacy -9999 sparkle color randomizes each channel from 0.33 to 1.
     * Seven particles and their 1.2-block position spread are kept exact. The
     * modern dust sprite is the explicit 1.20.1 rendering adapter.
     */
    private static void spawnThaumonomiconSparkles(
            ServerLevel level,
            BlockPos position
    ) {
        RandomSource random = level.getRandom();
        for (int index = 0; index < THAUMONOMICON_SPARKLES; index++) {
            Vector3f color = new Vector3f(
                    0.33F + random.nextFloat() * 0.67F,
                    0.33F + random.nextFloat() * 0.67F,
                    0.33F + random.nextFloat() * 0.67F
            );
            double x = position.getX() - 0.1D + random.nextDouble() * 1.2D;
            double y = position.getY() - 0.1D + random.nextDouble() * 1.2D;
            double z = position.getZ() - 0.1D + random.nextDouble() * 1.2D;
            level.sendParticles(
                    new DustParticleOptions(color, 1.0F),
                    x,
                    y,
                    z,
                    0,
                    0.0D,
                    random.nextDouble() * 0.02D,
                    0.0D,
                    1.0D
            );
        }
    }
}
