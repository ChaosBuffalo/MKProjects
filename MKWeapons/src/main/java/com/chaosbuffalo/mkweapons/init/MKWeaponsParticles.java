package com.chaosbuffalo.mkweapons.init;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKWeaponsParticles {

    public static final DeferredRegister<ParticleType<?>> REGISTRY =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MKWeapons.MODID);

    public static DeferredHolder<ParticleType<?>, SimpleParticleType> DRIPPING_BLOOD = REGISTRY.register("dripping_blood",
            () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
