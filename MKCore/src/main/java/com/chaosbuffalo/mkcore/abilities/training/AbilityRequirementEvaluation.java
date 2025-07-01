package com.chaosbuffalo.mkcore.abilities.training;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public record AbilityRequirementEvaluation(Component description, boolean isMet) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityRequirementEvaluation> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, AbilityRequirementEvaluation::description,
            ByteBufCodecs.BOOL, AbilityRequirementEvaluation::isMet,
            AbilityRequirementEvaluation::new);
}
