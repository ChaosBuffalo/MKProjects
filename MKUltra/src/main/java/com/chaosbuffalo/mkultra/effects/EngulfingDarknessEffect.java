package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.status.DamageTypeDotEffect;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class EngulfingDarknessEffect extends DamageTypeDotEffect {

    public static int DEFAULT_PERIOD = 40;
    private static final UUID modUUID = UUID.fromString("b349fa30-5995-42c0-8fff-19b5c181cc75");

    public EngulfingDarknessEffect() {
        addAttribute(Attributes.MOVEMENT_SPEED, modUUID, -0.10, -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL, MKAttributes.CONJURATION);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, float base, float scaling, float modifierScaling,
                                          float chanceToTrigger, int ticksForTrigger,
                                          ResourceLocation castParticles) {
        return MKUEffects.ENGULFING_DARKNESS.get().builder(source)
                .state(s -> {
                    s.setEffectParticles(castParticles);
                    s.setScalingParameters(base, scaling, modifierScaling);
                    s.setTriggerChance(chanceToTrigger);
                    s.setTriggerTime(ticksForTrigger);
                })
                .periodic(DEFAULT_PERIOD);
    }

    @Override
    public EngulfingDarknessEffect.State makeState() {
        return new EngulfingDarknessEffect.State();
    }

    @Override
    public MKEffectBuilder<EngulfingDarknessEffect.State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<EngulfingDarknessEffect.State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends DamageTypeDotEffect.State {
        protected float triggerChance = 0.0f;
        protected int triggerTime = GameConstants.TICKS_PER_SECOND;

        public State() {
            super();
            setDamageType(CoreDamageTypes.ShadowDamage.get());
        }

        public void setTriggerChance(float triggerChance) {
            this.triggerChance = triggerChance;
        }

        public float getTriggerChance() {
            return triggerChance;
        }

        public void setTriggerTime(int triggerTime) {
            this.triggerTime = triggerTime;
        }

        public int getTriggerTime() {
            return triggerTime;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            SoundUtils.serverPlaySoundAtEntity(targetData.getEntity(), MKUSounds.spell_dark_1.get(),
                    targetData.getEntity().getSoundSource());
            sendEffectParticles(targetData.getEntity());
            LivingEntity source = activeEffect.getSourceEntity();
            if (source != null && source.getRandom().nextFloat() <= getTriggerChance()) {
                MKCore.getEntityData(source).ifPresent(
                        x -> {
                            x.getEffects().addEffect(ShadowbringerEffect.from(source, getTriggerTime()));
                            SoundUtils.serverPlaySoundAtEntity(source, MKUSounds.spell_dark_9.get(),
                                    source.getSoundSource());
                        });
            }
            return super.performEffect(targetData, activeEffect);
        }
    }
}
