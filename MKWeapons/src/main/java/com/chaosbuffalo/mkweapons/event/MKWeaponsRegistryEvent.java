package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkweapons.init.WeaponTierItemFactory;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

public class MKWeaponsRegistryEvent extends Event implements IModBusEvent {
    private final Map<IMKTier, WeaponTierItemFactory> tierFactoryMap = new HashMap<>();

    public void registerTier(IMKTier tier, WeaponTierItemFactory factory) {
        tierFactoryMap.put(tier, factory);
    }

    public Map<IMKTier, WeaponTierItemFactory> getTierFactoryMap() {
        return tierFactoryMap;
    }
}