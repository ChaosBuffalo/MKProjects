package com.chaosbuffalo.mkultra.abilities.cleric;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.CureEffect;
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

public class GalvanizeAbility extends MKAbility {
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("galvanize.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("galvanize.duration.per_level"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("galvanize_casting");
    public static final ResourceLocation CAST_1_PARTICLES = MKUltra.id("galvanize_cast_1");
    public static final ResourceLocation CAST_2_PARTICLES = MKUltra.id("galvanize_cast_2");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DURATION_BASE_PARAMETER, 5.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 2.0f)
                    .build());
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final ResourceLocationAttribute cast_1_particles = new ResourceLocationAttribute("cast_1_particles", CAST_1_PARTICLES);
    protected final ResourceLocationAttribute cast_2_particles = new ResourceLocationAttribute("cast_2_particles", CAST_2_PARTICLES);

    public GalvanizeAbility() {
        super();
        setCooldownSeconds(25);
        setManaCost(8);
        setCastTime(GameConstants.TICKS_PER_SECOND / 4);
        addAttributes(formulaParameters, durationFormula, cast_1_particles, cast_2_particles);
        addSkillAttribute(MKAttributes.ABJURATION);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ABJURATION);
        int duration = getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), level)
                / GameConstants.TICKS_PER_SECOND;
        return Component.translatable(getDescriptionTranslationKey(), duration);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f;
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
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_heal_1.value();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.ABJURATION);
        int duration = getBuffDuration(data, durationFormula.value(), formulaParameters.value(), level);

        int oldAmp = Math.round(level);
        MobEffectInstance jump = new MobEffectInstance(MobEffects.JUMP, duration, oldAmp, false, false);
        MKEffectBuilder<?> cure = CureEffect.from(entity)
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> sound = SoundEffect.from(entity, MKUSounds.spell_buff_5.value(), entity.getSoundSource())
                .ability(this);
        MKEffectBuilder<?> particles = MKParticleEffect.from(entity, cast_2_particles.getValue(), false, new Vec3(0.0, 1.0, 0.0))
                .ability(this);

        AreaEffectBuilder.createOnCaster(entity)
                .effect(jump, getTargetContext())
                .effect(cure, getTargetContext())
                .effect(sound, getTargetContext())
                .effect(particles, getTargetContext())
                .instant()
                .color(1048370)
                .radius(getDistance(entity), true)
                .disableParticle()
                .spawn();

        MKParticles.spawnOffset(entity, new Vec3(0.0, 1.0, 0.0), cast_1_particles.getValue());
    }
}
