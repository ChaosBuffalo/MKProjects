package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public record VictimDamageTriggerContext(LivingDamageEvent event, DamageSource source,
                                         IMKEntityData victimData) {

    public LivingEntity victim() {
        return victimData.getEntity();
    }
}
