package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.WindUpPulseAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.ClericHealEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class HealingCircleAbility extends WindUpPulseAbility {
    private static final ResourceLocation PULSE_PARTICLES = new ResourceLocation(MKUltra.MODID, "healing_circle_detonate");
    private static final ResourceLocation WAIT_PARTICLES = new ResourceLocation(MKUltra.MODID, "healing_circle_wait");
    private static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "healing_circle_casting");
    protected final FloatAttribute base = new FloatAttribute("base", 3.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 1.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);

    public HealingCircleAbility() {
        super();
        waitParticles.setDefaultValue(WAIT_PARTICLES);
        pulseParticles.setDefaultValue(PULSE_PARTICLES);
        duration.setDefaultValue(GameConstants.TICKS_PER_SECOND * 4);
        addAttributes(base, scale, modifierScaling);
        addSkillAttribute(MKAttributes.RESTORATION);
        waitTime.setDefaultValue(GameConstants.TICKS_PER_SECOND);
        waitTickRate.setDefaultValue(GameConstants.TICKS_PER_SECOND / 4);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        radius.setDefaultValue(1.6f);
        setCooldownSeconds(20);
        setManaCost(8);
        setEndpointOnWait.setDefaultValue(true);
        tickRate.setDefaultValue(GameConstants.TICKS_PER_SECOND / 2);
        setCastTime(GameConstants.TICKS_PER_SECOND / 2);
    }

    @Override
    public Vec3 getEndpointOffset(Vec3 pulsePos) {
        return new Vec3(0.0f, 3.0f, 0.0f);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.RESTORATION);
        Component damageStr = getHealDescription(casterData, base.value(), scale.value(), level, modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(),
                NUMBER_FORMATTER.format(radius.value()),
                NUMBER_FORMATTER.format(convertDurationToSeconds(waitTime.value())),
                damageStr,
                NUMBER_FORMATTER.format(convertDurationToSeconds(tickRate.value())),
                NUMBER_FORMATTER.format(convertDurationToSeconds(duration.value())));
    }

    @Override
    public void setupEntityEffect(EntityEffectBuilder.PointEffectBuilder builder, IMKEntityData casterData, Vec3 position, AbilityContext context) {
        float level = context.getSkill(MKAttributes.RESTORATION);
        LivingEntity castingEntity = casterData.getEntity();
        MKEffectBuilder<?> damage = ClericHealEffect.from(castingEntity,
                        base.value(), scale.value(), modifierScaling.value())
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_holy_4.get(), castingEntity.getSoundSource())
                .ability(this);

        builder.effect(damage, getTargetContext())
                .delayedEffect(damage, getTargetContext(), waitTime.value())
                .delayedEffect(sound, getTargetContext(), waitTime.value());
        SoundUtils.serverPlaySoundFromEntity(position.x(), position.y(), position.z(), MKUSounds.spell_holy_8.get(),
                castingEntity.getSoundSource(), 1.0f, 1.0f, castingEntity);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return super.isValidTarget(caster, target) || MKHealing.wouldHealHurtUndead(caster, target);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 20.0f;
    }

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_holy.get();
    }
}

