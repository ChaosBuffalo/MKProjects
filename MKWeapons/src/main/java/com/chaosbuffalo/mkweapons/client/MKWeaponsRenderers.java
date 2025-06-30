package com.chaosbuffalo.mkweapons.client;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.client.particle.BloodDripParticle;
import com.chaosbuffalo.mkweapons.init.MKWeaponsParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = MKWeapons.MODID, value = Dist.CLIENT)
public class MKWeaponsRenderers {

    @SubscribeEvent
    public static void registerParticleFactory(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MKWeaponsParticles.DRIPPING_BLOOD.get(), BloodDripParticle.BloodDripFactory::new);
    }
}
