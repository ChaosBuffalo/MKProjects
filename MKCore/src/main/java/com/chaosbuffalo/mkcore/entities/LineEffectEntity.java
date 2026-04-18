package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class LineEffectEntity extends BaseEffectEntity {
    private Vec3 startPoint;
    private Vec3 endPoint;
    private float growth = 0.25f;


    public LineEffectEntity(EntityType<? extends LineEffectEntity> entityType, Level world) {
        super(entityType, world);
    }

    public LineEffectEntity(Level worldIn, double x, double y, double z) {
        this(CoreEntities.LINE_EFFECT.get(), worldIn);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public void setStartPoint(Vec3 startPoint) {
        this.startPoint = startPoint;
        updateTraceBounds();
    }

    public Vec3 getStartPoint() {
        return startPoint;
    }

    public Vec3 getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Vec3 endPoint) {
        this.endPoint = endPoint;
        updateTraceBounds();
    }

    public void setGrowth(float aaGrowth) {
        this.growth = aaGrowth;
        updateTraceBounds();
    }

    public float getGrowth() {
        return growth;
    }

    @Override
    protected Collection<LivingEntity> getEntitiesInBounds() {
        return RayTraceUtils.traceAllEntitiesInCapsule(LivingEntity.class, getCommandSenderWorld(),
                        startPoint, endPoint, growth, 0.0f, this::entityCheck).getEntities().stream().map(x -> x.entity)
                .collect(Collectors.toList());
    }

    @Override
    protected void spawnClientParticles(ParticleDisplay display) {
        ParticleAnimation anim = ParticleAnimationManager.getAnimation(display.getParticles());
        if (anim != null) {
            anim.spawn(getCommandSenderWorld(), startPoint, new Vec3(1., 1., 1.), Collections.singletonList(endPoint));
        }
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        writeVector(buffer, startPoint);
        writeVector(buffer, endPoint);
        buffer.writeFloat(growth);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        startPoint = readVector(additionalData);
        endPoint = readVector(additionalData);
        growth = additionalData.readFloat();
        updateTraceBounds();
    }

    public AABB getTraceBounds() {
        if (startPoint == null || endPoint == null) {
            return new AABB(position(), position());
        }
        return new AABB(startPoint, endPoint).inflate(growth);
    }

    private void updateTraceBounds() {
        if (startPoint != null && endPoint != null) {
            setBoundingBox(getTraceBounds());
        }
    }
}
