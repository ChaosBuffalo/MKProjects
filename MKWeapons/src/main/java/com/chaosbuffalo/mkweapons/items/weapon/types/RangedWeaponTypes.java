package com.chaosbuffalo.mkweapons.items.weapon.types;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkweapons.MKWeapons;

public class RangedWeaponTypes {


    public static final IRangedWeaponType LONGBOW = RangedWeaponType.builder(MKWeapons.id("longbow"))
            .drawTime(GameConstants.TICKS_PER_SECOND * 2.5f)
            .launchVel(4.0f)
            .baseDamage(5.0f)
            .build();
}
