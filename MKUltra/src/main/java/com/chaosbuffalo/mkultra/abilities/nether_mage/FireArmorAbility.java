package com.chaosbuffalo.mkultra.abilities.nether_mage;

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
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUEffects;
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

import javax.annotation.Nullable;

public class FireArmorAbility extends MKAbility {
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("fire_armor.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("fire_armor.duration.per_level"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("fire_armor_casting");
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("fire_armor_cast");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DURATION_BASE_PARAMETER, 60.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 15.0f)
                    .build());
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public FireArmorAbility() {
        super();
        setCooldownSeconds(150);
        setManaCost(12);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addSkillAttribute(MKAttributes.ABJURATION);
        addAttributes(formulaParameters, durationFormula, cast_particles);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ABJURATION);
        float amount = MKUEffects.FIRE_ARMOR.get().getPerLevel() * (level + 1) * 100.0f;
        int duration = getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), level)
                / GameConstants.TICKS_PER_SECOND;
        return Component.translatable(getDescriptionTranslationKey(), amount,
                CoreDamageTypes.FireDamage.get().getFormattedDisplayName(), duration);
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_buff_5.value();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.ABJURATION);
        int duration = getBuffDuration(data, durationFormula.value(), formulaParameters.value(), level);

        int oldAmp = Math.round(level);
        MobEffectInstance fireResist = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, oldAmp, false, false, true);
        MobEffectInstance absorb = new MobEffectInstance(MobEffects.ABSORPTION, duration, oldAmp, false, false, true);

        MKEffectBuilder<?> particles = MKParticleEffect.from(entity, cast_particles.getValue(), true, new Vec3(0.0, 1.0, 0.0))
                .ability(this);

        MKEffectBuilder<?> sound = SoundEffect.from(entity, MKUSounds.spell_fire_2.value(), entity.getSoundSource())
                .ability(this);

        MKEffectBuilder<?> fireArmor = MKUEffects.FIRE_ARMOR.get().builder(entity)
                .ability(this)
                .timed(duration)
                .skillLevel(level);

        AreaEffectBuilder.createOnCaster(entity)
                .effect(fireResist, getTargetContext())
                .effect(absorb, getTargetContext())
                .effect(sound, getTargetContext())
                .effect(fireArmor, getTargetContext())
                .effect(particles, getTargetContext())
                .disableParticle()
                .instant()
                .color(16762905)
                .radius(getDistance(entity), true)
                .spawn();
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }
}
