package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ItemGrantedAbility(Holder<MKAbility> ability) {
    public static final Codec<ItemGrantedAbility> CODEC = RecordCodecBuilder.<ItemGrantedAbility>mapCodec(builder -> builder.group(
            MKCoreRegistry.ABILITIES.holderByNameCodec().fieldOf("ability").forGetter(ItemGrantedAbility::ability)
    ).apply(builder, ItemGrantedAbility::new)).codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemGrantedAbility> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static void setAbility(ItemStack stack, Holder<MKAbility> abilityHolder) {
        stack.set(CoreItemComponents.ITEM_ABILITY, new ItemGrantedAbility(abilityHolder));
    }
}
