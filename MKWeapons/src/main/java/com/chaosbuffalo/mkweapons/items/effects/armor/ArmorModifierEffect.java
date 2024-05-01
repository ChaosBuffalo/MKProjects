package com.chaosbuffalo.mkweapons.items.effects.armor;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ArmorModifierEffect extends ItemModifierEffect implements IArmorEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.armor_modifier");
    public static final Codec<ArmorModifierEffect> CODEC =
            AttributeOptionEntry.CODEC.listOf().xmap(ArmorModifierEffect::new, ArmorModifierEffect::getModifiers);

    public ArmorModifierEffect(List<AttributeOptionEntry> modifiers) {
        super(NAME, ChatFormatting.WHITE);
        this.modifiers.addAll(modifiers);
    }
}
