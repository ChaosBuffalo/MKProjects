package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MKWeaponRecipeProvider extends RecipeProvider {

    public record WeaponRecipe(List<String> pattern, Map<Character, Item> definitions) {

        public boolean hasHaft() {
            return definitions.containsKey('H');
        }

        public boolean hasStick() {
            return definitions.containsKey('S');
        }
    }

    public static final Map<IMeleeWeaponType, WeaponRecipe> weaponRecipes = new HashMap<>();

    static {
        weaponRecipes.put(MeleeWeaponTypes.DAGGER_TYPE, new WeaponRecipe(
                Arrays.asList("I", "S"),
                Map.of('S', Items.STICK)));
        weaponRecipes.put(MeleeWeaponTypes.BATTLEAXE_TYPE, new WeaponRecipe(
                Arrays.asList("III", "IHI", " H "),
                Map.of('H', MKWeaponsItems.Haft.get())));
        weaponRecipes.put(MeleeWeaponTypes.GREATSWORD_TYPE, new WeaponRecipe(
                Arrays.asList(" I ", " I ", "IHI"),
                Map.of('H', MKWeaponsItems.Haft.get())));
        weaponRecipes.put(MeleeWeaponTypes.KATANA_TYPE, new WeaponRecipe(
                Arrays.asList("  I", " I ", "H  "),
                Map.of('H', MKWeaponsItems.Haft.get())));
        weaponRecipes.put(MeleeWeaponTypes.LONGSWORD_TYPE, new WeaponRecipe(
                Arrays.asList(" I ", " I ", " H "),
                Map.of('H', MKWeaponsItems.Haft.get())));
        weaponRecipes.put(MeleeWeaponTypes.SPEAR_TYPE, new WeaponRecipe(
                Arrays.asList("  I", " H ", "H  "),
                Map.of('H', MKWeaponsItems.Haft.get())));
        weaponRecipes.put(MeleeWeaponTypes.WARHAMMER_TYPE, new WeaponRecipe(
                Arrays.asList(" II", " HI", "H  "),
                Map.of('H', MKWeaponsItems.Haft.get())));
        weaponRecipes.put(MeleeWeaponTypes.MACE_TYPE, new WeaponRecipe(
                Arrays.asList(" I ", " H ", " H "),
                Map.of('H', MKWeaponsItems.Haft.get())));
        weaponRecipes.put(MeleeWeaponTypes.STAFF_TYPE, new WeaponRecipe(
                Arrays.asList("  I", " H ", "I  "),
                Map.of('H', MKWeaponsItems.Haft.get())));
    }


    public MKWeaponRecipeProvider(PackOutput generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        getHaftRecipe().save(consumer);

        for (MKMeleeWeapon weapon : MKWeaponsItems.WEAPONS) {
            if (!weapon.getMKTier().equals(MKWeaponsItems.NETHERITE_TIER)) {
                getRecipe(weapon).save(consumer);
            } else {
                mkNetheriteSmithing(consumer,
                        MKWeaponsItems.lookupWeapon(MKWeaponsItems.DIAMOND_TIER, weapon.getWeaponType()),
                        RecipeCategory.COMBAT, weapon);
            }
        }

        for (MKBow bow : MKWeaponsItems.BOWS) {
            getLongbowRecipe(bow).save(consumer);
        }
    }

    private ShapedRecipeBuilder getHaftRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MKWeaponsItems.Haft.get(), 3)
                .define('S', Items.STICK)
                .define('L', Tags.Items.LEATHER)
                .pattern("SSS")
                .pattern(" L ")
                .pattern("SSS")
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_leather", has(Tags.Items.LEATHER));
    }

    private static void mkNetheriteSmithing(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Item pIngredientItem, RecipeCategory pCategory, Item pResultItem) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(pIngredientItem),
                        Ingredient.of(Items.NETHERITE_INGOT), pCategory, pResultItem)
                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(pFinishedRecipeConsumer, new ResourceLocation(MKWeapons.MODID, getItemName(pResultItem) + "_smithing"));
    }

    private ShapedRecipeBuilder getLongbowRecipe(MKBow bow) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, bow)
                .define('H', MKWeaponsItems.Haft.get())
                .define('I', bow.getMKTier().getPrimaryIngredient())
                .define('S', Tags.Items.STRING)
                .pattern(" IS")
                .pattern("H S")
                .pattern(" IS")
                .unlockedBy("has_haft", has(MKWeaponsItems.Haft.get()))
                .unlockedBy("has_string", has(Tags.Items.STRING))
                .unlockedBy("has_ingot", has(bow.getMKTier().getPrimaryIngredientTag()))
                ;
    }

    private ShapedRecipeBuilder getRecipe(MKMeleeWeapon weapon) {
        ShapedRecipeBuilder recipeBuilder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, weapon);
        recipeBuilder.define('I', weapon.getMKTier().getPrimaryIngredient());
        WeaponRecipe weaponRecipe = weaponRecipes.get(weapon.getWeaponType());
        weaponRecipe.definitions().forEach(recipeBuilder::define);
        for (String line : weaponRecipe.pattern()) {
            recipeBuilder.pattern(line);
        }
        if (weaponRecipe.hasHaft()) {
            recipeBuilder.unlockedBy("has_haft", has(MKWeaponsItems.Haft.get()));
        }
        if (weaponRecipe.hasStick()) {
            recipeBuilder.unlockedBy("has_stick", has(Items.STICK));
        }
        recipeBuilder.unlockedBy("has_ingot", has(weapon.getMKTier().getPrimaryIngredientTag()));
        return recipeBuilder;
    }
}
