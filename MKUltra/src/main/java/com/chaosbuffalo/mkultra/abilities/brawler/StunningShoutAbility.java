package com.chaosbuffalo.mkultra.abilities.brawler;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class StunningShoutAbility extends MKAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("stunning_shout.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("stunning_shout.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("stunning_shout.damage.modifier_scaling"));
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("stunning_shout.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("stunning_shout.duration.per_level"));
    public static final ResourceLocation TICK_PARTICLES = MKUltra.id("stunning_shout_tick");
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("stunning_shout_cast");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 4.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 2.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .with(DURATION_BASE_PARAMETER, 1.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 1.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final ResourceLocationAttribute tick_particles = new ResourceLocationAttribute("tick_particles", TICK_PARTICLES);

    public StunningShoutAbility() {
        super();
        setCooldownSeconds(12);
        setManaCost(4);
        addAttributes(formulaParameters, damage, durationFormula, cast_particles, tick_particles);
        addSkillAttribute(MKAttributes.PNEUMA);
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Basic;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.LINE;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.PNEUMA);
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.BleedDamage.get(),
                damage.value(), formulaParameters.value(), level);
        int dur = getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), level)
                / GameConstants.TICKS_PER_SECOND;
        return Component.translatable(getDescriptionTranslationKey(), INTEGER_FORMATTER.format(dur), damageStr);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_shout_1.value();
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        float level = context.getSkill(MKAttributes.PNEUMA);


        MKEffectBuilder<?> damageEffect = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.BleedDamage.get(),
                        damage.value(), formulaParameters.value())
                .skillLevel(level)
                .ability(this);
        MKEffectBuilder<?> stun = StunEffect.from(castingEntity).ability(this).skillLevel(level).timed(
                getBuffDuration(casterData, durationFormula.value(), formulaParameters.value(), level));
        MKEffectBuilder<?> particles = MKParticleEffect.from(castingEntity, tick_particles.getValue(),
                        false, new Vec3(0.0, 1.5, 0.0))
                .ability(this);

        Vec3 look = castingEntity.getLookAngle().scale(getDistance(castingEntity));
        Vec3 from = castingEntity.position().add(0, castingEntity.getEyeHeight(), 0);
        Vec3 to = from.add(look);
        List<LivingEntity> entityHit = TargetUtil.getTargetsInLine(castingEntity, from, to, 1.0f, this::isValidTarget);

        for (LivingEntity entHit : entityHit) {
            MKCore.getEntityData(entHit).ifPresent(targetData -> {
                targetData.getEffects().addEffect(damageEffect);
                targetData.getEffects().addEffect(stun);
                targetData.getEffects().addEffect(particles);
            });
        }

        MKParticles.spawn(castingEntity, from, CAST_PARTICLES, spawn -> {
            spawn.addLoc(to);
        });
    }
}
