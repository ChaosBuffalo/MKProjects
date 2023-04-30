package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AbilityTrainingEvaluation {

    private final MKAbilityInfo ability;
    private final boolean usesAbilityPool;
    private final List<AbilityRequirementEvaluation> requirements;

    public AbilityTrainingEvaluation(MKAbilityInfo ability, List<AbilityRequirementEvaluation> requirements, boolean usesAbilityPool) {
        this.ability = ability;
        this.requirements = requirements;
        this.usesAbilityPool = usesAbilityPool;
    }

    public MKAbilityInfo getAbilityInfo() {
        return ability;
    }

    public boolean usesAbilityPool() {
        return usesAbilityPool;
    }

    public List<AbilityRequirementEvaluation> getRequirements() {
        return requirements;
    }

    public boolean canLearn() {
        return requirements.stream().allMatch(AbilityRequirementEvaluation::isMet);
    }

    public void write(FriendlyByteBuf buffer) {
        getAbilityInfo().write(buffer);
        buffer.writeBoolean(usesAbilityPool());
        buffer.writeVarInt(requirements.size());
        requirements.forEach(description -> description.write(buffer));
    }

    @Nullable
    public static AbilityTrainingEvaluation read(FriendlyByteBuf buffer) {
        MKAbilityInfo abilityInfo = MKAbilityInfo.read(buffer);
        if (abilityInfo == null)
            return null;
        boolean usesPool = buffer.readBoolean();
        List<AbilityRequirementEvaluation> requirementEvaluations = new ArrayList<>();
        int descCount = buffer.readVarInt();
        for (int j = 0; j < descCount; j++) {
            AbilityRequirementEvaluation eval = AbilityRequirementEvaluation.read(buffer);
            requirementEvaluations.add(eval);
        }

        return new AbilityTrainingEvaluation(abilityInfo, requirementEvaluations, usesPool);
    }
}
