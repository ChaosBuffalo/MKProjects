package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class MKWeaponModelProvider extends ItemModelProvider {

    public MKWeaponModelProvider(PackOutput generator, ExistingFileHelper existingFileHelper, String modId) {
        super(generator, modId, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (MKMeleeWeapon weapon : MKWeaponsItems.WEAPONS) {
            makeWeaponModel(weapon);
        }
        for (MKBow bow : MKWeaponsItems.BOWS) {
            makeBowModels(bow);
        }
        makeSimpleJewelry(MKWeaponsItems.CopperRing.get());
        makeSimpleJewelry(MKWeaponsItems.GoldEarring.get());
        makeSimpleJewelry(MKWeaponsItems.GoldRing.get());
        makeSimpleJewelry(MKWeaponsItems.RoseGoldRing.get());
        makeSimpleJewelry(MKWeaponsItems.SilverRing.get());
        makeSimpleJewelry(MKWeaponsItems.SilverEarring.get());
        makeSimpleJewelry(MKWeaponsItems.CopperEarring.get());
    }

    protected ResourceLocation getBaseLoc(String name) {
        return new ResourceLocation(MKWeapons.MODID, name);
    }

    protected ItemModelBuilder makeSimpleJewelry(Item item) {
        String path = ForgeRegistries.ITEMS.getKey(item).getPath();
        return singleTexture(path, new ResourceLocation(MKWeapons.MODID, "jewelry_base"), "layer0",
                modLoc(String.format("item/%s", path)));
    }

    protected void makeBowModels(MKBow bow) {
        String path = ForgeRegistries.ITEMS.getKey(bow).getPath();

        Map<String, Tuple<Integer, Double>> subModelKeys = new HashMap<>();
        subModelKeys.put("pulling_0", new Tuple<>(1, -1.0));
        subModelKeys.put("pulling_1", new Tuple<>(1, 0.65));
        subModelKeys.put("pulling_2", new Tuple<>(1, 0.9));

        ResourceLocation toolPath = modLoc(String.format("item/%s_tool", bow.getMKTier().getName()));
        for (String subModel : subModelKeys.keySet()) {
            String subPath = String.format("%s_%s", path, subModel);
            getBuilder(subPath)
                    .parent(getExistingFile(getBaseLoc(String.format("item/longbow_base_%s", subModel))))
                    .texture("0", toolPath)
                    .texture("particle", toolPath);
        }
        ItemModelBuilder builder = getBuilder(path)
                .parent(getExistingFile(getBaseLoc("item/longbow_base")))
                .texture("0", toolPath)
                .texture("particle", toolPath);

        for (String subModel : subModelKeys.keySet()) {
            Tuple<Integer, Double> predicates = subModelKeys.get(subModel);
            ItemModelBuilder.OverrideBuilder override = builder.override()
                    .model(getExistingFile(modLoc(String.format("item/longbow_%s_%s",
                            bow.getMKTier().getName(), subModel))))
                    .predicate(new ResourceLocation("pulling"), predicates.getA());
            if (predicates.getB() > 0) {
                override.predicate(new ResourceLocation("pull"), predicates.getB().floatValue());
            }
        }
    }

    protected void makeWeaponModel(MKMeleeWeapon weapon) {
        String path = ForgeRegistries.ITEMS.getKey(weapon).getPath();

        Map<String, Tuple<Integer, Double>> subModelKeys = new HashMap<>();
        subModelKeys.put("blocking", new Tuple<>(1, -1.0));

        ResourceLocation toolPath = modLoc(String.format("item/%s_tool", weapon.getMKTier().getName()));
        if (weapon.getWeaponType().canBlock()) {
            for (String subModel : subModelKeys.keySet()) {
                String subPath = String.format("%s_%s", path, subModel);
                getBuilder(subPath)
                        .parent(getExistingFile(getBaseLoc(String.format("item/%s_base_%s", weapon.getWeaponType().getName().getPath(), subModel))))
                        .texture("0", toolPath)
                        .texture("particle", toolPath);
            }
        }

        ItemModelBuilder builder = getBuilder(path)
                .parent(getExistingFile(getBaseLoc(String.format("item/%s_base", weapon.getWeaponType().getName().getPath()))))
                .texture("0", toolPath)
                .texture("particle", toolPath);

        if (weapon.getWeaponType().canBlock()) {
            for (String subModel : subModelKeys.keySet()) {
                Tuple<Integer, Double> predicates = subModelKeys.get(subModel);
                builder.override()
                        .model(getExistingFile(modLoc(String.format("item/%s_%s", path, subModel))))
                        .predicate(new ResourceLocation(subModel), predicates.getA());
            }
        }
    }
}
