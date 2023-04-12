package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingValueEffectState;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class IgniteEffect extends MKEffect {

    public static MKEffectBuilder<?> from(LivingEntity source, float baseDamage, float scaling, float modifierScaling) {
        return MKUEffects.IGNITE.get().builder(source)
                .state(s -> s.setScalingParameters(baseDamage, scaling, modifierScaling));
    }

    public IgniteEffect() {
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

    public static class State extends ScalingValueEffectState {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {

            float damage = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            float scaling = getModifierScale();
            targetData.getEntity().hurt(MKDamageSource.causeAbilityDamage(
                    targetData.getEntity().getLevel(),
                    CoreDamageTypes.FireDamage.get(),
                    activeEffect.getAbilityId(), activeEffect.getDirectEntity(), activeEffect.getSourceEntity(), scaling), damage);

            MKCore.getEntityData(activeEffect.getSourceEntity()).ifPresent(casterData -> {
                MKEffectBuilder<?> burn = MKUAbilities.EMBER.get().getBurnCast(casterData, activeEffect.getStackCount())
                        .ability(activeEffect.getAbilityId());
                targetData.getEffects().addEffect(burn);
            });

            return true;
        }
    }
}
