package com.chaosbuffalo.mkcore.core.healing;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class MKAbilityHealEvent extends LivingEvent implements ICancellableEvent {
    private final MKHealSource healSource;
    private float amount;

    public MKAbilityHealEvent(LivingEntity entity, float amount, MKHealSource healSource) {
        super(entity);
        this.healSource = healSource;
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public MKHealSource getHealSource() {
        return healSource;
    }
}
