package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

import java.util.List;

public class MeleeModifierEffect extends ItemModifierEffect implements IMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.melee_modifier");

    public MeleeModifierEffect(List<AttributeOptionEntry> modifiers) {
        this();
        this.modifiers.addAll(modifiers);
    }

    public MeleeModifierEffect(){
        super(NAME, ChatFormatting.WHITE);
    }
}
