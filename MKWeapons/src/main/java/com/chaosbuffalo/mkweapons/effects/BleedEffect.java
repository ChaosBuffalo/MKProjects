package com.chaosbuffalo.mkweapons.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingDamageEffectState;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkweapons.init.MKWeaponEffects;
import com.chaosbuffalo.mkweapons.init.MKWeaponsParticles;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;


public class BleedEffect extends MKEffect {

    public BleedEffect() {
        super(MobEffectCategory.HARMFUL);
    }

    public static MKEffectBuilder<?> from(LivingEntity caster, int maxStacks, float base, float scale, float modScale) {
        return MKWeaponEffects.BLEED_DAMAGE.get().builder(caster).state(s -> {
            s.setMaxStacks(maxStacks);
            s.setScalingParameters(base, scale, modScale);
        });
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    @Override
    public State makeState() {
        return new State();
    }

    public static class State extends ScalingDamageEffectState {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            float damage = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            //MKWeapons.LOGGER.info("bleed damage {} {} from {}", damage, activeEffect, source);
            LivingEntity target = targetData.getEntity();
            target.hurt(MKDamageSource.causeEffectDamage(target.getLevel(), CoreDamageTypes.BleedDamage.get(), "mkweapons.effect.bleed",
                    activeEffect.getDirectEntity(), activeEffect.getSourceEntity(), getModifierScale()), damage);

            PacketHandler.sendToTrackingAndSelf(
                    new ParticleEffectSpawnPacket(
                            MKWeaponsParticles.DRIPPING_BLOOD.get(),
                            ParticleEffects.DIRECTED_SPOUT, 8, 1,
                            target.getX(), target.getY() + target.getBbHeight() * .75,
                            target.getZ(), target.getBbWidth() / 2.0, 0.5, target.getBbWidth() / 2.0, 3,
                            target.getUpVector(0)), target);
            return true;
        }
    }
}
