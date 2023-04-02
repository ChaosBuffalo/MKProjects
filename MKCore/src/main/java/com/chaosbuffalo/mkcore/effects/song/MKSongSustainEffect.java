package com.chaosbuffalo.mkcore.effects.song;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class MKSongSustainEffect extends MKEffect {

    public MKSongSustainEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    @Override
    public SongSustainState makeState() {
        return new SongSustainState();
    }

    @Override
    public MKEffectBuilder<SongSustainState> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<?> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class SongSustainState extends MKSongStateBase {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            MKCore.LOGGER.info("MKSongSustainEffect.performEffect {} {}", instance, getSongAbility());

//            if (entityData.getAbilityExecutor().isCasting() ||
//                    !entityData.getStats().consumeMana(ability.getSustainEffectManaCost(playerData))) {
//                entity.removePotionEffect(this);
//                return;
//            }

            if (targetData instanceof MKPlayerData playerData) {
                if (!playerData.getStats().consumeMana(getSongAbility().getSustainEffectManaCost(playerData, instance))) {
                    // Remove the effect if you can't pay the upkeep
                    return false;
                }
            }

            MKActiveEffect pulse = getSongAbility().createPulseEffect(targetData);
            targetData.getEffects().addEffect(pulse);

            LivingEntity target = targetData.getEntity();
            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                    ParticleTypes.NOTE,
                    ParticleEffects.CIRCLE_MOTION, 12, 4,
                    target.getX(), target.getY() + 1.0f,
                    target.getZ(), .25, .25, .25, .5,
                    target.getLookAngle()), target);
            return true;
        }
    }
}
