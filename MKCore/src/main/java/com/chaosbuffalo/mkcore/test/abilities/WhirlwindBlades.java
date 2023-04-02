package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.MeleeUseCondition;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.AbilityMagicDamageEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKOldParticleEffect;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class WhirlwindBlades extends MKAbility {
    public static float BASE_DAMAGE = 2.0f;
    public static float DAMAGE_SCALE = 1.0f;

    public WhirlwindBlades() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND * 3);
        setCooldownSeconds(20);
        setManaCost(6);
        setUseCondition(new MeleeUseCondition(this));
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Ultimate;
    }

    @Override
    public boolean canApplyCastingSpeedModifier() {
        return false;
    }

    @Override
    public boolean isInterruptedBy(IMKEntityData targetData, CastInterruptReason reason) {
        return false;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return getMeleeReach(entity);
    }

    //    @Override
//    public SoundEvent getCastingSoundEvent() {
//        return ModSounds.spell_whirlwind_1;
//    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public void continueCast(LivingEntity castingEntity, IMKEntityData casterData, int castTimeLeft, AbilityContext context) {
        super.continueCast(castingEntity, casterData, castTimeLeft, context);
        int tickSpeed = 6;
        if (castTimeLeft % tickSpeed == 0) {
            int level = 1;
            int totalDuration = getCastTime(casterData);
            int count = (totalDuration - castTimeLeft) / tickSpeed;
            float baseAmount = 0.15f;
            float scaling = count * baseAmount;
            // What to do for each target hit
            MKEffectBuilder<?> damage = AbilityMagicDamageEffect.from(castingEntity, BASE_DAMAGE, DAMAGE_SCALE, scaling)
                    .ability(this)
                    .amplify(level);
            MKEffectBuilder<?> particlePotion = MKOldParticleEffect.from(castingEntity,
                    ParticleTypes.SWEEP_ATTACK,
                    ParticleEffects.CIRCLE_MOTION, false,
                    new Vec3(1.0, 1.0, 1.0),
                    new Vec3(0.0, 1.0, 0.0),
                    4, 0, 1.0)
                    .ability(this)
                    .amplify(level);


            AreaEffectBuilder.createOnCaster(castingEntity)
                    .effect(damage, getTargetContext())
                    .effect(particlePotion, getTargetContext())
//                    .spellCast(SoundPotion.Create(entity, ModSounds.spell_shadow_2, SoundCategory.PLAYERS),
//                            1, getTargetType())
                    .instant()
                    .color(16409620)
                    .radius(getDistance(castingEntity), true)
                    .particle(ParticleTypes.CRIT)
                    .spawn();

            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                    ParticleTypes.SWEEP_ATTACK,
                    ParticleEffects.SPHERE_MOTION, 16, 4,
                    castingEntity.getX(), castingEntity.getY() + 1.0,
                    castingEntity.getZ(), 1.0, 1.0, 1.0, 1.5,
                    castingEntity.getLookAngle()), castingEntity);
        }
    }
}
