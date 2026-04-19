package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record ItemGrantedAbility(ResourceLocation abilityId) {
    public static final Codec<ItemGrantedAbility> CODEC = RecordCodecBuilder.<ItemGrantedAbility>mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("ability").forGetter(ItemGrantedAbility::abilityId)
    ).apply(builder, ItemGrantedAbility::new)).codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemGrantedAbility> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public ItemGrantedAbility {
        Objects.requireNonNull(abilityId, "abilityId");
    }

    public static void setAbility(ItemStack stack, Holder<MKAbility> abilityHolder) {
        setAbility(stack, abilityHolder.value().getAbilityId());
    }

    public static void setAbility(ItemStack stack, ResourceLocation abilityId) {
        stack.set(CoreItemComponents.ITEM_ABILITY, new ItemGrantedAbility(abilityId));
    }
}
