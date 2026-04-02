package com.chaosbuffalo.mkweapons.items.weapon.types;

import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import net.minecraft.resources.ResourceLocation;

public interface IRangedWeaponType {
    ResourceLocation getName();

    default String getTypeName() {
        return getName().getPath();
    }

    float getBaseDrawTime();

    float getBaseLaunchVelocity();

    float getBaseDamage();

    default int getDurabilityMultiplier() {
        return 3;
    }
}
