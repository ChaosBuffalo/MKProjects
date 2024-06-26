package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.PositionTargetingAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Function;

public class WrathBeamAbility extends PositionTargetingAbility {
    private static final ResourceLocation PULSE_PARTICLES = new ResourceLocation(MKUltra.MODID, "wrath_beam_pulse");
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "flame_wave_casting");
    private static final ResourceLocation WAIT_PARTICLES = new ResourceLocation(MKUltra.MODID, "wrath_beam_wait");

    protected final FloatAttribute base = new FloatAttribute("base", 5.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 5.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final ResourceLocationAttribute pulse_particles = new ResourceLocationAttribute("pulse_particles", PULSE_PARTICLES);
    protected final ResourceLocationAttribute wait_particles = new ResourceLocationAttribute("wait_particles", WAIT_PARTICLES);
    protected final IntAttribute tickRate = new IntAttribute("tickRate", GameConstants.TICKS_PER_SECOND / 2);
    protected final IntAttribute duration = new IntAttribute("duration", GameConstants.TICKS_PER_SECOND * 2);
    protected final IntAttribute breakDuration = new IntAttribute("breakDuration", GameConstants.TICKS_PER_SECOND * 2);

    public WrathBeamAbility() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND);
        setCooldownSeconds(5);
        setManaCost(6);
        addSkillAttribute(MKAttributes.EVOCATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
        addAttributes(base, scale, modifierScaling, tickRate, pulse_particles, wait_particles, duration, breakDuration);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.EVOCATION);
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.FireDamage.get(), base.value(), scale.value(), level, modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(), damageStr,
                NUMBER_FORMATTER.format(convertDurationToSeconds(breakDuration.value())),
                NUMBER_FORMATTER.format(convertDurationToSeconds(tickRate.value())),
                NUMBER_FORMATTER.format(convertDurationToSeconds(duration.value())));
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 20.0f;
    }

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.hostile_casting_fire.get();
    }

    @Override
    public void castAtPosition(IMKEntityData casterData, Vec3 position, AbilityContext context) {
        LivingEntity castingEntity = casterData.getEntity();
        float level = context.getSkill(MKAttributes.EVOCATION);
        MKEffectBuilder<?> damage = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.FireDamage.get(),
                        base.value(), scale.value(), modifierScaling.value())
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> fireBreak = MKUEffects.BREAK_FIRE.get().builder(castingEntity)
                .ability(this)
                .timed(breakDuration.value())
                .skillLevel(level);
        MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_fire_7.get(), castingEntity.getSoundSource())
                .ability(this);
        EntityEffectBuilder.LineEffectBuilder lineBuilder = EntityEffectBuilder.createLineEffect(castingEntity,
                position.subtract(0.0, 0.1, 0.0),
                position.add(0.0, 4.1, 0.0));
        lineBuilder.effect(damage, getTargetContext())
                .effect(fireBreak, getTargetContext())
                .effect(sound, getTargetContext())
                .setParticles(new BaseEffectEntity.ParticleDisplay(pulse_particles.getValue(), tickRate.value(), BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                .setWaitingParticles(new BaseEffectEntity.ParticleDisplay(wait_particles.getValue(), tickRate.value(), BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                .duration(duration.value())
                .waitTime(duration.value() / 2)
                .tickRate(tickRate.value());
        SoundUtils.serverPlaySoundFromEntity(position.x(), position.y(), position.z(),
                MKUSounds.spell_dark_13.get(), castingEntity.getSoundSource(), 1.0f, 1.0f, castingEntity);
        lineBuilder.spawn();
    }
}
