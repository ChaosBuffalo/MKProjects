package com.chaosbuffalo.mkcore.init;


import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.AbilityPoolEntitlement;
import com.chaosbuffalo.mkcore.core.entitlements.AbilitySlotEntitlement;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoreEntitlements {

    public static final DeferredRegister<MKEntitlement> ENTITLEMENTS =
            DeferredRegister.create(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY, MKCore.MOD_ID);

    public static final DeferredHolder<MKEntitlement, AbilitySlotEntitlement> BASIC_ABILITY_SLOT = ENTITLEMENTS.register("ability_slot.basic",
            () -> new AbilitySlotEntitlement(AbilityGroupId.Basic));

    public static final DeferredHolder<MKEntitlement, AbilitySlotEntitlement> PASSIVE_ABILITY_SLOT = ENTITLEMENTS.register("ability_slot.passive",
            () -> new AbilitySlotEntitlement(AbilityGroupId.Passive));

    public static final DeferredHolder<MKEntitlement, AbilitySlotEntitlement> ULTIMATE_ABILITY_SLOT = ENTITLEMENTS.register("ability_slot.ultimate",
            () -> new AbilitySlotEntitlement(AbilityGroupId.Ultimate));

    public static final DeferredHolder<MKEntitlement, AbilityPoolEntitlement> ABILITY_POOL_SIZE = ENTITLEMENTS.register("ability_pool.count",
            () -> new AbilityPoolEntitlement(GameConstants.MAX_ABILITY_POOL_SIZE - GameConstants.DEFAULT_ABILITY_POOL_SIZE));

    public static void register(IEventBus modBus) {
        ENTITLEMENTS.register(modBus);
    }
}
