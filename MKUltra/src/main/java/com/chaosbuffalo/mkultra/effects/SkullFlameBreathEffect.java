package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingDamageEffectState;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class SkullFlameBreathEffect extends MKEffect {

    public static MKEffectBuilder<?> from(LivingEntity source, float baseDamage, float scaling, float modifierScaling,
                                          int fireSeconds) {
        return MKUEffects.SKULL_FLAME_BREATH.get().builder(source).state(s -> {
            s.fireSeconds = fireSeconds;
            s.setDamageParameters(baseDamage, scaling, modifierScaling);
        });
    }

    public SkullFlameBreathEffect() {
        super(MobEffectCategory.HARMFUL);
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
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends ScalingDamageEffectState {
        public int fireSeconds;

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            DamageSource damage = MKDamageSource.causeAbilityDamage(targetData.getEntity().level(),
                    CoreDamageTypes.FireDamage.get(), activeEffect.getAbilityId(),
                    activeEffect.getDirectEntity(), activeEffect.getSourceEntity())
                    .setDamageBonusFormula(getDamageBonusFormula());

            targetData.getEntity().hurt(damage, getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel()));
            targetData.getEntity().igniteForSeconds(fireSeconds);
            return true;
        }

        @Override
        public void serializeStorage(CompoundTag stateTag) {
            super.serializeStorage(stateTag);
            stateTag.putInt("fireSeconds", fireSeconds);
        }

        @Override
        public void deserializeStorage(CompoundTag stateTag) {
            super.deserializeStorage(stateTag);
            fireSeconds = stateTag.getInt("fireSeconds");
        }
    }
}
