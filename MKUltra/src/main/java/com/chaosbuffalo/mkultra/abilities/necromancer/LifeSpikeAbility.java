package com.chaosbuffalo.mkultra.abilities.necromancer;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.FormulaTextRenderer;
import com.chaosbuffalo.mkcore.formulas.FormulaTextStyle;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.VampiricDamageEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class LifeSpikeAbility extends MKAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("life_spike.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("life_spike.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("life_spike.damage.modifier_scaling"));
    private static final FormulaParameterKey HEAL_DAMAGE_SCALE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("life_spike.heal.damage_scale"));
    private static final FormulaParameterKey HEAL_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("life_spike.heal.modifier_scaling"));
    protected final ResourceLocation CASTING_PARTICLES = MKUltra.id("lifespike_casting");
    protected final ResourceLocation CAST_PARTICLES = MKUltra.id("lifespike_cast");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 10.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 2.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .with(HEAL_DAMAGE_SCALE_PARAMETER, 1.0f)
                    .with(HEAL_MODIFIER_SCALING_PARAMETER, 0.5f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public LifeSpikeAbility() {
        super();
        setCooldownSeconds(30);
        setManaCost(8);
        setCastTime(GameConstants.TICKS_PER_SECOND * 2);
        addAttributes(formulaParameters, damage, cast_particles);
        addSkillAttribute(MKAttributes.NECROMANCY);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    protected BonusFormulaSpec getBoundDamageSpec() {
        return damage.value().bindStrict(formulaParameters.value());
    }

    protected float getHealDamageScale() {
        return formulaParameters.value().get(HEAL_DAMAGE_SCALE_PARAMETER);
    }

    protected float getHealModifierScaling() {
        return formulaParameters.value().get(HEAL_MODIFIER_SCALING_PARAMETER);
    }

    protected Component getLifeStealDescription(IMKEntityData entityData, float skillLevel) {
        BonusFormulaSpec damageSpec = getBoundDamageSpec();
        AbilityFormula baseHealFormula = AbilityFormula.multiply(
                AbilityFormula.constant(getHealDamageScale()),
                damageSpec.baseFormula()
        );
        AbilityFormula bonusHealFormula = AbilityFormula.add(
                AbilityFormula.multiply(
                        AbilityFormula.constant(getHealDamageScale()),
                        damageSpec.bonusFormula()
                ),
                AbilityFormula.multiply(
                        AbilityFormula.constant(getHealModifierScaling()),
                        AbilityFormula.context(FormulaContextKey.HEAL_BONUS)
                )
        );
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(skillLevel)
                .withDamageBonus(entityData.getStats().getDamageTypeBonus(CoreDamageTypes.ShadowDamage.get()))
                .withHealBonus(entityData.getStats().getHealBonus())
                .build();
        return FormulaTextRenderer.render(baseHealFormula, bonusHealFormula, context, FormulaTextStyle.HEAL);
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Ultimate;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.NECROMANCY);
        Component valueStr = getDamageDescription(entityData,
                CoreDamageTypes.ShadowDamage.get(), damage.value(), formulaParameters.value(), level);
        Component healStr = getLifeStealDescription(entityData, level);
        return Component.translatable(getDescriptionTranslationKey(), valueStr, healStr);
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
        return MKUSounds.spell_magic_whoosh_4.value();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.NECROMANCY);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {

            MKEffectBuilder<?> damageEffect = VampiricDamageEffect.from(entity, CoreDamageTypes.ShadowDamage.get(),
                            damage.value(), formulaParameters.value(), getHealDamageScale(), getHealModifierScaling())
                    .ability(this)
                    .skillLevel(level);


            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                targetData.getEffects().addEffect(damageEffect);
            });

            SoundUtils.serverPlaySoundAtEntity(targetEntity, MKUSounds.spell_shadow_6.value(), targetEntity.getSoundSource());
            MKParticles.spawnOffset(targetEntity, new Vec3(0.0, 1.75, 0.0), cast_particles.getValue());
        });
    }
}
