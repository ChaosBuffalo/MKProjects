package com.chaosbuffalo.mknpc.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.init.MKNpcEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public class HealingThreatEffect extends MKEffect {

    public HealingThreatEffect() {
        super(MobEffectCategory.NEUTRAL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, LivingEntity threatSource, float threatAmount) {
        return MKNpcEffects.THREAT.get().builder(source).state(s -> {
            s.setThreatValue(threatAmount);
            s.setThreatSource(threatSource);
        });
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    @Override
    public State makeState() {
        return new State();
    }

    public static class State extends MKEffectState {
        protected float threatValue;
        @Nullable
        protected LivingEntity threatSource;

        public float getThreatValue() {
            return threatValue;
        }

        public void setThreatSource(@Nullable LivingEntity threatSource) {
            this.threatSource = threatSource;
        }

        @Nullable
        public LivingEntity getThreatSource() {
            return threatSource;
        }

        public void setThreatValue(float threatValue) {
            this.threatValue = threatValue;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            if (targetData.getEntity() instanceof MKEntity mkEntity) {
                LivingEntity source = activeEffect.getSourceEntity();
                LivingEntity threatSource = getThreatSource();
                if (source != null && threatSource != null) {
                    if (mkEntity.hasThreatWithTarget(threatSource)) {
                        mkEntity.addThreat(source, getThreatValue(), true);
                    }
                }
            }
            return true;
        }

        @Override
        public void serializeStorage(CompoundTag stateTag) {
            super.serializeStorage(stateTag);
            stateTag.putFloat("threat", threatValue);
        }

        @Override
        public void deserializeStorage(CompoundTag stateTag) {
            super.deserializeStorage(stateTag);
            threatValue = stateTag.getFloat("threat");
        }
    }
}
