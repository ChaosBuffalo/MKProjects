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
import net.minecraft.util.Tuple;
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

    public record WeaponRecipe(List<String> pattern, List<Tuple<Character, Item>> itemKeys) {

        public boolean hasHaft() {
                for (Tuple<Character, Item> tuple : itemKeys) {
                    if (tuple.getA().equals('H')) {
                        return true;
                    }
                }
                return false;
            }

            public boolean hasStick() {
                for (Tuple<Character, Item> tuple : itemKeys) {
                    if (tuple.getA().equals('S')) {
                        return true;
                    }
                }
                return false;
            }
        }

    public static final Map<IMeleeWeaponType, WeaponRecipe> weaponRecipes = new HashMap<>();

    static {
        weaponRecipes.put(MeleeWeaponTypes.DAGGER_TYPE, new WeaponRecipe(
                Arrays.asList("I", "S"),
                List.of(new Tuple<>('S', Items.STICK))));
        weaponRecipes.put(MeleeWeaponTypes.BATTLEAXE_TYPE, new WeaponRecipe(
                Arrays.asList("III", "IHI", " H "),
                List.of(new Tuple<>('H', MKWeaponsItems.Haft.get()))));
        weaponRecipes.put(MeleeWeaponTypes.GREATSWORD_TYPE, new WeaponRecipe(
                Arrays.asList(" I ", " I ", "IHI"),
                List.of(new Tuple<>('H', MKWeaponsItems.Haft.get()))));
        weaponRecipes.put(MeleeWeaponTypes.KATANA_TYPE, new WeaponRecipe(
                Arrays.asList("  I", " I ", "H  "),
                List.of(new Tuple<>('H', MKWeaponsItems.Haft.get()))));
        weaponRecipes.put(MeleeWeaponTypes.LONGSWORD_TYPE, new WeaponRecipe(
                Arrays.asList(" I ", " I ", " H "),
                List.of(new Tuple<>('H', MKWeaponsItems.Haft.get()))));
        weaponRecipes.put(MeleeWeaponTypes.SPEAR_TYPE, new WeaponRecipe(
                Arrays.asList("  I", " H ", "H  "),
                List.of(new Tuple<>('H', MKWeaponsItems.Haft.get()))));
        weaponRecipes.put(MeleeWeaponTypes.WARHAMMER_TYPE, new WeaponRecipe(
                Arrays.asList(" II", " HI", "H  "),
                List.of(new Tuple<>('H', MKWeaponsItems.Haft.get()))));
        weaponRecipes.put(MeleeWeaponTypes.MACE_TYPE, new WeaponRecipe(
                Arrays.asList(" I ", " H ", " H "),
                List.of(new Tuple<>('H', MKWeaponsItems.Haft.get()))));
        weaponRecipes.put(MeleeWeaponTypes.STAFF_TYPE, new WeaponRecipe(
                Arrays.asList("  I", " H ", "I  "),
                List.of(new Tuple<>('H', MKWeaponsItems.Haft.get()))));

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
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(pIngredientItem), Ingredient.of(Items.NETHERITE_INGOT), pCategory, pResultItem)
                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(pFinishedRecipeConsumer, new ResourceLocation(MKWeapons.MODID, getItemName(pResultItem) + "_smithing"));
    }

    private ShapedRecipeBuilder getLongbowRecipe(MKBow bow) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, bow)
                .define('H', MKWeaponsItems.Haft.get())
                .define('I', bow.getMKTier().getMajorIngredient())
                .define('S', Tags.Items.STRING)
                .pattern(" IS")
                .pattern("H S")
                .pattern(" IS")
                .unlockedBy("has_haft", has(MKWeaponsItems.Haft.get()))
                .unlockedBy("has_string", has(Tags.Items.STRING))
                .unlockedBy("has_ingot", has(bow.getMKTier().getItemTag()))
                ;
    }

    private ShapedRecipeBuilder getRecipe(MKMeleeWeapon weapon) {
        ShapedRecipeBuilder recipeBuilder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, weapon);
        recipeBuilder.define('I', weapon.getMKTier().getMajorIngredient());
        WeaponRecipe weaponRecipe = weaponRecipes.get(weapon.getWeaponType());
        for (Tuple<Character, Item> key : weaponRecipe.itemKeys()) {
            recipeBuilder.define(key.getA(), key.getB());
        }
        for (String line : weaponRecipe.pattern()) {
            recipeBuilder.pattern(line);
        }
        if (weaponRecipe.hasHaft()) {
            recipeBuilder.unlockedBy("has_haft", has(MKWeaponsItems.Haft.get()));
        }
        if (weaponRecipe.hasStick()) {
            recipeBuilder.unlockedBy("has_stick", has(Items.STICK));
        }
        recipeBuilder.unlockedBy("has_ingot", has(weapon.getMKTier().getItemTag()));
        return recipeBuilder;
    }
}
