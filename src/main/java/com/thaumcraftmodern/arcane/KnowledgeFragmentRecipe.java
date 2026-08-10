package com.thaumcraftmodern.arcane;

import com.thaumcraftmodern.item.ResearchNotesItem;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** TC4's 3x3 knowledge-fragment recipe for an unknown discovery. */
public final class KnowledgeFragmentRecipe extends CustomRecipe {
    public KnowledgeFragmentRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (container.getWidth() != 3 || container.getHeight() != 3) return false;
        for (int slot = 0; slot < 9; slot++) {
            if (!container.getItem(slot).is(ModItems.KNOWLEDGE_FRAGMENT.get())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
        return ResearchNotesItem.createUnknownDiscovery();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return ResearchNotesItem.createUnknownDiscovery();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.withSize(
                9,
                Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT.get())
        );
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModArcaneRecipes.KNOWLEDGE_FRAGMENT_SERIALIZER.get();
    }
}
