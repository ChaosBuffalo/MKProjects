package com.chaosbuffalo.mkcore.abilities.client_state;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AbilityClientStateTypes {

    public static final DeferredRegister<AbilityClientStateType<?>> REGISTRY = DeferredRegister.create(MKCoreRegistry.CLIENT_STATE_TYPES_NAME, MKCore.MOD_ID);
    public static final Supplier<AbilityClientStateType<ProjectileAbilityClientState>> PROJECTILE = REGISTRY.register("projectile_ability", () -> () -> ProjectileAbilityClientState.CODEC);

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
