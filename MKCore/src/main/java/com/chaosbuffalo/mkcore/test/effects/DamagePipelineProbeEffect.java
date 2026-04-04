package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.triggers.CoreTriggerTypes;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerRegistrar;
import com.google.common.reflect.TypeToken;
import com.chaosbuffalo.mkcore.effects.triggers.MKTriggerContributor;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.UUID;

public class DamagePipelineProbeEffect extends MKEffect implements MKTriggerContributor {
    public static final TypeToken<State> STATE = new TypeToken<>() {
    };

    public DamagePipelineProbeEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public void registerTriggers(MKActiveEffect activeEffect, EntityTriggerRegistrar registrar) {
        registrar.add(CoreTriggerTypes.ATTACKER_MELEE, context ->
                onAttackerMelee(activeEffect));
        registrar.add(CoreTriggerTypes.ATTACKER_PROJECTILE, context ->
                onAttackerProjectile(activeEffect));
        registrar.add(CoreTriggerTypes.ATTACKER_POST, context ->
                onAttackerPost(activeEffect));
        registrar.add(CoreTriggerTypes.VICTIM_INCOMING, context ->
                onVictimPreScale(activeEffect));
        registrar.add(CoreTriggerTypes.VICTIM_POST, context ->
                onVictimPostScale(activeEffect));
    }

    private void onAttackerMelee(MKActiveEffect activeEffect) {
        activeEffect.getState(STATE).attackerMeleeCount++;
    }

    private void onAttackerProjectile(MKActiveEffect activeEffect) {
        activeEffect.getState(STATE).attackerProjectileCount++;
    }

    private void onAttackerPost(MKActiveEffect activeEffect) {
        activeEffect.getState(STATE).attackerPostCount++;
    }

    private void onVictimPreScale(MKActiveEffect activeEffect) {
        activeEffect.getState(STATE).victimPreScaleCount++;
    }

    private void onVictimPostScale(MKActiveEffect activeEffect) {
        activeEffect.getState(STATE).victimPostScaleCount++;
    }

    public static class State extends MKEffectState {
        private int attackerMeleeCount;
        private int attackerProjectileCount;
        private int attackerPostCount;
        private int victimPreScaleCount;
        private int victimPostScaleCount;

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            return false;
        }

        public int getAttackerMeleeCount() {
            return attackerMeleeCount;
        }

        public int getAttackerProjectileCount() {
            return attackerProjectileCount;
        }

        public int getAttackerPostCount() {
            return attackerPostCount;
        }

        public int getVictimPreScaleCount() {
            return victimPreScaleCount;
        }

        public int getVictimPostScaleCount() {
            return victimPostScaleCount;
        }
    }
}
