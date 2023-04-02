package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.NeedsBuffCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class NewFireArmor extends MKAbility {

    public static int BASE_DURATION = 60;
    public static int DURATION_SCALE = 30;

    public NewFireArmor() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND);
        setCooldownSeconds(135);
        setManaCost(12);
        setUseCondition(new NeedsBuffCondition(this, MobEffects.FIRE_RESISTANCE));
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 12f;
    }

    private int getDuration(IMKEntityData casterData, int level) {
        int duration = (BASE_DURATION + DURATION_SCALE * level) * GameConstants.TICKS_PER_SECOND;
        return MKCombatFormulas.applyBuffDurationModifier(casterData, duration);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        int level = 1;

        int duration = getDuration(casterData, level);

        MobEffectInstance absorbEffect = new MobEffectInstance(MobEffects.ABSORPTION, duration, level + 1, false, true);

        MobEffectInstance fireResistanceEffect = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, level, false, true);

        MKEffectBuilder<?> newFireEffect = MKTestEffects.FIRE_ARMOR.get().builder(castingEntity)
                .ability(this)
                .timed(duration)
                .amplify(level);

        MKEffectBuilder<?> particleEffect = MKOldParticleEffect.from(castingEntity, ParticleTypes.FLAME,
                ParticleEffects.CIRCLE_PILLAR_MOTION, false, new Vec3(1.0, 1.0, 1.0),
                new Vec3(0.0, 1.0, 0.0), 40, 5, .1f)
                .ability(this)
                .amplify(level);

        AreaEffectBuilder.createOnCaster(castingEntity)
                .effect(absorbEffect, getTargetContext())
                .effect(fireResistanceEffect, getTargetContext())
                .effect(newFireEffect, getTargetContext())
                .effect(particleEffect, getTargetContext())
                .instant()
                .particle(ParticleTypes.DRIPPING_LAVA)
                .color(16762905)
                .radius(getDistance(castingEntity), true)
                .spawn();

        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.FLAME,
                ParticleEffects.CIRCLE_MOTION, 50, 0,
                castingEntity.getX(), castingEntity.getY() + 1.0,
                castingEntity.getZ(), 1.0, 1.0, 1.0, .1f,
                castingEntity.getLookAngle()), castingEntity);
    }
}
