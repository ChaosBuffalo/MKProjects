package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.MKOldParticleEffect;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.mkcore.test.effects.FeatherFallEffect;
import com.chaosbuffalo.mkcore.test.effects.PhoenixAspectEffect;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class PhoenixAspectAbility extends MKAbility {
    public static int BASE_DURATION = 60;
    public static int DURATION_SCALE = 60;

    public PhoenixAspectAbility() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND * 3);
        setCooldownSeconds(400);
        setManaCost(15);
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
        return 12.0f;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        int level = 1;

        // What to do for each target hit
        int duration = (BASE_DURATION + DURATION_SCALE * level) * GameConstants.TICKS_PER_SECOND;
//        duration = PlayerFormulas.applyBuffDurationBonus(data, duration);
        MKEffectBuilder<?> flying = MKTestEffects.PHOENIX_ASPECT.get().builder(castingEntity)
                .ability(this)
                .timed(duration)
                .amplify(level);
        MKEffectBuilder<?> feather = FeatherFallEffect.from(castingEntity)
                .ability(this)
                .timed(duration + 10 * GameConstants.TICKS_PER_SECOND)
                .amplify(level);
        MKEffectBuilder<?> particlePotion = MKOldParticleEffect.from(castingEntity,
                ParticleTypes.FIREWORK,
                ParticleEffects.DIRECTED_SPOUT, false, new Vec3(1.0, 1.5, 1.0),
                new Vec3(0.0, 1.0, 0.0), 40, 5, 1.0)
                .ability(this);

        AreaEffectBuilder.createOnCaster(castingEntity)
                .effect(flying, getTargetContext())
                .effect(feather, getTargetContext())
                .effect(particlePotion, getTargetContext())
                .instant()
                .particle(ParticleTypes.FIREWORK)
                .color(65480)
                .radius(getDistance(castingEntity), true)
                .spawn();

        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.FIREWORK,
                ParticleEffects.CIRCLE_MOTION, 50, 0,
                castingEntity.getX(), castingEntity.getY() + 1.5,
                castingEntity.getZ(), 1.0, 1.0, 1.0, 1.0f,
                castingEntity.getLookAngle()), castingEntity);
    }
}
