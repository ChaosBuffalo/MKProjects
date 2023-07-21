package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkweapons.items.effects.BaseItemEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseAccessoryEffect extends BaseItemEffect implements IAccessoryEffect {


    public BaseAccessoryEffect(ResourceLocation name, ChatFormatting color) {
        super(name, color);
    }


}
