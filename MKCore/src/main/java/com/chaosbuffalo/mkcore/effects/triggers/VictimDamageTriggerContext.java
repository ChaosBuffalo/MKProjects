package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public record VictimDamageTriggerContext(LivingDamageEvent.Pre event, DamageSource source,
                                         IMKEntityData victimData) {
}
