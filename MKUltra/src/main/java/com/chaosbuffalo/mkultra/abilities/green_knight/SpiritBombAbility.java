package com.chaosbuffalo.mkultra.abilities.green_knight;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SpiritBombAbility extends ProjectileAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("spirit_bomb.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("spirit_bomb.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("spirit_bomb.damage.modifier_scaling"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("spirit_bomb_casting");
    public static final ResourceLocation TRAIL_PARTICLES = MKUltra.id("spirit_bomb_trail");
    public static final ResourceLocation DETONATE_PARTICLES = MKUltra.id("spirit_bomb_detonate");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 4.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 4.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.25f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));

    public SpiritBombAbility() {
        super(MKAttributes.EVOCATION, false);
        setCooldownSeconds(10);
        setCastTime(GameConstants.TICKS_PER_SECOND + (GameConstants.TICKS_PER_SECOND / 4));
        setManaCost(4);
        addAttributes(formulaParameters, damage);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        trailParticles.setDefaultValue(TRAIL_PARTICLES);
        detonateParticles.setDefaultValue(DETONATE_PARTICLES);
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_holy.value();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_magic_whoosh_1.value();
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.NatureDamage.get(),
                damage.value(),
                formulaParameters.value(),
                context.getSkill(MKAttributes.EVOCATION));
        return Component.translatable(getDescriptionTranslationKey(), damageStr);
    }

    private boolean doEffect(AbilityProjectileEntity projectile, LivingEntity caster, int amplifier) {
        MKEffectBuilder<?> damageEffect = MKAbilityDamageEffect.from(caster, CoreDamageTypes.NatureDamage.get(),
                        damage.value(),
                        formulaParameters.value())
                .ability(this)
                .directEntity(projectile)
                .skillLevel(getSkillLevel(caster, skill))
                .amplify(amplifier);

        AreaEffectBuilder.createOnEntity(caster, projectile)
                .effect(damageEffect, getTargetContext())
                .instant()
                .color(65535)
                .radius(4.0f, true)
                .disableParticle()
                .spawn();
        SoundSource cat = caster.getSoundSource();
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_magic_explosion.value(), cat);
        MKParticles.spawnOffset(projectile, new Vec3(0.0, 0.0, 0.0), detonateParticles.getValue());
        return true;
    }

    @Override
    public boolean onAirProc(AbilityProjectileEntity projectile, LivingEntity caster, int amplifier) {
        return doEffect(projectile, caster, amplifier);
    }

    @Override
    public boolean onGroundProc(AbilityProjectileEntity projectile, LivingEntity caster, int amplifier) {
        return doEffect(projectile, caster, amplifier);
    }

    @Override
    public boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier) {
        SoundSource cat = caster.getSoundSource();
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_thunder_3.value(), cat);
        switch (result.getType()) {
            case BLOCK, MISS:
                break;
            case ENTITY:
                EntityHitResult entityTrace = (EntityHitResult) result;
                if (entityTrace.getEntity() instanceof LivingEntity target) {
                    if (Targeting.isValidTarget(getTargetContext(), caster, target)) {
                        projectile.setDeltaMovement(0.0, 0.0, 0.0);
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context) {
        AbilityProjectileEntity projectile = new AbilityProjectileEntity(CoreEntities.ABILITY_PROJECTILE_TYPE.get(), data.getEntity().level());
        projectile.setAbility(() -> this);
        projectile.setTrailAnimation(trailParticles.getValue());
        projectile.setItem(new ItemStack(MKUItems.spiritBombProjectileItem.get()));
        projectile.setDeathTime(GameConstants.TICKS_PER_SECOND * 3);
        projectile.setAirProcTime(GameConstants.TICKS_PER_SECOND);
        projectile.setDoAirProc(true);
        projectile.setDoGroundProc(true);
        projectile.setGroundProcTime(GameConstants.TICKS_PER_SECOND);
        return projectile;
    }
}
