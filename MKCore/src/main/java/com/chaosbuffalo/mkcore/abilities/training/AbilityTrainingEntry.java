package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbilityTrainingEntry {

    private final MKAbilityInfo ability;
    private final AbilitySource source;
    private final List<AbilityTrainingRequirement> requirementList;

    public AbilityTrainingEntry(MKAbilityInfo ability, AbilitySource source) {
        this.ability = ability;
        requirementList = new ArrayList<>();
        this.source = source;
    }

    public MKAbility getAbility() {
        return ability.getAbility();
    }

    public List<AbilityTrainingRequirement> getRequirements() {
        return requirementList;
    }

    public AbilityTrainingEntry addRequirement(AbilityTrainingRequirement requirement) {
        requirementList.add(requirement);
        return this;
    }

    public boolean checkRequirements(MKPlayerData playerData) {
        return getRequirements().stream().allMatch(req -> req.check(playerData, ability));
    }

    public void onAbilityLearned(MKPlayerData playerData) {
        getRequirements().forEach(req -> req.onLearned(playerData, ability));
    }

    private AbilityRequirementEvaluation evaluateRequirement(AbilityTrainingRequirement req, MKPlayerData playerData) {
        return new AbilityRequirementEvaluation(req.describe(playerData), req.check(playerData, ability));
    }

    public AbilityTrainingEvaluation evaluate(MKPlayerData playerData) {
        List<AbilityRequirementEvaluation> requirements = getRequirements()
                .stream()
                .map(req -> evaluateRequirement(req, playerData))
                .collect(Collectors.toList());
        return new AbilityTrainingEvaluation(ability, requirements, source.usesAbilityPool());
    }
}
