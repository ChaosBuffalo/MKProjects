package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.ScalingDamageEffectState;
import net.minecraft.world.effect.MobEffectCategory;

public abstract class DamageTypeDotEffect extends MKEffect {

    public DamageTypeDotEffect() {
        super(MobEffectCategory.HARMFUL);
    }

    public static class State extends ScalingDamageEffectState {
        private String effectName;

        @Override
        public boolean validateOnLoad(MKActiveEffect activeEffect) {
            return damageType != null;
        }

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            return damageType != null;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            float damage = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            if (effectName == null && damageType != null) {
                effectName = MKDamageType.periodicNameKey(damageType.getId());
            }

            targetData.getEntity().hurt(
                    MKDamageSource.causeEffectDamage(targetData.getEntity().getLevel(), damageType, effectName, activeEffect.getDirectEntity(), activeEffect.getSourceEntity(), getModifierScale()),
                    damage);
            return true;
        }
    }
}
