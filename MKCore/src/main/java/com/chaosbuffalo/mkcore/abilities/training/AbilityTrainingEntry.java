package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class AbilityTrainingEntry {
    public static final Codec<AbilityTrainingEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            MKCoreRegistry.ABILITIES.byNameCodec().fieldOf("ability").forGetter(AbilityTrainingEntry::getAbility),
            AbilityTrainingRequirement.CODEC.listOf().fieldOf("requirements").forGetter(AbilityTrainingEntry::getRequirements),
            Codec.BOOL.fieldOf("usesAbilityPool").forGetter(i -> i.usesAbilityPool)
    ).apply(builder, AbilityTrainingEntry::new));

    private final MKAbility ability;
    private final List<AbilityTrainingRequirement> requirementList;
    private final boolean usesAbilityPool;

    public AbilityTrainingEntry(MKAbility ability, List<AbilityTrainingRequirement> requirements, boolean usesAbilityPool) {
        this.ability = ability;
        requirementList = List.copyOf(requirements);
        this.usesAbilityPool = usesAbilityPool;
    }

    public MKAbility getAbility() {
        return ability;
    }

    public List<AbilityTrainingRequirement> getRequirements() {
        return requirementList;
    }

    public boolean is(ResourceLocation abilityId) {
        return ability.getAbilityId().equals(abilityId);
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
        return new AbilityTrainingEvaluation(getAbility(), requirements, usesAbilityPool);
    }
}
