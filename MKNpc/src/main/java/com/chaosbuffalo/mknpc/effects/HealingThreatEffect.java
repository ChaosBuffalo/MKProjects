package com.chaosbuffalo.mknpc.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;

public class HealingThreatEffect extends MKEffect {
    public static final HealingThreatEffect INSTANCE = new HealingThreatEffect();

    private HealingThreatEffect() {
        super(MobEffectCategory.NEUTRAL);
        setRegistryName(MKNpc.MODID, "effect.threat");
    }

    public static MKEffectBuilder<?> from(LivingEntity source, LivingEntity threatSource, float threatAmount) {
        return INSTANCE.builder(source).state(s -> {
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
            if (targetData.getEntity() instanceof MKEntity) {
                MKEntity mkEntity = (MKEntity) targetData.getEntity();
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
        public void deserializeStorage(CompoundTag tag) {
            super.deserializeStorage(tag);
            threatValue = tag.getFloat("threat");
        }
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class RegisterMe {
        @SubscribeEvent
        public static void register(RegistryEvent.Register<MKEffect> event) {
            event.getRegistry().register(INSTANCE);
        }
    }
}
