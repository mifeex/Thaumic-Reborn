package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeScanIdentity;
import com.thaumcraftmodern.visnet.EnergizedAuraNodeBlockEntity;
import com.thaumcraftmodern.client.render.ThaumometerItemClientExtensions;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.scan.ScanSessionManager;
import com.thaumcraftmodern.scan.ScanTargetType;
import com.thaumcraftmodern.scan.ScanTargeting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class ThaumometerItem extends Item {
    public static final int USE_DURATION = 25;

    public ThaumometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ThaumometerItemClientExtensions.create());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Entity entity = ScanTargeting.findAnyEntity(player, 1.0F).orElse(null);
        boolean started;
        if (entity != null) {
            started = tryStartEntityTarget(player, context.getHand(), entity);
        } else {
            BlockHitResult preciseHit = ScanTargeting.findBlock(player, 1.0F)
                    .orElse(null);
            started = tryStartBlockTarget(
                    player,
                    context.getHand(),
                    preciseHit == null
                            ? context.getClickedPos()
                            : preciseHit.getBlockPos()
            );
        }
        /*
         * SUCCESS asks the client to swing the hand. Scanning is a held use,
         * not an ordinary right-click interaction, so consume it without the
         * one-shot arm swing. PASS keeps already studied targets available to
         * the normal block interaction pipeline.
         */
        return started ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        return tryStartEntityTarget(player, hand, target)
                ? InteractionResult.CONSUME
                : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        Entity entity = ScanTargeting.findAnyEntity(player, 1.0F).orElse(null);
        if (entity != null) {
            return tryStartEntityTarget(player, hand, entity)
                    ? InteractionResultHolder.consume(player.getItemInHand(hand))
                    : InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        BlockHitResult blockHit = ScanTargeting.findBlock(player, 1.0F).orElse(null);
        if (blockHit != null) {
            return tryStartBlockTarget(player, hand, blockHit.getBlockPos())
                    ? InteractionResultHolder.consume(player.getItemInHand(hand))
                    : InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(otherHand);
        if (target.isEmpty() || target.getItem() instanceof ThaumometerItem) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        ScanRegistry.ItemScanIdentity identity = ScanRegistry.identityForItem(target);
        if (!isAlreadyStudied(player, identity.type(), identity.targetId())) {
            player.startUsingItem(hand);
            if (player instanceof ServerPlayer serverPlayer) {
                ScanSessionManager.startItem(serverPlayer, hand, otherHand, target);
            }
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    static boolean tryStartEntityTarget(
            Player player,
            InteractionHand hand,
            Entity entity
    ) {
        if (entity instanceof ItemEntity itemEntity) {
            ScanRegistry.ItemScanIdentity identity =
                    ScanRegistry.identityForItem(itemEntity.getItem());
            if (isAlreadyStudied(player, identity.type(), identity.targetId())) {
                return false;
            }
            player.startUsingItem(hand);
            if (player instanceof ServerPlayer serverPlayer) {
                ScanSessionManager.startDroppedItem(serverPlayer, hand, itemEntity);
            }
        } else {
            String targetId =
                    com.thaumcraftmodern.scan.EntityScanIdentity
                            .targetId(entity);
            if (isAlreadyStudied(player, ScanTargetType.ENTITY, targetId)) {
                return false;
            }
            player.startUsingItem(hand);
            if (player instanceof ServerPlayer serverPlayer) {
                ScanSessionManager.startEntity(serverPlayer, hand, entity);
            }
        }
        return true;
    }

    private static boolean tryStartBlockTarget(
            Player player,
            InteractionHand hand,
            BlockPos position
    ) {
        AuraNodeBlockEntity auraNode = player.level().getBlockEntity(position)
                instanceof AuraNodeBlockEntity node
                ? node
                : null;
        EnergizedAuraNodeBlockEntity energizedNode =
                player.level().getBlockEntity(position)
                        instanceof EnergizedAuraNodeBlockEntity node
                        ? node
                        : null;
        boolean isAuraNode = auraNode != null || energizedNode != null;
        ScanTargetType targetType = isAuraNode
                ? ScanTargetType.PHENOMENON
                : ScanTargetType.BLOCK;
        String targetId = isAuraNode
                ? AuraNodeScanIdentity.TARGET_ID.toString()
                : ScanRegistry.canonicalBlockId(BuiltInRegistries.BLOCK
                        .getKey(player.level().getBlockState(position).getBlock())
                        .toString());
        boolean alreadyStudied = isAuraNode
                ? KnowledgeAccess.get(player)
                        .map(knowledge -> knowledge.hasScan(
                                new AuraNodeScanIdentity(auraNode != null
                                        ? auraNode.scanIdentity().nodeId()
                                        : energizedNode.originalState().nodeId())
                                        .scanKey()
                        ))
                        .orElse(false)
                : isAlreadyStudied(player, targetType, targetId);
        if (alreadyStudied) {
            return false;
        }
        player.startUsingItem(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            ScanSessionManager.startBlock(serverPlayer, hand, position);
        }
        return true;
    }

    private static boolean isAlreadyStudied(
            Player player,
            ScanTargetType type,
            String targetId
    ) {
        return KnowledgeAccess.get(player)
                .map(knowledge -> knowledge.hasScan(
                        ScanRegistry.knowledgeKey(type, targetId)
                ))
                .orElse(false);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide) {
            int elapsedTicks = USE_DURATION - remainingUseDuration;
            if ((elapsedTicks & 1) == 0) {
                level.playLocalSound(
                        livingEntity.getX(),
                        livingEntity.getY(),
                        livingEntity.getZ(),
                        ModSounds.CAMERA_TICKS.get(),
                        SoundSource.PLAYERS,
                        0.2F,
                        0.45F + livingEntity.getRandom().nextFloat() * 0.1F,
                        false
                );
            }
        } else if (livingEntity instanceof ServerPlayer player) {
            ScanSessionManager.tick(player, stack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!level.isClientSide && livingEntity instanceof ServerPlayer player) {
            ScanSessionManager.cancel(player.getUUID());
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        /*
         * The scan session still uses the vanilla held-use lifecycle, but it
         * must not put the third-person player model into the bow pose.
         */
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("tooltip.thaumcraftmodern.thaumometer"));
    }
}
