package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class LivingDeathTriggers extends SpellTriggers.EffectBasedTriggerCollection<LivingDeathTriggers.DeathTrigger> {
    @FunctionalInterface
    public interface DeathTrigger {
        void apply(LivingDeathEvent event, DamageSource source, LivingEntity player);
    }

    private static final String TAG = "LIVING_DEATH";

    public void onEntityDeath(LivingDeathEvent event, DamageSource source, LivingEntity entity) {
        runTrigger(entity, TAG, (trigger, instance) -> trigger.apply(event, source, entity));
    }
}
