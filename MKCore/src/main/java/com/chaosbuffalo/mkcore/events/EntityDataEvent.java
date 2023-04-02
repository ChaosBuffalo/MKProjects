package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraftforge.event.entity.living.LivingEvent;

public class EntityDataEvent extends LivingEvent {
    private final IMKEntityData entityData;

    public EntityDataEvent(IMKEntityData entityData) {
        super(entityData.getEntity());
        this.entityData = entityData;
    }

    public IMKEntityData getEntityData() {
        return entityData;
    }
}
