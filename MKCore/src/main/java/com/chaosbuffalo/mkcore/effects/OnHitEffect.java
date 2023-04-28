package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class OnHitEffect<T extends MKEffect> extends MKEffect {
    private final Supplier<T> effectSupplier;

    public OnHitEffect(Supplier<T> effect) {
        super(MobEffectCategory.BENEFICIAL);
        this.effectSupplier = effect;
        SpellTriggers.LIVING_HURT_ENTITY.registerMelee(this::onLivingHurtEntity);
    }

    public void onLivingHurtEntity(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                   LivingEntity livingSource, IMKEntityData sourceData) {
        MKCore.getEntityData(livingSource).ifPresent(src -> {
            if (src.getEffects().isEffectActive(this)) {
                MKCore.getEntityData(livingTarget).ifPresent(data -> {
                    // retrieve the duration and skill level from State here
                    data.getEffects().addEffect(effectSupplier.get().builder(livingSource)
                            .skillLevel(1).timed(10 * GameConstants.TICKS_PER_SECOND));
                });
            }
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

    public static class State extends ScalingValueEffectState {
        // store the duration and skill level here

    }
}