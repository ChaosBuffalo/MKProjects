package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.combat.AbilityMeleeAttackContext;
import com.chaosbuffalo.mkcore.core.combat.AbilityMeleeAttackExecutor;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingDamageEffectState;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.StackingBonusFormulaSpec;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class AbilityMeleeDamageEffect extends MKEffect {

    public AbilityMeleeDamageEffect() {
        super(MobEffectCategory.HARMFUL);
    }

    public static MKEffectBuilder<State> from(LivingEntity source, InteractionHand hand, float baseDamage,
                                              float scaling, float modifierScaling) {
        return CoreEffects.ABILITY_MELEE_DAMAGE.get().builder(source).state(s -> {
            s.setDamageParameters(baseDamage, scaling, modifierScaling);
            s.setHand(hand);
        });
    }

    public static MKEffectBuilder<State> fromSwingScaling(LivingEntity source, InteractionHand hand, float swingDamageScale) {
        return CoreEffects.ABILITY_MELEE_DAMAGE.get().builder(source).state(s -> {
            s.setHand(hand);
            s.setSwingDamageScale(swingDamageScale);
        });
    }

    public static MKEffectBuilder<State> from(LivingEntity source, InteractionHand hand, BonusFormulaSpec damage,
                                              FormulaParameters parameters) {
        return CoreEffects.ABILITY_MELEE_DAMAGE.get().builder(source).state(s -> {
            s.setHand(hand);
            s.setParameterizedDamageFormula(damage, parameters);
        });
    }

    public static MKEffectBuilder<State> from(LivingEntity source, InteractionHand hand, StackingBonusFormulaSpec damage,
                                              FormulaParameters parameters) {
        return CoreEffects.ABILITY_MELEE_DAMAGE.get().builder(source).state(s -> {
            s.setHand(hand);
            s.setParameterizedDamageFormula(damage, parameters);
        });
    }

    public static MKEffectBuilder<State> from(LivingEntity source, InteractionHand hand, float swingDamageScale,
                                              float baseDamage, float scaling, float modifierScaling) {
        return CoreEffects.ABILITY_MELEE_DAMAGE.get().builder(source).state(s -> {
            s.setHand(hand);
            s.setSwingDamageScale(swingDamageScale);
            s.setDamageParameters(baseDamage, scaling, modifierScaling);
        });
    }

    public static MKEffectBuilder<State> from(LivingEntity source, InteractionHand hand, float swingDamageScale,
                                              BonusFormulaSpec damage, FormulaParameters parameters) {
        return CoreEffects.ABILITY_MELEE_DAMAGE.get().builder(source).state(s -> {
            s.setHand(hand);
            s.setSwingDamageScale(swingDamageScale);
            s.setParameterizedDamageFormula(damage, parameters);
        });
    }

    public static MKEffectBuilder<State> from(LivingEntity source, InteractionHand hand, float swingDamageScale,
                                              StackingBonusFormulaSpec damage, FormulaParameters parameters) {
        return CoreEffects.ABILITY_MELEE_DAMAGE.get().builder(source).state(s -> {
            s.setHand(hand);
            s.setSwingDamageScale(swingDamageScale);
            s.setParameterizedDamageFormula(damage, parameters);
        });
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
        private InteractionHand hand = InteractionHand.MAIN_HAND;
        private float swingDamageScale = 1.0f;

        public void setHand(InteractionHand hand) {
            this.hand = hand;
        }

        public void setSwingDamageScale(float swingDamageScale) {
            this.swingDamageScale = swingDamageScale;
        }

        @Override
        public void serializeStorage(CompoundTag stateTag) {
            super.serializeStorage(stateTag);
            stateTag.putString("hand", hand.name());
            stateTag.putFloat("swingDamageScale", swingDamageScale);
        }

        @Override
        public void deserializeStorage(CompoundTag stateTag) {
            super.deserializeStorage(stateTag);
            if (stateTag.contains("hand")) {
                hand = InteractionHand.valueOf(stateTag.getString("hand"));
            }
            if (stateTag.contains("swingDamageScale")) {
                swingDamageScale = stateTag.getFloat("swingDamageScale");
            }
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            LivingEntity sourceEntity = activeEffect.getSourceEntity();
            if (sourceEntity == null) {
                return false;
            }
            return MKCore.getEntityData(sourceEntity).map(sourceData -> {
                float rawBonus = sourceData.getStats().getDamageTypeBonus(CoreDamageTypes.MeleeDamage.get());
                float bonusDamage = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel())
                        + getDamageBonusFormula().evaluate(FormulaContext.builder()
                                .withDamageBonus(rawBonus)
                                .build());
                AbilityMeleeAttackContext context = new AbilityMeleeAttackContext(
                        sourceData,
                        sourceEntity,
                        targetData.getEntity(),
                        hand,
                        activeEffect.getAbilityId(),
                        swingDamageScale,
                        bonusDamage,
                        activeEffect.getDirectEntity() != null ? activeEffect.getDirectEntity() : sourceEntity,
                        new int[0],
                        new int[0],
                        false
                );
                return AbilityMeleeAttackExecutor.executeAttack(context);
            }).orElse(false);
        }
    }
}
