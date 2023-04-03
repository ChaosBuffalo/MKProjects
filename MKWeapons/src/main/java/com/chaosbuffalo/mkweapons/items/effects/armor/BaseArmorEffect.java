package com.chaosbuffalo.mkweapons.items.effects.armor;

import com.chaosbuffalo.mkweapons.items.effects.BaseItemEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseArmorEffect extends BaseItemEffect implements IArmorEffect {
    public BaseArmorEffect(ResourceLocation name, ChatFormatting color) {
        super(name, color);
    }
}
