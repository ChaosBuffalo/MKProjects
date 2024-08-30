package com.chaosbuffalo.mkweapons.components;

import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record MeleeEffectsComponent(List<IMeleeWeaponEffect> effects) {
    public static final MeleeEffectsComponent EMPTY = new MeleeEffectsComponent(List.of());

    public static final Codec<MeleeEffectsComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    IMeleeWeaponEffect.DISPATCH_CODEC.listOf().fieldOf("effects").forGetter(MeleeEffectsComponent::effects)
            ).apply(builder, MeleeEffectsComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MeleeEffectsComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);


    public MeleeEffectsComponent withAddedEffect(IMeleeWeaponEffect newEffect) {
        ImmutableList.Builder<IMeleeWeaponEffect> builder = ImmutableList.builderWithExpectedSize(this.effects.size() + 1);

        for (IMeleeWeaponEffect oldEffect : this.effects) {
            builder.add(oldEffect);
        }

        builder.add(newEffect);
        return new MeleeEffectsComponent(builder.build());
    }


    public static void addEffect(ItemStack itemStack, IMeleeWeaponEffect effect) {
        itemStack.update(WeaponsComponents.MELEE_EFFECTS, MeleeEffectsComponent.EMPTY, existing -> existing.withAddedEffect(effect));
    }
}
