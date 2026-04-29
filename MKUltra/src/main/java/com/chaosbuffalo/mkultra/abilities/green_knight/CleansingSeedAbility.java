package com.chaosbuffalo.mkultra.abilities.green_knight;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.CureEffect;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
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

import javax.annotation.Nullable;

public class CleansingSeedAbility extends ProjectileAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("cleansing_seed.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("cleansing_seed.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("cleansing_seed.damage.modifier_scaling"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("cleansing_seed_casting");
    public static final ResourceLocation TRAIL_PARTICLES = MKUltra.id("cleansing_seed_trail");
    public static final ResourceLocation DETONATE_PARTICLES = MKUltra.id("cleansing_seed_detonate");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 4.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 4.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final FormulaAttribute damageFormula = new FormulaAttribute("damageFormula",
            AbilityFormula.bonusScaledLinear(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));


    public CleansingSeedAbility() {
        super(MKAttributes.RESTORATION, false);
        setCooldownSeconds(8);
        setManaCost(4);
        setCastTime(GameConstants.TICKS_PER_SECOND - 5);
        addAttributes(formulaParameters, damageFormula);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        trailParticles.setDefaultValue(TRAIL_PARTICLES);
        detonateParticles.setDefaultValue(DETONATE_PARTICLES);
    }

    private AbilityFormula.Breakdown getDamageBreakdown() {
        AbilityFormula.Breakdown breakdown = damageFormula.value().breakdown(formulaParameters.value());
        if (breakdown == null) {
            throw new IllegalStateException("Parameterized damage formulas must provide a runtime bonus breakdown");
        }
        return breakdown;
    }

    protected float getDamageForLevel(float level) {
        return getDamageBreakdown().baseFormula().evaluate(FormulaContext.builder()
                .withSkillLevel(level)
                .build());
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.NatureDamage.get(),
                damageFormula.value(), formulaParameters.value(), context.getSkill(MKAttributes.RESTORATION));
        return Component.translatable(getDescriptionTranslationKey(), damageStr);
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_cast_6.value();
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }


    @Override
    public boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier) {
        SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_water_6.value(), cat);
        if (result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityTrace = (EntityHitResult) result;
            if (entityTrace.getEntity() instanceof LivingEntity target) {
                Targeting.TargetRelation relation = Targeting.getTargetRelation(caster, target);
                switch (relation) {
                    case FRIEND: {
                        MKEffectBuilder<?> cure = CureEffect.from(caster)
                                .ability(this)
                                .directEntity(projectile)
                                .skillLevel(getSkillLevel(caster, skill))
                                .amplify(amplifier);

                        MKCore.getEntityData(target).ifPresent(targetData -> targetData.getEffects().addEffect(cure));

                        SoundUtils.serverPlaySoundAtEntity(target, MKUSounds.spell_water_2.value(), cat);
                        break;
                    }
                    case ENEMY: {
                        float skillLevel = getSkillLevel(caster, skill);
                        AbilityFormula.Breakdown damageBreakdown = getDamageBreakdown();
                        target.hurt(MKDamageSource.causeAbilityDamage(target.level(), CoreDamageTypes.NatureDamage.get(),
                                        getAbilityId(), projectile, caster)
                                        .setDamageBonusFormula(damageBreakdown.bonusFormula()),
                                getDamageForLevel(skillLevel));
                        SoundUtils.serverPlaySoundAtEntity(target, MKUSounds.spell_water_8.value(), cat);
                        break;
                    }
                }
            }
        }
        MKParticles.spawnOffset(projectile, new Vec3(0.0, 0.0, 0.0), detonateParticles.getValue());
        return true;
    }

    @Override
    public AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context) {
        AbilityProjectileEntity projectile = new AbilityProjectileEntity(CoreEntities.ABILITY_PROJECTILE_TYPE.get(), data.getEntity().level());
        projectile.setAbility(() -> this);
        projectile.setTrailAnimation(trailParticles.getValue());
        projectile.setItem(new ItemStack(MKUItems.cleansingSeedProjectileItem.get()));
        projectile.setDeathTime(GameConstants.TICKS_PER_SECOND * 2);
        projectile.setGravityVelocity(BaseProjectileEntity.DEFAULT_MC_GRAVITY);
        return projectile;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_water.value();
    }
}
