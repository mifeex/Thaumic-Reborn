package com.thaumcraftmodern.arcane;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

/**
 * Arcane shaped crafting with vanilla shaped-recipe identity. Extending
 * ShapedRecipe is important: recipe-book and third-party transfer handlers
 * use that contract to preserve rows, columns and empty pattern cells.
 */
public final class ArcaneShapedRecipe extends ShapedRecipe implements ArcaneRecipe {
    private final ResourceLocation id;
    private final String researchId;
    private final int width;
    private final int height;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final ArcaneVisCost visCost;

    public ArcaneShapedRecipe(
            ResourceLocation id,
            String researchId,
            int width,
            int height,
            NonNullList<Ingredient> ingredients,
            ItemStack result,
            ArcaneVisCost visCost
    ) {
        super(
                id,
                "",
                CraftingBookCategory.MISC,
                width,
                height,
                ingredients,
                result
        );
        this.id = id;
        this.researchId = researchId;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result.copy();
        this.visCost = visCost;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        for (int offsetX = 0; offsetX <= container.getWidth() - width; offsetX++) {
            for (int offsetY = 0; offsetY <= container.getHeight() - height; offsetY++) {
                if (matchesAt(container, offsetX, offsetY, false)
                        || matchesAt(container, offsetX, offsetY, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesAt(
            CraftingContainer container,
            int offsetX,
            int offsetY,
            boolean mirrored
    ) {
        for (int gridX = 0; gridX < container.getWidth(); gridX++) {
            for (int gridY = 0; gridY < container.getHeight(); gridY++) {
                int recipeX = gridX - offsetX;
                int recipeY = gridY - offsetY;
                Ingredient expected = Ingredient.EMPTY;
                if (recipeX >= 0 && recipeY >= 0 && recipeX < width && recipeY < height) {
                    int actualX = mirrored ? width - recipeX - 1 : recipeX;
                    expected = ingredients.get(actualX + recipeY * width);
                }
                if (!expected.test(container.getItem(gridX + gridY * container.getWidth()))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int gridWidth, int gridHeight) {
        return gridWidth >= width && gridHeight >= height;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public int width() {
        return width;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModArcaneRecipes.ARCANE_SHAPED_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModArcaneRecipes.ARCANE_CRAFTING_TYPE.get();
    }

    @Override
    public String researchId() {
        return researchId;
    }

    @Override
    public ArcaneVisCost visCost() {
        return visCost;
    }

    public static final class Serializer implements RecipeSerializer<ArcaneShapedRecipe> {
        @Override
        public ArcaneShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            String[] pattern = ArcaneRecipeJson.pattern(json);
            NonNullList<Ingredient> ingredients = ArcaneRecipeJson.ingredients(
                    pattern,
                    GsonHelper.getAsJsonObject(json, "key")
            );
            return new ArcaneShapedRecipe(
                    id,
                    ArcaneRecipeJson.researchId(json),
                    pattern[0].length(),
                    pattern.length,
                    ingredients,
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")),
                    ArcaneRecipeJson.visCost(json)
            );
        }

        @Override
        public ArcaneShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            String researchId = buffer.readUtf();
            ArcaneVisCost visCost = ArcaneVisCost.fromNetwork(buffer);
            NonNullList<Ingredient> ingredients =
                    NonNullList.withSize(width * height, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();
            return new ArcaneShapedRecipe(
                    id,
                    researchId,
                    width,
                    height,
                    ingredients,
                    result,
                    visCost
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ArcaneShapedRecipe recipe) {
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);
            buffer.writeUtf(recipe.researchId);
            recipe.visCost.toNetwork(buffer);
            recipe.ingredients.forEach(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeItem(recipe.result);
        }
    }
}
