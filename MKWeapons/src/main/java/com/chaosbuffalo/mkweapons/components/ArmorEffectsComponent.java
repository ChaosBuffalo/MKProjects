package com.chaosbuffalo.mkweapons.components;

import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ArmorEffectsComponent(List<IArmorEffect> effects) {
    public static final ArmorEffectsComponent EMPTY = new ArmorEffectsComponent(List.of());

    public static final Codec<ArmorEffectsComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IArmorEffect.DISPATCH_CODEC.listOf().fieldOf("effects").forGetter(ArmorEffectsComponent::effects)
    ).apply(builder, ArmorEffectsComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorEffectsComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);


    public ArmorEffectsComponent withAddedEffect(IArmorEffect newEffect) {
        ImmutableList.Builder<IArmorEffect> builder = ImmutableList.builderWithExpectedSize(this.effects.size() + 1);

        for (IArmorEffect oldEffect : this.effects) {
            builder.add(oldEffect);
        }

        builder.add(newEffect);
        return new ArmorEffectsComponent(builder.build());
    }

    public static void addEffect(ItemStack itemStack, IArmorEffect effect) {
        itemStack.update(WeaponsComponents.ARMOR_EFFECTS, ArmorEffectsComponent.EMPTY, existing -> existing.withAddedEffect(effect));
    }
}
