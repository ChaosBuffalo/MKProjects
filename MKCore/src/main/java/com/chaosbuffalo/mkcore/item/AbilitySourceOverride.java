package com.chaosbuffalo.mkcore.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record AbilitySourceOverride(UUID source) {
    public static final Codec<AbilitySourceOverride> CODEC = RecordCodecBuilder.<AbilitySourceOverride>mapCodec(builder -> builder.group(
            UUIDUtil.STRING_CODEC.fieldOf("source").forGetter(AbilitySourceOverride::source)
    ).apply(builder, AbilitySourceOverride::new)).codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilitySourceOverride> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static void setSource(ItemStack stack, UUID source) {
        stack.set(CoreItemComponents.ABILITY_SOURCE, new AbilitySourceOverride(source));
    }

    public static UUID getSource(ItemStack stack) {
        AbilitySourceOverride override = stack.get(CoreItemComponents.ABILITY_SOURCE);
        if (override != null) {
            return override.source();
        }
        UUID newUUID = UUID.randomUUID();
        setSource(stack, newUUID);
        return newUUID;
    }
}