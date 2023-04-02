package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

import java.util.List;

public class AccessoryModifierEffect extends ItemModifierEffect implements IAccessoryEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "accessory_effect.modifier");

    public AccessoryModifierEffect(List<AttributeOptionEntry> modifiers) {
        this();
        this.modifiers.addAll(modifiers);
    }

    public AccessoryModifierEffect(){
        super(NAME, ChatFormatting.WHITE);
    }
}
