package com.chaosbuffalo.mkweapons.components;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record WeaponAbilityComponent(Holder<MKAbility> abilityHolder) {

    public static final Codec<WeaponAbilityComponent> CODEC = MKCoreRegistry.ABILITIES.holderByNameCodec()
            .xmap(WeaponAbilityComponent::new, WeaponAbilityComponent::abilityHolder);

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponAbilityComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static void setAbility(ItemStack stack, Holder<MKAbility> abilityHolder) {
        stack.set(WeaponsComponents.WEAPON_ABILITY, new WeaponAbilityComponent(abilityHolder));
    }

}
