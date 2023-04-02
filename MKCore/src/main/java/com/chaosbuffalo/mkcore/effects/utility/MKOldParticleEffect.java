package com.chaosbuffalo.mkcore.effects.utility;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MKOldParticleEffect extends MKEffect {

    public MKOldParticleEffect() {
        super(MobEffectCategory.NEUTRAL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, ParticleOptions particleId, int motionType, boolean includeSelf,
                                          Vec3 radius, Vec3 offsets, int particleCount, int particleData,
                                          double particleSpeed) {
        return CoreEffects.OLD_PARTICLE.get().builder(source).state(s ->
                s.setup(source, particleId, motionType, radius, offsets, particleCount, particleData, particleSpeed, includeSelf));
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends MKEffectState {
        Entity source;
        ParticleOptions particleId;
        int motionType;
        Vec3 radius;
        Vec3 offsets;
        int particleCount;
        int particleData;
        double particleSpeed;
        boolean includeSelf;

        public void setup(Entity source, ParticleOptions particleId, int motionType,
                          Vec3 radius, Vec3 offsets, int particleCount, int particleData,
                          double particleSpeed, boolean includeSelf) {
            this.source = source;
            this.particleId = particleId;
            this.motionType = motionType;
            this.radius = radius;
            this.offsets = offsets;
            this.particleCount = particleCount;
            this.particleData = particleData;
            this.particleSpeed = particleSpeed;
            this.includeSelf = includeSelf;
        }

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            // Don't apply if we're the caster and the caller didn't want us included
            return includeSelf || !isEffectSource(targetData.getEntity(), activeEffect);
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            PacketHandler.sendToTrackingAndSelf(createPacket(targetData.getEntity()), targetData.getEntity());
            return false;
        }

        public ParticleEffectSpawnPacket createPacket(Entity target) {
            return new ParticleEffectSpawnPacket(particleId,
                    motionType, particleCount, particleData,
                    target.getX() + offsets.x,
                    target.getY() + offsets.y,
                    target.getZ() + offsets.z,
                    radius.x,
                    radius.y,
                    radius.z,
                    particleSpeed,
                    source.position().subtract(target.position()).normalize());
        }
    }
}
