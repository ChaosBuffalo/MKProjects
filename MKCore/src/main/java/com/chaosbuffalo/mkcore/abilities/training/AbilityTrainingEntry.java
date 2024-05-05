package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbilityTrainingEntry {

    private final MKAbilityInfo abilityInfo;
    private final AbilitySource source;
    private final List<AbilityTrainingRequirement> requirementList;

    public AbilityTrainingEntry(MKAbilityInfo abilityInfo, AbilitySource source) {
        this.abilityInfo = abilityInfo;
        requirementList = new ArrayList<>();
        this.source = source;
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public List<AbilityTrainingRequirement> getRequirements() {
        return requirementList;
    }

    public AbilityTrainingEntry addRequirement(AbilityTrainingRequirement requirement) {
        requirementList.add(requirement);
        return this;
    }

    public boolean checkRequirements(MKPlayerData playerData) {
        return getRequirements().stream().allMatch(req -> req.check(playerData, abilityInfo));
    }

    public void onAbilityLearned(MKPlayerData playerData) {
        getRequirements().forEach(req -> req.onLearned(playerData, abilityInfo));
    }

    private AbilityRequirementEvaluation evaluateRequirement(AbilityTrainingRequirement req, MKPlayerData playerData) {
        return new AbilityRequirementEvaluation(req.describe(playerData), req.check(playerData, abilityInfo));
    }

    public AbilityTrainingEvaluation evaluate(MKPlayerData playerData) {
        List<AbilityRequirementEvaluation> requirements = getRequirements()
                .stream()
                .map(req -> evaluateRequirement(req, playerData))
                .collect(Collectors.toList());
        return new AbilityTrainingEvaluation(abilityInfo, requirements, source.usesAbilityPool());
    }
}
