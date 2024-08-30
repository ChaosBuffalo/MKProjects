package com.chaosbuffalo.mkweapons.components;

import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record RangedEffectsComponent(List<IRangedWeaponEffect> effects) {
    public static final RangedEffectsComponent EMPTY = new RangedEffectsComponent(List.of());

    public static final Codec<RangedEffectsComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IRangedWeaponEffect.DISPATCH_CODEC.listOf().fieldOf("effects").forGetter(RangedEffectsComponent::effects)
    ).apply(builder, RangedEffectsComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RangedEffectsComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);


    public RangedEffectsComponent withAddedEffect(IRangedWeaponEffect newEffect) {
        ImmutableList.Builder<IRangedWeaponEffect> builder = ImmutableList.builderWithExpectedSize(this.effects.size() + 1);

        for (IRangedWeaponEffect oldEffect : this.effects) {
            builder.add(oldEffect);
        }

        builder.add(newEffect);
        return new RangedEffectsComponent(builder.build());
    }

    public static void addEffect(ItemStack itemStack, IRangedWeaponEffect effect) {
        itemStack.update(WeaponsComponents.RANGED_EFFECTS, RangedEffectsComponent.EMPTY, existing -> existing.withAddedEffect(effect));
    }
}
