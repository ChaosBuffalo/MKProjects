package com.chaosbuffalo.mkultra.abilities.necromancer;

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
import com.chaosbuffalo.mkultra.effects.PullEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class ShadowPulseAbility extends PositionTargetingAbility {
    private static final ResourceLocation PULSE_PARTICLES = new ResourceLocation(MKUltra.MODID, "shadow_pulse_detonate");
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "shadow_bolt_casting");
    private static final ResourceLocation WAIT_PARTICLES = new ResourceLocation(MKUltra.MODID, "shadow_pulse_wait");
    protected final FloatAttribute base = new FloatAttribute("base", 1.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 0.25f);
    protected final FloatAttribute baseGravity = new FloatAttribute("baseGravity", 0.25f);
    protected final FloatAttribute scaleGravity = new FloatAttribute("scaleGravity", 0.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final ResourceLocationAttribute pulse_particles = new ResourceLocationAttribute("pulse_particles", PULSE_PARTICLES);
    protected final ResourceLocationAttribute wait_particles = new ResourceLocationAttribute("wait_particles", WAIT_PARTICLES);
    protected final IntAttribute tickRate = new IntAttribute("tickRate", GameConstants.TICKS_PER_SECOND / 5);
    protected final IntAttribute duration = new IntAttribute("duration", GameConstants.TICKS_PER_SECOND);
    protected final IntAttribute waitTime = new IntAttribute("waitTime", 3 * GameConstants.TICKS_PER_SECOND / 5);
    protected final FloatAttribute detonateBase = new FloatAttribute("detonateBase", 5.0f);
    protected final FloatAttribute detonateScale = new FloatAttribute("detonateScale", 5.0f);
    protected final FloatAttribute radius = new FloatAttribute("radius", 1.5f);


    public ShadowPulseAbility() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND);
        setCooldownSeconds(10);
        setManaCost(5);
        addSkillAttribute(MKAttributes.CONJURATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
        addAttributes(base, scale, modifierScaling, tickRate, pulse_particles, wait_particles, duration,
                baseGravity, scaleGravity, detonateBase, detonateScale, radius, waitTime);
    }


    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.CONJURATION);
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.ShadowDamage.get(), base.value(), scale.value(), level, modifierScaling.value());
        Component detonateStr = getDamageDescription(casterData, CoreDamageTypes.ShadowDamage.get(), detonateBase.value(), detonateScale.value(), level, modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(),
                NUMBER_FORMATTER.format(radius.value()),
                damageStr,
                NUMBER_FORMATTER.format(convertDurationToSeconds(tickRate.value())),
                NUMBER_FORMATTER.format(convertDurationToSeconds(duration.value())),
                detonateStr);
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
        return MKUSounds.hostile_casting_shadow.get();
    }

    @Override
    public void castAtPosition(IMKEntityData casterData, Vec3 position, AbilityContext context) {
        LivingEntity castingEntity = casterData.getEntity();
        Vec3 pulseOffset = new Vec3(0.0, 0.5, 0.0);
        Vec3 pulsePos = position.add(pulseOffset);
        float level = context.getSkill(MKAttributes.CONJURATION);
        MKEffectBuilder<?> damage = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.ShadowDamage.get(),
                        base.value(), scale.value(), modifierScaling.value())
                .ability(this)
                .skillLevel(level);
//        MKEffectBuilder<?> fireBreak = ResistanceEffects.BREAK_FIRE.builder(castingEntity)
//                .ability(this)
//                .timed(breakDuration.value())
//                .skillLevel(level);
        MKEffectBuilder<?> pull = PullEffect.from(castingEntity, baseGravity.value(), scaleGravity.value(), pulsePos)
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_shadow_10.get(), castingEntity.getSoundSource())
                .ability(this);
        MKEffectBuilder<?> detonateDamage = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.ShadowDamage.get(),
                        detonateBase.value(), detonateScale.value(), modifierScaling.value())
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> detonateSound = SoundEffect.from(castingEntity, MKUSounds.spell_shadow_9.get(), castingEntity.getSoundSource())
                .ability(this);
        EntityEffectBuilder.PointEffectBuilder builder = EntityEffectBuilder.createPointEffect(castingEntity, pulsePos);

        builder.radius(radius.value())
                .effect(damage, getTargetContext())
                .effect(sound, getTargetContext())
                .effect(pull, getTargetContext())
                .delayedEffect(detonateSound, getTargetContext(), duration.value())
                .delayedEffect(detonateDamage, getTargetContext(), duration.value())
                .setParticles(new BaseEffectEntity.ParticleDisplay(pulse_particles.getValue(), tickRate.value(), BaseEffectEntity.ParticleDisplay.DisplayType.ONCE))
                .setWaitingParticles(new BaseEffectEntity.ParticleDisplay(wait_particles.getValue(), GameConstants.TICKS_PER_SECOND / 5, BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                .duration(duration.value())
                .waitTime(waitTime.value())
                .tickRate(tickRate.value());
        SoundUtils.serverPlaySoundFromEntity(position.x(), position.y(), position.z(), MKUSounds.spell_dark_13.get(),
                castingEntity.getSoundSource(), 1.0f, 1.0f, castingEntity);
        builder.spawn();
    }

}
