package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public class EntityUtils {

    private static final float DEFAULT_CRIT_RATE = 0.0f;
    private static final float DEFAULT_CRIT_DAMAGE = 1.0f;
    // Based on Skeleton Volume
    private static final double LARGE_VOLUME = 3.0 * .6 * .6 * 1.8;
    public static CriticalStats<Entity> ENTITY_CRIT = new CriticalStats<>(DEFAULT_CRIT_RATE, DEFAULT_CRIT_DAMAGE);

    public static void addCriticalStats(Class<? extends Entity> entityIn, int priority, float criticalChance,
                                        float damageMultiplier) {
        ENTITY_CRIT.addCriticalStats(entityIn, priority, criticalChance, damageMultiplier);
    }


    static {
        addCriticalStats(Arrow.class, 0, .1f, 2.0f);
        addCriticalStats(SpectralArrow.class, 1, .15f, 2.0f);
        addCriticalStats(ThrowableProjectile.class, 0, .05f, 2.0f);
    }

    public static double calculateBoundingBoxVolume(LivingEntity entityIn) {
        AABB box = entityIn.getBoundingBox();
        return (box.maxX - box.minX) * (box.maxY - box.minY) * (box.maxZ - box.minZ);
    }

    public static boolean isLargeEntity(LivingEntity entityIn) {
        double vol = calculateBoundingBoxVolume(entityIn);
        return vol >= LARGE_VOLUME;
    }

    public static double getCooldownPeriod(LivingEntity entity) {
        return 1.0D / entity.getAttribute(Attributes.ATTACK_SPEED).getValue() *
                GameConstants.TICKS_PER_SECOND;
    }

    public static void shootArrow(LivingEntity source, AbstractArrow arrowEntity, LivingEntity target, float launchVelocity) {


        Vec3 targetVec = new Vec3(target.getX(), target.getY(0.9D), target.getZ());
        Vec3 diff = targetVec.subtract(arrowEntity.position());
        Vec3 diffXZ = new Vec3(diff.x, 0.0, diff.z);
        double groundDist = diffXZ.length();

        double vel = launchVelocity * GameConstants.TICKS_PER_SECOND;
        double seconds = groundDist / vel;
        double heightLostToGravity = arrowEntity.isNoGravity() ? 0.0 : 0.05 * GameConstants.TICKS_PER_SECOND * seconds;

        double yDiff = diff.y;
        double yWithGravity = yDiff + heightLostToGravity;
        Vec3 targetPos = new Vec3(diff.x(), yWithGravity, diff.z());

        // emulates the logic skeleton uses to shoot an arrow
//        double d0 = target.getPosX() - source.getPosX();
//        double d1 = target.getPosYHeight(0.3333333333333333D) - arrowEntity.getPosY();
//        double d2 = target.getPosZ() - source.getPosZ();
//        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        arrowEntity.shoot(targetPos.x(), targetPos.y(), targetPos.z(), launchVelocity, (float) (
                14 - source.getCommandSenderWorld().getDifficulty().getId() * 4));
    }

    public static boolean shootProjectileAtTarget(BaseProjectileEntity projectile, LivingEntity target,
                                                  float velocity, float accuracy) {

        ProjectileUtils.BallisticResult result = ProjectileUtils.solveBallisticArcStationaryTarget(
                projectile.position(),
                new Vec3(target.getX(), target.getY(0.9D), target.getZ()),
                velocity, projectile.getGravityVelocity());

        if (!result.foundSolution) {
            MKCore.LOGGER.info("No solution found for {}", projectile.toString());
            return false;
        } else {
            projectile.shoot(result.lowArc.x, result.lowArc.y, result.lowArc.z, velocity, accuracy);
            return true;
        }
    }

    public static boolean canTeleportEntity(LivingEntity target) {
        return true;
    }

    public static boolean safeTeleportEntity(LivingEntity targetEntity, Vec3 teleLoc) {
        Entity finalTarget = targetEntity;
        if (targetEntity.isPassenger()) {
            finalTarget = targetEntity.getRootVehicle();
        }
        AABB axisalignedbb = targetEntity.getLocalBoundsForPose(targetEntity.getPose());
        if (DismountHelper.canDismountTo(targetEntity.level, targetEntity, axisalignedbb.move(teleLoc))) {
            finalTarget.teleportTo(teleLoc.x, teleLoc.y, teleLoc.z);
            return true;
        }
        return false;

    }
}
