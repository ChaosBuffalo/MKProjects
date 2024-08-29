package com.chaosbuffalo.mkweapons.init;


import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.effects.BleedEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKWeaponEffects {

    public static final DeferredRegister<MKEffect> EFFECTS = DeferredRegister.create(MKCoreRegistry.EFFECT_REGISTRY_KEY, MKWeapons.MODID);

    public static final DeferredHolder<MKEffect, BleedEffect> BLEED_DAMAGE = EFFECTS
            .register("effect.bleed_damage", BleedEffect::new);

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
