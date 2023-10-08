package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class LivingKillEntityTriggers extends SpellTriggers.EffectBasedTriggerCollection<LivingKillEntityTriggers.Trigger> {
    @FunctionalInterface
    public interface Trigger {
        void apply(LivingDeathEvent event, DamageSource source, IMKEntityData killerData);
    }

    private static final String TAG = "LIVING_KILL_ENTITY";

    public void onEntityDeath(LivingDeathEvent event, DamageSource source, IMKEntityData killerData) {
        runTrigger(killerData, TAG, (trigger, instance) -> trigger.apply(event, source, killerData));
    }
}
