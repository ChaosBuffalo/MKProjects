package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.combat.AbilityMeleeAttackHelper;
import com.chaosbuffalo.mkcore.core.combat.MeleeAttackVisualHelper;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.AbilityMeleeDamageEffect;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.serialization.attributes.EnumAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.effects.SeverTendonEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkweapons.init.MKWeaponsParticles;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SeverTendonAbility extends MKAbility {
    private static final FormulaParameterKey HIT_DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("sever_tendon.hit_damage.base"));
    private static final FormulaParameterKey HIT_DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("sever_tendon.hit_damage.per_level"));
    private static final FormulaParameterKey HIT_DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("sever_tendon.hit_damage.modifier_scaling"));
    private static final FormulaParameterKey BLEED_DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("sever_tendon.bleed_damage.base"));
    private static final FormulaParameterKey BLEED_DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("sever_tendon.bleed_damage.per_level"));
    private static final FormulaParameterKey BLEED_DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("sever_tendon.bleed_damage.modifier_scaling"));
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("sever_tendon.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("sever_tendon.duration.per_level"));
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(HIT_DAMAGE_BASE_PARAMETER, 1.0f)
                    .with(HIT_DAMAGE_PER_LEVEL_PARAMETER, 0.25f)
                    .with(HIT_DAMAGE_MODIFIER_SCALING_PARAMETER, 0.1f)
                    .with(BLEED_DAMAGE_BASE_PARAMETER, 1.0f)
                    .with(BLEED_DAMAGE_PER_LEVEL_PARAMETER, 1.0f)
                    .with(BLEED_DAMAGE_MODIFIER_SCALING_PARAMETER, 0.1f)
                    .with(DURATION_BASE_PARAMETER, 4.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 1.0f)
                    .build());
    protected final FormulaAttribute hitDamageFormula = new FormulaAttribute("hitDamageFormula",
            AbilityFormula.bonusScaledLinear(HIT_DAMAGE_BASE_PARAMETER, HIT_DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, HIT_DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute bleedDamageFormula = new FormulaAttribute("bleedDamageFormula",
            AbilityFormula.bonusScaledLinear(BLEED_DAMAGE_BASE_PARAMETER, BLEED_DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, BLEED_DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final EnumAttribute<InteractionHand> attackHand = new EnumAttribute<>("attackHand", InteractionHand.MAIN_HAND, InteractionHand.class);

    public SeverTendonAbility() {
        super();
        setCooldownSeconds(12);
        setManaCost(5);
        setCastTime(0);
        addAttributes(formulaParameters, hitDamageFormula, bleedDamageFormula, durationFormula, attackHand);
        addSkillAttribute(MKAttributes.PANKRATION);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.PANKRATION);
        Component valueStr = getDamageDescription(entityData,
                CoreDamageTypes.MeleeDamage.get(), hitDamageFormula.value(), formulaParameters.value(), level);
        Component dotStr = getDamageDescription(entityData,
                CoreDamageTypes.BleedDamage.get(), bleedDamageFormula.value(), formulaParameters.value(), level);
        int periodSeconds = SeverTendonEffect.DEFAULT_PERIOD / 20;
        return Component.translatable(getDescriptionTranslationKey(), valueStr,
                getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), level) / 20,
                dotStr, periodSeconds, (level + 1) * .05f * 100.0f);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return getMeleeReach(entity);
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
        return null;
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_magic_whoosh_4.value();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.PANKRATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            InteractionHand hand = AbilityMeleeAttackHelper.resolveHand(entity, attackHand.getValue());
            MeleeAttackVisualHelper.startVisualAttack(entity, hand, new int[]{0}, new int[]{6});
            MKEffectBuilder<?> damage = AbilityMeleeDamageEffect.from(entity, hand,
                            hitDamageFormula.value(), formulaParameters.value())
                    .ability(this)
                    .skillLevel(level);


            int dur = getBuffDuration(data, durationFormula.value(), formulaParameters.value(), level);
            MKEffectBuilder<?> severTendon = SeverTendonEffect.from(entity, bleedDamageFormula.value(), formulaParameters.value())
                    .ability(this)
                    .timed(dur)
                    .skillLevel(level);

            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                targetData.getEffects().addEffect(damage);
                targetData.getEffects().addEffect(severTendon);
            });

            SoundUtils.serverPlaySoundAtEntity(targetEntity, MKUSounds.spell_punch_6.value(), targetEntity.getSoundSource());
            Vec3 lookVec = entity.getLookAngle();
            PacketHandler.sendToTrackingAndSelf(
                    new ParticleEffectSpawnPacket(
                            MKWeaponsParticles.DRIPPING_BLOOD.get(),
                            ParticleEffects.CIRCLE_MOTION, 25, 10,
                            targetEntity.getX(), targetEntity.getY() + 1.0f,
                            targetEntity.getZ(), 0.75, 0.75, 0.75, 1.0,
                            lookVec), targetEntity);
        });
    }
}
