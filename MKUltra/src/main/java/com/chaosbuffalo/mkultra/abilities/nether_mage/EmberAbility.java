package com.chaosbuffalo.mkultra.abilities.nether_mage;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.StackingBonusFormulaSpec;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.StackingBonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.BurnEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class EmberAbility extends MKAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ember.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ember.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ember.damage.modifier_scaling"));
    private static final FormulaParameterKey BURN_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ember.burn.base"));
    private static final FormulaParameterKey BURN_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ember.burn.per_level"));
    private static final FormulaParameterKey BURN_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ember.burn.modifier_scaling"));
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ember.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ember.duration.per_level"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("ember_casting");
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("ember_cast");
    public static final ResourceLocation BURN_PARTICLES = MKUltra.id("burn_tick");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 8.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 3.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .with(BURN_BASE_PARAMETER, 2.0f)
                    .with(BURN_PER_LEVEL_PARAMETER, 1.0f)
                    .with(BURN_MODIFIER_SCALING_PARAMETER, 0.2f)
                    .with(DURATION_BASE_PARAMETER, 6.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 1.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final StackingBonusFormulaSpecAttribute burnDamage = new StackingBonusFormulaSpecAttribute("burnDamage",
            StackingBonusFormulaSpec.skilledBonusScaled(BURN_BASE_PARAMETER, BURN_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, BURN_MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final ResourceLocationAttribute burn_cast_particles = new ResourceLocationAttribute("burn_cast_particles", BURN_PARTICLES);


    public EmberAbility() {
        super();
        setCooldownSeconds(6);
        setManaCost(4);
        setCastTime(GameConstants.TICKS_PER_SECOND / 2);
        addAttributes(formulaParameters, damage, burnDamage, durationFormula,
                cast_particles, burn_cast_particles);
        addSkillAttribute(MKAttributes.EVOCATION);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.EVOCATION);
        Component valueStr = getDamageDescription(entityData,
                CoreDamageTypes.FireDamage.get(), damage.value(), formulaParameters.value(), level);
        Component dotStr = getDamageDescription(entityData,
                CoreDamageTypes.FireDamage.get(), burnDamage.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(), valueStr,
                NUMBER_FORMATTER.format(convertDurationToSeconds(
                        getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), level))),
                dotStr, NUMBER_FORMATTER.format(convertDurationToSeconds(BurnEffect.DEFAULT_PERIOD)));
    }

    public MKEffectBuilder<?> getBurnCast(IMKEntityData casterData, float level) {
        int burnTicks = getBuffDuration(casterData, durationFormula.value(), formulaParameters.value(), level);
        return BurnEffect.from(casterData.getEntity(), burnDamage.value(), formulaParameters.value(),
                        burn_cast_particles.getValue())
                .ability(this)
                .skillLevel(level)
                .timed(burnTicks);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 25.0f;
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
        return MKUSounds.casting_fire.value();
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_cast_7.value();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.EVOCATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            MKEffectBuilder<?> damageEffect = MKAbilityDamageEffect.from(entity, CoreDamageTypes.FireDamage.get(),
                            damage.value(), formulaParameters.value())
                    .ability(this)
                    .skillLevel(level);
            MKEffectBuilder<?> burn = getBurnCast(data, level)
                    .ability(this)
                    .skillLevel(level);

            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                targetData.getEffects().addEffect(damageEffect);
                targetData.getEffects().addEffect(burn);
            });


            SoundUtils.serverPlaySoundAtEntity(targetEntity, MKUSounds.spell_fire_6.value(), targetEntity.getSoundSource());
            MKParticles.spawnOffset(targetEntity, new Vec3(0.0, 1.0, 0.0), cast_particles.getValue());
        });
    }
}
