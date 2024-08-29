package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.armor.ArmorModifierEffect;
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

public class AccessoryModifierEffect extends ItemModifierEffect implements IAccessoryEffect {
    public static final ResourceLocation NAME = MKWeapons.id("accessory_effect.modifier");
    public static final MapCodec<AccessoryModifierEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            AttributeOptionEntry.CODEC.listOf().fieldOf("modifiers").forGetter(AccessoryModifierEffect::getModifiers)
    ).apply(builder, AccessoryModifierEffect::new));
    public static final Codec<AccessoryModifierEffect> CODEC = MAP_CODEC.codec();

    public static StreamCodec<RegistryFriendlyByteBuf, AccessoryModifierEffect> STREAM_CODEC = StreamCodec.composite(
            AttributeOptionEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), AccessoryModifierEffect::getModifiers,
            AccessoryModifierEffect::new
    );

    public AccessoryModifierEffect(List<AttributeOptionEntry> modifiers) {
        super(NAME, ChatFormatting.WHITE);
        this.modifiers.addAll(modifiers);
    }
}
