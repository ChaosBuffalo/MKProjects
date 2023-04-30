package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKEntityData;

public class EntityAbilityEvent extends EntityDataEvent {
    private final MKAbilityInfo ability;

    public EntityAbilityEvent(MKAbilityInfo ability, IMKEntityData entityData) {
        super(entityData);
        this.ability = ability;
    }

    public MKAbilityInfo getAbilityInfo() {
        return ability;
    }

    public static class EntityCompleteAbilityEvent extends EntityAbilityEvent {

        public EntityCompleteAbilityEvent(MKAbilityInfo ability, IMKEntityData entityData) {
            super(ability, entityData);
        }
    }
}
