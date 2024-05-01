package com.chaosbuffalo.mkcore.fx.particles.effect_instances;

import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class ParticleEffectInstance {
    public static final Codec<ParticleEffectInstance> CODEC = ParticleAnimationManager.EFFECT_INSTANCE_CODEC;

    protected ResourceLocation particleAnimName;
    private final UUID instanceUUID;
    private final ResourceLocation instanceType;
    private ParticleAnimation animation;

    public ParticleEffectInstance(ResourceLocation instanceType) {
        this(instanceType, UUID.randomUUID());
    }

    public ParticleEffectInstance(ResourceLocation instanceType, UUID instanceUUID) {
        this.instanceType = instanceType;
        this.instanceUUID = instanceUUID;
    }

    public ResourceLocation getTypeName() {
        return instanceType;
    }

    public UUID getInstanceUUID() {
        return instanceUUID;
    }

    public ResourceLocation getParticleAnimName() {
        return particleAnimName;
    }

    public Optional<ParticleAnimation> getAnimation() {
        if (animation == null) {
            this.animation = ParticleAnimationManager.getAnimation(getParticleAnimName());
        }
        return Optional.ofNullable(animation);
    }

    public abstract void update(Entity entity, MCSkeleton skeleton, float partialTicks, Vec3 offset);

}
