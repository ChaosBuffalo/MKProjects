package com.chaosbuffalo.mkultra.init;


import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.entitlements.SimpleEntitlement;
import com.chaosbuffalo.mkultra.MKUltra;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class MKUEntitlements {

    public static final DeferredRegister<MKEntitlement> REGISTRY =
            DeferredRegister.create(MKCoreRegistry.ENTITLEMENTS, MKUltra.MODID);

    public static DeferredHolder<MKEntitlement, MKEntitlement> GreenKnightTier1 = REGISTRY.register("green_knight.tier_1",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> GreenKnightTier2 = REGISTRY.register("green_knight.tier_2",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> GreenKnightTier3 = REGISTRY.register("green_knight.tier_3",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> ClericTier1 = REGISTRY.register("cleric.tier_1",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> ClericTier2 = REGISTRY.register("cleric.tier_2",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> ClericTier3 = REGISTRY.register("cleric.tier_3",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> IntroClericTier1 = REGISTRY.register("cleric.intro.tier_1",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> NetherMageTier1 = REGISTRY.register("nether_mage.tier_1",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> NetherMageTier2 = REGISTRY.register("nether_mage.tier_2",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> NetherMageTier3 = REGISTRY.register("nether_mage.tier_3",
            () -> new SimpleEntitlement(1));
    public static DeferredHolder<MKEntitlement, MKEntitlement> IntroNetherMageTier1 = REGISTRY.register("nether_mage.intro.tier_1",
            () -> new SimpleEntitlement(1));


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
