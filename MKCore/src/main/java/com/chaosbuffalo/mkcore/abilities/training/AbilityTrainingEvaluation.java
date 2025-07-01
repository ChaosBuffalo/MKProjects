package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record AbilityTrainingEvaluation(MKAbility ability,
                                        List<AbilityRequirementEvaluation> requirements,
                                        boolean usesAbilityPool) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityTrainingEvaluation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(MKCoreRegistry.ABILITY_REGISTRY_KEY), AbilityTrainingEvaluation::ability,
            AbilityRequirementEvaluation.STREAM_CODEC.apply(ByteBufCodecs.list()), AbilityTrainingEvaluation::requirements,
            ByteBufCodecs.BOOL, AbilityTrainingEvaluation::usesAbilityPool,
            AbilityTrainingEvaluation::new);

    public boolean canLearn() {
        return requirements.stream().allMatch(AbilityRequirementEvaluation::isMet);
    }
}
