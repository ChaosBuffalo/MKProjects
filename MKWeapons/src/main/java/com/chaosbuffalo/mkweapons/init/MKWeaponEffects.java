package com.chaosbuffalo.mkweapons.init;


import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.effects.BleedEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MKWeaponEffects {

    public static final DeferredRegister<MKEffect> EFFECTS = DeferredRegister.create(MKCoreRegistry.EFFECT_REGISTRY_NAME, MKWeapons.MODID);

    public static final RegistryObject<BleedEffect> BLEED_DAMAGE = EFFECTS
            .register("effect.bleed_damage", BleedEffect::new);

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
