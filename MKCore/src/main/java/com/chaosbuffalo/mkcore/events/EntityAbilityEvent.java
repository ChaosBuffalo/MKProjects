package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKEntityData;

public class EntityAbilityEvent extends EntityDataEvent {
    private final MKAbilityInfo abilityInfo;

    public EntityAbilityEvent(MKAbilityInfo abilityInfo, IMKEntityData entityData) {
        super(entityData);
        this.abilityInfo = abilityInfo;
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public MKAbility getAbility() {
        return abilityInfo.getAbility();
    }

    public static class EntityCompleteAbilityEvent extends EntityAbilityEvent {

        public EntityCompleteAbilityEvent(MKAbilityInfo abilityInfo, IMKEntityData entityData) {
            super(abilityInfo, entityData);
        }
    }
}
