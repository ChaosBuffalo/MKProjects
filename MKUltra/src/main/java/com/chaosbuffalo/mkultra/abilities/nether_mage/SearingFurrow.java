package com.chaosbuffalo.mkultra.abilities.nether_mage;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.MeleeUseCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SearingFurrow extends MKAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("searing_furrow.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("searing_furrow.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("searing_furrow.damage.modifier_scaling"));
    private static final ResourceLocation CASTING_PARTICLES = MKUltra.id("flame_wave_casting");
    private static final ResourceLocation LINE_PARTICLES = MKUltra.id("searing_furrow_particles");
    private static final ResourceLocation CAST_SLAM_CATEGORY = MKUltra.id("cast_slam");

    private final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 8.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 4.2f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    private final FormulaAttribute damageFormula = new FormulaAttribute("damageFormula",
            AbilityFormula.bonusScaledLinear(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    private final IntAttribute lineLength = new IntAttribute("lineLength", 8);
    private final IntAttribute lingerDuration = new IntAttribute("lingerDuration", GameConstants.TICKS_PER_SECOND * 30);
    private final IntAttribute tickRate = new IntAttribute("tickRate", 10);
    private final ResourceLocationAttribute lineParticles = new ResourceLocationAttribute("line_particles", LINE_PARTICLES);

    public SearingFurrow() {
        super();
        setCooldownSeconds(5);
        setManaCost(8);
        setCastTime(GameConstants.TICKS_PER_SECOND * 8);
        setUseCondition(new MeleeUseCondition(this));
        addSkillAttribute(MKAttributes.EVOCATION);
        addAttributes(formulaParameters, damageFormula, lineLength, lingerDuration, tickRate, lineParticles);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        castAnimationCategory.setDefaultValue(CAST_SLAM_CATEGORY);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.EVOCATION);
        Component damage = getDamageDescription(casterData, com.chaosbuffalo.mkcore.init.CoreDamageTypes.FireDamage.get(),
                damageFormula.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(),
                INTEGER_FORMATTER.format(lineLength.value()),
                damage,
                NUMBER_FORMATTER.format(convertDurationToSeconds(tickRate.value())),
                NUMBER_FORMATTER.format(convertDurationToSeconds(lingerDuration.value())));
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return lineLength.value();
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.hostile_casting_fire.value();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_fire_7.value();
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        float level = context.getSkill(MKAttributes.EVOCATION);
        Vec3 direction = getHorizontalLookDirection(castingEntity);
        BlockPos startPos = findGroundAnchor(castingEntity, BlockPos.containing(castingEntity.position().add(direction.scale(0.85))));
        Vec3 start = startPos != null ? Vec3.atBottomCenterOf(startPos) : castingEntity.position();
        Vec3 end = start.add(direction.scale(lineLength.value() - 1.0));
        EntityEffectBuilder.createLineEffect(castingEntity, start, end)
                .effect(MKAbilityDamageEffect.from(castingEntity, com.chaosbuffalo.mkcore.init.CoreDamageTypes.FireDamage.get(),
                                damageFormula.value(), formulaParameters.value())
                        .ability(this)
                        .skillLevel(level), getTargetContext())
                .duration(lingerDuration.value())
                .waitTime(0)
                .tickRate(tickRate.value())
                .setParticles(new BaseEffectEntity.ParticleDisplay(lineParticles.getValue(),
                        Math.max(2, tickRate.value() / 2), BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                .spawn();
    }

    private Vec3 getHorizontalLookDirection(LivingEntity caster) {
        Vec3 direction = caster.getLookAngle();
        direction = new Vec3(direction.x, 0.0, direction.z);
        if (direction.lengthSqr() < 1.0e-4) {
            direction = Vec3.directionFromRotation(0.0f, caster.getYRot());
            direction = new Vec3(direction.x, 0.0, direction.z);
        }
        return direction.normalize();
    }

    @Nullable
    private BlockPos findGroundAnchor(LivingEntity caster, BlockPos guess) {
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos pos = guess.offset(0, dy, 0);
            if (!caster.level().getBlockState(pos).isAir()) {
                continue;
            }
            BlockPos below = pos.below();
            if (caster.level().getBlockState(below).isFaceSturdy(caster.level(), below, Direction.UP)) {
                return pos;
            }
        }
        return null;
    }
}
