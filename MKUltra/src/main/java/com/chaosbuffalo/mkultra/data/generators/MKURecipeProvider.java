package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkultra.init.MKUItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

public class MKURecipeProvider extends RecipeProvider {
    public MKURecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(pOutput, registries);
    }


    @Override
    protected void buildRecipes(RecipeOutput p_recipeOutput, HolderLookup.Provider holderLookup) {
        super.buildRecipes(p_recipeOutput, holderLookup);
        getSimpleChestplate(MKUItems.seawovenChestplate.get(), MKUItems.seawovenScrap.get()).save(p_recipeOutput);
        getSimpleBoots(MKUItems.seawovenBoots.get(), MKUItems.seawovenScrap.get()).save(p_recipeOutput);
        getSimpleLeggings(MKUItems.seawovenLeggings.get(), MKUItems.seawovenScrap.get()).save(p_recipeOutput);
        getSimpleHelmet(MKUItems.seawovenHelmet.get(), MKUItems.seawovenScrap.get()).save(p_recipeOutput);
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
