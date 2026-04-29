package com.chaosbuffalo.mkultra.abilities.enchanter;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.FormulaTextRenderer;
import com.chaosbuffalo.mkcore.formulas.FormulaTextStyle;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
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
import net.minecraft.world.phys.Vec3;

public class AlacrityAbility extends MKAbility {
    private static final FormulaParameterKey VALUE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("alacrity.value.base"));
    private static final FormulaParameterKey VALUE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("alacrity.value.per_level"));
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("alacrity.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("alacrity.duration.per_level"));
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("inspire_cast");

    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(VALUE_BASE_PARAMETER, 0.25f)
                    .with(VALUE_PER_LEVEL_PARAMETER, 0.03f)
                    .with(DURATION_BASE_PARAMETER, 600.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 60.0f)
                    .build());
    protected final FormulaAttribute valueFormula = new FormulaAttribute("valueFormula", AbilityFormula.skilledLinear(VALUE_BASE_PARAMETER, VALUE_PER_LEVEL_PARAMETER));
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public AlacrityAbility() {
        super();
        setCooldownSeconds(30);
        setManaCost(10);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addAttributes(formulaParameters, valueFormula, durationFormula, cast_particles);
        addSkillAttribute(MKAttributes.ENCHANTMENT);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ENCHANTMENT);
        FormulaContext formulaContext = baseFormulaContext(entityData, level).build();
        int duration = getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), formulaContext);
        String value = FormulaTextRenderer.format(valueFormula.value(), formulaParameters.value(), formulaContext,
                FormulaTextStyle.PERCENT);
        return Component.translatable(getDescriptionTranslationKey(), value, convertDurationToSeconds(duration));
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 20.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET;
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_cast_12.value();
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            float level = context.getSkill(MKAttributes.ENCHANTMENT);
            int duration = getBuffDuration(casterData, durationFormula.value(), formulaParameters.value(), level);
            MKEffectBuilder<?> effect = MKUEffects.ATTACK_SPEED_HASTE.get()
                    .from(castingEntity,
                            AbilityFormula.param(VALUE_BASE_PARAMETER),
                            AbilityFormula.multiply(
                                    AbilityFormula.param(VALUE_PER_LEVEL_PARAMETER),
                                    AbilityFormula.context(FormulaContextKey.SKILL_LEVEL)
                            ),
                            formulaParameters.value())
                    .ability(this)
                    .skillLevel(level)
                    .timed(duration);
            MKCore.getEntityData(targetEntity).ifPresent(targetData -> targetData.getEffects().addEffect(effect));
            SoundUtils.serverPlaySoundAtEntity(targetEntity, getSpellCompleteSoundEvent(), targetEntity.getSoundSource());
            MKParticles.spawnOffset(targetEntity, new Vec3(0.0, 1.0, 0.0), cast_particles.getValue());
        });
    }
}
