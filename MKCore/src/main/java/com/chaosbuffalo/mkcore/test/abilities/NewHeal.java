package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.formulas.StackingBonusFormulaSpec;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.StackingBonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class NewHeal extends MKAbility {
    private static final FormulaParameterKey HEAL_BASE_PARAMETER =
            FormulaParameterKey.of(MKCore.id("test_new_heal.heal.base"));
    private static final FormulaParameterKey HEAL_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKCore.id("test_new_heal.heal.per_level"));
    private static final FormulaParameterKey HEAL_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKCore.id("test_new_heal.heal.modifier_scaling"));
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(HEAL_BASE_PARAMETER, 5.0f)
                    .with(HEAL_PER_LEVEL_PARAMETER, 5.0f)
                    .with(HEAL_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final StackingBonusFormulaSpecAttribute healing = new StackingBonusFormulaSpecAttribute("healing",
            StackingBonusFormulaSpec.skilledBonusScaled(HEAL_BASE_PARAMETER, HEAL_PER_LEVEL_PARAMETER,
                    FormulaContextKey.HEAL_BONUS, HEAL_MODIFIER_SCALING_PARAMETER));

    public NewHeal() {
        super();
        setCooldownSeconds(6);
        setManaCost(4);
        setCastTime(GameConstants.TICKS_PER_SECOND / 4);
        addAttributes(formulaParameters, healing);
        addSkillAttribute(MKAttributes.RESTORATION);

    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.RESTORATION);
        Component valueStr = getHealDescription(casterData, healing.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(), valueStr);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET_OR_SELF;
    }

    @Override
    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return super.isValidTarget(caster, target) || MKHealing.wouldHealHurtUndead(caster, target);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        float level = context.getSkill(MKAttributes.RESTORATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                MKEffectBuilder<?> heal = MKTestEffects.NEW_HEAL.get().builder(castingEntity)
                        .ability(this)
                        .state(s -> s.setParameterizedHealingFormula(healing.value(), formulaParameters.value()))
                        .timed(200)
                        .skillLevel(level)
                        .periodic(40);
                targetData.getEffects().addEffect(heal);
            });

            //            SoundUtils.playSoundAtEntity(targetEntity, ModSounds.spell_heal_3);
            Vec3 lookVec = castingEntity.getLookAngle();
            PacketHandler.sendToTrackingAndSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.HAPPY_VILLAGER,
                            ParticleEffects.SPHERE_MOTION, 50, 10,
                            targetEntity.getX(),
                            targetEntity.getY() + 1.0f,
                            targetEntity.getZ(),
                            1.0, 1.0, 1.0, 1.5, lookVec),
                    targetEntity);
        });
    }
}
