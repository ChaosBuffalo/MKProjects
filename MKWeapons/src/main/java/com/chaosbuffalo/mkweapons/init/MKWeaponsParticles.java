package com.chaosbuffalo.mkweapons.init;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MKWeaponsParticles {

    public static final DeferredRegister<ParticleType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MKWeapons.MODID);

    public static RegistryObject<SimpleParticleType> DRIPPING_BLOOD = REGISTRY.register("dripping_blood",
            () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
