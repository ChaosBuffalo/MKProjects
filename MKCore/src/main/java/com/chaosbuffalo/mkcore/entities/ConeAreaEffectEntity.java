package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class ConeAreaEffectEntity extends BaseEffectEntity {
    private static final double MIN_DIRECTION_MAGNITUDE = 1.0e-8;

    private Vec3 endpoint = Vec3.ZERO;
    private float range = 1.0f;
    private float halfAngleDegrees = 20.0f;
    private boolean useOwnerLook = false;

    public ConeAreaEffectEntity(EntityType<? extends ConeAreaEffectEntity> entityType, Level world) {
        super(entityType, world);
    }

    public ConeAreaEffectEntity(Level worldIn, double x, double y, double z) {
        this(CoreEntities.CONE_AREA_EFFECT.get(), worldIn);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Nonnull
    @Override
    public EntityDimensions getDimensions(@Nonnull Pose poseIn) {
        return EntityDimensions.scalable(range * 2.0F, range * 2.0F);
    }

    public void setRange(float range) {
        this.range = range;
        updateBounds();
    }

    public float getRange() {
        return range;
    }

    public void setHalfAngleDegrees(float halfAngleDegrees) {
        this.halfAngleDegrees = halfAngleDegrees;
    }

    public float getHalfAngleDegrees() {
        return halfAngleDegrees;
    }

    public void setAngleDegrees(float angleDegrees) {
        setHalfAngleDegrees(angleDegrees / 2.0f);
    }

    public void setEndpoint(Vec3 endpoint) {
        this.endpoint = endpoint;
        double distance = endpoint.distanceTo(position());
        if (distance > MIN_DIRECTION_MAGNITUDE) {
            setRange((float) distance);
        } else {
            updateBounds();
        }
    }

    public Vec3 getEndpoint() {
        return endpoint;
    }

    public void setDirection(Vec3 direction) {
        if (direction.lengthSqr() < MIN_DIRECTION_MAGNITUDE) {
            this.endpoint = position();
        } else {
            this.endpoint = position().add(direction.normalize().scale(range));
        }
        updateBounds();
    }

    public Vec3 getConeDirection() {
        Vec3 direction = endpoint.subtract(position());
        if (direction.lengthSqr() < MIN_DIRECTION_MAGNITUDE) {
            return new Vec3(0.0, 1.0, 0.0);
        }
        return direction.normalize();
    }

    public void setUseOwnerLook(boolean useOwnerLook) {
        this.useOwnerLook = useOwnerLook;
    }

    public boolean isUsingOwnerLook() {
        return useOwnerLook;
    }

    private void updateOwnerLook() {
        LivingEntity owner = getOwner();
        if (owner == null) {
            return;
        }

        if (useOwnerLook) {
            Vec3 look = owner.getLookAngle();
            endpoint = look.lengthSqr() < MIN_DIRECTION_MAGNITUDE
                    ? position()
                    : position().add(look.normalize().scale(range));
            updateBounds();
        }
    }

    private void updateBounds() {
        AABB bounds = new AABB(position(), position()).inflate(range);
        setBoundingBox(bounds);
        refreshDimensions();
    }

    @Override
    public void tick() {
        updateOwnerLook();
        super.tick();
    }

    @Override
    protected Collection<LivingEntity> getEntitiesInBounds() {
        Vec3 origin = position();
        Vec3 forward = getConeDirection();
        double cosHalfAngle = Math.cos(Math.toRadians(halfAngleDegrees));
        double maxRangeSqr = range * range;

        return this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox(), this::entityCheck).stream()
                .filter(target -> {
                    Vec3 toTarget = target.getBoundingBox().getCenter().subtract(origin);
                    double distanceSqr = toTarget.lengthSqr();
                    if (distanceSqr < MIN_DIRECTION_MAGNITUDE || distanceSqr > maxRangeSqr) {
                        return false;
                    }
                    return toTarget.normalize().dot(forward) >= cosHalfAngle;
                })
                .collect(Collectors.toList());
    }

    @Override
    protected void spawnClientParticles(ParticleDisplay display) {
        ParticleAnimation anim = ParticleAnimationManager.getAnimation(display.getParticles());
        if (anim != null) {
            anim.spawn(getCommandSenderWorld(), position(), new Vec3(1.0, 1.0, 1.0), Collections.singletonList(endpoint));
        }
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        writeVector(buffer, endpoint);
        buffer.writeFloat(range);
        buffer.writeFloat(halfAngleDegrees);
        buffer.writeBoolean(useOwnerLook);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        endpoint = readVector(additionalData);
        range = additionalData.readFloat();
        halfAngleDegrees = additionalData.readFloat();
        useOwnerLook = additionalData.readBoolean();
        updateOwnerLook();
        updateBounds();
    }
}
