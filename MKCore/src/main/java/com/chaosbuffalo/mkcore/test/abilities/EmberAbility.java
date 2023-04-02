package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import com.google.common.collect.ImmutableSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.Set;

public class EmberAbility extends MKAbility {
    private static final ResourceLocation TEST_PARTICLES = new ResourceLocation(MKCore.MOD_ID, "beam_effect");
    protected final FloatAttribute damage = new FloatAttribute("damage", 6.0f);
    protected final IntAttribute burnTime = new IntAttribute("burnTime", 5);

    public EmberAbility() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND / 2);
        setCooldownSeconds(4);
        setManaCost(6);
        addAttributes(damage, burnTime);
    }

    @Override
    protected Component getAbilityDescription(IMKEntityData casterData) {
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.FireDamage, damage.value(), 0.0f, 0, 1.0f);
        Component burn = new TextComponent(burnTime.valueAsString()).withStyle(ChatFormatting.UNDERLINE);
        return new TranslatableComponent(getDescriptionTranslationKey(), damageStr, burn);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 25.0f;
    }


    @Override
    public void continueCastClient(LivingEntity castingEntity, IMKEntityData casterData, int castTimeLeft) {
        super.continueCastClient(castingEntity, casterData, castTimeLeft);
        Random rand = castingEntity.getRandom();
        castingEntity.getCommandSenderWorld().addParticle(ParticleTypes.LAVA,
                castingEntity.getX(), castingEntity.getY() + 0.5F, castingEntity.getZ(),
                rand.nextFloat() / 2.0F, 5.0E-5D, rand.nextFloat() / 2.0F);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            int burnDuration = burnTime.value();
            float amount = damage.value();
            MKCore.LOGGER.info("Ember damage {} burnTime {}", amount, burnDuration);
            targetEntity.setSecondsOnFire(burnDuration);
            targetEntity.hurt(MKDamageSource.causeAbilityDamage(CoreDamageTypes.FireDamage,
                    getAbilityId(), castingEntity, castingEntity), amount);
//            SoundUtils.playSoundAtEntity(targetEntity, ModSounds.spell_fire_6);
            EntityEffectBuilder.LineEffectBuilder lineBuilder = EntityEffectBuilder.createLineEffectOnEntity(castingEntity, targetEntity,
                    new Vec3(targetEntity.getX(), targetEntity.getY(0.5), targetEntity.getZ()),
                    new Vec3(castingEntity.getX(), castingEntity.getY() + castingEntity.getEyeHeight(), castingEntity.getZ()));
            lineBuilder.setParticles(TEST_PARTICLES);
            lineBuilder.duration(40);
            lineBuilder.spawn();
            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                    ParticleTypes.FLAME,
                    ParticleEffects.CIRCLE_PILLAR_MOTION, 60, 10,
                    targetEntity.getX(), targetEntity.getY() + 1.0,
                    targetEntity.getZ(), 1.0, 1.0, 1.0, .25,
                    castingEntity.getLookAngle()), targetEntity);
        });
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET;
    }
}
