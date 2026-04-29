package com.chaosbuffalo.mkultra.abilities.brawler;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.HealCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.FuriousBroodingEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class FuriousBroodingAbility extends MKAbility {
    public static final ResourceLocation TICK_PARTICLES = MKUltra.id("furious_brooding_pulse");
    private static final FormulaParameterKey HEAL_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("furious_brooding.heal.base"));
    private static final FormulaParameterKey HEAL_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("furious_brooding.heal.per_level"));
    private static final FormulaParameterKey MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("furious_brooding.heal.modifier_scaling"));
    protected final ResourceLocationAttribute tick_particles = new ResourceLocationAttribute("cast_particles", TICK_PARTICLES);
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(HEAL_BASE_PARAMETER, 2.0f)
                    .with(HEAL_PER_LEVEL_PARAMETER, 1.0f)
                    .with(MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final FormulaAttribute healingFormula = new FormulaAttribute("healingFormula",
            AbilityFormula.bonusScaledLinear(HEAL_BASE_PARAMETER, HEAL_PER_LEVEL_PARAMETER,
                    FormulaContextKey.HEAL_BONUS, MODIFIER_SCALING_PARAMETER));
    protected final IntAttribute baseDuration = new IntAttribute("baseDuration", 6);
    protected final IntAttribute scaleDuration = new IntAttribute("scaleDuration", 5);

    public FuriousBroodingAbility() {
        super();
        setCooldownSeconds(18);
        setManaCost(6);
        addAttributes(tick_particles, formulaParameters, healingFormula, baseDuration, scaleDuration);
        addSkillAttribute(MKAttributes.PNEUMA);
        setUseCondition(new HealCondition(this, 0.8f).setSelfOnly(true));
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SELF;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.PNEUMA);
        Component damageStr = getHealDescription(casterData, healingFormula.value(), formulaParameters.value(), level);
        int duration = getBuffDuration(casterData, level, baseDuration.value(), scaleDuration.value()) / GameConstants.TICKS_PER_SECOND;
        float speedReduction = -0.6f + 0.05f * level;
        return Component.translatable(getDescriptionTranslationKey(), damageStr, INTEGER_FORMATTER.format(duration), PERCENT_FORMATTER.format(speedReduction));
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_negative_effect_7.value();
    }

    public MKEffectBuilder<?> createFuriousBroodingEffect(IMKEntityData casterData, float level) {
        int duration = getBuffDuration(casterData, level, baseDuration.value(), scaleDuration.value());
        return FuriousBroodingEffect.from(casterData.getEntity(), healingFormula.value(),
                        formulaParameters.value(), tick_particles.getValue())
                .ability(this)
                .skillLevel(level)
                .timed(duration);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        float level = context.getSkill(MKAttributes.PNEUMA);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            MKEffectBuilder<?> heal = createFuriousBroodingEffect(casterData, level).ability(this);
            MKCore.getEntityData(targetEntity).ifPresent(targetData -> targetData.getEffects().addEffect(heal));

            MKParticles.spawnOffset(targetEntity, new Vec3(0.0, 1.0, 0.0), tick_particles.getValue());
        });
    }
}
