package com.chaosbuffalo.mkultra.abilities.necromancer;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.WindUpPulseAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
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

public class ShadowPulseAbility extends WindUpPulseAbility {
    private static final ResourceLocation PULSE_PARTICLES = MKUltra.id("shadow_pulse_detonate");
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("shadow_bolt_casting");
    private static final ResourceLocation WAIT_PARTICLES = MKUltra.id("shadow_pulse_wait");
    protected final FormulaAttribute damageFormula = new FormulaAttribute("damageFormula", AbilityFormula.linear(1.0f, 0.25f));
    protected final FloatAttribute baseGravity = new FloatAttribute("baseGravity", 0.25f);
    protected final FloatAttribute scaleGravity = new FloatAttribute("scaleGravity", 0.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final FormulaAttribute detonateDamageFormula = new FormulaAttribute("detonateDamageFormula", AbilityFormula.linear(5.0f, 5.0f));


    public ShadowPulseAbility() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND);
        setCooldownSeconds(10);
        setManaCost(5);
        addSkillAttribute(MKAttributes.CONJURATION);
        pulseParticles.setDefaultValue(PULSE_PARTICLES);
        waitParticles.setDefaultValue(WAIT_PARTICLES);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        addAttributes(damageFormula, modifierScaling, baseGravity, scaleGravity, detonateDamageFormula);
    }

    @Override
    public void setupEntityEffect(EntityEffectBuilder.PointEffectBuilder builder, IMKEntityData casterData, Vec3 position, AbilityContext context) {
        float level = context.getSkill(MKAttributes.CONJURATION);
        LivingEntity castingEntity = casterData.getEntity();
        MKEffectBuilder<?> damage = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.ShadowDamage.get(),
                        damageFormula.value(), modifierScaling.value())
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> pull = PullEffect.from(castingEntity, baseGravity.value(), scaleGravity.value(), position)
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_shadow_10.value(), castingEntity.getSoundSource())
                .ability(this);
        MKEffectBuilder<?> detonateDamage = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.ShadowDamage.get(),
                        detonateDamageFormula.value(), modifierScaling.value())
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> detonateSound = SoundEffect.from(castingEntity, MKUSounds.spell_shadow_9.value(), castingEntity.getSoundSource())
                .ability(this);
        builder.effect(damage, getTargetContext())
                .effect(sound, getTargetContext())
                .effect(pull, getTargetContext())
                .delayedEffect(detonateSound, getTargetContext(), duration.value())
                .delayedEffect(detonateDamage, getTargetContext(), duration.value());
        SoundUtils.serverPlaySoundFromEntity(position.x(), position.y(), position.z(), MKUSounds.spell_dark_13.value(),
                castingEntity.getSoundSource(), 1.0f, 1.0f, castingEntity);
    }


    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.CONJURATION);
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.ShadowDamage.get(), damageFormula.value(), level, modifierScaling.value());
        Component detonateStr = getDamageDescription(casterData, CoreDamageTypes.ShadowDamage.get(), detonateDamageFormula.value(), level, modifierScaling.value());
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
        return MKUSounds.hostile_casting_shadow.value();
    }

}
