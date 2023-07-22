package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class PostAttackEvent extends EntityDataEvent {

    public PostAttackEvent(IMKEntityData entityData) {
        super(entityData);
    }
}
