package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkultra.init.MKUItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class MKURecipeProvider extends RecipeProvider {
    public MKURecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        getSimpleChestplate(MKUItems.seawovenChestplate.get(), MKUItems.seawovenScrap.get()).save(pWriter);
        getSimpleBoots(MKUItems.seawovenBoots.get(), MKUItems.seawovenScrap.get()).save(pWriter);
        getSimpleLeggings(MKUItems.seawovenLeggings.get(), MKUItems.seawovenScrap.get()).save(pWriter);
        getSimpleHelmet(MKUItems.seawovenHelmet.get(), MKUItems.seawovenScrap.get()).save(pWriter);
    }

    protected ShapedRecipeBuilder getSimpleChestplate(ArmorItem item, ItemLike ingredient) {
        ShapedRecipeBuilder recipeBuilder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item);
        return recipeBuilder.define('I', ingredient)
                .pattern("I I")
                .pattern("III")
                .pattern("III")
                .unlockedBy("has_ingredient", has(ingredient));
    }

    protected ShapedRecipeBuilder getSimpleHelmet(ArmorItem item, ItemLike ingredient) {
        ShapedRecipeBuilder recipeBuilder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item);
        return recipeBuilder.define('I', ingredient)
                .pattern("III")
                .pattern("I I")
                .unlockedBy("has_ingredient", has(ingredient));
    }

    protected ShapedRecipeBuilder getSimpleLeggings(ArmorItem item, ItemLike ingredient) {
        ShapedRecipeBuilder recipeBuilder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item);
        return recipeBuilder.define('I', ingredient)
                .pattern("III")
                .pattern("I I")
                .pattern("I I")
                .unlockedBy("has_ingredient", has(ingredient));
    }

    protected ShapedRecipeBuilder getSimpleBoots(ArmorItem item, ItemLike ingredient) {
        ShapedRecipeBuilder recipeBuilder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item);
        return recipeBuilder.define('I', ingredient)
                .pattern("I I")
                .pattern("I I")
                .unlockedBy("has_ingredient", has(ingredient));
    }
}
