package com.chaosbuffalo.mkweapons;

import com.chaosbuffalo.mkweapons.client.MKWeaponsItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = MKWeapons.MODID, dist = Dist.CLIENT)
public class MKWeaponsClient {

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(MKWeaponsItemProperties::registerItemProperties);
    }
}
