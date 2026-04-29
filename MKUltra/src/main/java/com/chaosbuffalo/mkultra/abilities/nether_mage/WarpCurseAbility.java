package com.chaosbuffalo.mkultra.abilities.nether_mage;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.WarpCurseEffect;
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

public class WarpCurseAbility extends MKAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("warp_curse.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("warp_curse.damage.per_level"));
    private static final FormulaParameterKey MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("warp_curse.damage.modifier_scaling"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("warp_curse_casting");
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("warp_curse_cast");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 4.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 2.0f)
                    .with(MODIFIER_SCALING_PARAMETER, 0.25f)
                    .build());
    protected final FormulaAttribute damageFormula = new FormulaAttribute("damageFormula",
            AbilityFormula.bonusScaledLinear(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, MODIFIER_SCALING_PARAMETER));
    protected final IntAttribute baseDuration = new IntAttribute("baseDuration", 4);
    protected final IntAttribute scaleDuration = new IntAttribute("scaleDuration", 2);
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public WarpCurseAbility() {
        super();
        setCooldownSeconds(16);
        setManaCost(8);
        setCastTime(GameConstants.TICKS_PER_SECOND + 10);
        addAttributes(formulaParameters, damageFormula, baseDuration, scaleDuration, cast_particles);
        addSkillAttribute(MKAttributes.ALTERATON);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ALTERATON);
        int duration = getBuffDuration(entityData, level, baseDuration.value(), scaleDuration.value());
        Component valueStr = getDamageDescription(entityData,
                CoreDamageTypes.ShadowDamage.get(), damageFormula.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(), valueStr,
                WarpCurseEffect.DEFAULT_PERIOD / 20,
                duration / 20);
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
        return MKUSounds.casting_shadow.value();
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_dark_15.value();
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            float level = context.getSkill(MKAttributes.ALTERATON);
            int duration = getBuffDuration(casterData, level, baseDuration.value(), scaleDuration.value());
            MKEffectBuilder<?> warpCast = WarpCurseEffect.from(castingEntity, damageFormula.value(),
                            formulaParameters.value(), cast_particles.getValue())
                    .ability(this)
                    .timed(duration)
                    .skillLevel(level);

            int oldAmp = Math.round(level);
            MKCore.getEntityData(targetEntity).ifPresent(targetData -> targetData.getEffects().addEffect(warpCast));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, oldAmp, false, false, true));

            SoundUtils.serverPlaySoundAtEntity(targetEntity, MKUSounds.spell_fire_5.value(), targetEntity.getSoundSource());
            MKParticles.spawnOffset(castingEntity, new Vec3(0.0, 1.0, 0.0), cast_particles.getValue());
        });
    }
}
