package com.chaosbuffalo.mkultra.abilities.cleric;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.formulas.*;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SmiteAbility extends MKAbility {
    public static final FormulaParameterKey BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("smite.damage.base"));
    public static final FormulaParameterKey PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("smite.damage.per_level"));
    public static final FormulaParameterKey MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("smite.damage.modifier_scaling"));
    public static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("smite.duration.base"));
    public static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("smite.duration.per_level"));
    protected final ResourceLocation CASTING_PARTICLES = MKUltra.id("smite_casting");
    protected final ResourceLocation CAST_PARTICLES = MKUltra.id("smite_cast");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(BASE_PARAMETER, 5.0f)
                    .with(PER_LEVEL_PARAMETER, 5.0f)
                    .with(MODIFIER_SCALING_PARAMETER, 1.0f)
                    .with(DURATION_BASE_PARAMETER, 1.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 1.0f)
                    .build());
    protected final FormulaAttribute damageFormula = new FormulaAttribute("damageFormula",
            AbilityFormula.bonusScaledLinear(BASE_PARAMETER, PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public SmiteAbility() {
        super();
        setCooldownSeconds(6);
        setManaCost(5);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addAttributes(formulaParameters, damageFormula, durationFormula, cast_particles);
        addSkillAttribute(MKAttributes.EVOCATION);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.EVOCATION);
        Component valueStr = getDamageDescription(entityData,
                CoreDamageTypes.HolyDamage.get(), damageFormula.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(), valueStr,
                convertDurationToSeconds(getFormulaDuration(entityData, durationFormula.value(), formulaParameters.value(), level)));
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 15.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_shadow.value();
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_magic_whoosh_2.value();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.EVOCATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {

            MKEffectBuilder<?> damage = MKAbilityDamageEffect.from(entity, CoreDamageTypes.HolyDamage.get(),
                            damageFormula.value(), formulaParameters.value())
                    .ability(this)
                    .skillLevel(level);

            MKEffectBuilder<?> stun = StunEffect.from(entity)
                    .ability(this)
                    .timed(getFormulaDuration(data, durationFormula.value(), formulaParameters.value(), level))
                    .skillLevel(level);

            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                targetData.getEffects().addEffect(damage);
                targetData.getEffects().addEffect(stun);
            });
            SoundUtils.serverPlaySoundAtEntity(targetEntity, MKUSounds.spell_holy_2.value(), targetEntity.getSoundSource());
            MKParticles.spawnOffset(targetEntity, new Vec3(0.0, 1.0, 0.0), cast_particles.getValue());
        });
    }
}
