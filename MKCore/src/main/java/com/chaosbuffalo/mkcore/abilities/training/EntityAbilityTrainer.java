package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
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
    public AbilityTrainingEntry getTrainingEntry(MKAbility ability) {
        return entries.stream().filter(entry -> entry.getAbility() == ability).findFirst().orElse(null);
    }

    @Override
    public AbilityTrainingEntry addTrainedAbility(MKAbility ability) {
        AbilityTrainingEntry entry = new AbilityTrainingEntry(ability, AbilitySource.TRAINED);
        entries.add(entry);
        return entry;
    }
}
