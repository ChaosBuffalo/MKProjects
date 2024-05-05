package com.chaosbuffalo.mkcore.abilities;


import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.serialization.attributes.BooleanAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.LocationProviderAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.location.LocationProvider;
import com.chaosbuffalo.mkcore.utils.location.SingleLocationProvider;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import com.google.common.collect.Lists;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
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
    protected final LocationProviderAttribute locationProvider = new LocationProviderAttribute("locationProvider",
            new SingleLocationProvider(new Vec3(0.5f, 0.0, 0.5f), .75f));
    protected final Attribute skill;

    public ProjectileAbility(Attribute skillAttribute) {
        super();
        addSkillAttribute(skillAttribute);
        skill = skillAttribute;
        addAttributes(baseDamage, scaleDamage, projectileSpeed, projectileInaccuracy, modifierScaling, trailParticles,
                detonateParticles, solveBallisticsForNpc, locationProvider);
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

    public abstract AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context);


    @Override
    public void startCast(IMKEntityData casterData, AbilityContext context) {
        super.startCast(casterData, context);
        float level = context.getSkill(skill);
        AbilityProjectileEntity proj = makeProjectile(casterData, context);
        proj.setOwner(casterData.getEntity());
        proj.setSkillLevel(level);
        LocationProvider.WorldLocationResult location = locationProvider.getValue().getPosition(casterData.getEntity(), 0);
        proj.setPos(location.worldPosition());
        Vec3 offset = location.worldPosition().subtract(casterData.getEntity().position()).yRot(casterData.getEntity().getYRot() * ((float)Math.PI / 180F));

        proj.setXRot(location.rotation().x);
        proj.setYRot(location.rotation().y);
        context.setMemory(MKAbilityMemories.CURRENT_PROJECTILES.get(), Optional.of(Lists.newArrayList(proj)));
        casterData.getRiders().addRider(proj, offset);
        casterData.getEntity().level.addFreshEntity(proj);


    }

    @Override
    public void interruptCast(CastInterruptReason reason, IMKEntityData casterData, AbilityContext context) {
        super.interruptCast(reason, casterData, context);
        context.getMemory(MKAbilityMemories.CURRENT_PROJECTILES).ifPresent(current -> {
            for (BaseProjectileEntity proj : current) {
                casterData.getRiders().removeRider(proj);
                proj.remove(Entity.RemovalReason.KILLED);
            }
        });
        context.setMemory(MKAbilityMemories.CURRENT_PROJECTILES.get(), Optional.empty());
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        context.getMemory(MKAbilityMemories.CURRENT_PROJECTILES).ifPresent(current -> {
            for (BaseProjectileEntity proj : current) {
                casterData.getRiders().removeRider(proj);
                shoot(proj, projectileSpeed.value(), projectileInaccuracy.value(), castingEntity, context);
            }
        });
        context.setMemory(MKAbilityMemories.CURRENT_PROJECTILES.get(), Optional.empty());

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
