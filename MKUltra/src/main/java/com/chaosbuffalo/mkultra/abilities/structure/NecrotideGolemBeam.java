package com.chaosbuffalo.mkultra.abilities.structure;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.MeleeUseCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.StringAttribute;
import com.chaosbuffalo.mknpc.abilities.StructureAbility;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.level.block.LanternBlock.HANGING;

public class NecrotideGolemBeam extends StructureAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("necrotide_golem_beam.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("necrotide_golem_beam.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("necrotide_golem_beam.damage.modifier_scaling"));

    private static final ResourceLocation PULSE_PARTICLES = MKUltra.id("necrotide_golem_beam");
    private static final ResourceLocation WAIT_PARTICLES = MKUltra.id("necrotide_golem_beam_wait");

    protected final ResourceLocationAttribute pulse_particles = new ResourceLocationAttribute("pulse_particles", PULSE_PARTICLES);
    protected final ResourceLocationAttribute wait_particles = new ResourceLocationAttribute("wait_particles", WAIT_PARTICLES);
    protected final StringAttribute poi_name = new StringAttribute("poi_name", "golem_lantern");
    protected final IntAttribute tickRate = new IntAttribute("tick_rate", GameConstants.TICKS_PER_SECOND / 2);
    protected final IntAttribute duration = new IntAttribute("duration", GameConstants.TICKS_PER_SECOND * 2);
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 8.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 5.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final FloatAttribute beamSpeed = new FloatAttribute("beam_speed", 1.85f);
    protected final FloatAttribute beamSpeedScale = new FloatAttribute("beam_speed_scale", 0.025f);
    protected final FloatAttribute beamDeathSelfDamage = new FloatAttribute("beam_death_damage", 25.0f);
    protected final IntAttribute charge_time = new IntAttribute("charge_time", GameConstants.TICKS_PER_SECOND * 2);

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    public NecrotideGolemBeam() {
        super();
        setCooldownSeconds(45);
        setManaCost(8);
        setCastTime(GameConstants.TICKS_PER_SECOND * 3);
        addAttributes(pulse_particles, wait_particles, poi_name, tickRate, duration, charge_time, formulaParameters,
                damage, beamSpeed, beamSpeedScale, beamDeathSelfDamage);
        setUseCondition(new MeleeUseCondition(this));
        addSkillAttribute(MKAttributes.NECROMANCY);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 30.0f;
    }

    protected void onEffectDie(BaseEffectEntity.DeathReason deathReason, BaseEffectEntity entity) {
        LivingEntity owner = entity.getOwner();
        if (owner != null && deathReason == BaseEffectEntity.DeathReason.KILLED) {
            owner.hurt(MKDamageSource.causeAbilityDamage(entity.level(), CoreDamageTypes.ArcaneDamage.get(),
                    getAbilityId(), entity, entity, 0.0f), beamDeathSelfDamage.value());
        }
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        float skillLevel = context.getSkill(MKAttributes.NECROMANCY);
        getStructure(castingEntity).ifPresent(entry -> entry.getPoisWithTag("golem_lantern").forEach(
                poi -> {
                    BlockPos pos = poi.getLocation().pos().below(2);
                    var builder = EntityEffectBuilder.createBlockAnchoredEffect(castingEntity, Vec3.atCenterOf(pos));
                    builder.setBlock(Blocks.SOUL_LANTERN);
                    MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_dark_1.value(),
                                    castingEntity.getSoundSource())
                            .ability(this);
                    MKEffectBuilder<?> damageEffect = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.ShadowDamage.get(),
                                    damage.value(), formulaParameters.value())
                            .ability(this)
                            .skillLevel(skillLevel);
                    castingEntity.level().setBlockAndUpdate(pos, Blocks.SOUL_LANTERN.defaultBlockState().setValue(HANGING, true));
                    builder.setRange(10.0f)
                            .setTargetContext(TargetingContexts.ENEMY)
                            .setBeamSpeed(beamSpeed.value() + beamSpeedScale.value() * skillLevel)
                            .setParticles(new BaseEffectEntity.ParticleDisplay(pulse_particles.getValue(),
                                    tickRate.value(), BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                            .setWaitingParticles(new BaseEffectEntity.ParticleDisplay(wait_particles.getValue(),
                                    tickRate.value(), BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                            .duration(getCooldown(casterData))
                            .effect(sound, TargetingContexts.ENEMY)
                            .effect(damageEffect, TargetingContexts.ENEMY)
                            .waitTime(charge_time.value())
                            .tickRate(tickRate.value())
                            .setDeathCallback(this::onEffectDie);
                    builder.spawn();
                })
        );
    }
}
