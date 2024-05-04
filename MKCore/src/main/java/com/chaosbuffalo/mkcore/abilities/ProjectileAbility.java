package com.chaosbuffalo.mkcore.abilities;


import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.serialization.attributes.BooleanAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.location.LocationProvider;
import com.chaosbuffalo.mkcore.utils.location.SingleLocationProvider;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public abstract class ProjectileAbility extends MKAbility {
    protected final FloatAttribute baseDamage = new FloatAttribute("baseDamage", 6.0f);
    protected final FloatAttribute scaleDamage = new FloatAttribute("scaleDamage", 2.0f);
    protected final FloatAttribute projectileSpeed = new FloatAttribute("projectileSpeed", 1.25f);
    protected final FloatAttribute projectileInaccuracy = new FloatAttribute("projectileInaccuracy", 0.2f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final ResourceLocationAttribute trailParticles = new ResourceLocationAttribute("trail_particles", EMPTY_PARTICLES);
    protected final ResourceLocationAttribute detonateParticles = new ResourceLocationAttribute("detonate_particles", EMPTY_PARTICLES);

    protected final BooleanAttribute solveBallisticsForNpc = new BooleanAttribute("npc_solve_ballistics", true);
    protected LocationProvider locationProvider = new SingleLocationProvider(new Vec3(0.5f, 0.0, 0.5f), .9f);
    protected final Attribute skill;

    public ProjectileAbility(Attribute skillAttribute) {
        super();
        addSkillAttribute(skillAttribute);
        skill = skillAttribute;
        addAttributes(baseDamage, scaleDamage, projectileSpeed, projectileInaccuracy, modifierScaling, trailParticles,
                detonateParticles, solveBallisticsForNpc);
    }

    public float getBaseDamage() {
        return baseDamage.value();
    }

    public float getScaleDamage() {
        return scaleDamage.value();
    }

    public float getModifierScaling() {
        return modifierScaling.value();
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 50.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PROJECTILE;
    }

    public abstract boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier);

    public boolean onAirProc(AbilityProjectileEntity projectile, LivingEntity caster, int amplifier) {
        return false;
    }

    public boolean onGroundProc(AbilityProjectileEntity projectile, LivingEntity caster, int amplifier) {
        return false;
    }

    public abstract AbilityProjectileEntity makeProjectile(LivingEntity entity, IMKEntityData data, AbilityContext context);


    @Override
    public void startCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.startCast(castingEntity, casterData, context);
        if (!(castingEntity instanceof Player)) {
            float level = context.getSkill(skill);
            AbilityProjectileEntity proj = makeProjectile(castingEntity, casterData, context);
            proj.setOwner(castingEntity);
            proj.setSkillLevel(level);
            LocationProvider.WorldLocationResult location = locationProvider.getPosition(castingEntity, castingEntity.getRotationVector(), 0);
            if (location.isValid()) {
                proj.moveTo(location.worldPosition().x, location.worldPosition().y, location.worldPosition().z,
                        location.rotation().y, location.rotation().x);
                context.setMemory(MKAbilityMemories.CURRENT_PROJECTILE.get(), Optional.of(proj));
                castingEntity.level.addFreshEntity(proj);
            }
        }
    }
    
    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        if (castingEntity instanceof Player) {
            float level = context.getSkill(skill);
            AbilityProjectileEntity proj = makeProjectile(castingEntity, casterData, context);
            proj.setOwner(castingEntity);
            proj.setSkillLevel(level);
            LocationProvider.WorldLocationResult location = locationProvider.getPosition(castingEntity, castingEntity.getRotationVector(), 0);
            if (location.isValid()) {
                proj.setPos(location.worldPosition());
                proj.setXRot(location.rotation().x);
                proj.setYRot(location.rotation().y);
                shoot(proj, projectileSpeed.value(), projectileInaccuracy.value(), castingEntity, context);
                castingEntity.level.addFreshEntity(proj);
            }
        } else {
            context.getMemory(MKAbilityMemories.CURRENT_PROJECTILE).ifPresent(proj -> {
                shoot(proj, projectileSpeed.value(), projectileInaccuracy.value(), castingEntity, context);
            });
        }
    }

    protected void shoot(BaseProjectileEntity projectileEntity, float velocity, float accuracy,
                         LivingEntity entity, AbilityContext context) {
        if (!solveBallisticsForNpc.value() || entity instanceof Player) {
            projectileEntity.shoot(projectileEntity, projectileEntity.getXRot(), projectileEntity.getYRot(),
                    0, velocity, accuracy);
        } else {
            context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity ->
                    EntityUtils.shootProjectileAtTarget(projectileEntity, targetEntity, velocity, accuracy));
        }
    }
}
