package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class NewHeal extends MKAbility {
    protected final FloatAttribute base = new FloatAttribute("base", 5.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 5.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);

    public NewHeal() {
        super();
        setCooldownSeconds(6);
        setManaCost(4);
        setCastTime(GameConstants.TICKS_PER_SECOND / 4);
        addAttributes(base, scale, modifierScaling);
        addSkillAttribute(MKAttributes.RESTORATION);

    }

    @Override
    protected Component getAbilityDescription(IMKEntityData casterData) {
        float level = getSkillLevel(casterData.getEntity(), MKAttributes.RESTORATION);
        Component valueStr = getHealDescription(casterData, base.value(),
                scale.value(), level,
                modifierScaling.value());
        return new TranslatableComponent(getDescriptionTranslationKey(), valueStr);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET_OR_SELF;
    }

    @Override
    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return super.isValidTarget(caster, target) || MKHealing.wouldHealHurtUndead(caster, target);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        float level = getSkillLevel(castingEntity, MKAttributes.RESTORATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                MKEffectBuilder<?> heal = MKTestEffects.NEW_HEAL.get().builder(castingEntity)
                        .ability(this)
                        .state(s -> s.setScalingParameters(base.value(), scale.value()))
                        .timed(200)
                        .skillLevel(level)
                        .periodic(40);
                targetData.getEffects().addEffect(heal);
            });

            //            SoundUtils.playSoundAtEntity(targetEntity, ModSounds.spell_heal_3);
            Vec3 lookVec = castingEntity.getLookAngle();
            PacketHandler.sendToTrackingAndSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.HAPPY_VILLAGER,
                            ParticleEffects.SPHERE_MOTION, 50, 10,
                            targetEntity.getX(),
                            targetEntity.getY() + 1.0f,
                            targetEntity.getZ(),
                            1.0, 1.0, 1.0, 1.5, lookVec),
                    targetEntity);
        });
    }
}
