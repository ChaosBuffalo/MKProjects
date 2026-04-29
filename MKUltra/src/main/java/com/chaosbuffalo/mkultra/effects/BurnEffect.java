package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.status.DamageTypeDotEffect;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class BurnEffect extends DamageTypeDotEffect {

    public static int DEFAULT_PERIOD = 40;


    public BurnEffect() {
    }

    public static MKEffectBuilder<?> from(LivingEntity source, AbilityFormula damageFormula,
                                          FormulaParameters parameters, ResourceLocation castParticles) {
        return MKUEffects.BURN.get().builder(source)
                .state(s -> {
                    s.setEffectParticles(castParticles);
                    s.setParameterizedDamageFormula(damageFormula, parameters);
                })
                .periodic(DEFAULT_PERIOD);
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

    public static class State extends DamageTypeDotEffect.State {

        public State() {
            super();
            setDamageType(CoreDamageTypes.FireDamage.get());
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            SoundUtils.serverPlaySoundAtEntity(targetData.getEntity(), MKUSounds.spell_fire_6.value(), targetData.getEntity().getSoundSource());
            sendEffectParticles(targetData.getEntity());
            return super.performEffect(targetData, activeEffect);
        }
    }
}
