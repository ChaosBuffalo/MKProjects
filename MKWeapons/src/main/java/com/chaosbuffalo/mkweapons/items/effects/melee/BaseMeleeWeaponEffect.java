package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkweapons.items.effects.BaseItemEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

public abstract class BaseMeleeWeaponEffect extends BaseItemEffect implements IMeleeWeaponEffect {

    public BaseMeleeWeaponEffect(ResourceLocation name, ChatFormatting color) {
        super(name, color);
    }
}
