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
import com.chaosbuffalo.mkcore.serialization.components.MKComponentKey;
import com.chaosbuffalo.mkcore.serialization.components.MKDataComponents;
import com.chaosbuffalo.mkcore.serialization.components.MKDataSerializers;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class EmberAbility extends MKAbility {
    private static final MKDataComponents.ComponentDefiner DEFINER = MKDataComponents.definer(EmberAbility.class);

    public static final MKComponentKey<Float> DAMAGE = DEFINER.define("damage", MKDataSerializers.FLOAT);
    public static final MKComponentKey<Integer> BURN_TIME = DEFINER.define("burnTime", MKDataSerializers.INT);


    private static final ResourceLocation TEST_PARTICLES = new ResourceLocation(MKCore.MOD_ID, "beam_effect");

    public EmberAbility() {
        super();
    }

    @Override
    protected void defineComponents(MKAbility.AbilityDefaultsBuilder builder) {
        super.defineComponents(builder);
        builder.castTicks(GameConstants.TICKS_PER_SECOND / 2);
        builder.castSeconds(4);
        builder.manaCost(6);
        builder.set(DAMAGE, 6.0f);
        builder.set(BURN_TIME, 5);
    }

    private float getDamage() {
        return getComponentValue(DAMAGE);
    }

    private int getBurnTime() {
        return getComponentValue(BURN_TIME);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.FireDamage.get(), getDamage(), 0.0f, 0, 1.0f);
        Component burn = Component.literal(NUMBER_FORMATTER.format(getBurnTime())).withStyle(ChatFormatting.UNDERLINE);
        return Component.translatable(getDescriptionTranslationKey(), damageStr, burn);
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
        RandomSource rand = castingEntity.getRandom();
        castingEntity.getCommandSenderWorld().addParticle(ParticleTypes.LAVA,
                castingEntity.getX(), castingEntity.getY() + 0.5F, castingEntity.getZ(),
                rand.nextFloat() / 2.0F, 5.0E-5D, rand.nextFloat() / 2.0F);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            int burnDuration = getBurnTime();
            float amount = getDamage();
            MKCore.LOGGER.info("Ember damage {} burnTime {}", amount, burnDuration);
            targetEntity.setSecondsOnFire(burnDuration);
            targetEntity.hurt(MKDamageSource.causeAbilityDamage(targetEntity.getLevel(), CoreDamageTypes.FireDamage.get(),
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
