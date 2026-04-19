package com.chaosbuffalo.mkcore.abilities.training;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record AbilityTrainingEvaluation(ResourceLocation abilityId,
                                        List<AbilityRequirementEvaluation> requirements,
                                        boolean usesAbilityPool) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityTrainingEvaluation> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AbilityTrainingEvaluation::abilityId,
            AbilityRequirementEvaluation.STREAM_CODEC.apply(ByteBufCodecs.list()), AbilityTrainingEvaluation::requirements,
            ByteBufCodecs.BOOL, AbilityTrainingEvaluation::usesAbilityPool,
            AbilityTrainingEvaluation::new);

    public boolean canLearn() {
        return requirements.stream().allMatch(AbilityRequirementEvaluation::isMet);
    }
}
