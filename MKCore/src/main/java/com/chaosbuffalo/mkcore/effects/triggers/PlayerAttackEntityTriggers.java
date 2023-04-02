package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class PlayerAttackEntityTriggers extends SpellTriggers.EffectBasedTriggerCollection<PlayerAttackEntityTriggers.PlayerAttackEntityTrigger> {
    @FunctionalInterface
    public interface PlayerAttackEntityTrigger {
        void apply(LivingEntity player, Entity target, MKActiveEffect effect);
    }

    private static final String TAG = "PLAYER_ATTACK_ENTITY";

    public void onAttackEntity(LivingEntity attacker, Entity target) {
        runTrigger(attacker, TAG, (trigger, instance) -> trigger.apply(attacker, target, instance));
    }
}
