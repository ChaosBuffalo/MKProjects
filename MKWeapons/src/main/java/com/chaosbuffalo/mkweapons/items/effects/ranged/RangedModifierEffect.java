package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

import java.util.List;

public class RangedModifierEffect extends ItemModifierEffect implements IRangedWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.ranged_modifier");

    public RangedModifierEffect(List<AttributeOptionEntry> modifiers) {
        this();
        this.modifiers.addAll(modifiers);
    }

    public RangedModifierEffect(){
        super(NAME, ChatFormatting.WHITE);
    }
}
