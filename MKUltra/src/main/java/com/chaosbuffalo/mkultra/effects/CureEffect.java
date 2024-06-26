package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

public class CureEffect extends MKEffect {

    public CureEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source) {
        return MKUEffects.CURE.get().builder(source);
    }

    @Override
    public MKEffectState makeState() {
        return new State();
    }

    public static class State extends MKEffectState {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            ArrayList<MobEffectInstance> toRemove = new ArrayList<>();
            int count = 0;
            for (MobEffectInstance effect : targetData.getEntity().getActiveEffects()) {
                if (count > activeEffect.getStackCount()) {
                    break;
                }
                if (!effect.getEffect().isBeneficial()) {
                    toRemove.add(effect);
                    count++;
                }
            }
            for (MobEffectInstance effect : toRemove) {
                targetData.getEntity().removeEffect(effect.getEffect());
            }
            return true;
        }
    }
}
