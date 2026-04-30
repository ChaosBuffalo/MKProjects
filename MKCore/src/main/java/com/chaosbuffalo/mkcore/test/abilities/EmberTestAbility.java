package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class EmberTestAbility extends MKAbility {
    private static final ResourceLocation TEST_PARTICLES = MKCore.id("beam_effect");
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKCore.id("test_ember.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKCore.id("test_ember.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKCore.id("test_ember.damage.modifier_scaling"));
    private static final FormulaParameterKey BURN_DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKCore.id("test_ember.burn_duration.base"));
    private static final FormulaParameterKey BURN_DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKCore.id("test_ember.burn_duration.per_level"));
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 6.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 0.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .with(BURN_DURATION_BASE_PARAMETER, 5.0f)
                    .with(BURN_DURATION_PER_LEVEL_PARAMETER, 0.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute burnDurationFormula = new FormulaAttribute("burnDurationFormula",
            AbilityFormula.skilledLinear(BURN_DURATION_BASE_PARAMETER, BURN_DURATION_PER_LEVEL_PARAMETER));

    public EmberTestAbility() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND / 2);
        setCooldownSeconds(4);
        setManaCost(6);
        addAttributes(formulaParameters, damage, burnDurationFormula);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = 0.0f;
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.FireDamage.get(),
                damage.value(), formulaParameters.value(), level);
        int burnTicks = getDebuffDuration(casterData, burnDurationFormula.value(), formulaParameters.value(), level);
        Component burn = Component.literal(NUMBER_FORMATTER.format(convertDurationToSeconds(burnTicks)))
                .withStyle(ChatFormatting.UNDERLINE);
        return Component.translatable(getDescriptionTranslationKey(), damageStr, burn);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 25.0f;
    }


    @Override
    public void continueCastClient(IMKEntityData casterData, int castTimeLeft, int totalTicks, @Nullable AbilityClientState clientState) {
        super.continueCastClient(casterData, castTimeLeft, totalTicks, clientState);
        LivingEntity castingEntity = casterData.getEntity();
        RandomSource rand = castingEntity.getRandom();
        castingEntity.getCommandSenderWorld().addParticle(ParticleTypes.LAVA,
                castingEntity.getX(), castingEntity.getY() + 0.5F, castingEntity.getZ(),
                rand.nextFloat() / 2.0F, 5.0E-5D, rand.nextFloat() / 2.0F);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            float level = 0.0f;
            BonusFormulaSpec boundDamage = damage.value().bindStrict(formulaParameters.value());
            FormulaContext damageContext = damageFormulaContext(casterData, CoreDamageTypes.FireDamage.get(), level).build();
            int burnDuration = getDebuffDuration(casterData, burnDurationFormula.value(), formulaParameters.value(), level);
            float amount = boundDamage.baseFormula().evaluate(damageContext);
            targetEntity.setRemainingFireTicks(burnDuration);
            targetEntity.hurt(MKDamageSource.causeAbilityDamage(targetEntity.level(), CoreDamageTypes.FireDamage.get(),
                    getAbilityId(), castingEntity, castingEntity)
                    .setDamageBonusFormula(boundDamage.bonusFormula()), amount);
//            SoundUtils.playSoundAtEntity(targetEntity, ModSounds.spell_fire_6);
            EntityEffectBuilder.LineEffectBuilder lineBuilder = EntityEffectBuilder.createLineEffectOnEntity(castingEntity, targetEntity,
                    new Vec3(targetEntity.getX(), targetEntity.getY(0.5), targetEntity.getZ()),
                    new Vec3(castingEntity.getX(), castingEntity.getY() + castingEntity.getEyeHeight(), castingEntity.getZ()));
            lineBuilder.setParticles(TEST_PARTICLES);
            lineBuilder.duration(40);
            lineBuilder.spawn();
            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                    ParticleTypes.FLAME,
                    ParticleEffects.CIRCLE_PILLAR_MOTION, 60, 10,
                    targetEntity.getX(), targetEntity.getY() + 1.0,
                    targetEntity.getZ(), 1.0, 1.0, 1.0, .25,
                    castingEntity.getLookAngle()), targetEntity);
        });
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET;
    }
}
