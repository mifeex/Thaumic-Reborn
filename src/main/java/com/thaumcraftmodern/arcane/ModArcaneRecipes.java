package com.thaumcraftmodern.arcane;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.thaumcraftmodern.alchemy.DoubleSmeltingRecipe;
import com.thaumcraftmodern.alchemy.DoubleBlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModArcaneRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ThaumcraftModern.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<RecipeType<ArcaneRecipe>> ARCANE_CRAFTING_TYPE =
            RECIPE_TYPES.register(
                    "arcane_crafting",
                    () -> RecipeType.simple(
                            new ResourceLocation(ThaumcraftModern.MOD_ID, "arcane_crafting")
                    )
            );
    public static final RegistryObject<RecipeSerializer<ArcaneShapedRecipe>>
            ARCANE_SHAPED_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "arcane_shaped",
                    ArcaneShapedRecipe.Serializer::new
            );
    public static final RegistryObject<RecipeSerializer<ArcaneShapelessRecipe>>
            ARCANE_SHAPELESS_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "arcane_shapeless",
                    ArcaneShapelessRecipe.Serializer::new
            );
    public static final RegistryObject<RecipeSerializer<ArcaneWandAssemblyRecipe>>
            ARCANE_WAND_ASSEMBLY_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "arcane_wand_assembly",
                    () -> new ArcaneWandAssemblyRecipe.Serializer(false)
            );
    public static final RegistryObject<RecipeSerializer<ArcaneWandAssemblyRecipe>>
            ARCANE_SCEPTRE_ASSEMBLY_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "arcane_sceptre_assembly",
                    () -> new ArcaneWandAssemblyRecipe.Serializer(true)
            );
    public static final RegistryObject<RecipeSerializer<DoubleSmeltingRecipe>>
            DOUBLE_SMELTING_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "double_smelting",
                    DoubleSmeltingRecipe.Serializer::new
            );
    public static final RegistryObject<RecipeSerializer<DoubleBlastingRecipe>>
            DOUBLE_BLASTING_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "double_blasting",
                    DoubleBlastingRecipe.Serializer::new
            );
    public static final RegistryObject<RecipeSerializer<KnowledgeFragmentRecipe>>
            KNOWLEDGE_FRAGMENT_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "knowledge_fragment_research",
                    () -> new SimpleCraftingRecipeSerializer<>(KnowledgeFragmentRecipe::new)
            );

    private ModArcaneRecipes() {
    }

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
    }
}
