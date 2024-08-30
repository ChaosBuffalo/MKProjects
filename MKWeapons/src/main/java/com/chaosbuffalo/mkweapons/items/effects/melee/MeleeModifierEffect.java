package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MeleeModifierEffect extends ItemModifierEffect implements IMeleeWeaponEffect {
    public static final ResourceLocation NAME = MKWeapons.id("weapon_effect.melee_modifier");
    public static final MapCodec<MeleeModifierEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            AttributeOptionEntry.CODEC.listOf().fieldOf("modifiers").forGetter(MeleeModifierEffect::getModifiers)
    ).apply(builder, MeleeModifierEffect::new));
    public static final Codec<MeleeModifierEffect> CODEC =
            AttributeOptionEntry.CODEC.listOf().xmap(MeleeModifierEffect::new, MeleeModifierEffect::getModifiers);

    public MeleeModifierEffect(List<AttributeOptionEntry> modifiers) {
        super(NAME, ChatFormatting.WHITE);
        this.modifiers.addAll(modifiers);
    }
}
