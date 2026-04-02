package com.chaosbuffalo.mkweapons.data.content;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.data.providers.MKWeaponModelProvider;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MKWeaponsModelGenerator extends MKWeaponModelProvider {
    public MKWeaponsModelGenerator(PackOutput generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, MKWeapons.MODID);
    }

    @Override
    protected void registerModels() {
        for (MKMeleeWeapon weapon : MKWeaponsItems.getMeleeWeaponsFromMod(MKWeapons.MODID)) {
            makeWeaponModel(weapon);
        }
        for (MKBow bow : MKWeaponsItems.getRangedWeaponsFromMod(MKWeapons.MODID)) {
            makeBowModels(bow);
        }
        makeSimpleJewelry(MKWeaponsItems.CopperRing.get());
        makeSimpleJewelry(MKWeaponsItems.GoldEarring.get());
        makeSimpleJewelry(MKWeaponsItems.GoldRing.get());
        makeSimpleJewelry(MKWeaponsItems.RoseGoldRing.get());
        makeSimpleJewelry(MKWeaponsItems.SilverRing.get());
        makeSimpleJewelry(MKWeaponsItems.SilverEarring.get());
        makeSimpleJewelry(MKWeaponsItems.CopperEarring.get());
        basicItem(MKWeaponsItems.Haft.get());
    }
}
