package com.chaosbuffalo.mkultra.client;

import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkweapons.client.MKWeaponsItemProperties;

public class MKUItemProperties {
    public static void registerItemProperties() {
        MKWeaponsItemProperties.registerDefaultRangedWeaponItemProperties(MKUItems.BOWS);
        MKWeaponsItemProperties.registerDefaultMeleeWeaponItemProperties(MKUItems.WEAPONS);
    }
}
