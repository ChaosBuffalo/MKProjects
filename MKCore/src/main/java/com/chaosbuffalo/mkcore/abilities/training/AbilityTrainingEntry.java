package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class AbilityTrainingEntry {
    public static final Codec<AbilityTrainingEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("ability").forGetter(AbilityTrainingEntry::getAbilityId),
            AbilityTrainingRequirement.CODEC.listOf().fieldOf("requirements").forGetter(AbilityTrainingEntry::getRequirements),
            Codec.BOOL.fieldOf("usesAbilityPool").forGetter(i -> i.usesAbilityPool)
    ).apply(builder, AbilityTrainingEntry::new));

    private final ResourceLocation abilityId;
    private final List<AbilityTrainingRequirement> requirementList;
    private final boolean usesAbilityPool;

    public AbilityTrainingEntry(ResourceLocation abilityId, List<AbilityTrainingRequirement> requirements, boolean usesAbilityPool) {
        this.abilityId = abilityId;
        requirementList = List.copyOf(requirements);
        this.usesAbilityPool = usesAbilityPool;
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    public List<AbilityTrainingRequirement> getRequirements() {
        return requirementList;
    }

    public boolean is(ResourceLocation abilityId) {
        return this.abilityId.equals(abilityId);
    }

    public boolean checkRequirements(MKPlayerData playerData) {
        return getRequirements().stream().allMatch(req -> req.check(playerData, abilityId));
    }

    public void onAbilityLearned(MKPlayerData playerData) {
        getRequirements().forEach(req -> req.onLearned(playerData, abilityId));
    }

    private AbilityRequirementEvaluation evaluateRequirement(AbilityTrainingRequirement req, MKPlayerData playerData) {
        return new AbilityRequirementEvaluation(req.describe(playerData), req.check(playerData, abilityId));
    }

    public AbilityTrainingEvaluation evaluate(MKPlayerData playerData) {
        List<AbilityRequirementEvaluation> requirements = getRequirements()
                .stream()
                .map(req -> evaluateRequirement(req, playerData))
                .collect(Collectors.toList());
        return new AbilityTrainingEvaluation(getAbilityId(), requirements, usesAbilityPool);
    }

    public boolean learn(MKPlayerData playerData, AbilitySource source) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        boolean learned;
        if (ability != null) {
            learned = playerData.getAbilities().learnAbility(ability, source);
        } else if (MKCore.getAbilityDefinitionService().getDefinition(abilityId) != null) {
            learned = playerData.getAbilities().learnAbilityDefinition(abilityId, source);
        } else {
            MKCore.LOGGER.warn("Failed to learn unknown trained ability {}", abilityId);
            return false;
        }

        if (learned) {
            onAbilityLearned(playerData);
        }
        return learned;
    }
}
