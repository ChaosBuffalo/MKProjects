package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AbilityTrainingEvaluation {

    private final MKAbility ability;
    private final boolean usesAbilityPool;
    private final List<AbilityRequirementEvaluation> requirements;

    public AbilityTrainingEvaluation(MKAbility ability, List<AbilityRequirementEvaluation> requirements, boolean usesAbilityPool) {
        this.ability = ability;
        this.requirements = requirements;
        this.usesAbilityPool = usesAbilityPool;
    }

    public MKAbility getAbility() {
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
        buffer.writeResourceLocation(getAbility().getAbilityId());
        buffer.writeBoolean(usesAbilityPool());
        buffer.writeVarInt(requirements.size());
        requirements.forEach(description -> description.write(buffer));
    }

    @Nullable
    public static AbilityTrainingEvaluation read(FriendlyByteBuf buffer) {
        ResourceLocation abilityId = buffer.readResourceLocation();
        boolean usesPool = buffer.readBoolean();
        List<AbilityRequirementEvaluation> requirementEvaluations = new ArrayList<>();
        int descCount = buffer.readVarInt();
        for (int j = 0; j < descCount; j++) {
            AbilityRequirementEvaluation eval = AbilityRequirementEvaluation.read(buffer);
            requirementEvaluations.add(eval);
        }

        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;
        return new AbilityTrainingEvaluation(ability, requirementEvaluations, usesPool);
    }
}
