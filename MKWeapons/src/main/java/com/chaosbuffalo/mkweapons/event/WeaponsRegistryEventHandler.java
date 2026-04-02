package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;

@EventBusSubscriber(modid = MKWeapons.MODID)
public class WeaponsRegistryEventHandler {

    @SubscribeEvent
    public static void onModify(ModifyRegistriesEvent event) {
        var registryEvent = new MKWeaponsRegistryEvent();
        ModLoader.postEvent(registryEvent);

        registryEvent.getTierFactoryMap().forEach(MKWeaponsItems::registerTierFactory);
    }
}