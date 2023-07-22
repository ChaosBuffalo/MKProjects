package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.IMKEntityData;

public class PostAttackEvent extends EntityDataEvent {

    public PostAttackEvent(IMKEntityData entity) {
        super(entity);
    }
}
