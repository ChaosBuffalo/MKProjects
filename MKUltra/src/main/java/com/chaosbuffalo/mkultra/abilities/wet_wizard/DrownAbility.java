package com.chaosbuffalo.mkultra.abilities.wet_wizard;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.DrownEffect;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DrownAbility extends ProjectileAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("drown.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("drown.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("drown.damage.modifier_scaling"));
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("drown.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("drown.duration.per_level"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("drown_casting");
    public static final ResourceLocation TICK_PARTICLES = MKUltra.id("drown_effect");
    public static final ResourceLocation TRAIL_PARTICLES = MKUltra.id("drown_trail");
    public static final ResourceLocation DETONATE_PARTICLES = MKUltra.id("drown_detonate");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 4.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 2.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 0.2f)
                    .with(DURATION_BASE_PARAMETER, 10.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 2.0f)
                    .build());
    protected final FormulaAttribute damageFormula = new FormulaAttribute("damageFormula",
            AbilityFormula.bonusScaledLinear(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final ResourceLocationAttribute tick_particles = new ResourceLocationAttribute("tick_particles", TICK_PARTICLES);


    public DrownAbility() {
        super(MKAttributes.CONJURATION, false);
        setCooldownSeconds(10);
        setManaCost(5);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addAttributes(formulaParameters, damageFormula, durationFormula, tick_particles);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        trailParticles.setDefaultValue(TRAIL_PARTICLES);
        detonateParticles.setDefaultValue(DETONATE_PARTICLES);
        projectileSpeed.setDefaultValue(0.9f);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(skill);
        Component dotStr = getDamageDescription(entityData,
                CoreDamageTypes.NatureDamage.get(), damageFormula.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(),
                dotStr, NUMBER_FORMATTER.format(convertDurationToSeconds(DrownEffect.DEFAULT_PERIOD)),
                NUMBER_FORMATTER.format(convertDurationToSeconds(
                        getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), level))));
    }

    protected MKEffectBuilder<?> getDotEffect(IMKEntityData casterData, float level) {
        int durTicks = getBuffDuration(casterData, durationFormula.value(), formulaParameters.value(), level);
        return DrownEffect.from(casterData.getEntity(), damageFormula.value(), formulaParameters.value(),
                        tick_particles.getValue())
                .ability(this)
                .skillLevel(level)
                .timed(durTicks);
    }

    @Override
    public boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier) {
        SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_water_5.value(), cat);
        MKParticles.spawnOffset(projectile, new Vec3(0.0, 0.0, 0.0), detonateParticles.getValue());
        if (result.getType().equals(HitResult.Type.ENTITY)) {
            EntityHitResult entityTrace = (EntityHitResult) result;
            MKCore.getEntityData(caster).ifPresent(casterData -> {
                MKEffectBuilder<?> damage = getDotEffect(casterData, getSkillLevel(caster, skill));
                MKCore.getEntityData(entityTrace.getEntity()).ifPresent(x -> {
                    x.getEffects().addEffect(damage);
                });
            });
        }
        return true;
    }

    @Override
    public AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context) {
        AbilityProjectileEntity projectile = new AbilityProjectileEntity(CoreEntities.ABILITY_PROJECTILE_TYPE.get(), data.getEntity().level());
        projectile.setAbility(() -> this);
        projectile.setTrailAnimation(trailParticles.getValue());
        projectile.setItem(new ItemStack(MKUItems.drownProjectileItem.get()));
        projectile.setDeathTime(GameConstants.TICKS_PER_SECOND * 3);
        return projectile;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_water.value();
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_water_7.value();
    }
}
