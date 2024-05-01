package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RangedModifierEffect extends ItemModifierEffect implements IRangedWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.ranged_modifier");
    public static final Codec<RangedModifierEffect> CODEC =
            AttributeOptionEntry.CODEC.listOf().xmap(RangedModifierEffect::new, RangedModifierEffect::getModifiers);

    public RangedModifierEffect(List<AttributeOptionEntry> modifiers) {
        super(NAME, ChatFormatting.WHITE);
        this.modifiers.addAll(modifiers);
    }

    public RangedModifierEffect() {
        super(NAME, ChatFormatting.WHITE);
    }
}
