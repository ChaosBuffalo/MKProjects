package com.chaosbuffalo.mkweapons.client;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.client.particle.BloodDripParticle;
import com.chaosbuffalo.mkweapons.init.MKWeaponsParticles;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKWeapons.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKWeaponsRenderers {

    @SubscribeEvent
    public static void registerParticleFactory(RegisterParticleProvidersEvent evt) {
        Minecraft.getInstance().particleEngine.register(MKWeaponsParticles.DRIPPING_BLOOD.get(),
                BloodDripParticle.BloodDripFactory::new);
    }
}
