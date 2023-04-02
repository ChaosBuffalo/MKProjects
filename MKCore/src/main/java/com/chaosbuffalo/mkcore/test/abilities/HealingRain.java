package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.MKOldParticleEffect;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HealingRain extends MKAbility {
    public static float BASE_AMOUNT = 2.0f;
    public static float AMOUNT_SCALE = 1.0f;

    public HealingRain() {
        super();
        setCastTime(2 * GameConstants.TICKS_PER_SECOND);
        setManaCost(10);
        setCooldownSeconds(20);
    }

    @Override
    public boolean isInterruptedBy(IMKEntityData targetData, CastInterruptReason reason) {
        return false;
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Ultimate;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }


    @Override
    public float getDistance(LivingEntity entity) {
        return 6.0f;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public void continueCast(LivingEntity castingEntity, IMKEntityData casterData, int castTimeLeft, AbilityContext context) {
        super.continueCast(castingEntity, casterData, castTimeLeft, context);
        int tickSpeed = 5;
        if (castTimeLeft % tickSpeed == 0) {
            int level = 0;
            MKEffectBuilder<?> heal = MKTestEffects.NEW_HEAL.get().builder(castingEntity)
                    .state(s -> s.setScalingParameters(BASE_AMOUNT, AMOUNT_SCALE))
                    .ability(this)
                    .amplify(level);
            MKEffectBuilder<?> particlePotion = MKOldParticleEffect.from(castingEntity,
                    ParticleTypes.BUBBLE,
                    ParticleEffects.CIRCLE_MOTION, false,
                    new Vec3(1.0, 1.0, 1.0),
                    new Vec3(0.0, 1.0, 0.0),
                    10, 0, 1.0)
                    .ability(this);

            float dist = getDistance(castingEntity);
            AreaEffectBuilder.createOnCaster(castingEntity)
                    .effect(heal, getTargetContext())
                    .effect(particlePotion, getTargetContext())
                    .instant()
                    .color(16409620)
                    .radius(dist, true)
                    .disableParticle()
                    .spawn();

            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                    ParticleTypes.BUBBLE,
                    ParticleEffects.RAIN_EFFECT, 30, 4,
                    castingEntity.getX(), castingEntity.getY() + 3.0,
                    castingEntity.getZ(), dist, 0.5, dist, 1.0,
                    castingEntity.getLookAngle()), castingEntity);
        }
    }
}
