package com.chaosbuffalo.mkultra.abilities.misc;

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
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
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

import javax.annotation.Nullable;

public class HolyFireAbility extends WindUpPulseAbility {
    private static final FormulaParameterKey BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_fire.damage.base"));
    private static final FormulaParameterKey PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_fire.damage.per_level"));
    private static final FormulaParameterKey MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_fire.damage.modifier_scaling"));
    private static final ResourceLocation PULSE_PARTICLES = MKUltra.id("holy_fire_detonate");
    private static final ResourceLocation WAIT_PARTICLES = MKUltra.id("holy_fire_wait");
    private static final ResourceLocation CASTING_PARTICLES = MKUltra.id("holy_fire_casting");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(BASE_PARAMETER, 4.0f)
                    .with(PER_LEVEL_PARAMETER, 1.0f)
                    .with(MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(BASE_PARAMETER, PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, MODIFIER_SCALING_PARAMETER));

    public HolyFireAbility() {
        super();
        waitParticles.setDefaultValue(WAIT_PARTICLES);
        pulseParticles.setDefaultValue(PULSE_PARTICLES);
        duration.setDefaultValue(GameConstants.TICKS_PER_SECOND * 3);
        addAttributes(formulaParameters, damage);
        addSkillAttribute(MKAttributes.EVOCATION);
        waitTime.setDefaultValue(GameConstants.TICKS_PER_SECOND * 2);
        waitTickRate.setDefaultValue(GameConstants.TICKS_PER_SECOND / 4);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        radius.setDefaultValue(1.6f);
        setCooldownSeconds(10);
        setManaCost(6);
        setEndpointOnWait.setDefaultValue(true);
        tickRate.setDefaultValue(GameConstants.TICKS_PER_SECOND / 3);
        setCastTime(GameConstants.TICKS_PER_SECOND / 2);
    }

    @Override
    public Vec3 getEndpointOffset(Vec3 pulsePos) {
        return new Vec3(0.0f, 3.0f, 0.0f);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.EVOCATION);
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.FireDamage.get(),
                damage.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(),
                NUMBER_FORMATTER.format(radius.value()),
                NUMBER_FORMATTER.format(convertDurationToSeconds(waitTime.value())),
                damageStr,
                NUMBER_FORMATTER.format(convertDurationToSeconds(tickRate.value())),
                NUMBER_FORMATTER.format(convertDurationToSeconds(duration.value())));
    }

    @Override
    public void setupEntityEffect(EntityEffectBuilder.PointEffectBuilder builder, IMKEntityData casterData, Vec3 position, AbilityContext context) {
        float level = context.getSkill(MKAttributes.EVOCATION);
        LivingEntity castingEntity = casterData.getEntity();
        MKEffectBuilder<?> damageEffect = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.FireDamage.get(),
                        damage.value(), formulaParameters.value())
                .ability(this)
                .skillLevel(level);
        MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_fire_8.value(), castingEntity.getSoundSource())
                .ability(this);

        builder.effect(damageEffect, getTargetContext())
                .delayedEffect(damageEffect, getTargetContext(), waitTime.value())
                .delayedEffect(sound, getTargetContext(), waitTime.value());
        SoundUtils.serverPlaySoundFromEntity(position.x(), position.y(), position.z(), MKUSounds.spell_holy_9.value(),
                castingEntity.getSoundSource(), 1.0f, 1.0f, castingEntity);
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
        return MKUSounds.hostile_casting_holy.value();
    }
}
