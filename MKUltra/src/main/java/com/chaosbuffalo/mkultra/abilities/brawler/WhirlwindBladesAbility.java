package com.chaosbuffalo.mkultra.abilities.brawler;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.MeleeUseCondition;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.combat.AbilityMeleeAttackHelper;
import com.chaosbuffalo.mkcore.core.combat.MeleeAttackVisualHelper;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.AbilityMeleeDamageEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;


public class WhirlwindBladesAbility extends MKAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("whirlwind_blades.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("whirlwind_blades.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("whirlwind_blades.damage.modifier_scaling"));
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("whirlwind_blades_pulse");
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 1.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 0.5f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 0.15f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final FloatAttribute perTick = new FloatAttribute("perTick", 0.15f);
    protected final FloatAttribute offHandScaleModifier = new FloatAttribute("offHandScaleModifier", 0.6f);
    protected final IntAttribute tickRate = new IntAttribute("tickRate", 10);

    public WhirlwindBladesAbility() {
        super();
        addAttributes(cast_particles, formulaParameters, damage, perTick, offHandScaleModifier, tickRate);
        setCastTime(GameConstants.TICKS_PER_SECOND * 3);
        setCooldownSeconds(20);
        setManaCost(6);
        addSkillAttribute(MKAttributes.PANKRATION);
        setUseCondition(new MeleeUseCondition(this));
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Ultimate;
    }

    @Override
    public boolean canApplyCastingSpeedModifier() {
        return false;
    }

    @Override
    public boolean isInterruptedBy(IMKEntityData targetData, CastInterruptReason reason) {
        return false;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.PANKRATION);
        Component bonusDamage = getDamageDescription(entityData,
                CoreDamageTypes.MeleeDamage.get(), damage.value(), formulaParameters.value(), level);
        float periodSeconds = ((float)tickRate.value()) / GameConstants.TICKS_PER_SECOND;
        int castSeconds = getCastTime(entityData) / GameConstants.TICKS_PER_SECOND;
        int numberOfCasts = Math.round(castSeconds / periodSeconds);
        return Component.translatable(getDescriptionTranslationKey(), NUMBER_FORMATTER.format(periodSeconds), INTEGER_FORMATTER.format(castSeconds),
                PERCENT_FORMATTER.format(perTick.value()),
                PERCENT_FORMATTER.format(numberOfCasts * perTick.value()),
                PERCENT_FORMATTER.format(offHandScaleModifier.value()),
                bonusDamage);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return getMeleeReach(entity);
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.spell_whirlwind_1.value();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public void continueCast(IMKEntityData casterData, int castTimeLeft, int totalTicks, AbilityContext context) {
        super.continueCast(casterData, castTimeLeft, totalTicks, context);
        LivingEntity castingEntity = casterData.getEntity();
        int tickSpeed = tickRate.value();
        if (castTimeLeft % tickSpeed == 0) {
            float level = context.getSkill(MKAttributes.PANKRATION);
            int count = (totalTicks - castTimeLeft) / tickSpeed;
            float swingDamageScale = count * perTick.value();
            List<InteractionHand> hands = AbilityMeleeAttackHelper.resolveAttackHands(castingEntity);
            MKEffectBuilder<?> particles = MKParticleEffect.from(castingEntity,
                            cast_particles.getValue(), true, new Vec3(0.0, 1.0, 0.0))
                    .ability(this);
            MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_shadow_2.value(), castingEntity.getSoundSource())
                    .ability(this);

            AreaEffectBuilder builder = AreaEffectBuilder.createOnCaster(castingEntity)
                    .effect(particles, TargetingContexts.SELF)
                    .effect(sound, getTargetContext());
            for (int i = 0; i < hands.size(); i++) {
                InteractionHand hand = hands.get(i);
                int swingDelay = i * 3;
                MeleeAttackVisualHelper.startVisualAttack(castingEntity, hand, new int[]{swingDelay}, new int[]{6});
                float handSwingDamageScale = hand == InteractionHand.OFF_HAND ?
                        swingDamageScale * offHandScaleModifier.value() : swingDamageScale;
                MKEffectBuilder<?> damageEffect = AbilityMeleeDamageEffect.from(castingEntity, hand, handSwingDamageScale,
                                damage.value(), formulaParameters.value())
                        .ability(this)
                        .skillLevel(level);
                builder.effect(damageEffect, getTargetContext());
            }
            builder.instant()
                    .color(16409620)
                    .radius(getDistance(castingEntity), true)
                    .disableParticle()
                    .spawn();
        }
    }
}
