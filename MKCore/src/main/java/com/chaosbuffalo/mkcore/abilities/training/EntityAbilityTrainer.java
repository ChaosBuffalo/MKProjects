package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EntityAbilityTrainer implements IAbilityTrainer {

    private final List<AbilityTrainingEntry> entries;
    private final Entity hostEntity;

    public EntityAbilityTrainer(Entity entity) {
        entries = new ArrayList<>();
        hostEntity = entity;
    }

    @Override
    public int getEntityId() {
        return hostEntity.getId();
    }

    @Override
    public List<AbilityTrainingEntry> getTrainableAbilities(IMKEntityData entityData) {
        return entries;
    }

    @Override
    public AbilityTrainingEntry getTrainingEntry(ResourceLocation abilityId) {
        return entries.stream().filter(entry -> entry.getAbilityInfo().getId().equals(abilityId)).findFirst().orElse(null);
    }

    @Override
    public AbilityTrainingEntry addTrainedAbility(MKAbilityInfo abilityInfo) {
        AbilityTrainingEntry entry = new AbilityTrainingEntry(abilityInfo, AbilitySource.TRAINED);
        entries.add(entry);
        return entry;
    }
}
