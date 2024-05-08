package com.chaosbuffalo.mkcore.abilities.projectiles;


import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.serialization.attributes.CodecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.BooleanAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.location.SingleLocationProvider;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class ProjectileAbility extends MKAbility {
    protected final FloatAttribute baseDamage = new FloatAttribute("baseDamage", 6.0f);
    protected final FloatAttribute scaleDamage = new FloatAttribute("scaleDamage", 2.0f);
    protected final FloatAttribute projectileSpeed = new FloatAttribute("projectileSpeed", 1.25f);
    protected final FloatAttribute projectileInaccuracy = new FloatAttribute("projectileInaccuracy", 0.2f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final ResourceLocationAttribute trailParticles = new ResourceLocationAttribute("trail_particles", EMPTY_PARTICLES);
    protected final ResourceLocationAttribute detonateParticles = new ResourceLocationAttribute("detonate_particles", EMPTY_PARTICLES);
    protected final CodecAttribute<ProjectileCastBehavior> castBehavior = new CodecAttribute<>("castBehavior",
            new SingleProjectileBehavior(new SingleLocationProvider(new Vec3(0.5f, 0.0f, 0.5f), 0.75f)),
            ProjectileCastBehavior.CODEC);

    protected final BooleanAttribute solveBallisticsForNpc = new BooleanAttribute("npc_solve_ballistics", true);

    protected final Attribute skill;

    public ProjectileAbility(Attribute skillAttribute) {
        super();
        addSkillAttribute(skillAttribute);
        skill = skillAttribute;
        addAttributes(baseDamage, scaleDamage, projectileSpeed, projectileInaccuracy, modifierScaling, trailParticles,
                detonateParticles, solveBallisticsForNpc, castBehavior);
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

    public float getProjectileSpeed() {
        return projectileSpeed.value();
    }

    public float getProjectileInaccuracy() {
        return projectileInaccuracy.value();
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 50.0f;
    }

    public Attribute getSkill() {
        return skill;
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
    public void startCast(IMKEntityData casterData, int castTime, AbilityContext context) {
        super.startCast(casterData, castTime, context);
        castBehavior.getValue().startCast(this, casterData, castTime, context);
    }

    @Override
    public void continueCast(LivingEntity castingEntity, IMKEntityData casterData, int castTimeLeft, int totalTicks, AbilityContext context) {
        super.continueCast(castingEntity, casterData, castTimeLeft, totalTicks, context);
        castBehavior.getValue().continueCast(this, casterData, context, castTimeLeft, totalTicks);
    }

    @Override
    public void continueCastClient(LivingEntity castingEntity, IMKEntityData casterData, int castTimeLeft, int totalTicks, @Nullable AbilityClientState clientState) {
        super.continueCastClient(castingEntity, casterData, castTimeLeft, totalTicks, clientState);
        castBehavior.getValue().continueCastClient(this, casterData, castTimeLeft, totalTicks, clientState);
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
        castBehavior.getValue().endCast(this, casterData, context);
    }

    @Override
    public void endCastClient(IMKEntityData casterData, @Nullable AbilityClientState clientState) {
        super.endCastClient(casterData, clientState);
        castBehavior.getValue().endCastClient(this, casterData, clientState);
    }

    public void fireCurrentProjectiles(IMKEntityData casterData, AbilityContext context) {
        context.getMemory(MKAbilityMemories.CURRENT_PROJECTILES).ifPresent(current -> {
            for (BaseProjectileEntity proj : current) {
                casterData.getRiders().removeRider(proj);
                fireProjectile(proj, projectileSpeed.value(), projectileInaccuracy.value(), casterData.getEntity(), context);
            }
        });
        context.setMemory(MKAbilityMemories.CURRENT_PROJECTILES.get(), Optional.empty());
    }

    protected void fireProjectile(BaseProjectileEntity projectileEntity, float velocity, float accuracy,
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
