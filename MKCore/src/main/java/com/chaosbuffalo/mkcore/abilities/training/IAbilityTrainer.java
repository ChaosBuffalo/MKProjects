package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IAbilityTrainer {

    int getEntityId();

    List<AbilityTrainingEntry> getTrainableAbilities(IMKEntityData entityData);

    AbilityTrainingEntry getTrainingEntry(ResourceLocation abilityId);

    AbilityTrainingEntry addTrainedAbility(MKAbilityInfo ability);
}
