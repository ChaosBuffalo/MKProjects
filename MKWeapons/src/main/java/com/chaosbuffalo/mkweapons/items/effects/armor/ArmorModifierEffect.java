package com.chaosbuffalo.mkweapons.items.effects.armor;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ArmorModifierEffect extends ItemModifierEffect implements IArmorEffect {
    public static final ResourceLocation NAME = MKWeapons.id("weapon_effect.armor_modifier");
    public static final MapCodec<ArmorModifierEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            AttributeOptionEntry.CODEC.listOf().fieldOf("modifiers").forGetter(ArmorModifierEffect::getModifiers)
    ).apply(builder, ArmorModifierEffect::new));
    public static final Codec<ArmorModifierEffect> CODEC = MAP_CODEC.codec();

    public static StreamCodec<RegistryFriendlyByteBuf, ArmorModifierEffect> STREAM_CODEC = StreamCodec.composite(
            AttributeOptionEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), ArmorModifierEffect::getModifiers,
            ArmorModifierEffect::new
    );

    public ArmorModifierEffect(List<AttributeOptionEntry> modifiers) {
        super(NAME, ChatFormatting.WHITE);
        this.modifiers.addAll(modifiers);
    }
}
