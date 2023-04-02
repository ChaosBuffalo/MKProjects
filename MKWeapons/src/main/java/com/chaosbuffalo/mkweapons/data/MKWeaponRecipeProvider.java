package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.Tags;

import java.util.*;
import java.util.function.Consumer;

public class MKWeaponRecipeProvider extends RecipeProvider {

    public static class WeaponRecipe {
        private final List<String> pattern;
        private final List<Tuple<Character, Item>> itemKeys;

        public WeaponRecipe(List<String> pattern, List<Tuple<Character, Item>> itemKeys){
            this.pattern = pattern;
            this.itemKeys = itemKeys;
        }

        public List<String> getPattern() {
            return pattern;
        }

        public boolean hasHaft(){
            for (Tuple<Character, Item> tuple : itemKeys){
                if (tuple.getA().equals('H')){
                    return true;
                }
            }
            return false;
        }

        public boolean hasStick(){
            for (Tuple<Character, Item> tuple : itemKeys){
                if (tuple.getA().equals('S')){
                    return true;
                }
            }
            return false;
        }

        public List<Tuple<Character, Item>> getItemKeys() {
            return itemKeys;
        }
    }

    public static final Map<IMeleeWeaponType, WeaponRecipe> weaponRecipes = new HashMap<>();
    static {
        weaponRecipes.put(MeleeWeaponTypes.DAGGER_TYPE, new WeaponRecipe(
                Arrays.asList("I", "S"),
                Arrays.asList(new Tuple<>('S', Items.STICK))));
        weaponRecipes.put(MeleeWeaponTypes.BATTLEAXE_TYPE, new WeaponRecipe(
                Arrays.asList("III", "IHI", " H "),
                Arrays.asList(new Tuple<>('H', MKWeaponsItems.Haft))));
        weaponRecipes.put(MeleeWeaponTypes.GREATSWORD_TYPE, new WeaponRecipe(
                Arrays.asList(" I ", " I ", "IHI"),
                Arrays.asList(new Tuple<>('H', MKWeaponsItems.Haft))));
        weaponRecipes.put(MeleeWeaponTypes.KATANA_TYPE, new WeaponRecipe(
                Arrays.asList("  I", " I ", "H  "),
                Arrays.asList(new Tuple<>('H', MKWeaponsItems.Haft))));
        weaponRecipes.put(MeleeWeaponTypes.LONGSWORD_TYPE, new WeaponRecipe(
                Arrays.asList(" I ", " I ", " H "),
                Arrays.asList(new Tuple<>('H', MKWeaponsItems.Haft))));
        weaponRecipes.put(MeleeWeaponTypes.SPEAR_TYPE, new WeaponRecipe(
                Arrays.asList("  I", " H ", "H  "),
                Arrays.asList(new Tuple<>('H', MKWeaponsItems.Haft))));
        weaponRecipes.put(MeleeWeaponTypes.WARHAMMER_TYPE, new WeaponRecipe(
                Arrays.asList(" II", " HI", "H  "),
                Arrays.asList(new Tuple<>('H', MKWeaponsItems.Haft))));
        weaponRecipes.put(MeleeWeaponTypes.MACE_TYPE, new WeaponRecipe(
                Arrays.asList(" I ", " H ", " H "),
                Arrays.asList(new Tuple<>('H', MKWeaponsItems.Haft))));
        weaponRecipes.put(MeleeWeaponTypes.STAFF_TYPE, new WeaponRecipe(
                Arrays.asList("  I", " H ", "I  "),
                Arrays.asList(new Tuple<>('H', MKWeaponsItems.Haft))));

    }



    public MKWeaponRecipeProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }


    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        getHaftRecipe().save(consumer);
        for (MKMeleeWeapon weapon : MKWeaponsItems.WEAPONS){
            getRecipe(weapon).save(consumer);
        }
        for (MKBow bow : MKWeaponsItems.BOWS){
            getLongbowRecipe(bow).save(consumer);
        }
    }

    private ShapedRecipeBuilder getHaftRecipe(){
        return ShapedRecipeBuilder.shaped(MKWeaponsItems.Haft, 3)
                .define('S', Items.STICK)
                .define('L', Tags.Items.LEATHER)
                .pattern("SSS")
                .pattern(" L ")
                .pattern("SSS")
                .unlockedBy("has_stick", this.has(Items.STICK))
                .unlockedBy("has_leather", this.has(Tags.Items.LEATHER));
    }

    private ShapedRecipeBuilder getLongbowRecipe(MKBow bow){
        return ShapedRecipeBuilder.shaped(bow)
                .define('H', MKWeaponsItems.Haft)
                .define('I', bow.getMKTier().getMajorIngredient())
                .define('S', Tags.Items.STRING)
                .pattern(" IS")
                .pattern("H S")
                .pattern(" IS")
                .unlockedBy("has_haft", this.has(MKWeaponsItems.Haft))
                .unlockedBy("has_string", this.has(Tags.Items.STRING))
                .unlockedBy("has_ingot", this.has(bow.getMKTier().getItemTag()))
                ;
    }

    private ShapedRecipeBuilder getRecipe(MKMeleeWeapon weapon) {
        ShapedRecipeBuilder recipeBuilder = ShapedRecipeBuilder.shaped(weapon);
        recipeBuilder.define('I', weapon.getMKTier().getMajorIngredient());;
        WeaponRecipe weaponRecipe = weaponRecipes.get(weapon.getWeaponType());
        for (Tuple<Character, Item> key : weaponRecipe.getItemKeys()) {
            recipeBuilder.define(key.getA(), key.getB());
        }
        for (String line : weaponRecipe.getPattern()){
            recipeBuilder.pattern(line);
        }
        if (weaponRecipe.hasHaft()){
            recipeBuilder.unlockedBy("has_haft", this.has(MKWeaponsItems.Haft));
        }
        if (weaponRecipe.hasStick()){
            recipeBuilder.unlockedBy("has_stick", this.has(Items.STICK));
        }
        recipeBuilder.unlockedBy("has_ingot", this.has(weapon.getMKTier().getItemTag()));
        return recipeBuilder;
    }
}
