package com.chaosbuffalo.mkcore.utils.location;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LocationProviderTypes {
    public static final DeferredRegister<LocationProviderType<?>> REGISTRY = DeferredRegister.create(MKCoreRegistry.LOC_PROVIDER_TYPES_NAME, MKCore.MOD_ID);
    public static final Supplier<LocationProviderType<SingleLocationProvider>> SINGLE_LOCATION = REGISTRY.register(
            "single_location", () -> () -> SingleLocationProvider.CODEC);
    public static final Supplier<LocationProviderType<PerpendicularLineLocationProvider>> PERPENDICULAR_LINE_LOCATION = REGISTRY.register(
            "perpendicular_line_location", () -> () -> PerpendicularLineLocationProvider.CODEC);
    public static final Supplier<LocationProviderType<CircularLocationProvider>> CIRCULAR_LOCATION = REGISTRY.register(
            "circular_location", () -> () -> CircularLocationProvider.CODEC);


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
