package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.effects.HealingThreatEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKNpcEffects {

    private static final DeferredRegister<MKEffect> REGISTRY =
            DeferredRegister.create(MKCoreRegistry.EFFECT_REGISTRY_KEY, MKNpc.MODID);

    public static final DeferredHolder<MKEffect, HealingThreatEffect> THREAT = REGISTRY.register("effect.threat",
            HealingThreatEffect::new);

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
