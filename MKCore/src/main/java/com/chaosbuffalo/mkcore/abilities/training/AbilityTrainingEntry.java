package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbilityTrainingEntry {

    private final MKAbility ability;
    private final AbilitySource source;
    private final List<AbilityTrainingRequirement> requirementList;

    public AbilityTrainingEntry(MKAbility ability, AbilitySource source) {
        this.ability = ability;
        requirementList = new ArrayList<>();
        this.source = source;
    }

    public MKAbility getAbility() {
        return ability;
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
        return new AbilityRequirementEvaluation(req.describe(playerData), req.check(playerData, getAbility()));
    }

    public AbilityTrainingEvaluation evaluate(MKPlayerData playerData) {
        List<AbilityRequirementEvaluation> requirements = getRequirements()
                .stream()
                .map(req -> evaluateRequirement(req, playerData))
                .collect(Collectors.toList());
        return new AbilityTrainingEvaluation(getAbility(), requirements, source.usesAbilityPool());
    }
}
