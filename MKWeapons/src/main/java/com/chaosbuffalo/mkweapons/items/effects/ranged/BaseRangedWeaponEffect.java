package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkweapons.items.effects.BaseItemEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

public abstract class BaseRangedWeaponEffect extends BaseItemEffect implements IRangedWeaponEffect {
    public BaseRangedWeaponEffect(ResourceLocation name, ChatFormatting color) {
        super(name, color);
    }
}
