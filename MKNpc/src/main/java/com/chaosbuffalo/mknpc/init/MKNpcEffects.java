package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.effects.HealingThreatEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MKNpcEffects {

    private static final DeferredRegister<MKEffect> REGISTRY =
            DeferredRegister.create(MKCoreRegistry.EFFECT_REGISTRY_NAME, MKNpc.MODID);

    public static final RegistryObject<HealingThreatEffect> THREAT = REGISTRY.register("effect.threat",
            HealingThreatEffect::new);

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
