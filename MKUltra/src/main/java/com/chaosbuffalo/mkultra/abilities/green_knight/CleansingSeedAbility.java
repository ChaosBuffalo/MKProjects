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
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
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
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "cleansing_seed_casting");
    public static final ResourceLocation TRAIL_PARTICLES = new ResourceLocation(MKUltra.MODID, "cleansing_seed_trail");
    public static final ResourceLocation DETONATE_PARTICLES = new ResourceLocation(MKUltra.MODID, "cleansing_seed_detonate");


    public CleansingSeedAbility() {
        super(MKAttributes.RESTORATION);
        setCooldownSeconds(8);
        setManaCost(4);
        setCastTime(GameConstants.TICKS_PER_SECOND - 5);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
        baseDamage.setDefaultValue(4.0f);
        scaleDamage.setDefaultValue(4.0f);
        trailParticles.setDefaultValue(TRAIL_PARTICLES);
        detonateParticles.setDefaultValue(DETONATE_PARTICLES);
    }

    protected float getDamageForLevel(float level) {
        return baseDamage.value() + scaleDamage.value() * level;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.NatureDamage.get(), baseDamage.value(),
                scaleDamage.value(), context.getSkill(MKAttributes.RESTORATION),
                modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(), damageStr);
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_cast_6.get();
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }


    @Override
    public boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier) {
        SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_water_6.get(), cat);
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

                        SoundUtils.serverPlaySoundAtEntity(target, MKUSounds.spell_water_2.get(), cat);
                        break;
                    }
                    case ENEMY: {
                        target.hurt(MKDamageSource.causeAbilityDamage(target.getLevel(), CoreDamageTypes.NatureDamage.get(),
                                        getAbilityId(), projectile, caster,
                                        getModifierScaling()), getDamageForLevel(getSkillLevel(caster, skill)));
                        SoundUtils.serverPlaySoundAtEntity(target, MKUSounds.spell_water_8.get(), cat);
                        break;
                    }
                }
            }
        }
        MKParticles.spawn(projectile, new Vec3(0.0, 0.0, 0.0), detonateParticles.getValue());
        return true;
    }

    @Override
    public AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context) {
        AbilityProjectileEntity projectile = new AbilityProjectileEntity(CoreEntities.ABILITY_PROJECTILE_TYPE.get(), data.getEntity().level);
        projectile.setAbility(() -> this);
        projectile.setTrailAnimation(trailParticles.getValue());
        projectile.setItem(new ItemStack(MKUItems.cleansingSeedProjectileItem.get()));
        projectile.setDeathTime(GameConstants.TICKS_PER_SECOND * 2);
        projectile.setGravityVelocity(BaseProjectileEntity.DEFAULT_MC_GRAVITY);
        return projectile;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_water.get();
    }
}
