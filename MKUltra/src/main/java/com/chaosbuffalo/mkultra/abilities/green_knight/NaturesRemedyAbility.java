package com.chaosbuffalo.mkultra.abilities.green_knight;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.HealCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.NaturesRemedyEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class NaturesRemedyAbility extends MKAbility {
    private static final FormulaParameterKey HEAL_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("natures_remedy.heal.base"));
    private static final FormulaParameterKey HEAL_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("natures_remedy.heal.per_level"));
    private static final FormulaParameterKey MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("natures_remedy.heal.modifier_scaling"));
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("natures_remedy.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("natures_remedy.duration.per_level"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("natures_remedy_casting");
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("natures_remedy_cast");
    public static final ResourceLocation TICK_PARTICLES = MKUltra.id("natures_remedy_tick");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(HEAL_BASE_PARAMETER, 2.0f)
                    .with(HEAL_PER_LEVEL_PARAMETER, 1.0f)
                    .with(MODIFIER_SCALING_PARAMETER, 1.0f)
                    .with(DURATION_BASE_PARAMETER, 4.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 1.0f)
                    .build());
    protected final FormulaAttribute healingFormula = new FormulaAttribute("healingFormula",
            AbilityFormula.bonusScaledLinear(HEAL_BASE_PARAMETER, HEAL_PER_LEVEL_PARAMETER,
                    FormulaContextKey.HEAL_BONUS, MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.add(
            AbilityFormula.param(DURATION_BASE_PARAMETER),
            AbilityFormula.multiply(
                    AbilityFormula.param(DURATION_PER_LEVEL_PARAMETER),
                    AbilityFormula.context(FormulaContextKey.SKILL_LEVEL)
            )));
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final ResourceLocationAttribute tick_particles = new ResourceLocationAttribute("tick_particles", TICK_PARTICLES);

    public NaturesRemedyAbility() {
        super();
        setCooldownSeconds(10);
        setManaCost(4);
        setCastTime(GameConstants.TICKS_PER_SECOND / 2);
        addSkillAttribute(MKAttributes.RESTORATION);
        addAttributes(formulaParameters, healingFormula, durationFormula, cast_particles, tick_particles);
        setUseCondition(new HealCondition(this, .75f));
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.RESTORATION);
        FormulaContext formulaContext = baseFormulaContext(entityData, level).build();
        Component damageStr = getHealDescription(entityData, healingFormula.value(), formulaParameters.value(), level);
        int duration = getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), formulaContext)
                / GameConstants.TICKS_PER_SECOND;
        return Component.translatable(getDescriptionTranslationKey(), damageStr, duration);
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_cast_5.value();
    }

    public MKEffectBuilder<?> createNaturesRemedyEffect(IMKEntityData casterData, float level) {
        FormulaContext formulaContext = baseFormulaContext(casterData, level).build();
        int duration = getBuffDuration(casterData, durationFormula.value(), formulaParameters.value(), formulaContext);
        return NaturesRemedyEffect.from(casterData.getEntity(), healingFormula.value(),
                        formulaParameters.value(), tick_particles.getValue())
                .ability(this)
                .skillLevel(level)
                .timed(duration);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        float level = context.getSkill(MKAttributes.RESTORATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            MKEffectBuilder<?> heal = createNaturesRemedyEffect(casterData, level).ability(this);

            MKCore.getEntityData(targetEntity).ifPresent(targetData -> targetData.getEffects().addEffect(heal));

            SoundUtils.serverPlaySoundAtEntity(targetEntity, MKUSounds.spell_heal_8.value(), targetEntity.getSoundSource());
            MKParticles.spawnOffset(targetEntity, new Vec3(0.0, 1.0, 0.0), cast_particles.getValue());
        });
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET_OR_SELF;
    }
}
