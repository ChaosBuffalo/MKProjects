package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;

public class EntityAbilityEvent extends EntityDataEvent {
    private final MKAbility ability;

    public EntityAbilityEvent(MKAbility ability, IMKEntityData entityData) {
        super(entityData);
        this.ability = ability;
    }

    public MKAbility getAbility() {
        return ability;
    }

    public static class EntityCompleteAbilityEvent extends EntityAbilityEvent {

        public EntityCompleteAbilityEvent(MKAbility ability, IMKEntityData entityData) {
            super(ability, entityData);
        }
    }
}
