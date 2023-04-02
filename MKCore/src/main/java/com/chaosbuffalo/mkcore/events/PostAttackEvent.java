package com.chaosbuffalo.mkcore.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class PostAttackEvent extends LivingEvent {

    public PostAttackEvent(LivingEntity entity) {
        super(entity);
    }
}
