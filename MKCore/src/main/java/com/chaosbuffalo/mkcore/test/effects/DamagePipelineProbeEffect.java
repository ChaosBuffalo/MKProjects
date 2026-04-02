package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.google.common.reflect.TypeToken;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.UUID;

public class DamagePipelineProbeEffect extends MKEffect {
    public static final TypeToken<State> STATE = new TypeToken<>() {
    };

    public DamagePipelineProbeEffect() {
        super(MobEffectCategory.BENEFICIAL);
        SpellTriggers.LIVING_HURT_ENTITY.registerMelee(this::onAttackerMelee);
        SpellTriggers.LIVING_HURT_ENTITY.registerProjectile(this::onAttackerProjectile);
        SpellTriggers.LIVING_HURT_ENTITY.registerPostHandler(this::onAttackerPost);
        SpellTriggers.ENTITY_HURT.registerPreScale(this::onVictimPreScale);
        SpellTriggers.ENTITY_HURT.registerPostScale(this::onVictimPostScale);
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    private void onAttackerMelee(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                 IMKEntityData attackerData) {
        attackerData.getEffects().effects(this).forEach(effect -> effect.getState(STATE).attackerMeleeCount++);
    }

    private void onAttackerProjectile(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                      IMKEntityData attackerData) {
        attackerData.getEffects().effects(this).forEach(effect -> effect.getState(STATE).attackerProjectileCount++);
    }

    private void onAttackerPost(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                IMKEntityData attackerData) {
        attackerData.getEffects().effects(this).forEach(effect -> effect.getState(STATE).attackerPostCount++);
    }

    private void onVictimPreScale(LivingDamageEvent.Pre event, DamageSource source, IMKEntityData victimData) {
        victimData.getEffects().effects(this).forEach(effect -> effect.getState(STATE).victimPreScaleCount++);
    }

    private void onVictimPostScale(LivingDamageEvent.Pre event, DamageSource source, IMKEntityData victimData) {
        victimData.getEffects().effects(this).forEach(effect -> effect.getState(STATE).victimPostScaleCount++);
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
