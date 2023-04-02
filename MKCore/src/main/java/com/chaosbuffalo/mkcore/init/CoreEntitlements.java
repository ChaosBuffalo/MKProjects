package com.chaosbuffalo.mkcore.init;


import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.player.AbilityPoolEntitlement;
import com.chaosbuffalo.mkcore.core.player.AbilitySlotEntitlement;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CoreEntitlements {

    public static final DeferredRegister<MKEntitlement> ENTITLEMENTS =
            DeferredRegister.create(MKCoreRegistry.ENTITLEMENT_REGISTRY_NAME, MKCore.MOD_ID);

    public static final RegistryObject<AbilitySlotEntitlement> BASIC_ABILITY_SLOT = ENTITLEMENTS.register("ability_slot.basic",
            () -> new AbilitySlotEntitlement(AbilityGroupId.Basic));

    public static final RegistryObject<AbilitySlotEntitlement> PASSIVE_ABILITY_SLOT = ENTITLEMENTS.register("ability_slot.passive",
            () -> new AbilitySlotEntitlement(AbilityGroupId.Passive));

    public static final RegistryObject<AbilitySlotEntitlement> ULTIMATE_ABILITY_SLOT = ENTITLEMENTS.register("ability_slot.ultimate",
            () -> new AbilitySlotEntitlement(AbilityGroupId.Ultimate));

    public static final RegistryObject<AbilityPoolEntitlement> ABILITY_POOL_SIZE = ENTITLEMENTS.register("ability_pool.count",
            () -> new AbilityPoolEntitlement(GameConstants.MAX_ABILITY_POOL_SIZE - GameConstants.DEFAULT_ABILITY_POOL_SIZE));

    public static void register(IEventBus modBus) {
        ENTITLEMENTS.register(modBus);
    }
}
