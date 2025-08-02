package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoreEntitlementTypes {
    public static final DeferredRegister<EntitlementType<?>> REGISTRY = DeferredRegister.create(MKCoreRegistry.ENTITLEMENT_TYPE_REGISTRY_KEY, MKCore.MOD_ID);

    public static final DeferredHolder<EntitlementType<?>, EntitlementType<AbilityPoolEntitlement>> ABILITY_POOL_COUNT = REGISTRY.register("ability_pool.count",
            AbilityPoolEntitlementType::new);

    public static final DeferredHolder<EntitlementType<?>, EntitlementType<AbilitySlotEntitlement>> ABILITY_SLOT = REGISTRY.register("ability_slot",
            AbilitySlotEntitlementType::new);

    public static final DeferredHolder<EntitlementType<?>, EntitlementType<ArmorClassMasteryEntitlement>> ARMOR_CLASS_MASTERY = REGISTRY.register("armor_class_mastery",
            ArmorClassMasteryEntitlementType::new);

    public static final DeferredHolder<EntitlementType<?>, EntitlementType<SimpleEntitlement>> PLAYER_FLAG = REGISTRY.register("player_flag",
            SimpleEntitlementType::new);


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
