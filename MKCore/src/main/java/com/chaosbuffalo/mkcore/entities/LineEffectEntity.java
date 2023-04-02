package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class LineEffectEntity extends BaseEffectEntity {
    private Vec3 startPoint;
    private Vec3 endPoint;

    public LineEffectEntity(EntityType<? extends LineEffectEntity> entityType, Level world) {
        super(entityType, world);
    }

    public LineEffectEntity(Level worldIn, double x, double y, double z) {
        this(CoreEntities.LINE_EFFECT.get(), worldIn);
        this.setPos(x, y, z);
    }

    public void setStartPoint(Vec3 startPoint) {
        this.startPoint = startPoint;
    }

    public void setEndPoint(Vec3 endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    protected Collection<LivingEntity> getEntitiesInBounds() {
        return RayTraceUtils.rayTraceAllEntities(LivingEntity.class, getCommandSenderWorld(),
                startPoint, endPoint, Vec3.ZERO,
                1.5f, 0.0f, this::entityCheck).getEntities().stream().map(x -> x.entity)
                .collect(Collectors.toList());
    }

    @Override
    protected void spawnClientParticles(ParticleDisplay display) {
        ParticleAnimation anim = ParticleAnimationManager.getAnimation(display.getParticles());
        if (anim != null) {
            anim.spawn(getCommandSenderWorld(), startPoint, Collections.singletonList(endPoint));
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        writeVector(buffer, startPoint);
        writeVector(buffer, endPoint);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        startPoint = readVector(additionalData);
        endPoint = readVector(additionalData);
    }
}
