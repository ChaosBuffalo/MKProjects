package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MKWeaponModelProvider extends ItemModelProvider {

    public MKWeaponModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MKWeapons.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (MKMeleeWeapon weapon : MKWeaponsItems.WEAPONS){
            makeWeaponModel(weapon);
        }
        for (MKBow bow : MKWeaponsItems.BOWS){
            makeBowModels(bow);
        }
        makeSimpleItem(MKWeaponsItems.CopperRing);
        makeSimpleItem(MKWeaponsItems.GoldEarring);
        makeSimpleItem(MKWeaponsItems.GoldRing);
        makeSimpleItem(MKWeaponsItems.RoseGoldRing);
        makeSimpleItem(MKWeaponsItems.SilverRing);
        makeSimpleItem(MKWeaponsItems.SilverEarring);
    }

    private void makeSimpleItem(Item item) {
        String path = ForgeRegistries.ITEMS.getKey(item).getPath();
        singleTexture(path, new ResourceLocation(MKWeapons.MODID, "jewelry_base"), "layer0",
                modLoc(String.format("items/%s", path)));
    }

    private void makeBowModels(MKBow bow) {
        String path = ForgeRegistries.ITEMS.getKey(bow).getPath();
        List<String> subModels = Arrays.asList("pulling_0", "pulling_1", "pulling_2");
        for (String subModel : subModels) {
            String subPath = String.format("%s_%s", path, subModel);
            getBuilder(subPath)
                    .parent(getExistingFile(modLoc(String.format("item/longbow_base_%s", subModel))))
                    .texture("0", modLoc(String.format("items/%s_tool", bow.getMKTier().getName())))
                    .texture("particle", modLoc(String.format("items/%s_tool", bow.getMKTier().getName())));
        }
        ItemModelBuilder builder = getBuilder(path)
                .parent(getExistingFile(modLoc("item/longbow_base")))
                .texture("0", modLoc(String.format("items/%s_tool", bow.getMKTier().getName())))
                .texture("particle", modLoc(String.format("items/%s_tool", bow.getMKTier().getName())));
        int index = 0;
        Map<String, Tuple<Integer, Double>> subModelKeys = new HashMap<>();
        subModelKeys.put("pulling_0", new Tuple<>(1, -1.0));
        subModelKeys.put("pulling_1", new Tuple<>(1, 0.65));
        subModelKeys.put("pulling_2", new Tuple<>(1, 0.9));
        for (String subModel : subModels) {
            Tuple<Integer, Double> predicates = subModelKeys.get(subModel);
            ItemModelBuilder.OverrideBuilder override = builder.override().model(getExistingFile(modLoc(String.format("item/longbow_%s_%s",
                            bow.getMKTier().getName(), subModel))))
                    .predicate(new ResourceLocation("pulling"), predicates.getA());
            if (predicates.getB() > 0) {
                override.predicate(new ResourceLocation("pull"), predicates.getB().floatValue());
            }
            index++;
        }
    }

    private void makeWeaponModel(MKMeleeWeapon weapon) {
        String path = ForgeRegistries.ITEMS.getKey(weapon).getPath();
        List<String> subModels = Arrays.asList("blocking");
        if (MeleeWeaponTypes.WITH_BLOCKING.contains(weapon.getWeaponType())){

            for (String subModel : subModels){
                String subPath = String.format("%s_%s", path, subModel);
                getBuilder(subPath)
                        .parent(getExistingFile(modLoc(String.format("item/%s_base_%s", weapon.getWeaponType().getName().getPath(), subModel))))
                        .texture("0", modLoc(String.format("items/%s_tool", weapon.getMKTier().getName())))
                        .texture("particle", modLoc(String.format("items/%s_tool", weapon.getMKTier().getName())));
            }
        }


        ItemModelBuilder builder = getBuilder(path)
                .parent(getExistingFile(modLoc(String.format("item/%s_base", weapon.getWeaponType().getName().getPath()))))
                .texture("0", modLoc(String.format("items/%s_tool", weapon.getMKTier().getName())))
                .texture("particle", modLoc(String.format("items/%s_tool", weapon.getMKTier().getName())));

        Map<String, Tuple<Integer, Double>> subModelKeys = new HashMap<>();
        subModelKeys.put("blocking", new Tuple<>(1, -1.0));

        if (MeleeWeaponTypes.WITH_BLOCKING.contains(weapon.getWeaponType())){
            for (String subModel : subModels){
                Tuple<Integer, Double> predicates = subModelKeys.get(subModel);
                ItemModelBuilder.OverrideBuilder override = builder.override().model(getExistingFile(modLoc(String.format("item/%s_%s",
                        path, subModel))))
                        .predicate(new ResourceLocation(subModel), predicates.getA());
            }
        }

    }

}
