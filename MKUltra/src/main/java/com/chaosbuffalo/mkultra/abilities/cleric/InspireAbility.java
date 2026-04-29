package com.chaosbuffalo.mkultra.abilities.cleric;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class InspireAbility extends MKAbility {
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("inspire.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("inspire.duration.per_level"));
    protected final ResourceLocation CASTING_PARTICLES = MKUltra.id("inspire_casting");
    protected final ResourceLocation CAST_PARTICLES = MKUltra.id("inspire_cast");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DURATION_BASE_PARAMETER, 8.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 2.0f)
                    .build());
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public InspireAbility() {
        super();
        setCooldownSeconds(35);
        setManaCost(8);
        setCastTime(GameConstants.TICKS_PER_SECOND * 2);
        addAttributes(formulaParameters, durationFormula, cast_particles);
        addSkillAttribute(MKAttributes.ALTERATON);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ALTERATON);
        int duration = getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), level)
                / GameConstants.TICKS_PER_SECOND;
        return Component.translatable(getDescriptionTranslationKey(), duration);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 20.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_holy.value();
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_cast_12.value();
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Ultimate;
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        float level = context.getSkill(MKAttributes.ALTERATON);
        int duration = getBuffDuration(casterData, durationFormula.value(), formulaParameters.value(), level);
        int oldAmp = Math.round(level);

        MobEffectInstance hasteEffect = new MobEffectInstance(MobEffects.DIG_SPEED, duration, oldAmp, false, false);
        MobEffectInstance regenEffect = new MobEffectInstance(MobEffects.REGENERATION, duration, oldAmp, false, false);
        MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_holy_8.value(), castingEntity.getSoundSource())
                .ability(this);
        MKEffectBuilder<?> particles = MKParticleEffect.from(castingEntity, cast_particles.getValue(), true, new Vec3(0.0, 1.0, 0.0))
                .ability(this);

        AreaEffectBuilder.createOnCaster(castingEntity)
                .effect(hasteEffect, getTargetContext())
                .effect(regenEffect, getTargetContext())
                .effect(sound, getTargetContext())
                .effect(particles, getTargetContext())
                .instant()
                .color(1034415)
                .radius(getDistance(castingEntity), true)
                .disableParticle()
                .spawn();
    }
}
