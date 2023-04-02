package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkweapons.items.effects.BaseItemEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

public class BaseAccessoryEffect extends BaseItemEffect implements IAccessoryEffect {

    public BaseAccessoryEffect(ResourceLocation name, ChatFormatting color) {
        super(name, color);
    }
}
