package com.chaosbuffalo.mkultra.effects;

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

public class DrownEffect extends DamageTypeDotEffect {

    public static int DEFAULT_PERIOD = 60;
    private static final UUID modUUID = UUID.fromString("2b977cba-ada8-4296-a62d-c6fa5cae6974");


    public DrownEffect() {
        addAttribute(Attributes.ATTACK_SPEED, modUUID, -0.05, -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL, MKAttributes.CONJURATION);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, float base, float scaling, float modifierScaling,
                                          ResourceLocation castParticles) {
        return MKUEffects.DROWN.get().builder(source)
                .state(s -> {
                    s.setEffectParticles(castParticles);
                    s.setScalingParameters(base, scaling, modifierScaling);
                })
                .periodic(DEFAULT_PERIOD);
    }

    @Override
    public DrownEffect.State makeState() {
        return new DrownEffect.State();
    }

    @Override
    public MKEffectBuilder<DrownEffect.State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<DrownEffect.State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends DamageTypeDotEffect.State {

        public State() {
            super();
            setDamageType(CoreDamageTypes.NatureDamage.get());
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            SoundUtils.serverPlaySoundAtEntity(targetData.getEntity(), MKUSounds.spell_water_4.get(), targetData.getEntity().getSoundSource());
            sendEffectParticles(targetData.getEntity());
            return super.performEffect(targetData, activeEffect);
        }
    }
}

