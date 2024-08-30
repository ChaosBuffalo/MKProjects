package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RangedModifierEffect extends ItemModifierEffect implements IRangedWeaponEffect {
    public static final ResourceLocation NAME = MKWeapons.id("weapon_effect.ranged_modifier");

    public static final MapCodec<RangedModifierEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            AttributeOptionEntry.CODEC.listOf().fieldOf("modifiers").forGetter(RangedModifierEffect::getModifiers)
    ).apply(builder, RangedModifierEffect::new));
    public static final Codec<RangedModifierEffect> CODEC = MAP_CODEC.codec();

    public RangedModifierEffect(List<AttributeOptionEntry> modifiers) {
        super(NAME, ChatFormatting.WHITE);
        this.modifiers.addAll(modifiers);
    }

    public RangedModifierEffect() {
        super(NAME, ChatFormatting.WHITE);
    }
}
