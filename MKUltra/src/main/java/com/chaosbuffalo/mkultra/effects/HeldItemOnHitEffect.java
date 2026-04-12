package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import com.chaosbuffalo.mkcore.effects.OnHitEffect;
import com.chaosbuffalo.mkcore.effects.triggers.LivingHurtEntityTriggers;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.HeldItemParticleEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class HeldItemOnHitEffect extends MKEffect {
    private final Function<OnHitEffect.OnHitCallbackData, MKEffectBuilder<?>> effectSupplier;
    private final Map<InteractionHand, UUID> effectUuids = new EnumMap<>(InteractionHand.class);
    private final ResourceLocation particles;
    private final boolean canBlock;

    public HeldItemOnHitEffect(Function<OnHitEffect.OnHitCallbackData, MKEffectBuilder<?>> effectSupplier,
                               BiConsumer<MKEffect, LivingHurtEntityTriggers.LivingHurtEntityEffectTriggers.Trigger> trigger,
                               ResourceLocation particles, boolean canBlock) {
        super(MobEffectCategory.BENEFICIAL);
        this.effectSupplier = effectSupplier;
        this.particles = particles;
        this.canBlock = canBlock;
        effectUuids.put(InteractionHand.MAIN_HAND, UUID.randomUUID());
        effectUuids.put(InteractionHand.OFF_HAND, UUID.randomUUID());
        trigger.accept(this, this::onLivingHurtEntity);
    }

    private void onLivingHurtEntity(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                    IMKEntityData sourceData, MKActiveEffect instance) {
        MKCore.getEntityData(livingTarget).ifPresent(data -> {
            if (!canBlock || !livingTarget.isBlocking()) {
                data.getEffects().addEffect(effectSupplier.apply(new OnHitEffect.OnHitCallbackData(sourceData, instance, livingTarget)));
            }
        });
    }

    protected void addParticles(IMKEntityData targetData) {
        targetData.getParticleEffectTracker().ifPresent(tracker -> {
            tracker.addParticleInstance(new HeldItemParticleEffectInstance(effectUuids.get(InteractionHand.MAIN_HAND),
                    particles, InteractionHand.MAIN_HAND));
            tracker.addParticleInstance(new HeldItemParticleEffectInstance(effectUuids.get(InteractionHand.OFF_HAND),
                    particles, InteractionHand.OFF_HAND));
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
        targetData.getParticleEffectTracker().ifPresent(tracker -> {
            tracker.removeParticleInstance(effectUuids.get(InteractionHand.MAIN_HAND));
            tracker.removeParticleInstance(effectUuids.get(InteractionHand.OFF_HAND));
        });
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
