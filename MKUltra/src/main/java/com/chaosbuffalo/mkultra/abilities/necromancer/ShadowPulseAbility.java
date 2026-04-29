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
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
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
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("shadow_pulse.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("shadow_pulse.damage.per_level"));
    private static final FormulaParameterKey DETONATE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("shadow_pulse.detonate.base"));
    private static final FormulaParameterKey DETONATE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("shadow_pulse.detonate.per_level"));
    private static final FormulaParameterKey MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("shadow_pulse.damage.modifier_scaling"));
    private static final ResourceLocation PULSE_PARTICLES = MKUltra.id("shadow_pulse_detonate");
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("shadow_bolt_casting");
    private static final ResourceLocation WAIT_PARTICLES = MKUltra.id("shadow_pulse_wait");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 1.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 0.25f)
                    .with(DETONATE_BASE_PARAMETER, 5.0f)
                    .with(DETONATE_PER_LEVEL_PARAMETER, 5.0f)
                    .with(MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, MODIFIER_SCALING_PARAMETER));
    protected final FloatAttribute baseGravity = new FloatAttribute("baseGravity", 0.25f);
    protected final FloatAttribute scaleGravity = new FloatAttribute("scaleGravity", 0.0f);
    protected final BonusFormulaSpecAttribute detonateDamage = new BonusFormulaSpecAttribute("detonateDamage",
            BonusFormulaSpec.skilledBonusScaled(DETONATE_BASE_PARAMETER, DETONATE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, MODIFIER_SCALING_PARAMETER));


    public ShadowPulseAbility() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND);
        setCooldownSeconds(10);
        setManaCost(5);
        addSkillAttribute(MKAttributes.CONJURATION);
        pulseParticles.setDefaultValue(PULSE_PARTICLES);
        waitParticles.setDefaultValue(WAIT_PARTICLES);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        addAttributes(formulaParameters, damage, baseGravity, scaleGravity, detonateDamage);
    }

    @Override
    public void setupEntityEffect(EntityEffectBuilder.PointEffectBuilder builder, IMKEntityData casterData, Vec3 position, AbilityContext context) {
        float level = context.getSkill(MKAttributes.CONJURATION);
        LivingEntity castingEntity = casterData.getEntity();
        MKEffectBuilder<?> damageEffect = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.ShadowDamage.get(),
                        damage.value(), formulaParameters.value())
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> pull = PullEffect.from(castingEntity, baseGravity.value(), scaleGravity.value(), position)
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_shadow_10.value(), castingEntity.getSoundSource())
                .ability(this);
        MKEffectBuilder<?> detonateDamageEffect = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.ShadowDamage.get(),
                        detonateDamage.value(), formulaParameters.value())
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> detonateSound = SoundEffect.from(castingEntity, MKUSounds.spell_shadow_9.value(), castingEntity.getSoundSource())
                .ability(this);
        builder.effect(damageEffect, getTargetContext())
                .effect(sound, getTargetContext())
                .effect(pull, getTargetContext())
                .delayedEffect(detonateSound, getTargetContext(), duration.value())
                .delayedEffect(detonateDamageEffect, getTargetContext(), duration.value());
        SoundUtils.serverPlaySoundFromEntity(position.x(), position.y(), position.z(), MKUSounds.spell_dark_13.value(),
                castingEntity.getSoundSource(), 1.0f, 1.0f, castingEntity);
    }


    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.CONJURATION);
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.ShadowDamage.get(),
                damage.value(), formulaParameters.value(), level);
        Component detonateStr = getDamageDescription(casterData, CoreDamageTypes.ShadowDamage.get(),
                detonateDamage.value(), formulaParameters.value(), level);
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
