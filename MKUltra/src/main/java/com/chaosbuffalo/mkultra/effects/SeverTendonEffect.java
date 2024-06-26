package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.GameConstants;
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
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkweapons.init.MKWeaponsParticles;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class SeverTendonEffect extends MKEffect {
    public static final int DEFAULT_PERIOD = GameConstants.TICKS_PER_SECOND * 2;

    private static final UUID modUUID = UUID.fromString("bde03af5-32ed-4f6b-9f2c-c23296d60fa8");

    public SeverTendonEffect() {
        super(MobEffectCategory.HARMFUL);
        addAttribute(Attributes.MOVEMENT_SPEED, modUUID, -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, float baseDamage, float scaling, float modifierScaling) {
        return MKUEffects.SEVER_TENDON.get().builder(source)
                .state(s -> s.setScalingParameters(baseDamage, scaling, modifierScaling))
                .periodic(DEFAULT_PERIOD);
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

    public static class State extends ScalingDamageEffectState {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {

            float damage = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            LivingEntity target = targetData.getEntity();
            target.hurt(MKDamageSource.causeAbilityDamage(targetData.getEntity().getLevel(), CoreDamageTypes.BleedDamage.get(),
                    activeEffect.getAbilityId(), activeEffect.getDirectEntity(), activeEffect.getSourceEntity(),
                    getModifierScale()), damage);
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
