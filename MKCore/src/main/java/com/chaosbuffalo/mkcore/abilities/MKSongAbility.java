package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongPulseEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongSustainEffect;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

public abstract class MKSongAbility extends MKToggleAbility {
    public MKSongAbility() {
        super();
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public final MKSongSustainEffect getToggleEffect() {
        return getSustainEffect();
    }

    public abstract MKSongSustainEffect getSustainEffect();

    public abstract int getSustainEffectTicks();

    public abstract MKSongPulseEffect getPulseEffect();

    public abstract int getPulseEffectTicks();

    @Override
    public void applyEffect(LivingEntity castingEntity, IMKEntityData casterData) {
        super.applyEffect(castingEntity, casterData);
        applySustainEffect(casterData);
    }

    public MKActiveEffect createSustainEffect(IMKEntityData casterData) {
        return getSustainEffect().builder(casterData.getEntity())
                .ability(this)
                .periodic(getSustainEffectTicks())
                .infinite()
                .createApplication();
    }

    public MKActiveEffect createPulseEffect(IMKEntityData casterData) {
        return getPulseEffect().builder(casterData.getEntity())
                .ability(this)
                .periodic(getPulseEffectTicks())
                .timed(getSustainEffectTicks())
                .createApplication();
    }

    public float getSongDistance(IMKEntityData casterData, MKActiveEffect instance) {
        return 10f;
    }

    public ParticleOptions getSongPulseParticle() {
        return ParticleTypes.NOTE;
    }

    protected void applySustainEffect(IMKEntityData casterData) {
        MKCore.LOGGER.info("MKSongAbilityNew.applySustainEffect");

        MKActiveEffect sustain = createSustainEffect(casterData);
        casterData.getEffects().addEffect(sustain);

        LivingEntity entity = casterData.getEntity();
        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.NOTE,
                ParticleEffects.SPHERE_MOTION, 50, 5,
                entity.getX(), entity.getY() + 1.0,
                entity.getZ(), 1.0, 1.0, 1.0, 1.0f,
                entity.getLookAngle()), entity);
    }

    public void addPulseAreaEffects(IMKEntityData casterData, AreaEffectBuilder addEffect) {

    }

    public int getSustainEffectManaCost(IMKEntityData casterData, MKActiveEffect activeEffect) {
        return 1;
    }

    @Override
    public float getManaCost(IMKEntityData casterData) {
        // Songs cost nothing to activate, but the CasterEffect will try to drain getSustainEffectManaCost() on the first tick
        return 0;
    }
}
