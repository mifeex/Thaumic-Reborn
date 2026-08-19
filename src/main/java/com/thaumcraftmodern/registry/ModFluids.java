package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

/**
 * Client-visible fluid states used by the finite Flux Goo block.
 *
 * <p>The block remains responsible for the original TC4 finite-volume
 * simulation. These fluid states supply vanilla's sloped, flowing surface
 * renderer instead of eight rigid cuboid block models.</p>
 */
public final class ModFluids {
    private static final ResourceLocation FLUX_GOO_TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "block/flux_goo"
            );
    private static final ResourceLocation PURIFYING_FLUID_TEXTURE =
            new ResourceLocation(ThaumcraftModern.MOD_ID, "block/fluidpure");

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(
                    ForgeRegistries.Keys.FLUID_TYPES,
                    ThaumcraftModern.MOD_ID
            );
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(
                    ForgeRegistries.Keys.FLUIDS,
                    ThaumcraftModern.MOD_ID
            );

    public static final RegistryObject<FluidType> FLUX_GOO_TYPE =
            FLUID_TYPES.register(
                    "flux_goo",
                    () -> new FluidType(
                            FluidType.Properties.create()
                                    .density(8)
                                    .viscosity(6000)
                                    .canPushEntity(false)
                                    .canSwim(false)
                                    .canDrown(false)
                                    .supportsBoating(false)
                    ) {
                        @Override
                        public void initializeClient(
                                Consumer<IClientFluidTypeExtensions> consumer
                        ) {
                            consumer.accept(
                                    new IClientFluidTypeExtensions() {
                                        @Override
                                        public ResourceLocation
                                        getStillTexture() {
                                            return FLUX_GOO_TEXTURE;
                                        }

                                        @Override
                                        public ResourceLocation
                                        getFlowingTexture() {
                                            return FLUX_GOO_TEXTURE;
                                        }
                                    }
                            );
                        }
                    }
            );

    public static final RegistryObject<FlowingFluid> FLUX_GOO_SOURCE =
            FLUIDS.register(
                    "flux_goo",
                    () -> new StaticSource(properties())
            );
    public static final RegistryObject<FlowingFluid> FLUX_GOO_FLOWING =
            FLUIDS.register(
                    "flowing_flux_goo",
                    () -> new StaticFlowing(properties())
            );

    public static final RegistryObject<FluidType> PURIFYING_TYPE =
            FLUID_TYPES.register(
                    "purifying_fluid",
                    () -> new FluidType(
                            FluidType.Properties.create()
                                    .lightLevel(8)
                                    .viscosity(1500)
                                    .rarity(Rarity.RARE)
                    ) {
                        @Override
                        public void initializeClient(
                                Consumer<IClientFluidTypeExtensions> consumer
                        ) {
                            consumer.accept(new IClientFluidTypeExtensions() {
                                @Override
                                public ResourceLocation getStillTexture() {
                                    return PURIFYING_FLUID_TEXTURE;
                                }

                                @Override
                                public ResourceLocation getFlowingTexture() {
                                    return PURIFYING_FLUID_TEXTURE;
                                }
                            });
                        }
                    }
            );
    public static final RegistryObject<FlowingFluid> PURIFYING_SOURCE =
            FLUIDS.register(
                    "purifying_fluid",
                    () -> new ForgeFlowingFluid.Source(purifyingProperties())
            );
    public static final RegistryObject<FlowingFluid> PURIFYING_FLOWING =
            FLUIDS.register(
                    "flowing_purifying_fluid",
                    () -> new ForgeFlowingFluid.Flowing(purifyingProperties())
            );

    private ModFluids() {
    }

    private static ForgeFlowingFluid.Properties properties() {
        return new ForgeFlowingFluid.Properties(
                FLUX_GOO_TYPE,
                FLUX_GOO_SOURCE,
                FLUX_GOO_FLOWING
        ).tickRate(30);
    }

    private static ForgeFlowingFluid.Properties purifyingProperties() {
        return new ForgeFlowingFluid.Properties(
                PURIFYING_TYPE,
                PURIFYING_SOURCE,
                PURIFYING_FLOWING
        ).block(() -> (LiquidBlock) ModBlocks.PURIFYING_FLUID.get())
                .bucket(ModItems.PURIFYING_FLUID_BUCKET)
                .tickRate(5)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }

    /**
     * Vanilla's liquid renderer needs a FlowingFluid, but the actual movement
     * must remain the finite TC4 algorithm in {@code FiniteFluxFlow}.
     */
    private static final class StaticSource
            extends ForgeFlowingFluid.Source {
        private StaticSource(Properties properties) {
            super(properties);
        }

        @Override
        public void tick(Level level, net.minecraft.core.BlockPos position,
                         FluidState state) {
            // Movement is server-authoritatively handled by FluxGooBlock.
        }
    }

    private static final class StaticFlowing
            extends ForgeFlowingFluid.Flowing {
        private StaticFlowing(Properties properties) {
            super(properties);
        }

        @Override
        public void tick(Level level, net.minecraft.core.BlockPos position,
                         FluidState state) {
            // Movement is server-authoritatively handled by FluxGooBlock.
        }
    }
}
