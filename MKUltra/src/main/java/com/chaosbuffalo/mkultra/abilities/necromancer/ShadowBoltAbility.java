package com.chaosbuffalo.mkultra.abilities.necromancer;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.*;
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

public class ShadowBoltAbility extends ProjectileAbility {
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "shadow_bolt_casting");
    public static final ResourceLocation TRAIL_PARTICLES = new ResourceLocation(MKUltra.MODID, "shadow_bolt_trail");
    public static final ResourceLocation DETONATE_PARTICLES = new ResourceLocation(MKUltra.MODID, "shadow_bolt_detonate");

    public ShadowBoltAbility() {
        super(MKAttributes.EVOCATION);
        setCooldownSeconds(8);
        setManaCost(7);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        baseDamage.setDefaultValue(8.0f);
        scaleDamage.setDefaultValue(4.0f);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
        trailParticles.setDefaultValue(TRAIL_PARTICLES);
        detonateParticles.setDefaultValue(DETONATE_PARTICLES);
    }

    @Override
    public int getCastTime(IMKEntityData casterData) {
        return casterData.getEffects().isEffectActive(MKUEffects.SHADOWBRINGER.get()) ? 0 : super.getCastTime(casterData);
    }

    @Override
    public float getManaCost(IMKEntityData casterData) {
        float cost = super.getManaCost(casterData);
        return casterData.getEffects().isEffectActive(MKUEffects.SHADOWBRINGER.get()) ? cost / 2.0f : cost;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float skillLevel = context.getSkill(skill);
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.ShadowDamage.get(), baseDamage.value(),
                scaleDamage.value(), skillLevel,
                modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(), damageStr);
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_dark_3.get();
    }

    @Override
    public boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier) {
        SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_dark_8.get(), cat);
        MKParticles.spawn(projectile, new Vec3(0.0, 0.0, 0.0), detonateParticles.getValue());

        if (result.getType().equals(HitResult.Type.ENTITY)) {
            EntityHitResult entityTrace = (EntityHitResult) result;
            MKEffectBuilder<?> damage = MKAbilityDamageEffect.from(caster, CoreDamageTypes.ShadowDamage.get(),
                            getBaseDamage(),
                            getScaleDamage(),
                            getModifierScaling())
                    .ability(this)
                    .directEntity(projectile)
                    .skillLevel(getSkillLevel(caster, skill))
                    .amplify(amplifier);
            MKCore.getEntityData(entityTrace.getEntity()).ifPresent(x -> {
                x.getEffects().addEffect(damage);
            });
        }
        return true;
    }

    @Override
    public AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context) {
        AbilityProjectileEntity projectile = new AbilityProjectileEntity(CoreEntities.ABILITY_PROJECTILE_TYPE.get(), data.getEntity().level);
        projectile.setAbility(() -> this);
        projectile.setTrailAnimation(trailParticles.getValue());
        projectile.setItem(new ItemStack(MKUItems.shadowBoltProjectileItem.get()));
        projectile.setDeathTime(GameConstants.TICKS_PER_SECOND * 6);
        return projectile;
    }
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_shadow.get();
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        if (casterData.getEffects().isEffectActive(MKUEffects.SHADOWBRINGER.get())) {
            casterData.getEffects().removeEffect(MKUEffects.SHADOWBRINGER.get());
        }
        super.endCast(castingEntity, casterData, context);
    }
}
