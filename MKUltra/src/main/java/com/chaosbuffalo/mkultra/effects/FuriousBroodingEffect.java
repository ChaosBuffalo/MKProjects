package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingDamageEffectState;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class FuriousBroodingEffect extends MKEffect {
    public static final int DEFAULT_PERIOD = GameConstants.TICKS_PER_SECOND;

    private static final UUID modUUID = UUID.fromString("06bbfb88-d53e-4565-964c-4642b9165c6d");

    public FuriousBroodingEffect() {
        super(MobEffectCategory.BENEFICIAL);
        addAttribute(Attributes.MOVEMENT_SPEED, modUUID, -0.60, 0.05, AttributeModifier.Operation.MULTIPLY_TOTAL, MKAttributes.PNEUMA);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, float baseHealing, float scaling, float modifierScaling,
                                          ResourceLocation castParticles) {
        return MKUEffects.FURIOUS_BROODING.get().builder(source)
                .state(s -> {
                    s.setEffectParticles(castParticles);
                    s.setScalingParameters(baseHealing, scaling, modifierScaling);
                })
                .periodic(DEFAULT_PERIOD);
    }

    @Override
    public FuriousBroodingEffect.State makeState() {
        return new FuriousBroodingEffect.State();
    }

    @Override
    public MKEffectBuilder<FuriousBroodingEffect.State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<FuriousBroodingEffect.State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends ScalingDamageEffectState {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            float healing = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            LivingEntity target = targetData.getEntity();
            MKHealing.healEntityFrom(target, healing, MKHealSource.getNatureHeal(activeEffect.getAbilityId(),
                    activeEffect.getDirectEntity(), activeEffect.getSourceEntity(), getModifierScale()));
            sendEffectParticles(targetData.getEntity());
            return true;
        }
    }
}
