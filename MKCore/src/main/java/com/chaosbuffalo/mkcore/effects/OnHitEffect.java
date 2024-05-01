package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.triggers.LivingHurtEntityTriggers;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.BoneEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class OnHitEffect extends MKEffect {

    public static class OnHitCallbackData {
        public IMKEntityData entityData;
        public MKActiveEffect instance;
        public LivingEntity target;

        public OnHitCallbackData(IMKEntityData entityData, MKActiveEffect instance, LivingEntity target) {
            this.entityData = entityData;
            this.instance = instance;
            this.target = target;
        }
    }

    private final Function<OnHitCallbackData, MKEffectBuilder<?>> effectSupplier;
    private final UUID effectUUID;

    private final ResourceLocation particles;
    private final boolean canBlock;

    public OnHitEffect(Function<OnHitCallbackData, MKEffectBuilder<?>> effect,
                       BiConsumer<MKEffect, LivingHurtEntityTriggers.LivingHurtEntityEffectTriggers.Trigger> trigger,
                       ResourceLocation particles, boolean canBlock) {
        super(MobEffectCategory.BENEFICIAL);
        this.effectSupplier = effect;
        this.effectUUID = UUID.randomUUID();
        this.particles = particles;
        this.canBlock = canBlock;
        trigger.accept(this, this::onLivingHurtEntity);
    }

    public void onLivingHurtEntity(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                   IMKEntityData sourceData, MKActiveEffect instance) {

        MKCore.getEntityData(livingTarget).ifPresent(data -> {
            // retrieve the duration and skill level from State here
            if (!canBlock || !livingTarget.isBlocking()) {
                data.getEffects().addEffect(effectSupplier.apply(new OnHitCallbackData(sourceData, instance, livingTarget)));
            }
            instance.modifyStackCount(-1);
            if (instance.getStackCount() <= 0) {
                sourceData.getEffects().removeEffect(instance.getSourceId(), this);
            }
        });
    }

    protected void addParticles(IMKEntityData targetData) {
        targetData.getParticleEffectTracker().ifPresent(x -> {
            x.addParticleInstance(new BoneEffectInstance(effectUUID,
                    particles, targetData.getEntity().getMainArm() == HumanoidArm.RIGHT ? BipedSkeleton.RIGHT_HAND_BONE_NAME : BipedSkeleton.LEFT_HAND_BONE_NAME
            ));
        });
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        addParticles(targetData);

    }

    @Override
    public void onInstanceLoaded(IMKEntityData targetData, MKActiveEffect activeInstance) {
        super.onInstanceLoaded(targetData, activeInstance);
        addParticles(targetData);
    }

    @Override
    public void onInstanceRemoved(IMKEntityData targetData, MKActiveEffect expiredEffect) {
        super.onInstanceRemoved(targetData, expiredEffect);
        targetData.getParticleEffectTracker().ifPresent(x -> {
            x.removeParticleInstance(effectUUID);
        });
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}