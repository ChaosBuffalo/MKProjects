package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.triggers.LivingHurtEntityTriggers;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.BoneEffectInstance;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class OnHitEffect<T extends MKEffect> extends MKEffect {
    private final Supplier<T> effectSupplier;
    private final UUID effectUUID;
    private final ResourceLocation particles;

    public OnHitEffect(Supplier<T> effect, BiConsumer<MKEffect, LivingHurtEntityTriggers.LivingHurtEntityEffectTriggers.Trigger> trigger,
                       UUID effectUUID, ResourceLocation particles) {
        super(MobEffectCategory.BENEFICIAL);
        this.effectSupplier = effect;
        this.effectUUID = effectUUID;
        this.particles = particles;
        trigger.accept(this, this::onLivingHurtEntity);
    }

    public void onLivingHurtEntity(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                   IMKEntityData sourceData, MKActiveEffect instance) {

        MKCore.getEntityData(livingTarget).ifPresent(data -> {
            // retrieve the duration and skill level from State here
            if (instance.getState() instanceof State onHitState) {
                data.getEffects().addEffect(effectSupplier.get().builder(sourceData.getEntity())
                        .skillLevel(instance.getSkillLevel()).timed(onHitState.getDuration()));
                onHitState.sendEffectParticles(livingTarget);
            }
            instance.modifyStackCount(-1);
            if (instance.getStackCount() <= 0) {
                sourceData.getEffects().removeEffect(instance.getSourceId(), this);
            }
        });
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        targetData.getParticleEffectTracker().ifPresent(x -> {
            x.addParticleInstance(new BoneEffectInstance(effectUUID,
                    targetData.getEntity().getMainArm() == HumanoidArm.RIGHT ? BipedSkeleton.RIGHT_HAND_BONE_NAME : BipedSkeleton.LEFT_HAND_BONE_NAME,
                    particles));
        });
    }

    @Override
    public void onInstanceRemoved(IMKEntityData targetData, MKActiveEffect expiredEffect) {
        super.onInstanceRemoved(targetData, expiredEffect);
        targetData.getParticleEffectTracker().ifPresent(x -> {
            x.removeParticleInstance(effectUUID);
        });
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

    public static class State extends MKSimplePassiveState {

        protected int duration = GameConstants.TICKS_PER_SECOND * 10;
        @Nullable
        protected ResourceLocation particles = null;

        public void setEffectParticles(ResourceLocation particle) {
            this.particles = particle;
        }


        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getDuration() {
            return duration;
        }

        @Override
        public void serializeStorage(CompoundTag stateTag) {
            super.serializeStorage(stateTag);
            stateTag.putInt("duration", duration);
            if (particles != null) {
                MKNBTUtil.writeResourceLocation(stateTag, "particles", particles);
            }
        }

        @Override
        public void deserializeStorage(CompoundTag stateTag) {
            super.deserializeStorage(stateTag);
            duration = stateTag.getInt("duration");
            if (stateTag.contains("particles")) {
                particles = MKNBTUtil.readResourceLocation(stateTag, "particles");
            }
        }

        private final Vec3 YP = new Vec3(0.0, 1.0, 0.0);

        protected void sendEffectParticles(Entity target) {
            if (particles != null) {
                MKParticles.spawn(target, YP, particles);
            }
        }
    }
}